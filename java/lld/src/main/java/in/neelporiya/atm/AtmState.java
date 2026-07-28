package in.neelporiya.atm;

/**
 * // DESIGN PATTERN: State — each state owns which ATM actions are legal. This avoids a fragile
 * pile of {@code if (cardInserted && authenticated)} checks inside {@link Atm}.
 */
public interface AtmState {
    void insertCard(Atm atm, Card card);

    PinAuthenticationResult enterPin(Atm atm, String pin);

    OperationSelection selectOperation(Atm atm, AtmOperation operation);

    WithdrawResult withdraw(Atm atm, int amountCents);

    DepositResult deposit(Atm atm, int amountCents);

    BalanceInquiryResult balanceInquiry(Atm atm);

    void ejectCard(Atm atm);

    String name();
}
