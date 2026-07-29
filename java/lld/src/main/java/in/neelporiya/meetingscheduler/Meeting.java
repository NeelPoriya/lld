package in.neelporiya.meetingscheduler;

import java.time.Instant;
import java.util.Set;

public class Meeting {

    private final String id;
    private final Room room;
    private final TimeInterval interval;
    private final String organizer;
    private final Set<String> attendees;
    private final String title;
    private final Instant createdAt;
    private volatile MeetingStatus status = MeetingStatus.SCHEDULED;

    public Meeting(String id, Room room, TimeInterval interval, String organizer,
                   Set<String> attendees, String title, Instant createdAt) {
        this.id = id;
        this.room = room;
        this.interval = interval;
        this.organizer = organizer;
        this.attendees = Set.copyOf(attendees);
        this.title = title;
        this.createdAt = createdAt;
    }

    void cancel() {
        this.status = MeetingStatus.CANCELLED;
    }

    public String getId() {
        return id;
    }

    public Room getRoom() {
        return room;
    }

    public TimeInterval getInterval() {
        return interval;
    }

    public String getOrganizer() {
        return organizer;
    }

    public Set<String> getAttendees() {
        return attendees;
    }

    public String getTitle() {
        return title;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public MeetingStatus getStatus() {
        return status;
    }
}
