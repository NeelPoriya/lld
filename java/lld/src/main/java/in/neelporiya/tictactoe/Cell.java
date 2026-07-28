package in.neelporiya.tictactoe;

/**
 * One immutable coordinate with a mutable occupant, guarded by the owning Board/Game.
 */
public final class Cell {

    private final int row;
    private final int column;
    private Piece piece;

    Cell(int row, int column) {
        this.row = row;
        this.column = column;
    }

    void place(Piece piece) {
        this.piece = piece;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public Piece getPiece() {
        return piece;
    }

    public boolean isEmpty() {
        return piece == null;
    }
}
