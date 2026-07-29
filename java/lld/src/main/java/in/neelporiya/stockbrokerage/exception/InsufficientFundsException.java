package in.neelporiya.stockbrokerage.exception;

/** Thrown when an account's cash cannot cover a buy (or a withdrawal). */
public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String message) {
        super(message);
    }
}
