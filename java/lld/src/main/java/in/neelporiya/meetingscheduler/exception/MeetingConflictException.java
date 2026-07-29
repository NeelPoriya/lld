package in.neelporiya.meetingscheduler.exception;

/** Thrown when a booking clashes with an existing meeting on the same room or attendee. */
public class MeetingConflictException extends RuntimeException {
    public MeetingConflictException(String message) {
        super(message);
    }
}
