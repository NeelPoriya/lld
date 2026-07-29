package in.neelporiya.musicstreaming;

/** A track is playing. {@code play} is idempotent; {@code pause}/{@code stop} transition. */
public final class PlayingState implements PlayerState {

    public static final PlayingState INSTANCE = new PlayingState();

    private PlayingState() {
    }

    @Override
    public void play(Player player) {
        // already playing — no-op
    }

    @Override
    public void pause(Player player) {
        player.setState(PausedState.INSTANCE);
        player.notifyPaused();
    }

    @Override
    public void stop(Player player) {
        player.doStop();
    }

    @Override
    public PlaybackStateType type() {
        return PlaybackStateType.PLAYING;
    }
}
