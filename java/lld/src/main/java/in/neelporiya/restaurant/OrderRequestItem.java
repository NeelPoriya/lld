package in.neelporiya.restaurant;

/** Request DTO used by the facade when placing an order. */
public record OrderRequestItem(String menuItemId, int quantity) {
    public OrderRequestItem {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
    }
}
