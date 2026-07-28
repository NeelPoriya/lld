package in.neelporiya.chess;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Facade and state machine for a single Chess match.
 *
 * <p>// DESIGN PATTERN: Piece subclasses are movement strategies; Game only orchestrates validation,
 * turn order, and status transitions.
 *
 * <p>// INTERVIEW INSIGHT: Legal chess moves are not only geometric. A move that exposes your own
 * king is rejected by simulating the move and checking the resulting board.
 *
 * <p>// CONCURRENCY: makeMove is synchronized so "validate → mutate board → promote pawn → switch
 * turn → update status" is one atomic critical section. Two racing callers cannot both move on the
 * same turn.
 *
 * <p>// TESTABILITY: Tests can start from Game.emptyGame() and place exact pieces on the Board, so
 * check and checkmate scenarios do not require replaying a whole opening.
 *
 * <p>// EXTENSIBILITY: Castling, en passant, and richer draw rules can be introduced as Move
 * validators/commands without rewriting Piece polymorphism.
 */
public final class Game {

    private final Board board;
    private final Player whitePlayer;
    private final Player blackPlayer;
    private final List<Move> moves = new ArrayList<>();

    private Color currentTurn = Color.WHITE;
    private GameStatus status = GameStatus.ACTIVE;

    private Game(Board board, Player whitePlayer, Player blackPlayer) {
        this.board = Objects.requireNonNull(board, "board");
        this.whitePlayer = requireColor(whitePlayer, Color.WHITE);
        this.blackPlayer = requireColor(blackPlayer, Color.BLACK);
    }

    public static Game defaultGame() {
        return new Game(BoardFactory.standardBoard(), new Player("White", Color.WHITE), new Player("Black", Color.BLACK));
    }

    public static Game emptyGame() {
        return new Game(BoardFactory.emptyBoard(), new Player("White", Color.WHITE), new Player("Black", Color.BLACK));
    }

    public synchronized GameStatus makeMove(int fromRow, int fromColumn, int toRow, int toColumn) {
        return makeMove(new Position(fromRow, fromColumn), new Position(toRow, toColumn));
    }

    public synchronized GameStatus makeMove(Position from, Position to) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        if (status.isTerminal()) {
            throw new InvalidMoveException("game is already over with status " + status);
        }

        Piece moving = board.pieceAt(from);
        if (moving == null) {
            throw new InvalidMoveException("no piece at " + from);
        }
        if (moving.getColor() != currentTurn) {
            throw new InvalidMoveException("expected " + currentTurn + " to move next");
        }

        Piece target = board.pieceAt(to);
        if (target != null && target.getColor() == moving.getColor()) {
            throw new InvalidMoveException("cannot capture your own piece");
        }
        if (target instanceof King) {
            throw new InvalidMoveException("the king is checkmated, not captured");
        }
        if (!moving.legalMoves(board, from).contains(to)) {
            throw new InvalidMoveException("piece cannot move from " + from + " to " + to);
        }
        if (wouldLeaveKingInCheck(from, to, moving.getColor())) {
            throw new InvalidMoveException("move leaves " + moving.getColor() + " king in check");
        }

        Move move = board.movePiece(from, to);
        promotePawnIfNeeded(to);
        moves.add(move);

        currentTurn = currentTurn.opposite();
        status = calculateStatusFor(currentTurn);
        return status;
    }

    public synchronized GameStatus getStatus() {
        return status;
    }

    public synchronized Color getCurrentTurn() {
        return currentTurn;
    }

    public Board getBoard() {
        return board;
    }

    public Player getWhitePlayer() {
        return whitePlayer;
    }

    public Player getBlackPlayer() {
        return blackPlayer;
    }

    public synchronized List<Move> getMoves() {
        return List.copyOf(moves);
    }

    public synchronized boolean isInCheck(Color color) {
        Position king = findKing(color);
        return isSquareAttacked(king, color.opposite());
    }

    private GameStatus calculateStatusFor(Color sideToMove) {
        boolean inCheck = isInCheck(sideToMove);
        boolean hasLegalMove = hasAnyLegalMove(sideToMove);
        if (inCheck && !hasLegalMove) {
            return GameStatus.CHECKMATE;
        }
        if (!inCheck && !hasLegalMove) {
            return GameStatus.STALEMATE;
        }
        return inCheck ? GameStatus.CHECK : GameStatus.ACTIVE;
    }

    private boolean hasAnyLegalMove(Color color) {
        for (Position from : board.positionsOccupiedBy(color)) {
            Piece piece = board.pieceAt(from);
            for (Position to : piece.legalMoves(board, from)) {
                Piece target = board.pieceAt(to);
                if (!(target instanceof King) && !wouldLeaveKingInCheck(from, to, color)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean wouldLeaveKingInCheck(Position from, Position to, Color color) {
        Move move = board.movePiece(from, to);
        try {
            return isInCheck(color);
        } finally {
            board.undoMove(move);
        }
    }

    private Position findKing(Color color) {
        for (Position position : board.positionsOccupiedBy(color)) {
            Piece piece = board.pieceAt(position);
            if (piece instanceof King) {
                return position;
            }
        }
        throw new IllegalStateException("missing " + color + " king");
    }

    private boolean isSquareAttacked(Position square, Color byColor) {
        for (Position attackerPosition : board.positionsOccupiedBy(byColor)) {
            Piece attacker = board.pieceAt(attackerPosition);
            if (attacker.legalMoves(board, attackerPosition).contains(square)) {
                return true;
            }
        }
        return false;
    }

    private void promotePawnIfNeeded(Position position) {
        Piece piece = board.pieceAt(position);
        if (piece instanceof Pawn && (position.row() == 0 || position.row() == Board.SIZE - 1)) {
            board.placePiece(position, new Queen(piece.getColor()));
        }
    }

    private static Player requireColor(Player player, Color color) {
        Objects.requireNonNull(player, "player");
        if (player.getColor() != color) {
            throw new IllegalArgumentException("player must be " + color);
        }
        return player;
    }
}
