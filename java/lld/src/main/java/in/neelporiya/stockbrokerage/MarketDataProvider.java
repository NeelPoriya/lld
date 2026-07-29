package in.neelporiya.stockbrokerage;

import java.math.BigDecimal;

/**
 * // DESIGN PATTERN: Strategy — the brokerage reads the "current price" through this seam instead of
 * hard-coding a feed. Production wires in a live exchange feed; tests wire in a fake with settable
 * prices, so fills are fully deterministic.
 */
public interface MarketDataProvider {

    /** @return the last traded price for {@code symbol}, never null for a known symbol. */
    BigDecimal priceOf(String symbol);
}
