package in.neelporiya.snakeandladder;

import java.util.Map;
import java.util.Optional;

/**
 * Read-only state report suitable for UI, tests, or observers.
 */
public record GameSnapshot(
        GameStatus status,
        Player currentPlayer,
        Optional<Player> winner,
        Map<String, Integer> positions) {

    public GameSnapshot {
        positions = Map.copyOf(positions);
        winner = winner == null ? Optional.empty() : winner;
    }
}
