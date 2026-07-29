package in.neelporiya.stocktrading;

public enum OrderStatus {
    NEW,
    PARTIALLY_FILLED,
    FILLED,
    CANCELLED,
    /** A market order that found no (or not enough) liquidity and could not rest. */
    REJECTED
}
