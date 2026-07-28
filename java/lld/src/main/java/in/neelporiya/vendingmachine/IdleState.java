package in.neelporiya.vendingmachine;

/** Waiting for the first coin/note. */
public class IdleState implements VendingMachineState {

    static final IdleState INSTANCE = new IdleState();

    private IdleState() {
    }

    @Override
    public void insertMoney(VendingMachine machine, Denomination money) {
        machine.addInsertedMoney(money);
        machine.transitionTo(HasMoneyState.INSTANCE);
    }

    @Override
    public DispenseResult selectProduct(VendingMachine machine, String code) {
        throw new InvalidStateException("Insert money before selecting a product");
    }

    @Override
    public DispenseResult dispense(VendingMachine machine) {
        throw new InvalidStateException("No product is selected");
    }

    @Override
    public RefundResult refund(VendingMachine machine) {
        return new RefundResult(machine.drainInsertedMoney());
    }

    @Override
    public String name() {
        return "IDLE";
    }
}
