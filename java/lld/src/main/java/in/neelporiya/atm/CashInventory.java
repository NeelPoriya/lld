package in.neelporiya.atm;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe physical cash cassette inventory.
 *
 * <p>// CONCURRENCY: The reservation is atomic: compute exact note plan and decrement cassettes
 * while holding the same inventory lock. Without this, two withdrawals could both see the same
 * last notes and dispense cash that no longer exists.
 */
public class CashInventory {

    private final ReentrantLock lock = new ReentrantLock();
    private final Map<NoteDenomination, Integer> notes = new EnumMap<>(NoteDenomination.class); // guarded by lock

    public CashInventory add(NoteDenomination denomination, int count) {
        Objects.requireNonNull(denomination, "denomination");
        if (count < 0) {
            throw new IllegalArgumentException("count cannot be negative");
        }
        lock.lock();
        try {
            notes.merge(denomination, count, Integer::sum);
            return this;
        } finally {
            lock.unlock();
        }
    }

    public Map<NoteDenomination, Integer> reserve(int amountCents, CashDispenser dispenser) {
        requirePositive(amountCents);
        Objects.requireNonNull(dispenser, "dispenser");
        lock.lock();
        try {
            if (totalCentsLocked() < amountCents) {
                throw new InsufficientAtmCashException("ATM does not have enough total cash");
            }
            Map<NoteDenomination, Integer> snapshot = new EnumMap<>(notes);
            Map<NoteDenomination, Integer> plan = dispenser.dispense(amountCents, snapshot)
                    .orElseThrow(() -> new ExactCashUnavailableException(
                            "ATM cannot dispense exact amount with available notes"));
            plan.forEach((denomination, count) -> notes.merge(denomination, -count, Integer::sum));
            notes.entrySet().removeIf(entry -> entry.getValue() == 0);
            return Map.copyOf(plan);
        } finally {
            lock.unlock();
        }
    }

    public void release(Map<NoteDenomination, Integer> reservedNotes) {
        Objects.requireNonNull(reservedNotes, "reservedNotes");
        lock.lock();
        try {
            reservedNotes.forEach((denomination, count) -> notes.merge(denomination, count, Integer::sum));
        } finally {
            lock.unlock();
        }
    }

    public int count(NoteDenomination denomination) {
        Objects.requireNonNull(denomination, "denomination");
        lock.lock();
        try {
            return notes.getOrDefault(denomination, 0);
        } finally {
            lock.unlock();
        }
    }

    public int totalCents() {
        lock.lock();
        try {
            return totalCentsLocked();
        } finally {
            lock.unlock();
        }
    }

    public Map<NoteDenomination, Integer> snapshot() {
        lock.lock();
        try {
            return Map.copyOf(notes);
        } finally {
            lock.unlock();
        }
    }

    private int totalCentsLocked() {
        return notes.entrySet().stream()
                .mapToInt(entry -> entry.getKey().cents() * entry.getValue())
                .sum();
    }

    private static void requirePositive(int amountCents) {
        if (amountCents <= 0) {
            throw new IllegalArgumentException("amountCents must be positive");
        }
    }
}
