package in.neelporiya.connectionpool.exception;

/** Thrown when borrowing from a pool that has been shut down. */
public class PoolClosedException extends RuntimeException {
    public PoolClosedException(String message) {
        super(message);
    }
}
