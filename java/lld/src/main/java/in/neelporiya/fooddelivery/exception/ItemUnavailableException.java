package in.neelporiya.fooddelivery.exception;

public class ItemUnavailableException extends RuntimeException {
    public ItemUnavailableException(String message) {
        super(message);
    }
}
