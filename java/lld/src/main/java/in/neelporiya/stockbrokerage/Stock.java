package in.neelporiya.stockbrokerage;

/** Immutable identity of a tradable instrument. */
public record Stock(String symbol, String companyName) {

    public Stock {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("symbol is required");
        }
    }
}
