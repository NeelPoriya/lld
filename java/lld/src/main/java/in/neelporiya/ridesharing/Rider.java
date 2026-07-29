package in.neelporiya.ridesharing;

import java.util.Objects;

public record Rider(String id, String name) {

    public Rider {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
    }
}
