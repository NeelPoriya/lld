package in.neelporiya.vendingmachine;

/** Thrown when a selected product or the whole machine has no stock. */
public class OutOfStockException extends VendingMachineException {
    public OutOfStockException(String message) {
        super(message);
    }
}
