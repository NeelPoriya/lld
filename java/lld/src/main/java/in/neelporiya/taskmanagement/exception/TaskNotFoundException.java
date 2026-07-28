package in.neelporiya.taskmanagement.exception;

/** Thrown when a task id does not exist in the repository. */
public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(String message) {
        super(message);
    }
}
