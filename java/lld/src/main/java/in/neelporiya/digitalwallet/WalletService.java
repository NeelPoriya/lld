package in.neelporiya.digitalwallet;

import in.neelporiya.digitalwallet.exception.WalletNotFoundException;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * // DESIGN PATTERN: Facade. The wallet API: create wallets, credit/debit, and transfer — all
 * idempotent by key, all thread-safe.
 */
public class WalletService {

    private final Map<String, Wallet> wallets = new ConcurrentHashMap<>();
    // Idempotency stores: a key maps to the single memoized result of that operation.
    private final Map<String, WalletTransaction> singleOpResults = new ConcurrentHashMap<>();
    private final Map<String, TransferReceipt> transferResults = new ConcurrentHashMap<>();
    private final List<TransactionListener> listeners = new CopyOnWriteArrayList<>();

    private final Clock clock;
    private final Supplier<String> idGenerator;

    public WalletService(Clock clock, Supplier<String> idGenerator) {
        this.clock = Objects.requireNonNull(clock, "clock");
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
    }

    public static WalletService createDefault() {
        return new WalletService(Clock.systemUTC(), () -> UUID.randomUUID().toString());
    }

    public void addListener(TransactionListener listener) {
        listeners.add(listener);
    }

    public Wallet createWallet(String ownerId, Currency currency) {
        Wallet wallet = new Wallet(idGenerator.get(), ownerId, currency);
        wallets.put(wallet.getId(), wallet);
        return wallet;
    }

    /**
     * // CONCURRENCY + idempotency: {@code computeIfAbsent} runs the credit exactly once per key.
     * Concurrent retries with the same key all get the one memoized transaction.
     */
    public WalletTransaction credit(String walletId, Money amount, String idempotencyKey) {
        return singleOpResults.computeIfAbsent(idempotencyKey, key -> {
            Wallet wallet = require(walletId);
            WalletTransaction txn = wallet.credit(amount, TransactionType.CREDIT, idGenerator.get(), key, clock.instant());
            fire(txn);
            return txn;
        });
    }

    public WalletTransaction debit(String walletId, Money amount, String idempotencyKey) {
        // NOTE: if the debit throws (insufficient funds), computeIfAbsent stores nothing, so the
        // client may legitimately retry the same key later.
        return singleOpResults.computeIfAbsent(idempotencyKey, key -> {
            Wallet wallet = require(walletId);
            WalletTransaction txn = wallet.debit(amount, TransactionType.DEBIT, idGenerator.get(), key, clock.instant());
            fire(txn);
            return txn;
        });
    }

    public TransferReceipt transfer(String fromWalletId, String toWalletId, Money amount, String idempotencyKey) {
        return transferResults.computeIfAbsent(idempotencyKey,
                key -> doTransfer(fromWalletId, toWalletId, amount, key));
    }

    private TransferReceipt doTransfer(String fromWalletId, String toWalletId, Money amount, String reference) {
        if (fromWalletId.equals(toWalletId)) {
            throw new IllegalArgumentException("cannot transfer to the same wallet");
        }
        Wallet from = require(fromWalletId);
        Wallet to = require(toWalletId);

        // CONCURRENCY: acquire BOTH wallet locks in a global order (by id) so A->B and B->A can
        // never deadlock. Whichever wallet has the smaller id is always locked first.
        Wallet firstToLock = fromWalletId.compareTo(toWalletId) < 0 ? from : to;
        Wallet secondToLock = firstToLock == from ? to : from;

        ReentrantLock firstLock = firstToLock.getLock();
        ReentrantLock secondLock = secondToLock.getLock();

        firstLock.lock();
        try {
            secondLock.lock();
            try {
                java.time.Instant now = clock.instant();
                WalletTransaction out = from.debit(amount, TransactionType.TRANSFER_OUT, idGenerator.get(), reference, now);
                WalletTransaction in = to.credit(amount, TransactionType.TRANSFER_IN, idGenerator.get(), reference, now);
                fire(out);
                fire(in);
                return new TransferReceipt(reference, out, in);
            } finally {
                secondLock.unlock();
            }
        } finally {
            firstLock.unlock();
        }
    }

    public Money getBalance(String walletId) {
        return require(walletId).getBalance();
    }

    public List<WalletTransaction> getHistory(String walletId) {
        return require(walletId).getHistory();
    }

    private Wallet require(String walletId) {
        Wallet wallet = wallets.get(walletId);
        if (wallet == null) {
            throw new WalletNotFoundException("no wallet " + walletId);
        }
        return wallet;
    }

    private void fire(WalletTransaction txn) {
        listeners.forEach(listener -> listener.onTransaction(txn));
    }
}
