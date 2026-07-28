package in.neelporiya.atm;

/** Card is present; the ATM is waiting for PIN authentication. */
public class CardInsertedState implements AtmState {

    static final CardInsertedState INSTANCE = new CardInsertedState();

    private CardInsertedState() {
    }

    @Override
    public void insertCard(Atm atm, Card card) {
        throw new InvalidStateException("A card is already inserted");
    }

    @Override
    public PinAuthenticationResult enterPin(Atm atm, String pin) {
        PinAuthenticationResult result = atm.authenticateCurrentCard(pin);
        if (result.authenticated()) {
            atm.transitionTo(AuthenticatedState.INSTANCE);
        } else if (result.locked()) {
            // INTERVIEW INSIGHT: real ATMs retain/eject locked cards by policy; for this LLD we end
            // the session and leave the account locked in the bank model.
            atm.clearSession();
            atm.transitionTo(IdleState.INSTANCE);
        }
        return result;
    }

    @Override
    public OperationSelection selectOperation(Atm atm, AtmOperation operation) {
        throw new InvalidStateException("Enter the correct PIN before selecting an operation");
    }

    @Override
    public WithdrawResult withdraw(Atm atm, int amountCents) {
        throw new InvalidStateException("Enter the correct PIN before withdrawing cash");
    }

    @Override
    public DepositResult deposit(Atm atm, int amountCents) {
        throw new InvalidStateException("Enter the correct PIN before depositing cash");
    }

    @Override
    public BalanceInquiryResult balanceInquiry(Atm atm) {
        throw new InvalidStateException("Enter the correct PIN before checking balance");
    }

    @Override
    public void ejectCard(Atm atm) {
        atm.clearSession();
        atm.transitionTo(IdleState.INSTANCE);
    }

    @Override
    public String name() {
        return "CARD_INSERTED";
    }
}
