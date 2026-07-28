package in.neelporiya.atm;

/** Customer is authenticated and may choose/execute operations. */
public class AuthenticatedState implements AtmState {

    static final AuthenticatedState INSTANCE = new AuthenticatedState();

    private AuthenticatedState() {
    }

    @Override
    public void insertCard(Atm atm, Card card) {
        throw new InvalidStateException("A card is already inserted");
    }

    @Override
    public PinAuthenticationResult enterPin(Atm atm, String pin) {
        throw new InvalidStateException("PIN is already authenticated");
    }

    @Override
    public OperationSelection selectOperation(Atm atm, AtmOperation operation) {
        return atm.recordSelectedOperation(operation);
    }

    @Override
    public WithdrawResult withdraw(Atm atm, int amountCents) {
        atm.requireSelectedOperation(AtmOperation.WITHDRAW);
        return atm.completeWithdraw(amountCents);
    }

    @Override
    public DepositResult deposit(Atm atm, int amountCents) {
        atm.requireSelectedOperation(AtmOperation.DEPOSIT);
        return atm.completeDeposit(amountCents);
    }

    @Override
    public BalanceInquiryResult balanceInquiry(Atm atm) {
        atm.requireSelectedOperation(AtmOperation.BALANCE_INQUIRY);
        return atm.completeBalanceInquiry();
    }

    @Override
    public void ejectCard(Atm atm) {
        atm.clearSession();
        atm.transitionTo(IdleState.INSTANCE);
    }

    @Override
    public String name() {
        return "AUTHENTICATED";
    }
}
