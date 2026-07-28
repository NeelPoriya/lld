package in.neelporiya.restaurant;

import in.neelporiya.restaurant.exception.MenuItemNotFoundException;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Restaurant menu indexed by item id. */
public class Menu {
    private final Map<String, MenuItem> itemsById = new ConcurrentHashMap<>();

    public void addItem(MenuItem item) {
        itemsById.put(item.getId(), Objects.requireNonNull(item, "item"));
    }

    public Optional<MenuItem> findById(String id) {
        return Optional.ofNullable(itemsById.get(id));
    }

    public MenuItem requireItem(String id) {
        return findById(id).orElseThrow(() -> new MenuItemNotFoundException("No menu item with id " + id));
    }

    public Collection<MenuItem> items() {
        return Map.copyOf(itemsById).values();
    }
}
