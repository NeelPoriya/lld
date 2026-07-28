package in.neelporiya.vendingmachine;

/** Coins accepted by the machine, modelled as integer cents. */
public enum Coin implements Denomination {
    PENNY(1),
    NICKEL(5),
    DIME(10),
    QUARTER(25);

    private final int cents;

    Coin(int cents) {
        this.cents = cents;
    }

    @Override
    public int cents() {
        return cents;
    }
}
