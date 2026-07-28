package in.neelporiya.vendingmachine;

/** Notes accepted by the machine, modelled as integer cents. */
public enum Note implements Denomination {
    ONE_DOLLAR(100),
    FIVE_DOLLARS(500),
    TEN_DOLLARS(1_000),
    TWENTY_DOLLARS(2_000);

    private final int cents;

    Note(int cents) {
        this.cents = cents;
    }

    @Override
    public int cents() {
        return cents;
    }
}
