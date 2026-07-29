package in.neelporiya.cricinfo;

import java.util.List;
import java.util.Objects;

/** A cricket team with an immutable squad list. */
public class Team {

    private final String name;
    private final List<Player> players;

    public Team(String name, List<Player> players) {
        this.name = Objects.requireNonNull(name, "name");
        this.players = List.copyOf(Objects.requireNonNull(players, "players"));
        if (this.players.isEmpty()) {
            throw new IllegalArgumentException("A team needs at least one player");
        }
    }

    public String getName() {
        return name;
    }

    public List<Player> getPlayers() {
        return players;
    }

    @Override
    public String toString() {
        return name;
    }
}
