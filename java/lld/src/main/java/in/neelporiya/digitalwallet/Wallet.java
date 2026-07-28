package in.neelporiya.digitalwallet;

import in.neelporiya.digitalwallet.exception.CurrencyMismatchException;
import in.neelporiya.digitalwallet.exception.InsufficientFundsException;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A single-currency wallet with an append-only ledger.
 *
 * <p>// CONCURRENCY: the balance is guarded by a {@link ReentrantLock}. Single-wallet {@code credit}
 * /{@code debit} lock just this wallet. Transfers (in {@code WalletService}) lock two wallets in a
 * global id order and then call {@code debit}/{@code credit} here — because the lock is
 * <em>reentrant</em>, those nested calls re-acquire the same lock cheaply and correctly.
 */
public class Wallet {

    private final String id;
    private final String ownerId;
    private final Currency currency;
    private final ReentrantLock lock = new ReentrantLock();
    private final List<WalletTransaction> ledger = new CopyOnWriteArrayList<>();
    private Money balance;

    public Wallet(String id, String ownerId, Currency currency) {
        this.id = id;
        this.ownerId = ownerId;
        this.currency = currency;
        this.balance = Money.zero(currency);
    }

    public WalletTransaction credit(Money amount, TransactionType type, String txnId, String reference, Instant at) {
        validate(amount);
        lock.lock();
        try {
            balance = balance.plus(amount);
            WalletTransaction txn = new WalletTransaction(txnId, id, type, amount, balance, at, reference);
            ledger.add(txn);
            return txn;
        } finally {
            lock.unlock();
        }
    }

    public WalletTransaction debit(Money amount, TransactionType type, String txnId, String reference, Instant at) {
        validate(amount);
        lock.lock();
        try {
            if (balance.isLessThan(amount)) {
                throw new InsufficientFundsException(
                        "wallet " + id + " balance " + balance.minorUnits() + " < " + amount.minorUnits());
            }
            balance = balance.minus(amount);
            WalletTransaction txn = new WalletTransaction(txnId, id, type, amount, balance, at, reference);
            ledger.add(txn);
            return txn;
        } finally {
            lock.unlock();
        }
    }

    private void validate(Money amount) {
        if (amount.currency() != currency) {
            throw new CurrencyMismatchException("wallet is " + currency + " but amount is " + amount.currency());
        }
        if (!amount.isPositive()) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }

    public Money getBalance() {
        lock.lock();
        try {
            return balance;
        } finally {
            lock.unlock();
        }
    }

    /** Exposed so the service can implement ordered two-wallet locking for transfers. */
    public ReentrantLock getLock() {
        return lock;
    }

    public List<WalletTransaction> getHistory() {
        return List.copyOf(ledger);
    }

    public String getId() {
        return id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public Currency getCurrency() {
        return currency;
    }
}
