package in.neelporiya.stocktrading;

/**
 * An executed match. Trades always print at the RESTING (maker) order's price — the incoming taker
 * gets price improvement, which is the standard exchange rule.
 */
public record Trade(String symbol, String buyOrderId, String sellOrderId, long price, int quantity) {
}
