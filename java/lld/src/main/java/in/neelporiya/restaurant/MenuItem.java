package in.neelporiya.restaurant;

import java.util.Objects;

/** Immutable menu item. Money is stored as integer cents; never use double for currency. */
public class MenuItem {
    private final String id;
    private final String name;
    private final long priceCents;

    public MenuItem(String id, String name, long priceCents) {
        if (priceCents < 0) {
            throw new IllegalArgumentException("priceCents cannot be negative");
        }
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.priceCents = priceCents;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getPriceCents() {
        return priceCents;
    }
}
