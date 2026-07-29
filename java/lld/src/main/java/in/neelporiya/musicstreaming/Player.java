package in.neelporiya.musicstreaming;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The playback engine: a queue of songs, a cursor, a {@link PlaybackStrategy} for "what plays next"
 * and a {@link PlayerState} for "what transitions are legal now".
 *
 * <p>// DESIGN PATTERN: State (transitions) + Strategy (next-track selection). // CONCURRENCY: a
 * single {@link ReentrantLock} serializes all controls so a shared player can't corrupt its cursor.
 * The lock is reentrant, so state objects can call back into the player under the same lock.
 */
public class Player {

    private final List<Song> queue = new ArrayList<>();
    private final Deque<Integer> history = new ArrayDeque<>();
    private final List<PlaybackListener> listeners = new CopyOnWriteArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();

    private int cursor = 0;
    private PlaybackStrategy strategy;
    private PlayerState state = StoppedState.INSTANCE;

    public Player(PlaybackStrategy strategy) {
        this.strategy = Objects.requireNonNull(strategy, "strategy");
    }

    public void addListener(PlaybackListener listener) {
        listeners.add(listener);
    }

    /** Replace the queue and reset to a stopped state at the first track. */
    public void load(List<Song> songs) {
        lock.lock();
        try {
            queue.clear();
            queue.addAll(songs);
            history.clear();
            cursor = 0;
            state = StoppedState.INSTANCE;
        } finally {
            lock.unlock();
        }
    }

    public void setStrategy(PlaybackStrategy strategy) {
        lock.lock();
        try {
            this.strategy = Objects.requireNonNull(strategy, "strategy");
        } finally {
            lock.unlock();
        }
    }

    public void play() {
        lock.lock();
        try {
            state.play(this);
        } finally {
            lock.unlock();
        }
    }

    public void pause() {
        lock.lock();
        try {
            state.pause(this);
        } finally {
            lock.unlock();
        }
    }

    public void stop() {
        lock.lock();
        try {
            state.stop(this);
        } finally {
            lock.unlock();
        }
    }

    /** Advance per the strategy; a strategy returning {@code -1} (end of queue) stops the player. */
    public void next() {
        lock.lock();
        try {
            requireActive();
            int nextIndex = strategy.nextIndex(cursor, queue.size());
            if (nextIndex < 0) {
                doStop();
                return;
            }
            history.push(cursor);
            beginAt(nextIndex);
        } finally {
            lock.unlock();
        }
    }

    /** Go back to the previously played track (honours shuffle history); no-op restarts current. */
    public void previous() {
        lock.lock();
        try {
            requireActive();
            beginAt(history.isEmpty() ? cursor : history.pop());
        } finally {
            lock.unlock();
        }
    }

    public Song currentSong() {
        lock.lock();
        try {
            return (cursor >= 0 && cursor < queue.size()) ? queue.get(cursor) : null;
        } finally {
            lock.unlock();
        }
    }

    public PlaybackStateType stateType() {
        lock.lock();
        try {
            return state.type();
        } finally {
            lock.unlock();
        }
    }

    public String strategyName() {
        lock.lock();
        try {
            return strategy.name();
        } finally {
            lock.unlock();
        }
    }

    // --- package-private hooks used by the State objects (always under the lock) ---

    void setState(PlayerState newState) {
        this.state = newState;
    }

    int currentIndex() {
        return cursor;
    }

    int queueSize() {
        return queue.size();
    }

    void beginAt(int index) {
        cursor = index;
        Song song = queue.get(index);
        listeners.forEach(l -> l.onSongStarted(song));
    }

    void doStop() {
        state = StoppedState.INSTANCE;
        listeners.forEach(PlaybackListener::onStopped);
    }

    void notifyPaused() {
        Song song = currentSong();
        listeners.forEach(l -> l.onPaused(song));
    }

    void notifyResumed() {
        Song song = currentSong();
        listeners.forEach(l -> l.onResumed(song));
    }

    private void requireActive() {
        if (state.type() == PlaybackStateType.STOPPED) {
            throw new IllegalStateException("player is stopped — press play first");
        }
    }
}
