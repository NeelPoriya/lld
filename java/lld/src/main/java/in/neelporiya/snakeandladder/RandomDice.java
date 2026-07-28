package in.neelporiya.snakeandladder;

import java.util.Objects;
import java.util.Random;

/**
 * Production dice backed by an injected {@link Random}.
 *
 * <p>// TESTABILITY: callers may pass {@code new Random(seed)} to make production-like dice
 * deterministic, while unit tests usually inject a tiny scripted {@link Dice} instead.
 */
public final class RandomDice implements Dice {

    private static final int DEFAULT_SIDES = 6;

    private final Random random;
    private final int sides;

    public RandomDice() {
        this(new Random(), DEFAULT_SIDES);
    }

    public RandomDice(long seed) {
        this(new Random(seed), DEFAULT_SIDES);
    }

    public RandomDice(Random random) {
        this(random, DEFAULT_SIDES);
    }

    public RandomDice(Random random, int sides) {
        if (sides < 1) {
            throw new IllegalArgumentException("dice sides must be positive");
        }
        this.random = Objects.requireNonNull(random, "random");
        this.sides = sides;
    }

    @Override
    public synchronized int roll() {
        return random.nextInt(sides) + 1;
    }
}
