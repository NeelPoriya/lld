package in.neelporiya.restaurant.exception;

public class OrderNotFoundException extends RestaurantException {
    public OrderNotFoundException(String message) {
        super(message);
    }
}
