package in.neelporiya.fooddelivery;

/** A line on an order: an item and how many were ordered. */
public record OrderLine(MenuItem item, int quantity) {

    public OrderLine {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
    }
}
