package in.neelporiya.stocktrading;

/**
 * // DESIGN PATTERN: Observer — every execution is broadcast to subscribers (market-data tape, risk,
 * settlement) without the engine depending on them.
 */
public interface TradeListener {

    void onTrade(Trade trade);

    /** A market order that couldn't fill at all. */
    default void onOrderRejected(Order order) {
    }
}
