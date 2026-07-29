package in.neelporiya.musicstreaming;

import java.util.Objects;
import java.util.Random;

/**
 * Pick the next track at random.
 *
 * <p>// TESTABILITY: the {@link Random} is injected. Production uses {@code new Random()}; tests pass
 * {@code new Random(seed)} so the "random" order is exactly reproducible and assertable.
 */
public class ShufflePlayback implements PlaybackStrategy {

    private final Random random;

    public ShufflePlayback(Random random) {
        this.random = Objects.requireNonNull(random, "random");
    }

    @Override
    public int nextIndex(int currentIndex, int size) {
        return random.nextInt(size);
    }

    @Override
    public String name() {
        return "SHUFFLE";
    }
}
