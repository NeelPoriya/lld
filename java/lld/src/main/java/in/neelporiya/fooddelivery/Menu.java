package in.neelporiya.fooddelivery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A restaurant's items plus their live availability (an item can sell out mid-service).
 *
 * <p>Thread-safe: backed by concurrent maps so the kitchen can flip availability while customers browse.
 */
public class Menu {

    private final Map<String, MenuItem> items = new ConcurrentHashMap<>();
    private final Map<String, Boolean> available = new ConcurrentHashMap<>();

    public MenuItem addItem(MenuItem item) {
        items.put(item.id(), item);
        available.put(item.id(), true);
        return item;
    }

    public void setAvailable(String itemId, boolean isAvailable) {
        if (items.containsKey(itemId)) {
            available.put(itemId, isAvailable);
        }
    }

    public boolean isAvailable(String itemId) {
        return Boolean.TRUE.equals(available.get(itemId));
    }

    public MenuItem get(String itemId) {
        return items.get(itemId);
    }

    public List<MenuItem> items() {
        return new ArrayList<>(items.values());
    }
}
