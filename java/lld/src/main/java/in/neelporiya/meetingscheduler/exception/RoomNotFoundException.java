package in.neelporiya.meetingscheduler.exception;

/** Thrown when a room id is unknown. */
public class RoomNotFoundException extends RuntimeException {
    public RoomNotFoundException(String message) {
        super(message);
    }
}
