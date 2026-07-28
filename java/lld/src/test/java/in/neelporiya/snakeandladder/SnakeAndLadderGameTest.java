package in.neelporiya.snakeandladder;

import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnakeAndLadderGameTest {

    @Test
    void rollingMovesTheCurrentPlayer() {
        Player alice = new Player("A", "Alice");
        Player bob = new Player("B", "Bob");
        Game game = gameWith(Board.standardBoard(), new ScriptedDice(4), alice, bob);

        MoveResult result = game.playTurn();

        assertEquals(alice, result.player());
        assertEquals(4, alice.getPosition());
        assertEquals(bob, game.getCurrentPlayer(), "turn rotates after a non-winning move");
    }

    @Test
    void landingOnLadderBottomJumpsToTop() {
        Board board = Board.builder().addLadder(4, 14).build();
        Player alice = new Player("A", "Alice");
        Player bob = new Player("B", "Bob");
        Game game = gameWith(board, new ScriptedDice(4), alice, bob);

        MoveResult result = game.playTurn();

        assertTrue(result.jump().orElseThrow() instanceof Ladder);
        assertEquals(14, alice.getPosition());
        assertEquals(14, result.finalPosition());
    }

    @Test
    void landingOnSnakeHeadDropsToTail() {
        Board board = Board.builder().addSnake(6, 2).build();
        Player alice = new Player("A", "Alice");
        Player bob = new Player("B", "Bob");
        Game game = gameWith(board, new ScriptedDice(6), alice, bob);

        MoveResult result = game.playTurn();

        assertTrue(result.jump().orElseThrow() instanceof Snake);
        assertEquals(2, alice.getPosition());
        assertEquals(2, result.finalPosition());
    }

    @Test
    void overshootBeyondHundredDoesNotMoveWithDefaultPolicy() {
        Player alice = new Player("A", "Alice");
        Player bob = new Player("B", "Bob");
        Game game = gameWith(Board.standardBoard(), new ScriptedDice(98, 1, 5), alice, bob);

        game.playTurn(); // Alice -> 98
        game.playTurn(); // Bob -> 1
        MoveResult overshoot = game.playTurn(); // Alice attempts 103

        assertTrue(overshoot.overshot());
        assertEquals(98, alice.getPosition(), "exact-roll policy keeps the player in place");
        assertEquals(GameStatus.RUNNING, game.getStatus());
    }

    @Test
    void fullyScriptedMultiPlayerGameProducesExpectedWinner() {
        Board board = Board.builder().addLadder(3, 99).build();
        Player alice = new Player("A", "Alice");
        Player bob = new Player("B", "Bob");
        Game game = gameWith(board, new ScriptedDice(3, 4, 1), alice, bob);

        game.playTurn(); // Alice climbs 3 -> 99.
        game.playTurn(); // Bob moves to 4.
        MoveResult winningMove = game.playTurn(); // Alice rolls exact 1.

        assertEquals(GameStatus.FINISHED, winningMove.statusAfterTurn());
        assertEquals(alice, game.getWinner().orElseThrow());
        assertEquals(100, alice.getPosition());
    }

    @Test
    void turnRotationIsCorrectForMultiplePlayers() {
        Player alice = new Player("A", "Alice");
        Player bob = new Player("B", "Bob");
        Player cara = new Player("C", "Cara");
        Game game = Game.builder()
                .players(List.of(alice, bob, cara))
                .dice(new ScriptedDice(1, 2, 3))
                .build();

        game.playTurn();
        assertEquals(bob, game.getCurrentPlayer());
        game.playTurn();
        assertEquals(cara, game.getCurrentPlayer());
        game.playTurn();
        assertEquals(alice, game.getCurrentPlayer());

        assertEquals(1, alice.getPosition());
        assertEquals(2, bob.getPosition());
        assertEquals(3, cara.getPosition());
    }

    private static Game gameWith(Board board, Dice dice, Player... players) {
        return Game.builder()
                .board(board)
                .players(List.of(players))
                .dice(dice)
                .build();
    }

    private static final class ScriptedDice implements Dice {

        private final Queue<Integer> rolls = new ArrayDeque<>();

        private ScriptedDice(int... rolls) {
            for (int roll : rolls) {
                this.rolls.add(roll);
            }
        }

        @Override
        public int roll() {
            if (rolls.isEmpty()) {
                throw new AssertionError("scripted dice exhausted");
            }
            return rolls.remove();
        }
    }
}
