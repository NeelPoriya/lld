package in.neelporiya.chess;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChessGameTest {

    @Test
    void rookMovesHorizontallyWhenPathIsClear() {
        Game game = gameWithKings();
        game.getBoard().placePiece(p(4, 3), new Rook(Color.WHITE));

        assertEquals(GameStatus.ACTIVE, game.makeMove(p(4, 3), p(4, 7)));
        assertInstanceOf(Rook.class, game.getBoard().getPiece(p(4, 7)).orElseThrow());
    }

    @Test
    void bishopMovesDiagonallyWhenPathIsClear() {
        Game game = gameWithKings();
        game.getBoard().placePiece(p(4, 3), new Bishop(Color.WHITE));

        assertEquals(GameStatus.ACTIVE, game.makeMove(p(4, 3), p(2, 5)));
        assertInstanceOf(Bishop.class, game.getBoard().getPiece(p(2, 5)).orElseThrow());
    }

    @Test
    void knightJumpsInAnLShape() {
        Game game = gameWithKings();
        game.getBoard().placePiece(p(4, 4), new Knight(Color.WHITE));
        game.getBoard().placePiece(p(4, 5), new Pawn(Color.WHITE));

        assertEquals(GameStatus.ACTIVE, game.makeMove(p(4, 4), p(5, 6)));
        assertInstanceOf(Knight.class, game.getBoard().getPiece(p(5, 6)).orElseThrow());
    }

    @Test
    void queenMovesLikeRookOrBishop() {
        Game game = gameWithKings();
        game.getBoard().placePiece(p(4, 4), new Queen(Color.WHITE));

        assertEquals(GameStatus.ACTIVE, game.makeMove(p(4, 4), p(1, 7)));
        assertInstanceOf(Queen.class, game.getBoard().getPiece(p(1, 7)).orElseThrow());
    }

    @Test
    void pawnCanMoveTwoSquaresFromItsStartingRank() {
        Game game = gameWithKings();
        game.getBoard().placePiece(p(6, 4), new Pawn(Color.WHITE));

        assertEquals(GameStatus.ACTIVE, game.makeMove(p(6, 4), p(4, 4)));
        assertInstanceOf(Pawn.class, game.getBoard().getPiece(p(4, 4)).orElseThrow());
    }

    @Test
    void pawnCapturesDiagonally() {
        Game game = gameWithKings();
        game.getBoard().placePiece(p(6, 4), new Pawn(Color.WHITE));
        game.getBoard().placePiece(p(5, 3), new Knight(Color.BLACK));

        assertEquals(GameStatus.ACTIVE, game.makeMove(p(6, 4), p(5, 3)));
        assertInstanceOf(Pawn.class, game.getBoard().getPiece(p(5, 3)).orElseThrow());
    }

    @Test
    void blockedPathIsRejected() {
        Game game = gameWithKings();
        game.getBoard().placePiece(p(4, 3), new Rook(Color.WHITE));
        game.getBoard().placePiece(p(3, 3), new Pawn(Color.WHITE));

        assertThrows(InvalidMoveException.class, () -> game.makeMove(p(4, 3), p(1, 3)));
    }

    @Test
    void cannotCaptureOwnPiece() {
        Game game = gameWithKings();
        game.getBoard().placePiece(p(4, 4), new Bishop(Color.WHITE));
        game.getBoard().placePiece(p(2, 6), new Knight(Color.WHITE));

        assertThrows(InvalidMoveException.class, () -> game.makeMove(p(4, 4), p(2, 6)));
    }

    @Test
    void wrongTurnIsRejected() {
        Game game = Game.defaultGame();

        assertThrows(InvalidMoveException.class, () -> game.makeMove(p(1, 4), p(3, 4)));
    }

    @Test
    void capturesEnemyPiece() {
        Game game = gameWithKings();
        game.getBoard().placePiece(p(4, 4), new Rook(Color.WHITE));
        game.getBoard().placePiece(p(4, 7), new Knight(Color.BLACK));

        assertEquals(GameStatus.ACTIVE, game.makeMove(p(4, 4), p(4, 7)));
        assertFalse(game.getBoard().getPiece(p(4, 4)).isPresent());
        assertEquals(Color.WHITE, game.getBoard().getPiece(p(4, 7)).orElseThrow().getColor());
    }

    @Test
    void detectsCheck() {
        Game game = Game.emptyGame();
        game.getBoard().placePiece(p(7, 0), new King(Color.WHITE));
        game.getBoard().placePiece(p(4, 7), new King(Color.BLACK));
        game.getBoard().placePiece(p(4, 4), new Rook(Color.WHITE));

        assertEquals(GameStatus.CHECK, game.makeMove(p(4, 4), p(4, 6)));
        assertEquals(Color.BLACK, game.getCurrentTurn());
    }

    @Test
    void foolsMateIsCheckmate() {
        Game game = Game.defaultGame();

        game.makeMove(p(6, 5), p(5, 5)); // f2 -> f3
        game.makeMove(p(1, 4), p(3, 4)); // e7 -> e5
        game.makeMove(p(6, 6), p(4, 6)); // g2 -> g4

        assertEquals(GameStatus.CHECKMATE, game.makeMove(p(0, 3), p(4, 7))); // Qh4#
    }

    @Test
    void moveAfterGameOverIsRejected() {
        Game game = Game.defaultGame();

        game.makeMove(p(6, 5), p(5, 5));
        game.makeMove(p(1, 4), p(3, 4));
        game.makeMove(p(6, 6), p(4, 6));
        game.makeMove(p(0, 3), p(4, 7));

        assertThrows(InvalidMoveException.class, () -> game.makeMove(p(6, 4), p(4, 4)));
    }

    private static Game gameWithKings() {
        Game game = Game.emptyGame();
        game.getBoard().placePiece(p(7, 0), new King(Color.WHITE));
        game.getBoard().placePiece(p(0, 4), new King(Color.BLACK));
        return game;
    }

    private static Position p(int row, int column) {
        return new Position(row, column);
    }
}
