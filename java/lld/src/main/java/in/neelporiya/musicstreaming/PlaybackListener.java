package in.neelporiya.musicstreaming;

/**
 * // DESIGN PATTERN: Observer — playback events (a track starts, the player pauses/resumes/stops) are
 * broadcast to subscribers such as a "now playing" UI, scrobbler or play-count recorder.
 */
public interface PlaybackListener {

    default void onSongStarted(Song song) {
    }

    default void onPaused(Song song) {
    }

    default void onResumed(Song song) {
    }

    default void onStopped() {
    }
}
