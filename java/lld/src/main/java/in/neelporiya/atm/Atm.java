package in.neelporiya.atm;

import java.time.Clock;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * // DESIGN PATTERN: Facade — the client talks to one ATM object while state, bank account logic,
 * cash inventory and note dispensing remain separate collaborators.
 *
 * <p>// DESIGN PATTERN: State — every public operation delegates to the current {@link AtmState};
 * transitions model the real flow: Idle → CardInserted → Authenticated → Idle.
 *
 * <p>// TESTABILITY: {@link Clock} is injected, and operations return result objects with
 * timestamps/note breakdowns so tests need no sleeps or console scraping.
 */
public class Atm {

    private final String id;
    private final Bank bank;
    private final CashInventory cashInventory;
    private final CashDispenser cashDispenser;
    private final Clock clock;
    private final ReentrantLock lock = new ReentrantLock();

    private AtmState state = IdleState.INSTANCE; // guarded by lock
    private Card currentCard;                    // guarded by lock
    private Account currentAccount;              // guarded by lock
    private AtmOperation selectedOperation;       // guarded by lock

    private Atm(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id");
        this.bank = Objects.requireNonNull(builder.bank, "bank");
        this.cashInventory = Objects.requireNonNull(builder.cashInventory, "cashInventory");
        this.cashDispenser = Objects.requireNonNull(builder.cashDispenser, "cashDispenser");
        this.clock = Objects.requireNonNull(builder.clock, "clock");
    }

    public static Builder builder() {
        return new Builder();
    }

    public void insertCard(Card card) {
        Objects.requireNonNull(card, "card");
        lock.lock();
        try {
            state.insertCard(this, card);
        } finally {
            lock.unlock();
        }
    }

    public PinAuthenticationResult enterPin(String pin) {
        Objects.requireNonNull(pin, "pin");
        lock.lock();
        try {
            return state.enterPin(this, pin);
        } finally {
            lock.unlock();
        }
    }

    public OperationSelection selectOperation(AtmOperation operation) {
        Objects.requireNonNull(operation, "operation");
        lock.lock();
        try {
            return state.selectOperation(this, operation);
        } finally {
            lock.unlock();
        }
    }

    public WithdrawResult withdraw(int amountCents) {
        lock.lock();
        try {
            return state.withdraw(this, amountCents);
        } finally {
            lock.unlock();
        }
    }

    public DepositResult deposit(int amountCents) {
        lock.lock();
        try {
            return state.deposit(this, amountCents);
        } finally {
            lock.unlock();
        }
    }

    public BalanceInquiryResult balanceInquiry() {
        lock.lock();
        try {
            return state.balanceInquiry(this);
        } finally {
            lock.unlock();
        }
    }

    public void ejectCard() {
        lock.lock();
        try {
            state.ejectCard(this);
        } finally {
            lock.unlock();
        }
    }

    public String getStateName() {
        lock.lock();
        try {
            return state.name();
        } finally {
            lock.unlock();
        }
    }

    public String getId() {
        return id;
    }

    void acceptCard(Card card) {
        currentCard = card;
        currentAccount = null;
        selectedOperation = null;
    }

    PinAuthenticationResult authenticateCurrentCard(String pin) {
        Card card = Objects.requireNonNull(currentCard, "currentCard");
        Account account = bank.requireAccount(card.getAccountId());
        PinAuthenticationResult result = account.authenticate(pin);
        if (result.authenticated()) {
            currentAccount = account;
        }
        return result;
    }

    OperationSelection recordSelectedOperation(AtmOperation operation) {
        selectedOperation = Objects.requireNonNull(operation, "operation");
        return new OperationSelection(operation, clock.instant());
    }

    void requireSelectedOperation(AtmOperation expected) {
        if (selectedOperation != expected) {
            throw new InvalidStateException("Select " + expected + " before executing it");
        }
    }

    WithdrawResult completeWithdraw(int amountCents) {
        requirePositive(amountCents);
        Account account = requireCurrentAccount();
        if (!account.hasAtLeast(amountCents)) {
            throw new InsufficientFundsException("Account " + account.getId() + " has insufficient funds");
        }

        Map<NoteDenomination, Integer> reservedNotes = cashInventory.reserve(amountCents, cashDispenser);
        try {
            int balanceAfter = account.debit(amountCents);
            selectedOperation = null;
            return new WithdrawResult(account.getId(), amountCents, reservedNotes, balanceAfter, clock.instant());
        } catch (RuntimeException ex) {
            cashInventory.release(reservedNotes);
            throw ex;
        }
    }

    DepositResult completeDeposit(int amountCents) {
        requirePositive(amountCents);
        Account account = requireCurrentAccount();
        int balanceAfter = account.deposit(amountCents);
        selectedOperation = null;
        return new DepositResult(account.getId(), amountCents, balanceAfter, clock.instant());
    }

    BalanceInquiryResult completeBalanceInquiry() {
        Account account = requireCurrentAccount();
        selectedOperation = null;
        return new BalanceInquiryResult(account.getId(), account.balanceCents(), clock.instant());
    }

    void clearSession() {
        currentCard = null;
        currentAccount = null;
        selectedOperation = null;
    }

    void transitionTo(AtmState nextState) {
        state = Objects.requireNonNull(nextState, "nextState");
    }

    private Account requireCurrentAccount() {
        if (currentAccount == null) {
            throw new InvalidStateException("Authenticate before performing account operations");
        }
        return currentAccount;
    }

    private static void requirePositive(int amountCents) {
        if (amountCents <= 0) {
            throw new IllegalArgumentException("amountCents must be positive");
        }
    }

    /**
     * // DESIGN PATTERN: Builder — tests and interview examples can wire a bank, shared inventory,
     * dispenser strategy and clock without telescoping constructors.
     */
    public static final class Builder {
        private String id = "ATM-1";
        private Bank bank = new Bank();
        private CashInventory cashInventory = new CashInventory();
        private CashDispenser cashDispenser = CashDispenserFactory.standardIndianDispenser();
        private Clock clock = Clock.systemUTC();

        public Builder id(String id) {
            this.id = Objects.requireNonNull(id, "id");
            return this;
        }

        public Builder bank(Bank bank) {
            this.bank = Objects.requireNonNull(bank, "bank");
            return this;
        }

        public Builder cashInventory(CashInventory cashInventory) {
            this.cashInventory = Objects.requireNonNull(cashInventory, "cashInventory");
            return this;
        }

        public Builder cashDispenser(CashDispenser cashDispenser) {
            this.cashDispenser = Objects.requireNonNull(cashDispenser, "cashDispenser");
            return this;
        }

        public Builder clock(Clock clock) {
            this.clock = Objects.requireNonNull(clock, "clock");
            return this;
        }

        public Atm build() {
            return new Atm(this);
        }
    }
}
