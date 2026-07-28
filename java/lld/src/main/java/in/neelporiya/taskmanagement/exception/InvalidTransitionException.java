package in.neelporiya.taskmanagement.exception;

/** Thrown when a status change violates the task workflow (e.g. reopening a DONE task). */
public class InvalidTransitionException extends RuntimeException {
    public InvalidTransitionException(String message) {
        super(message);
    }
}
