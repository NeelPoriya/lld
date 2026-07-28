package in.neelporiya.snakeandladder;

/**
 * Snake head-to-tail transition.
 */
public record Snake(int from, int to) implements Jump {

    public Snake {
        if (from <= to) {
            throw new IllegalArgumentException("snake must move down from head to tail");
        }
    }
}
