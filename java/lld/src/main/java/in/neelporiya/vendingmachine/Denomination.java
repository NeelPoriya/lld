package in.neelporiya.vendingmachine;

/** A discrete money denomination. Keep values in integer cents; never use double for money. */
public interface Denomination {
    int cents();
}
