package in.neelporiya.vendingmachine;

/**
 * // DESIGN PATTERN: State — each state owns which operations are legal instead of scattering
 * switch/if checks through {@link VendingMachine}.
 */
public interface VendingMachineState {
    void insertMoney(VendingMachine machine, Denomination money);

    DispenseResult selectProduct(VendingMachine machine, String code);

    DispenseResult dispense(VendingMachine machine);

    RefundResult refund(VendingMachine machine);

    String name();
}
