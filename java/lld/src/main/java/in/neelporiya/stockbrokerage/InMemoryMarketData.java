package in.neelporiya.stockbrokerage;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A trivial in-memory quote source whose prices can be moved at will.
 *
 * <p>// TESTABILITY: this is the deterministic price feed used in tests — set a price, place an order,
 * assert the fill. It is also a perfectly good default for demos.
 */
public class InMemoryMarketData implements MarketDataProvider {

    private final Map<String, BigDecimal> prices = new ConcurrentHashMap<>();

    public InMemoryMarketData setPrice(String symbol, BigDecimal price) {
        prices.put(symbol, price);
        return this;
    }

    @Override
    public BigDecimal priceOf(String symbol) {
        BigDecimal price = prices.get(symbol);
        if (price == null) {
            throw new IllegalStateException("no quote for " + symbol);
        }
        return price;
    }
}
