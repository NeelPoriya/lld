package in.neelporiya.stocktrading;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The order book for ONE symbol, matching with strict PRICE-TIME priority.
 *
 * <p>Structure: each side is a {@code TreeMap} of price → FIFO queue of resting orders. Bids are
 * reverse-ordered so {@code firstEntry()} is the highest (best) bid; asks are natural-ordered so
 * {@code firstEntry()} is the lowest (best) ask. Within a price level the {@link Deque} preserves
 * arrival order — that is the "time" in price-time.
 *
 * <p>// CONCURRENCY: one lock PER book (held by the {@link MatchingEngine}), so matching within a
 * symbol is serialized while different symbols match fully in parallel.
 */
public class OrderBook {

    private final String symbol;
    private final ReentrantLock lock = new ReentrantLock();

    private final TreeMap<Long, Deque<Order>> bids = new TreeMap<>(Comparator.reverseOrder());
    private final TreeMap<Long, Deque<Order>> asks = new TreeMap<>();
    private final Map<String, Order> restingById = new java.util.HashMap<>();

    public OrderBook(String symbol) {
        this.symbol = symbol;
    }

    ReentrantLock getLock() {
        return lock;
    }

    /**
     * Match an incoming order against the opposite side, then rest the remainder (LIMIT only).
     *
     * @return the trades generated, in execution order.
     */
    List<Trade> submit(Order incoming) {
        List<Trade> trades = new ArrayList<>();
        TreeMap<Long, Deque<Order>> opposite = incoming.getSide() == OrderSide.BUY ? asks : bids;

        while (incoming.getRemainingQuantity() > 0 && !opposite.isEmpty()) {
            Map.Entry<Long, Deque<Order>> bestLevel = opposite.firstEntry();
            long makerPrice = bestLevel.getKey();
            if (!crosses(incoming, makerPrice)) {
                break; // best opposite price is not acceptable -> stop
            }
            Deque<Order> level = bestLevel.getValue();
            Order maker = level.peekFirst();
            int quantity = Math.min(incoming.getRemainingQuantity(), maker.getRemainingQuantity());

            trades.add(buildTrade(incoming, maker, makerPrice, quantity));
            incoming.fill(quantity);
            maker.fill(quantity);
            incoming.setStatus(incoming.getRemainingQuantity() == 0 ? OrderStatus.FILLED : OrderStatus.PARTIALLY_FILLED);

            if (maker.getRemainingQuantity() == 0) {
                level.pollFirst();
                restingById.remove(maker.getId());
                maker.setStatus(OrderStatus.FILLED);
                if (level.isEmpty()) {
                    opposite.remove(makerPrice);
                }
            } else {
                maker.setStatus(OrderStatus.PARTIALLY_FILLED);
            }
        }

        finalizeIncoming(incoming);
        return trades;
    }

    boolean cancel(String orderId) {
        Order order = restingById.remove(orderId);
        if (order == null) {
            return false;
        }
        TreeMap<Long, Deque<Order>> side = order.getSide() == OrderSide.BUY ? bids : asks;
        Deque<Order> level = side.get(order.getPrice());
        if (level != null) {
            level.remove(order);
            if (level.isEmpty()) {
                side.remove(order.getPrice());
            }
        }
        order.setStatus(OrderStatus.CANCELLED);
        return true;
    }

    private void finalizeIncoming(Order incoming) {
        if (incoming.getRemainingQuantity() == 0) {
            incoming.setStatus(OrderStatus.FILLED);
            return;
        }
        if (incoming.getType() == OrderType.LIMIT) {
            rest(incoming);
            incoming.setStatus(incoming.getFilledQuantity() > 0 ? OrderStatus.PARTIALLY_FILLED : OrderStatus.NEW);
        } else {
            // Market orders never rest: whatever couldn't fill is dropped.
            incoming.setStatus(incoming.getFilledQuantity() > 0 ? OrderStatus.PARTIALLY_FILLED : OrderStatus.REJECTED);
        }
    }

    private void rest(Order order) {
        TreeMap<Long, Deque<Order>> side = order.getSide() == OrderSide.BUY ? bids : asks;
        side.computeIfAbsent(order.getPrice(), k -> new ArrayDeque<>()).addLast(order);
        restingById.put(order.getId(), order);
    }

    private Trade buildTrade(Order incoming, Order maker, long price, int quantity) {
        boolean incomingIsBuy = incoming.getSide() == OrderSide.BUY;
        String buyId = incomingIsBuy ? incoming.getId() : maker.getId();
        String sellId = incomingIsBuy ? maker.getId() : incoming.getId();
        return new Trade(symbol, buyId, sellId, price, quantity);
    }

    private static boolean crosses(Order incoming, long bestOppositePrice) {
        if (incoming.getType() == OrderType.MARKET) {
            return true; // takes any available price
        }
        return incoming.getSide() == OrderSide.BUY
                ? bestOppositePrice <= incoming.getPrice()   // willing to pay at least the ask
                : bestOppositePrice >= incoming.getPrice();  // willing to sell at most the bid
    }

    // --- read-only views for callers/tests (call under the book lock for a consistent snapshot) ---

    public OptionalLong bestBid() {
        return bids.isEmpty() ? OptionalLong.empty() : OptionalLong.of(bids.firstKey());
    }

    public OptionalLong bestAsk() {
        return asks.isEmpty() ? OptionalLong.empty() : OptionalLong.of(asks.firstKey());
    }

    public int restingOrderCount() {
        return restingById.size();
    }

    public String getSymbol() {
        return symbol;
    }
}
