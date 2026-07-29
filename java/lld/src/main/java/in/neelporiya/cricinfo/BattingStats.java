package in.neelporiya.cricinfo;

/** Per-batter scorecard line. */
public class BattingStats {

    private int runs;
    private int ballsFaced;
    private boolean out;

    public BattingStats() {
    }

    private BattingStats(int runs, int ballsFaced, boolean out) {
        this.runs = runs;
        this.ballsFaced = ballsFaced;
        this.out = out;
    }

    void apply(BallOutcome outcome) {
        runs += outcome.batterRuns();
        if (outcome.isLegalDelivery()) {
            ballsFaced++;
        }
        if (outcome.wicket()) {
            out = true;
        }
    }

    public BattingStats copy() {
        return new BattingStats(runs, ballsFaced, out);
    }

    public int getRuns() {
        return runs;
    }

    public int getBallsFaced() {
        return ballsFaced;
    }

    public boolean isOut() {
        return out;
    }

    public double getStrikeRate() {
        return ballsFaced == 0 ? 0.0 : runs * 100.0 / ballsFaced;
    }
}
