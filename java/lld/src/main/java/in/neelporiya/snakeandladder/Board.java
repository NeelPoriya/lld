package in.neelporiya.snakeandladder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable board model: a numbered track plus snakes/ladders keyed by their starting cell.
 *
 * <p>// DESIGN PATTERN: Builder/Factory — {@link Builder} provides readable setup for the common
 * 100-cell board and custom interview boards.
 *
 * <p>// EXTENSIBILITY: the board stores the {@link Jump} abstraction, so adding a new transition
 * (portal, penalty, bonus) does not change {@code Game}.
 */
public final class Board {

    public static final int DEFAULT_SIZE = 100;

    private final int size;
    private final Map<Integer, Jump> jumpsByStart;

    private Board(int size, Map<Integer, Jump> jumpsByStart) {
        if (size < 2) {
            throw new IllegalArgumentException("board size must be at least 2");
        }
        this.size = size;
        this.jumpsByStart = Map.copyOf(jumpsByStart);
    }

    public static Board standardBoard() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public Optional<Jump> jumpAt(int cell) {
        validateCell(cell);
        return Optional.ofNullable(jumpsByStart.get(cell));
    }

    public int applyJump(int cell) {
        return jumpAt(cell).map(Jump::apply).orElse(cell);
    }

    public int getSize() {
        return size;
    }

    public Map<Integer, Jump> getJumpsByStart() {
        return jumpsByStart;
    }

    private void validateCell(int cell) {
        if (cell < 1 || cell > size) {
            throw new IllegalArgumentException("cell must be between 1 and " + size + ": " + cell);
        }
    }

    /**
     * // DESIGN PATTERN: Builder — assemble snakes and ladders fluently, with validation centralized.
     */
    public static final class Builder {

        private int size = DEFAULT_SIZE;
        private final List<Jump> jumps = new ArrayList<>();

        private Builder() {
        }

        public Builder size(int size) {
            this.size = size;
            return this;
        }

        public Builder addSnake(int head, int tail) {
            return addJump(new Snake(head, tail));
        }

        public Builder addLadder(int bottom, int top) {
            return addJump(new Ladder(bottom, top));
        }

        public Builder addJump(Jump jump) {
            this.jumps.add(Objects.requireNonNull(jump, "jump"));
            return this;
        }

        public Board build() {
            Map<Integer, Jump> byStart = new LinkedHashMap<>();
            for (Jump jump : jumps) {
                validateJump(jump);
                Jump previous = byStart.putIfAbsent(jump.from(), jump);
                if (previous != null) {
                    throw new IllegalStateException("duplicate jump start cell: " + jump.from());
                }
            }
            return new Board(size, byStart);
        }

        private void validateJump(Jump jump) {
            if (jump.from() < 1 || jump.from() > size || jump.to() < 1 || jump.to() > size) {
                throw new IllegalArgumentException("jump must stay within board: " + jump);
            }
            if (jump.from() == size) {
                throw new IllegalArgumentException("final cell cannot start a jump");
            }
        }
    }
}
