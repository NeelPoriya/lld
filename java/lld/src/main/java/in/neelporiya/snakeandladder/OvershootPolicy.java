package in.neelporiya.snakeandladder;

/**
 * Configures what happens when a roll goes past the last cell.
 */
public enum OvershootPolicy {
    /**
     * Classic interview rule: a player must roll the exact remaining distance; otherwise no movement.
     */
    EXACT_ROLL_REQUIRED {
        @Override
        int landingCell(int currentCell, int roll, int finalCell) {
            int attempted = currentCell + roll;
            return attempted > finalCell ? currentCell : attempted;
        }
    },

    /**
     * Alternate house rule: overshooting clamps the player to the final cell and wins immediately.
     */
    CLAMP_TO_FINAL_CELL {
        @Override
        int landingCell(int currentCell, int roll, int finalCell) {
            return Math.min(currentCell + roll, finalCell);
        }
    };

    abstract int landingCell(int currentCell, int roll, int finalCell);
}
