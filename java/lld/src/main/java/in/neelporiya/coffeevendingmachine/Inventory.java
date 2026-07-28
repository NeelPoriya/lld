package in.neelporiya.coffeevendingmachine;

import in.neelporiya.coffeevendingmachine.observer.LowInventoryListener;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The shared ingredient tanks.
 *
 * <p>// CONCURRENCY: This is where the coffee machine earns its "concurrency problem" reputation.
 * {@link #tryConsume} performs a <strong>multi-ingredient, all-or-nothing reservation</strong> under
 * a single lock: it first verifies every required ingredient is available, and only then decrements
 * them all. Without this atomicity, two brews racing for the last of the water could both pass an
 * individual check and then both decrement, driving a tank negative (an over-pour). Because the two
 * phases (check + decrement) sit in one critical section, exactly one brew wins.
 *
 * <p>// INTERVIEW INSIGHT: the lock guards only the tiny reservation, not the slow physical brew, so
 * outlets still brew in parallel. Low-inventory listeners are invoked <em>after</em> the lock is
 * released to avoid running arbitrary callback code while holding a lock (a deadlock risk).
 */
public class Inventory {

    private final Map<Ingredient, Integer> tanks = new EnumMap<>(Ingredient.class);
    private final ReentrantLock lock = new ReentrantLock();
    private final List<LowInventoryListener> listeners = new CopyOnWriteArrayList<>();
    private final int lowThreshold;

    public Inventory(int lowThreshold) {
        this.lowThreshold = lowThreshold;
    }

    public void refill(Ingredient ingredient, int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be >= 0");
        }
        lock.lock();
        try {
            tanks.merge(ingredient, amount, Integer::sum);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Atomically reserve every ingredient in {@code recipe}, or reserve nothing.
     *
     * @return {@code true} if all ingredients were consumed, {@code false} if any was short.
     */
    public boolean tryConsume(Recipe recipe) {
        List<LowTank> nowLow = new ArrayList<>();
        lock.lock();
        try {
            // Phase 1: check EVERYTHING is available before touching any tank.
            for (Map.Entry<Ingredient, Integer> need : recipe.ingredients().entrySet()) {
                if (tanks.getOrDefault(need.getKey(), 0) < need.getValue()) {
                    return false; // finally still unlocks; nothing was consumed
                }
            }
            // Phase 2: now it is safe to decrement all of them.
            for (Map.Entry<Ingredient, Integer> need : recipe.ingredients().entrySet()) {
                int remaining = tanks.merge(need.getKey(), -need.getValue(), Integer::sum);
                if (remaining <= lowThreshold) {
                    nowLow.add(new LowTank(need.getKey(), remaining));
                }
            }
        } finally {
            lock.unlock();
        }
        // Notify observers OUTSIDE the lock.
        for (LowTank low : nowLow) {
            listeners.forEach(listener -> listener.onLow(low.ingredient(), low.remaining()));
        }
        return true;
    }

    public int quantityOf(Ingredient ingredient) {
        lock.lock();
        try {
            return tanks.getOrDefault(ingredient, 0);
        } finally {
            lock.unlock();
        }
    }

    public void addListener(LowInventoryListener listener) {
        listeners.add(listener);
    }

    private record LowTank(Ingredient ingredient, int remaining) {
    }
}
