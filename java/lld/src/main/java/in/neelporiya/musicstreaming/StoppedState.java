package in.neelporiya.musicstreaming;

/** Nothing is playing. {@code play} starts the current track; {@code pause} is illegal. */
public final class StoppedState implements PlayerState {

    public static final StoppedState INSTANCE = new StoppedState();

    private StoppedState() {
    }

    @Override
    public void play(Player player) {
        if (player.queueSize() == 0) {
            throw new IllegalStateException("nothing loaded to play");
        }
        player.setState(PlayingState.INSTANCE);
        player.beginAt(player.currentIndex());
    }

    @Override
    public void pause(Player player) {
        throw new IllegalStateException("cannot pause: player is stopped");
    }

    @Override
    public void stop(Player player) {
        // already stopped — no-op
    }

    @Override
    public PlaybackStateType type() {
        return PlaybackStateType.STOPPED;
    }
}
