package in.neelporiya.meetingscheduler;

import in.neelporiya.meetingscheduler.exception.MeetingConflictException;
import in.neelporiya.meetingscheduler.exception.RoomNotFoundException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * // DESIGN PATTERN: Facade — one API over rooms, bookings and availability queries.
 *
 * <p>The core invariant: a booking must find the room free AND every attendee free for the interval,
 * then insert — a check-and-insert that spans several entities at once.
 *
 * <p>// TESTABILITY: an injected {@link Clock} timestamps creation (and could power "upcoming"
 * filters); an injected id {@link Supplier} makes meeting ids deterministic in tests.
 */
public class MeetingScheduler {

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Meeting> meetings = new ConcurrentHashMap<>();
    private final List<MeetingListener> listeners = new CopyOnWriteArrayList<>();

    // CONCURRENCY: the multi-entity (room + attendees) check-and-insert is guarded by one lock so
    // two organizers racing for the same room or a shared attendee can never both succeed.
    private final ReentrantLock lock = new ReentrantLock();

    private final Clock clock;
    private final Supplier<String> idGenerator;

    public MeetingScheduler(Clock clock, Supplier<String> idGenerator) {
        this.clock = Objects.requireNonNull(clock, "clock");
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
    }

    public void addListener(MeetingListener listener) {
        listeners.add(listener);
    }

    public Room addRoom(String name, int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        Room room = new Room(idGenerator.get(), name, capacity);
        rooms.put(room.id(), room);
        return room;
    }

    /**
     * Book a meeting, rejecting it if the room or any attendee is already busy for the interval.
     *
     * @throws RoomNotFoundException   if {@code roomId} is unknown.
     * @throws MeetingConflictException if the room lacks capacity or the room/an attendee is busy.
     */
    public Meeting book(String roomId, TimeInterval interval, String organizer,
                        Set<String> attendees, String title) {
        Room room = requireRoom(roomId);
        if (room.capacity() < attendees.size()) {
            throw new MeetingConflictException(
                    "room " + room.name() + " holds " + room.capacity() + " but " + attendees.size() + " invited");
        }
        lock.lock();
        try {
            if (!roomFree(roomId, interval, null)) {
                throw new MeetingConflictException("room " + room.name() + " is busy for " + interval);
            }
            for (String attendee : attendees) {
                if (!attendeeFree(attendee, interval, null)) {
                    throw new MeetingConflictException("attendee " + attendee + " is busy for " + interval);
                }
            }
            Meeting meeting = new Meeting(idGenerator.get(), room, interval, organizer,
                    attendees, title, clock.instant());
            meetings.put(meeting.getId(), meeting);
            listeners.forEach(l -> l.onScheduled(meeting));
            return meeting;
        } finally {
            lock.unlock();
        }
    }

    public boolean cancel(String meetingId) {
        lock.lock();
        try {
            Meeting meeting = meetings.get(meetingId);
            if (meeting == null || meeting.getStatus() == MeetingStatus.CANCELLED) {
                return false;
            }
            meeting.cancel();
            listeners.forEach(l -> l.onCancelled(meeting));
            return true;
        } finally {
            lock.unlock();
        }
    }

    /** Rooms with enough capacity and no scheduled meeting overlapping the interval. */
    public List<Room> findAvailableRooms(TimeInterval interval, int minCapacity) {
        lock.lock();
        try {
            List<Room> available = new ArrayList<>();
            for (Room room : rooms.values()) {
                if (room.capacity() >= minCapacity && roomFree(room.id(), interval, null)) {
                    available.add(room);
                }
            }
            available.sort(Comparator.comparingInt(Room::capacity));
            return available;
        } finally {
            lock.unlock();
        }
    }

    public boolean isRoomAvailable(String roomId, TimeInterval interval) {
        requireRoom(roomId);
        lock.lock();
        try {
            return roomFree(roomId, interval, null);
        } finally {
            lock.unlock();
        }
    }

    public boolean isAttendeeAvailable(String attendee, TimeInterval interval) {
        lock.lock();
        try {
            return attendeeFree(attendee, interval, null);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Contiguous free blocks in a room, each at least {@code duration} long, within
     * {@code [windowStart, windowEnd)}.
     *
     * <p>// INTERVIEW INSIGHT: this is a classic gap scan — sort busy intervals, sweep a cursor and
     * emit the holes. Mention an interval tree if the number of meetings gets large.
     */
    public List<TimeInterval> suggestFreeSlots(String roomId, Duration duration,
                                               Instant windowStart, Instant windowEnd) {
        requireRoom(roomId);
        if (!windowStart.isBefore(windowEnd)) {
            throw new IllegalArgumentException("window start must be before end");
        }
        lock.lock();
        try {
            List<TimeInterval> busy = new ArrayList<>();
            for (Meeting meeting : meetings.values()) {
                if (meeting.getStatus() != MeetingStatus.SCHEDULED || !meeting.getRoom().id().equals(roomId)) {
                    continue;
                }
                Instant start = max(meeting.getInterval().start(), windowStart);
                Instant end = min(meeting.getInterval().end(), windowEnd);
                if (start.isBefore(end)) {
                    busy.add(new TimeInterval(start, end));
                }
            }
            busy.sort(Comparator.comparing(TimeInterval::start));

            List<TimeInterval> free = new ArrayList<>();
            Instant cursor = windowStart;
            for (TimeInterval b : busy) {
                if (b.start().isAfter(cursor) && fits(cursor, b.start(), duration)) {
                    free.add(new TimeInterval(cursor, b.start()));
                }
                if (b.end().isAfter(cursor)) {
                    cursor = b.end();
                }
            }
            if (windowEnd.isAfter(cursor) && fits(cursor, windowEnd, duration)) {
                free.add(new TimeInterval(cursor, windowEnd));
            }
            return free;
        } finally {
            lock.unlock();
        }
    }

    public Meeting getMeeting(String meetingId) {
        return meetings.get(meetingId);
    }

    private boolean roomFree(String roomId, TimeInterval interval, String ignoreMeetingId) {
        return meetings.values().stream()
                .filter(m -> m.getStatus() == MeetingStatus.SCHEDULED)
                .filter(m -> !m.getId().equals(ignoreMeetingId))
                .filter(m -> m.getRoom().id().equals(roomId))
                .noneMatch(m -> m.getInterval().overlaps(interval));
    }

    private boolean attendeeFree(String attendee, TimeInterval interval, String ignoreMeetingId) {
        return meetings.values().stream()
                .filter(m -> m.getStatus() == MeetingStatus.SCHEDULED)
                .filter(m -> !m.getId().equals(ignoreMeetingId))
                .filter(m -> m.getAttendees().contains(attendee))
                .noneMatch(m -> m.getInterval().overlaps(interval));
    }

    private Room requireRoom(String roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            throw new RoomNotFoundException("no room with id " + roomId);
        }
        return room;
    }

    private static boolean fits(Instant start, Instant end, Duration duration) {
        return Duration.between(start, end).compareTo(duration) >= 0;
    }

    private static Instant max(Instant a, Instant b) {
        return a.isAfter(b) ? a : b;
    }

    private static Instant min(Instant a, Instant b) {
        return a.isBefore(b) ? a : b;
    }
}
