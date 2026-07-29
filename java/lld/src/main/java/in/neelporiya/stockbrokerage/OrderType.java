package in.neelporiya.stockbrokerage;

/**
 * // INTERVIEW INSIGHT: a MARKET order trades immediately at the prevailing price; a LIMIT order only
 * trades at its limit price or better, otherwise it rests until the market moves to it.
 */
public enum OrderType {
    MARKET,
    LIMIT
}
