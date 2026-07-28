package in.neelporiya.tictactoe;

/**
 * NxN board model. It owns cell occupancy and validates coordinate-level invariants.
 */
public final class Board {

    private static final int MIN_SIZE = 3;

    private final int size;
    private final Cell[][] cells;
    private int occupiedCount; // guarded by this

    public Board(int size) {
        if (size < MIN_SIZE) {
            throw new IllegalArgumentException("board size must be at least " + MIN_SIZE);
        }
        this.size = size;
        this.cells = new Cell[size][size];
        for (int row = 0; row < size; row++) {
            for (int column = 0; column < size; column++) {
                cells[row][column] = new Cell(row, column);
            }
        }
    }

    synchronized void place(int row, int column, Piece piece) {
        validateInBounds(row, column);
        Cell cell = cells[row][column];
        if (!cell.isEmpty()) {
            throw new InvalidMoveException("cell already occupied: (" + row + ", " + column + ")");
        }
        cell.place(piece);
        occupiedCount++;
    }

    public synchronized Piece getPieceAt(int row, int column) {
        validateInBounds(row, column);
        return cells[row][column].getPiece();
    }

    public synchronized Cell getCell(int row, int column) {
        validateInBounds(row, column);
        Cell cell = cells[row][column];
        Cell copy = new Cell(cell.getRow(), cell.getColumn());
        if (!cell.isEmpty()) {
            copy.place(cell.getPiece());
        }
        return copy;
    }

    public synchronized Piece[][] snapshot() {
        Piece[][] copy = new Piece[size][size];
        for (int row = 0; row < size; row++) {
            for (int column = 0; column < size; column++) {
                copy[row][column] = cells[row][column].getPiece();
            }
        }
        return copy;
    }

    public synchronized boolean isFull() {
        return occupiedCount == size * size;
    }

    public synchronized int getOccupiedCount() {
        return occupiedCount;
    }

    public int getSize() {
        return size;
    }

    private void validateInBounds(int row, int column) {
        if (row < 0 || row >= size || column < 0 || column >= size) {
            throw new InvalidMoveException("move out of bounds: (" + row + ", " + column + ")");
        }
    }
}

