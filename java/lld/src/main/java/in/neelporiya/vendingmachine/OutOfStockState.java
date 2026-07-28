package in.neelporiya.vendingmachine;

/** No products are available; service/restock is required. */
public class OutOfStockState implements VendingMachineState {

    static final OutOfStockState INSTANCE = new OutOfStockState();

    private OutOfStockState() {
    }

    @Override
    public void insertMoney(VendingMachine machine, Denomination money) {
        throw new OutOfStockException("Machine is out of stock");
    }

    @Override
    public DispenseResult selectProduct(VendingMachine machine, String code) {
        throw new OutOfStockException("Machine is out of stock");
    }

    @Override
    public DispenseResult dispense(VendingMachine machine) {
        throw new OutOfStockException("Machine is out of stock");
    }

    @Override
    public RefundResult refund(VendingMachine machine) {
        return new RefundResult(machine.drainInsertedMoney());
    }

    @Override
    public String name() {
        return "OUT_OF_STOCK";
    }
}
