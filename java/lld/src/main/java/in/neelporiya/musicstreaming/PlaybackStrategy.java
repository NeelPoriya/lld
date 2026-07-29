package in.neelporiya.musicstreaming;

/**
 * // DESIGN PATTERN: Strategy — decides which track plays next from the current position. Swapping
 * sequential ↔ shuffle ↔ repeat is a one-line change on the {@link Player}, no branching in the
 * player itself.
 */
public interface PlaybackStrategy {

    /**
     * @param currentIndex the index now playing.
     * @param size         number of tracks in the queue (> 0).
     * @return the next index to play, or {@code -1} to signal "end of queue" (stop).
     */
    int nextIndex(int currentIndex, int size);

    String name();
}
