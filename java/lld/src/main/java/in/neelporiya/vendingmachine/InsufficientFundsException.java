package in.neelporiya.vendingmachine;

/** Thrown when the inserted balance is less than the selected product price. */
public class InsufficientFundsException extends VendingMachineException {
    public InsufficientFundsException(String message) {
        super(message);
    }
}
