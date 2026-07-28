package in.neelporiya.chess;

public enum GameStatus {
    ACTIVE,
    CHECK,
    CHECKMATE,
    STALEMATE,
    DRAW;

    public boolean isTerminal() {
        return this == CHECKMATE || this == STALEMATE || this == DRAW;
    }
}
