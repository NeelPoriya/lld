package in.neelporiya.stockbrokerage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A brokerage account: an owner, a {@link Portfolio} (cash + positions), an order history and the
 * per-account lock that serializes everything touching this account's money.
 *
 * <p>// CONCURRENCY: one lock PER account (not a global lock) — trades on different accounts proceed
 * in parallel while trades on the same account are serialized so cash/holdings stay consistent.
 */
public class Account {

    private final String id;
    private final String ownerName;
    private final Portfolio portfolio;
    private final List<Order> orders = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();

    Account(String id, String ownerName, Portfolio portfolio) {
        this.id = id;
        this.ownerName = ownerName;
        this.portfolio = portfolio;
    }

    public String getId() {
        return id;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    ReentrantLock getLock() {
        return lock;
    }

    void addOrder(Order order) {
        lock.lock();
        try {
            orders.add(order);
        } finally {
            lock.unlock();
        }
    }

    public List<Order> getOrders() {
        lock.lock();
        try {
            return Collections.unmodifiableList(new ArrayList<>(orders));
        } finally {
            lock.unlock();
        }
    }
}
