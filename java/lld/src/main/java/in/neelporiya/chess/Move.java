package in.neelporiya.chess;

public record Move(Position from, Position to, Piece movedPiece, Piece capturedPiece) {
}
