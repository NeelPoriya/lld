package in.neelporiya.onlineshopping;

/** Base unchecked exception for domain failures. */
public class ShoppingException extends RuntimeException {
    public ShoppingException(String message) {
        super(message);
    }
}
