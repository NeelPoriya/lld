package in.neelporiya.digitalwallet.exception;

/** Thrown when arithmetic or a transfer mixes two different currencies. */
public class CurrencyMismatchException extends RuntimeException {
    public CurrencyMismatchException(String message) {
        super(message);
    }
}
