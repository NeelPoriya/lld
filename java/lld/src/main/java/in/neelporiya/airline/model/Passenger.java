package in.neelporiya.airline.model;

import java.util.Objects;

public final class Passenger {

    private final String id;
    private final String name;

    public Passenger(String id, String name) {
        this.id = requireText(id, "id");
        this.name = requireText(name, "name");
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
