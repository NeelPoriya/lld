package in.neelporiya.snakeandladder;

import java.util.Objects;
import java.util.Optional;

/**
 * Immutable report for a single turn.
 */
public record MoveResult(
        Player player,
        int roll,
        int startPosition,
        int attemptedPosition,
        int landingPosition,
        int finalPosition,
        Optional<Jump> jump,
        boolean overshot,
        GameStatus statusAfterTurn) {

    public MoveResult {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(jump, "jump");
        Objects.requireNonNull(statusAfterTurn, "statusAfterTurn");
    }

    public boolean moved() {
        return startPosition != finalPosition;
    }

    public boolean won() {
        return statusAfterTurn == GameStatus.FINISHED;
    }
}
