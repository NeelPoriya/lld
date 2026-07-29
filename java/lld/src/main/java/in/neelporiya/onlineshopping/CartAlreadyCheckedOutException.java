package in.neelporiya.onlineshopping;

public class CartAlreadyCheckedOutException extends ShoppingException {
    public CartAlreadyCheckedOutException(String message) {
        super(message);
    }
}
