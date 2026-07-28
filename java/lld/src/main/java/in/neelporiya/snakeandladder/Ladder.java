package in.neelporiya.snakeandladder;

/**
 * Ladder bottom-to-top transition.
 */
public record Ladder(int from, int to) implements Jump {

    public Ladder {
        if (from >= to) {
            throw new IllegalArgumentException("ladder must move up from bottom to top");
        }
    }
}
