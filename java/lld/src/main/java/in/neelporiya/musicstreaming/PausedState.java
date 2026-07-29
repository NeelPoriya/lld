package in.neelporiya.musicstreaming;

/** Playback is paused on a track. {@code play} resumes; {@code pause} is idempotent. */
public final class PausedState implements PlayerState {

    public static final PausedState INSTANCE = new PausedState();

    private PausedState() {
    }

    @Override
    public void play(Player player) {
        player.setState(PlayingState.INSTANCE);
        player.notifyResumed();
    }

    @Override
    public void pause(Player player) {
        // already paused — no-op
    }

    @Override
    public void stop(Player player) {
        player.doStop();
    }

    @Override
    public PlaybackStateType type() {
        return PlaybackStateType.PAUSED;
    }
}
