package in.neelporiya.stockbrokerage.exception;

/** Thrown when an account tries to sell more shares than it holds (no shorting). */
public class InsufficientHoldingsException extends RuntimeException {
    public InsufficientHoldingsException(String message) {
        super(message);
    }
}
