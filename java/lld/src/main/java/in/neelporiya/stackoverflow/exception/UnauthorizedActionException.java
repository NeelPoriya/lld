package in.neelporiya.stackoverflow.exception;

/** Thrown when a user attempts an action they are not allowed to perform (e.g. accepting an answer
 * on someone else's question, or voting on their own post). */
public class UnauthorizedActionException extends RuntimeException {
    public UnauthorizedActionException(String message) {
        super(message);
    }
}
