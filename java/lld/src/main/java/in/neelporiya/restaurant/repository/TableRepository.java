package in.neelporiya.restaurant.repository;

import in.neelporiya.restaurant.Table;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** // DESIGN PATTERN: Repository — hides storage behind a collection-like API. */
public class TableRepository {
    private final Map<String, Table> tables = new ConcurrentHashMap<>();

    public void save(Table table) {
        tables.put(table.getId(), table);
    }

    public Optional<Table> findById(String id) {
        return Optional.ofNullable(tables.get(id));
    }

    public Collection<Table> findAll() {
        return Map.copyOf(tables).values();
    }
}
