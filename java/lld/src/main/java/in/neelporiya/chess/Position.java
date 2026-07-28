package in.neelporiya.chess;

public record Position(int row, int column) {

    public Position {
        if (!isInside(row, column)) {
            throw new IllegalArgumentException("position must be on an 8x8 board");
        }
    }

    public static boolean isInside(int row, int column) {
        return row >= 0 && row < Board.SIZE && column >= 0 && column < Board.SIZE;
    }
}
