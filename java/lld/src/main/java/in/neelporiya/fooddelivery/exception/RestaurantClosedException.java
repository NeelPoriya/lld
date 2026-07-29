package in.neelporiya.fooddelivery.exception;

public class RestaurantClosedException extends RuntimeException {
    public RestaurantClosedException(String message) {
        super(message);
    }
}
