package in.neelporiya.vendingmachine;

/** Thrown when the cash box cannot produce exact change. */
public class UnableToMakeChangeException extends VendingMachineException {
    public UnableToMakeChangeException(String message) {
        super(message);
    }
}
