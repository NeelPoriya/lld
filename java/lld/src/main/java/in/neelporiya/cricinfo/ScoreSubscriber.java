package in.neelporiya.cricinfo;

/**
 * // DESIGN PATTERN: Observer — score displays, push notifications and commentary feeds subscribe
 * without Match knowing their concrete classes.
 */
public interface ScoreSubscriber {
    void onBallRecorded(ScoreUpdate update);
}
