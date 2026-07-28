package in.neelporiya.tictactoe;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TicTacToeGameTest {

    @Test
    void xWinsByRow() {
        Game game = Game.defaultGame();
        Player x = game.getXPlayer();
        Player o = game.getOPlayer();

        assertEquals(GameStatus.IN_PROGRESS, game.makeMove(x, 0, 0));
        assertEquals(GameStatus.IN_PROGRESS, game.makeMove(o, 1, 0));
        assertEquals(GameStatus.IN_PROGRESS, game.makeMove(x, 0, 1));
        assertEquals(GameStatus.IN_PROGRESS, game.makeMove(o, 1, 1));

        assertEquals(GameStatus.X_WON, game.makeMove(x, 0, 2));
    }

    @Test
    void xWinsByColumn() {
        Game game = Game.defaultGame();
        Player x = game.getXPlayer();
        Player o = game.getOPlayer();

        game.makeMove(x, 0, 0);
        game.makeMove(o, 0, 1);
        game.makeMove(x, 1, 0);
        game.makeMove(o, 1, 1);

        assertEquals(GameStatus.X_WON, game.makeMove(x, 2, 0));
    }

    @Test
    void xWinsByPrimaryDiagonal() {
        Game game = Game.defaultGame();
        Player x = game.getXPlayer();
        Player o = game.getOPlayer();

        game.makeMove(x, 0, 0);
        game.makeMove(o, 0, 1);
        game.makeMove(x, 1, 1);
        game.makeMove(o, 0, 2);

        assertEquals(GameStatus.X_WON, game.makeMove(x, 2, 2));
    }

    @Test
    void xWinsBySecondaryDiagonal() {
        Game game = Game.defaultGame();
        Player x = game.getXPlayer();
        Player o = game.getOPlayer();

        game.makeMove(x, 0, 2);
        game.makeMove(o, 0, 0);
        game.makeMove(x, 1, 1);
        game.makeMove(o, 1, 0);

        assertEquals(GameStatus.X_WON, game.makeMove(x, 2, 0));
    }

    @Test
    void fullBoardWithoutWinnerIsDraw() {
        Game game = Game.defaultGame();
        Player x = game.getXPlayer();
        Player o = game.getOPlayer();

        game.makeMove(x, 0, 0);
        game.makeMove(o, 0, 1);
        game.makeMove(x, 0, 2);
        game.makeMove(o, 1, 1);
        game.makeMove(x, 1, 0);
        game.makeMove(o, 1, 2);
        game.makeMove(x, 2, 1);
        game.makeMove(o, 2, 0);

        assertEquals(GameStatus.DRAW, game.makeMove(x, 2, 2));
    }

    @Test
    void invalidMovesAreRejected() {
        Game game = Game.defaultGame();
        Player x = game.getXPlayer();
        Player o = game.getOPlayer();

        assertThrows(InvalidMoveException.class, () -> game.makeMove(x, -1, 0));
        assertThrows(InvalidMoveException.class, () -> game.makeMove(o, 0, 0)); // X still starts.

        game.makeMove(x, 0, 0);
        assertThrows(InvalidMoveException.class, () -> game.makeMove(x, 0, 1));
        assertThrows(InvalidMoveException.class, () -> game.makeMove(o, 0, 0));
    }

    @Test
    void moveAfterGameOverIsRejected() {
        Game game = Game.defaultGame();
        Player x = game.getXPlayer();
        Player o = game.getOPlayer();

        game.makeMove(x, 0, 0);
        game.makeMove(o, 1, 0);
        game.makeMove(x, 0, 1);
        game.makeMove(o, 1, 1);
        game.makeMove(x, 0, 2);

        assertThrows(InvalidMoveException.class, () -> game.makeMove(o, 2, 2));
    }

    @Test
    void fourByFourRowWinUsesSameStrategies() {
        Game game = Game.builder().size(4).build();
        Player x = game.getXPlayer();
        Player o = game.getOPlayer();

        game.makeMove(x, 2, 0);
        game.makeMove(o, 0, 0);
        game.makeMove(x, 2, 1);
        game.makeMove(o, 0, 1);
        game.makeMove(x, 2, 2);
        game.makeMove(o, 0, 2);

        assertEquals(GameStatus.X_WON, game.makeMove(x, 2, 3));
    }

    @Test
    void concurrentMovesOnlyAllowOnePlayerForTheTurn() throws Exception {
        Game game = Game.defaultGame();
        Player x = game.getXPlayer();
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);

        Callable<Boolean> moveTopLeft = racingMove(start, game, x, 0, 0);
        Callable<Boolean> moveTopRight = racingMove(start, game, x, 0, 1);

        try {
            List<Future<Boolean>> results = List.of(pool.submit(moveTopLeft), pool.submit(moveTopRight));
            start.countDown();

            long successfulMoves = results.stream().filter(TicTacToeGameTest::completedWithTrue).count();
            assertEquals(1, successfulMoves);
            assertEquals(1, game.getBoard().getOccupiedCount());
            assertEquals(Piece.O, game.getCurrentPlayer().getPiece());
        } finally {
            pool.shutdownNow();
        }
    }

    private static Callable<Boolean> racingMove(CountDownLatch start, Game game, Player player, int row, int column) {
        return () -> {
            start.await();
            try {
                game.makeMove(player, row, column);
                return true;
            } catch (InvalidMoveException ignored) {
                return false;
            }
        };
    }

    private static boolean completedWithTrue(Future<Boolean> future) {
        try {
            return future.get();
        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
    }
}

