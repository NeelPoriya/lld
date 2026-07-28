package in.neelporiya.movieticket;

import java.util.Objects;

public class City implements Identifiable {

    private final String id;
    private final String name;

    public City(String id, String name) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
    }

    @Override
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
