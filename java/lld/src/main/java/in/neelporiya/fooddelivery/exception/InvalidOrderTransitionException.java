package in.neelporiya.fooddelivery.exception;

/** Thrown on an illegal order lifecycle transition (e.g. delivering a cancelled order). */
public class InvalidOrderTransitionException extends RuntimeException {
    public InvalidOrderTransitionException(String message) {
        super(message);
    }
}
