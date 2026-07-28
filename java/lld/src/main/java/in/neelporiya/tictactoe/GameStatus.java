package in.neelporiya.tictactoe;

/**
 * // DESIGN PATTERN: Enum state machine — every move transitions the game through these states.
 */
public enum GameStatus {
    IN_PROGRESS,
    X_WON,
    O_WON,
    DRAW;

    public boolean isTerminal() {
        return this != IN_PROGRESS;
    }
}
