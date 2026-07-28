package in.neelporiya.vendingmachine;

/** Thrown for unknown product codes. */
public class InvalidSelectionException extends VendingMachineException {
    public InvalidSelectionException(String message) {
        super(message);
    }
}
