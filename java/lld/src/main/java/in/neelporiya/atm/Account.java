package in.neelporiya.atm;

import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Bank account with a per-account critical section.
 *
 * <p>// CONCURRENCY: The dangerous race is "read balance=1000" in two threads, both approve a
 * withdrawal, then both write a new balance. A per-account lock makes check-then-debit one atomic
 * action, so concurrent withdrawals on the same account can never overdraw it.
 */
public class Account {

    private final String id;
    private final String pin;
    private final int maxPinAttempts;
    private final ReentrantLock lock = new ReentrantLock();

    private int balanceCents;       // guarded by lock
    private int failedPinAttempts;  // guarded by lock
    private boolean locked;         // guarded by lock

    public Account(String id, String pin, int openingBalanceCents) {
        this(id, pin, openingBalanceCents, 3);
    }

    public Account(String id, String pin, int openingBalanceCents, int maxPinAttempts) {
        if (openingBalanceCents < 0) {
            throw new IllegalArgumentException("openingBalanceCents cannot be negative");
        }
        if (maxPinAttempts <= 0) {
            throw new IllegalArgumentException("maxPinAttempts must be positive");
        }
        this.id = Objects.requireNonNull(id, "id");
        this.pin = Objects.requireNonNull(pin, "pin");
        this.balanceCents = openingBalanceCents;
        this.maxPinAttempts = maxPinAttempts;
    }

    public PinAuthenticationResult authenticate(String enteredPin) {
        Objects.requireNonNull(enteredPin, "enteredPin");
        lock.lock();
        try {
            if (locked) {
                return new PinAuthenticationResult(false, true, 0);
            }
            if (pin.equals(enteredPin)) {
                failedPinAttempts = 0;
                return new PinAuthenticationResult(true, false, maxPinAttempts);
            }
            failedPinAttempts++;
            int remaining = Math.max(0, maxPinAttempts - failedPinAttempts);
            if (remaining == 0) {
                locked = true;
            }
            return new PinAuthenticationResult(false, locked, remaining);
        } finally {
            lock.unlock();
        }
    }

    public int deposit(int amountCents) {
        requirePositive(amountCents);
        lock.lock();
        try {
            balanceCents += amountCents;
            return balanceCents;
        } finally {
            lock.unlock();
        }
    }

    public int debit(int amountCents) {
        requirePositive(amountCents);
        lock.lock();
        try {
            if (balanceCents < amountCents) {
                throw new InsufficientFundsException("Account " + id + " has insufficient funds");
            }
            balanceCents -= amountCents;
            return balanceCents;
        } finally {
            lock.unlock();
        }
    }

    public boolean hasAtLeast(int amountCents) {
        requirePositive(amountCents);
        lock.lock();
        try {
            return balanceCents >= amountCents;
        } finally {
            lock.unlock();
        }
    }

    public int balanceCents() {
        lock.lock();
        try {
            return balanceCents;
        } finally {
            lock.unlock();
        }
    }

    public boolean isLocked() {
        lock.lock();
        try {
            return locked;
        } finally {
            lock.unlock();
        }
    }

    public String getId() {
        return id;
    }

    private static void requirePositive(int amountCents) {
        if (amountCents <= 0) {
            throw new IllegalArgumentException("amountCents must be positive");
        }
    }
}
