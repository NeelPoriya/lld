package in.neelporiya.tictactoe;

/**
 * The mark a player places on the board.
 */
public enum Piece {
    X,
    O;

    public GameStatus winningStatus() {
        return this == X ? GameStatus.X_WON : GameStatus.O_WON;
    }
}
