package in.neelporiya.restaurant.exception;

public class ReservationConflictException extends RestaurantException {
    public ReservationConflictException(String message) {
        super(message);
    }
}
