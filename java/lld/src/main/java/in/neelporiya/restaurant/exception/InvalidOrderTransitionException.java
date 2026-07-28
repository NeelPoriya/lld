package in.neelporiya.restaurant.exception;

public class InvalidOrderTransitionException extends RestaurantException {
    public InvalidOrderTransitionException(String message) {
        super(message);
    }
}
