package in.neelporiya.restaurant.exception;

public class TableNotFoundException extends RestaurantException {
    public TableNotFoundException(String message) {
        super(message);
    }
}
