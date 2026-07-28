package in.neelporiya.digitalwallet.exception;

/** Thrown when a debit/transfer would drive a wallet balance negative. */
public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String message) {
        super(message);
    }
}
