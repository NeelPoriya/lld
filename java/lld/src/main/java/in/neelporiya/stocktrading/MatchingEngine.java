package in.neelporiya.stocktrading;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * // DESIGN PATTERN: Facade — the exchange. Routes each order to its symbol's {@link OrderBook},
 * matches it under that book's lock and publishes the resulting trades.
 *
 * <p>This is the counterpart to the brokerage (#31): the brokerage prices orders off a feed, while
 * this engine is where a buyer and a seller are actually PAIRED via price-time priority.
 *
 * <p>// CONCURRENCY: one lock per book means different symbols match in parallel; the same symbol is
 * serialized so its book never interleaves two matches. Books are created lazily and safely.
 */
public class MatchingEngine {

    private final Map<String, OrderBook> books = new ConcurrentHashMap<>();
    private final List<TradeListener> listeners = new CopyOnWriteArrayList<>();

    public void addListener(TradeListener listener) {
        listeners.add(listener);
    }

    /**
     * Submit an order for matching.
     *
     * @return the trades it generated (possibly empty if it rested or was rejected). Inspect
     *         {@code order.getStatus()} for its final state.
     */
    public List<Trade> placeOrder(Order order) {
        OrderBook book = books.computeIfAbsent(order.getSymbol(), OrderBook::new);
        ReentrantLock lock = book.getLock();
        lock.lock();
        try {
            List<Trade> trades = book.submit(order);
            trades.forEach(trade -> listeners.forEach(l -> l.onTrade(trade)));
            if (order.getStatus() == OrderStatus.REJECTED) {
                listeners.forEach(l -> l.onOrderRejected(order));
            }
            return trades;
        } finally {
            lock.unlock();
        }
    }

    public boolean cancelOrder(String symbol, String orderId) {
        OrderBook book = books.get(symbol);
        if (book == null) {
            return false;
        }
        book.getLock().lock();
        try {
            return book.cancel(orderId);
        } finally {
            book.getLock().unlock();
        }
    }

    /** The (lazily created) book for a symbol, for inspection. */
    public OrderBook getOrderBook(String symbol) {
        return books.computeIfAbsent(symbol, OrderBook::new);
    }
}
