package in.neelporiya.connectionpool.exception;

/** Thrown when a borrow could not obtain a resource within the configured timeout. */
public class PoolExhaustedException extends RuntimeException {
    public PoolExhaustedException(String message) {
        super(message);
    }
}
