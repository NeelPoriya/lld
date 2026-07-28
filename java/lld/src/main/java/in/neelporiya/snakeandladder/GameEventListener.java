package in.neelporiya.snakeandladder;

/**
 * // DESIGN PATTERN: Observer — scoreboards, logs, or analytics react to moves without Game knowing
 * concrete listener classes.
 */
public interface GameEventListener {

    default void onMove(MoveResult result) {
    }

    default void onWin(GameSnapshot snapshot) {
    }
}
