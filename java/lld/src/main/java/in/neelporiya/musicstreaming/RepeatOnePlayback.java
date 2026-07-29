package in.neelporiya.musicstreaming;

/** Loop the current track forever. */
public class RepeatOnePlayback implements PlaybackStrategy {

    @Override
    public int nextIndex(int currentIndex, int size) {
        return currentIndex;
    }

    @Override
    public String name() {
        return "REPEAT_ONE";
    }
}
