package in.neelporiya.cricinfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** One team's turn to bat. */
public class Innings {

    private final int number;
    private final Team battingTeam;
    private final Team bowlingTeam;
    private final Scorecard scorecard;
    private final List<Over> overs = new ArrayList<>();
    private InningsStatus status = InningsStatus.NOT_STARTED;

    public Innings(int number, Team battingTeam, Team bowlingTeam) {
        if (number < 1) {
            throw new IllegalArgumentException("number is 1-based");
        }
        this.number = number;
        this.battingTeam = Objects.requireNonNull(battingTeam, "battingTeam");
        this.bowlingTeam = Objects.requireNonNull(bowlingTeam, "bowlingTeam");
        this.scorecard = new Scorecard(battingTeam, bowlingTeam);
    }

    void start() {
        transitionTo(InningsStatus.IN_PROGRESS);
    }

    void complete() {
        transitionTo(InningsStatus.COMPLETED);
    }

    void addDelivery(Delivery delivery) {
        currentOrNewOver(delivery.overNumber(), delivery.bowler()).addDelivery(delivery);
        scorecard.record(delivery);
    }

    Over currentOrNewOver(int overNumber, Player bowler) {
        if (overs.isEmpty() || overs.get(overs.size() - 1).isComplete()) {
            Over over = new Over(overNumber, bowler);
            overs.add(over);
            return over;
        }
        return overs.get(overs.size() - 1);
    }

    Over currentOver() {
        return overs.isEmpty() ? null : overs.get(overs.size() - 1);
    }

    private void transitionTo(InningsStatus next) {
        if (!status.canTransitionTo(next)) {
            throw new IllegalStateException("Cannot move innings " + number + " from " + status + " to " + next);
        }
        status = next;
    }

    public int getNumber() {
        return number;
    }

    public Team getBattingTeam() {
        return battingTeam;
    }

    public Team getBowlingTeam() {
        return bowlingTeam;
    }

    public Scorecard getScorecard() {
        return scorecard;
    }

    public InningsStatus getStatus() {
        return status;
    }

    public List<Over> getOversList() {
        return List.copyOf(overs);
    }
}
