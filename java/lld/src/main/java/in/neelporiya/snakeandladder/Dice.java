package in.neelporiya.snakeandladder;

/**
 * // DESIGN PATTERN: Strategy — the game depends on a Dice abstraction, not on randomness.
 *
 * <p>// TESTABILITY: Injecting Dice is the key idea in this problem. It plays the same role that
 * injecting Clock plays in time-based problems: tests pass a scripted dice and assert an entire game
 * turn-by-turn without flakiness.
 */
public interface Dice {

    int roll();
}
