package in.neelporiya.musicstreaming;

/** Play in order but wrap around to the start after the last track (never stops). */
public class RepeatAllPlayback implements PlaybackStrategy {

    @Override
    public int nextIndex(int currentIndex, int size) {
        return (currentIndex + 1) % size;
    }

    @Override
    public String name() {
        return "REPEAT_ALL";
    }
}
