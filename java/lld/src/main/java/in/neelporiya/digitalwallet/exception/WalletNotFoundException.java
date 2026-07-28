package in.neelporiya.digitalwallet.exception;

/** Thrown when a wallet id is unknown. */
public class WalletNotFoundException extends RuntimeException {
    public WalletNotFoundException(String message) {
        super(message);
    }
}
