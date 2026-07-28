package in.neelporiya.vendingmachine;

/** Money has been inserted; the customer may add more, select, or cancel. */
public class HasMoneyState implements VendingMachineState {

    static final HasMoneyState INSTANCE = new HasMoneyState();

    private HasMoneyState() {
    }

    @Override
    public void insertMoney(VendingMachine machine, Denomination money) {
        machine.addInsertedMoney(money);
    }

    @Override
    public DispenseResult selectProduct(VendingMachine machine, String code) {
        Product product = machine.requireProduct(code);
        InventoryItem item = machine.itemFor(product.code());
        if (!item.isInStock()) {
            throw new OutOfStockException("Product is out of stock: " + product.code());
        }
        if (machine.insertedBalanceCents() < product.priceCents()) {
            throw new InsufficientFundsException("Need " + product.priceCents()
                    + " cents but only " + machine.insertedBalanceCents() + " inserted");
        }

        machine.setSelectedProduct(product);
        machine.transitionTo(DispenseState.INSTANCE);
        try {
            return machine.dispense();
        } catch (RuntimeException e) {
            // TESTABILITY: failed commits (for example, no exact change) leave the transaction open
            // so tests and callers can assert the same inserted balance and choose refund/another item.
            machine.setSelectedProduct(null);
            machine.transitionTo(HasMoneyState.INSTANCE);
            throw e;
        }
    }

    @Override
    public DispenseResult dispense(VendingMachine machine) {
        throw new InvalidStateException("Select a product before dispensing");
    }

    @Override
    public RefundResult refund(VendingMachine machine) {
        RefundResult result = new RefundResult(machine.drainInsertedMoney());
        machine.transitionTo(machine.hasAnyStock() ? IdleState.INSTANCE : OutOfStockState.INSTANCE);
        return result;
    }

    @Override
    public String name() {
        return "HAS_MONEY";
    }
}
