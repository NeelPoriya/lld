package in.neelporiya.fooddelivery;

/**
 * A restaurant: identity, where it is, its menu, and whether it's currently taking orders.
 *
 * <p>{@code open} is {@code volatile} — the owner toggling service is a single write other threads
 * must see promptly.
 */
public class Restaurant {

    private final String id;
    private final String name;
    private final Location location;
    private final Menu menu = new Menu();
    private volatile boolean open = true;

    public Restaurant(String id, String name, Location location) {
        this.id = id;
        this.name = name;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public Menu getMenu() {
        return menu;
    }

    public boolean isOpen() {
        return open;
    }

    public void open() {
        this.open = true;
    }

    public void close() {
        this.open = false;
    }
}
