package in.neelporiya.onlineshopping;

public class InsufficientStockException extends ShoppingException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
