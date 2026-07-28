package in.neelporiya.vendingmachine;

/** Thrown when an operation is illegal in the current state. */
public class InvalidStateException extends VendingMachineException {
    public InvalidStateException(String message) {
        super(message);
    }
}
