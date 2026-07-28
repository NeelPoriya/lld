package in.neelporiya.vendingmachine;

/** Product has been selected; dispense is the atomic commit step. */
public class DispenseState implements VendingMachineState {

    static final DispenseState INSTANCE = new DispenseState();

    private DispenseState() {
    }

    @Override
    public void insertMoney(VendingMachine machine, Denomination money) {
        throw new InvalidStateException("Cannot insert money while dispensing");
    }

    @Override
    public DispenseResult selectProduct(VendingMachine machine, String code) {
        throw new InvalidStateException("Already dispensing a selected product");
    }

    @Override
    public DispenseResult dispense(VendingMachine machine) {
        return machine.completeDispense();
    }

    @Override
    public RefundResult refund(VendingMachine machine) {
        throw new InvalidStateException("Cannot refund while dispensing");
    }

    @Override
    public String name() {
        return "DISPENSE";
    }
}
