package in.neelporiya.musicstreaming;

/** Play tracks in order; stop after the last one. */
public class SequentialPlayback implements PlaybackStrategy {

    @Override
    public int nextIndex(int currentIndex, int size) {
        return currentIndex + 1 < size ? currentIndex + 1 : -1;
    }

    @Override
    public String name() {
        return "SEQUENTIAL";
    }
}
