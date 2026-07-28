package in.neelporiya.atm;

/** Waiting for a customer to insert a card. */
public class IdleState implements AtmState {

    static final IdleState INSTANCE = new IdleState();

    private IdleState() {
    }

    @Override
    public void insertCard(Atm atm, Card card) {
        atm.acceptCard(card);
        atm.transitionTo(CardInsertedState.INSTANCE);
    }

    @Override
    public PinAuthenticationResult enterPin(Atm atm, String pin) {
        throw new InvalidStateException("Insert card before entering PIN");
    }

    @Override
    public OperationSelection selectOperation(Atm atm, AtmOperation operation) {
        throw new InvalidStateException("Insert card and authenticate before selecting an operation");
    }

    @Override
    public WithdrawResult withdraw(Atm atm, int amountCents) {
        throw new InvalidStateException("Authenticate before withdrawing cash");
    }

    @Override
    public DepositResult deposit(Atm atm, int amountCents) {
        throw new InvalidStateException("Authenticate before depositing cash");
    }

    @Override
    public BalanceInquiryResult balanceInquiry(Atm atm) {
        throw new InvalidStateException("Authenticate before checking balance");
    }

    @Override
    public void ejectCard(Atm atm) {
        atm.clearSession();
    }

    @Override
    public String name() {
        return "IDLE";
    }
}
