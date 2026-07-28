package in.neelporiya.restaurant.exception;

/** Base unchecked exception for the restaurant domain. */
public class RestaurantException extends RuntimeException {
    public RestaurantException(String message) {
        super(message);
    }
}
