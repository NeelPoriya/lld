package in.neelporiya.cricinfo;

import java.util.Objects;

/** A player in a cricket team. */
public record Player(String id, String name) {
    public Player {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
    }
}
