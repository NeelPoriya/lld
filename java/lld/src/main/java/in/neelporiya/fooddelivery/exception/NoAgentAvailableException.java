package in.neelporiya.fooddelivery.exception;

/** Thrown when an order is ready but no delivery agent is free to take it. */
public class NoAgentAvailableException extends RuntimeException {
    public NoAgentAvailableException(String message) {
        super(message);
    }
}
