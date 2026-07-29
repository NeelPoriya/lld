package in.neelporiya.meetingscheduler;

import in.neelporiya.meetingscheduler.exception.MeetingConflictException;
import in.neelporiya.meetingscheduler.exception.RoomNotFoundException;
import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MeetingSchedulerTest {

    private final MutableClock clock = MutableClock.atEpoch();

    private MeetingScheduler scheduler() {
        AtomicInteger seq = new AtomicInteger();
        return new MeetingScheduler(clock, () -> "id" + seq.incrementAndGet());
    }

    private static Instant at(int hour) {
        return Instant.EPOCH.plus(Duration.ofHours(hour));
    }

    private static TimeInterval slot(int fromHour, int toHour) {
        return new TimeInterval(at(fromHour), at(toHour));
    }

    @Test
    void bookingReturnsAScheduledMeeting() {
        MeetingScheduler scheduler = scheduler();
        Room room = scheduler.addRoom("Boardroom", 10);

        Meeting meeting = scheduler.book(room.id(), slot(10, 11), "alice", Set.of("alice", "bob"), "Design review");

        assertEquals(MeetingStatus.SCHEDULED, meeting.getStatus());
        assertEquals(room, meeting.getRoom());
        assertEquals("Design review", meeting.getTitle());
    }

    @Test
    void overlappingBookingOnSameRoomIsRejected() {
        MeetingScheduler scheduler = scheduler();
        Room room = scheduler.addRoom("Boardroom", 10);
        scheduler.book(room.id(), slot(10, 12), "alice", Set.of("alice"), "First");

        assertThrows(MeetingConflictException.class,
                () -> scheduler.book(room.id(), slot(11, 13), "bob", Set.of("bob"), "Clashes"));
    }

    @Test
    void backToBackMeetingsDoNotConflict() {
        MeetingScheduler scheduler = scheduler();
        Room room = scheduler.addRoom("Boardroom", 10);
        scheduler.book(room.id(), slot(10, 11), "alice", Set.of("alice"), "First");

        // Half-open [10,11) and [11,12) touch but don't overlap.
        Meeting second = scheduler.book(room.id(), slot(11, 12), "bob", Set.of("bob"), "Second");
        assertEquals(MeetingStatus.SCHEDULED, second.getStatus());
    }

    @Test
    void sharedAttendeeCannotBeDoubleBookedAcrossRooms() {
        MeetingScheduler scheduler = scheduler();
        Room a = scheduler.addRoom("A", 10);
        Room b = scheduler.addRoom("B", 10);
        scheduler.book(a.id(), slot(10, 11), "alice", Set.of("alice", "carol"), "Room A");

        // carol is busy 10-11 in room A, so she can't attend an overlapping meeting in room B.
        assertThrows(MeetingConflictException.class,
                () -> scheduler.book(b.id(), slot(10, 11), "bob", Set.of("bob", "carol"), "Room B"));
    }

    @Test
    void bookingRejectedWhenRoomTooSmall() {
        MeetingScheduler scheduler = scheduler();
        Room room = scheduler.addRoom("Phonebooth", 2);
        assertThrows(MeetingConflictException.class,
                () -> scheduler.book(room.id(), slot(10, 11), "alice", Set.of("a", "b", "c"), "Too many"));
    }

    @Test
    void bookingUnknownRoomThrows() {
        MeetingScheduler scheduler = scheduler();
        assertThrows(RoomNotFoundException.class,
                () -> scheduler.book("nope", slot(10, 11), "alice", Set.of("alice"), "Ghost"));
    }

    @Test
    void cancellingFreesTheSlotForRebooking() {
        MeetingScheduler scheduler = scheduler();
        Room room = scheduler.addRoom("Boardroom", 10);
        Meeting first = scheduler.book(room.id(), slot(10, 11), "alice", Set.of("alice"), "First");

        assertTrue(scheduler.cancel(first.getId()));
        assertEquals(MeetingStatus.CANCELLED, first.getStatus());
        assertFalse(scheduler.cancel(first.getId()), "cancelling twice is a no-op");

        // Slot is free again.
        Meeting second = scheduler.book(room.id(), slot(10, 11), "bob", Set.of("bob"), "Second");
        assertEquals(MeetingStatus.SCHEDULED, second.getStatus());
    }

    @Test
    void findAvailableRoomsFiltersByCapacityAndOverlap() {
        MeetingScheduler scheduler = scheduler();
        Room small = scheduler.addRoom("Small", 4);
        Room big = scheduler.addRoom("Big", 20);
        scheduler.book(big.id(), slot(10, 11), "alice", Set.of("alice"), "Occupies big");

        List<Room> forFour = scheduler.findAvailableRooms(slot(10, 11), 4);
        assertEquals(List.of(small), forFour, "big is booked; small fits 4");

        List<Room> forTen = scheduler.findAvailableRooms(slot(10, 11), 10);
        assertTrue(forTen.isEmpty(), "only big is large enough but it's booked");

        List<Room> laterForTen = scheduler.findAvailableRooms(slot(12, 13), 10);
        assertEquals(List.of(big), laterForTen, "big is free later");
    }

    @Test
    void availabilityQueries() {
        MeetingScheduler scheduler = scheduler();
        Room room = scheduler.addRoom("Boardroom", 10);
        scheduler.book(room.id(), slot(10, 11), "alice", Set.of("alice"), "Booked");

        assertFalse(scheduler.isRoomAvailable(room.id(), slot(10, 11)));
        assertTrue(scheduler.isRoomAvailable(room.id(), slot(11, 12)));
        assertFalse(scheduler.isAttendeeAvailable("alice", slot(10, 11)));
        assertTrue(scheduler.isAttendeeAvailable("bob", slot(10, 11)));
    }

    @Test
    void suggestFreeSlotsReturnsGapsLargeEnough() {
        MeetingScheduler scheduler = scheduler();
        Room room = scheduler.addRoom("Boardroom", 10);
        scheduler.book(room.id(), slot(10, 11), "alice", Set.of("alice"), "Morning");
        scheduler.book(room.id(), slot(13, 14), "alice", Set.of("alice"), "Afternoon");

        // Window 9-17, want 1h slots -> gaps [9,10), [11,13), [14,17).
        List<TimeInterval> free = scheduler.suggestFreeSlots(room.id(), Duration.ofHours(1), at(9), at(17));
        assertEquals(List.of(slot(9, 10), slot(11, 13), slot(14, 17)), free);

        // A 3h requirement only fits [14,17).
        List<TimeInterval> big = scheduler.suggestFreeSlots(room.id(), Duration.ofHours(3), at(9), at(17));
        assertEquals(List.of(slot(14, 17)), big);
    }

    @Test
    void cancelledMeetingsAreIgnoredBySuggestions() {
        MeetingScheduler scheduler = scheduler();
        Room room = scheduler.addRoom("Boardroom", 10);
        Meeting m = scheduler.book(room.id(), slot(12, 13), "alice", Set.of("alice"), "Cancelled soon");
        scheduler.cancel(m.getId());

        List<TimeInterval> free = scheduler.suggestFreeSlots(room.id(), Duration.ofHours(1), at(9), at(17));
        assertEquals(List.of(slot(9, 17)), free, "whole window is free once the meeting is cancelled");
    }
}
