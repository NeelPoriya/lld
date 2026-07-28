package in.neelporiya.stackoverflow.model;

/**
 * A vote direction with its numeric weight, so score maths is just addition.
 */
public enum VoteType {
    UP(1),
    DOWN(-1);

    private final int value;

    VoteType(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
