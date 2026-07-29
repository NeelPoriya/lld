package in.neelporiya.stockbrokerage;

public enum OrderStatus {
    /** Accepted but not yet executed (a resting limit order). */
    OPEN,
    FILLED,
    CANCELLED,
    /** Refused up front, e.g. a limit already marketable but unaffordable. */
    REJECTED
}
