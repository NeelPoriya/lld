package in.neelporiya.stockbrokerage.exception;

public class UnknownStockException extends RuntimeException {
    public UnknownStockException(String message) {
        super(message);
    }
}
