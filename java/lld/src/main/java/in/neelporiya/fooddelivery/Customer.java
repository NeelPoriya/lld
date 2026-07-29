package in.neelporiya.fooddelivery;

/** An immutable customer identity + where they want delivery. */
public record Customer(String id, String name, Location location) {
}
