package in.neelporiya.musicstreaming;

/**
 * // DESIGN PATTERN: State — each playback state is an object that knows which transitions are legal.
 * The {@link Player} delegates {@code play/pause/stop} to its current state instead of scattering
 * {@code if (state == ...)} checks everywhere. Adding a state (e.g. BUFFERING) is a new class, not an
 * edit to a giant switch.
 */
public interface PlayerState {

    void play(Player player);

    void pause(Player player);

    void stop(Player player);

    PlaybackStateType type();
}
