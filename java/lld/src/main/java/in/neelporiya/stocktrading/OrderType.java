package in.neelporiya.stocktrading;

/**
 * // INTERVIEW INSIGHT: a LIMIT order names a worst-acceptable price and rests if it can't fill; a
 * MARKET order takes whatever liquidity exists right now and never rests.
 */
public enum OrderType {
    LIMIT,
    MARKET
}
