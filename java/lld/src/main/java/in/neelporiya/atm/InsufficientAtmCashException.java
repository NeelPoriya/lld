package in.neelporiya.atm;

public class InsufficientAtmCashException extends AtmException {
    public InsufficientAtmCashException(String message) {
        super(message);
    }
}
