package in.neelporiya.cricinfo;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Simple observer that turns deliveries into timestamped commentary lines. */
public class CommentaryFeed implements ScoreSubscriber {

    private final List<String> lines = new CopyOnWriteArrayList<>();

    @Override
    public void onBallRecorded(ScoreUpdate update) {
        Delivery delivery = update.delivery();
        BallOutcome outcome = delivery.outcome();
        lines.add(update.timestamp() + " I" + update.inningsNumber()
                + " " + delivery.overNumber() + "." + delivery.ballInOver()
                + " " + delivery.bowler().name() + " to " + delivery.batter().name()
                + ": " + outcome.totalRuns() + " run(s)"
                + (outcome.wicket() ? ", wicket" : "")
                + " — " + update.scorecard().getRuns() + "/" + update.scorecard().getWickets());
    }

    public List<String> lines() {
        return List.copyOf(lines);
    }
}
