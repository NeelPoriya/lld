package in.neelporiya.atm;

/** Operations a customer can choose after successful PIN authentication. */
public enum AtmOperation {
    WITHDRAW,
    DEPOSIT,
    BALANCE_INQUIRY,
    TRANSFER
}
