package in.neelporiya.cricinfo;

/** Per-bowler figures. */
public class BowlingStats {

    private int legalBalls;
    private int runsConceded;
    private int wickets;

    public BowlingStats() {
    }

    private BowlingStats(int legalBalls, int runsConceded, int wickets) {
        this.legalBalls = legalBalls;
        this.runsConceded = runsConceded;
        this.wickets = wickets;
    }

    void apply(BallOutcome outcome) {
        if (outcome.isLegalDelivery()) {
            legalBalls++;
        }
        runsConceded += outcome.totalRuns();
        if (outcome.wicket()) {
            wickets++;
        }
    }

    public BowlingStats copy() {
        return new BowlingStats(legalBalls, runsConceded, wickets);
    }

    public int getLegalBalls() {
        return legalBalls;
    }

    public String getOvers() {
        return (legalBalls / 6) + "." + (legalBalls % 6);
    }

    public int getRunsConceded() {
        return runsConceded;
    }

    public int getWickets() {
        return wickets;
    }

    public double getEconomyRate() {
        return legalBalls == 0 ? 0.0 : runsConceded * 6.0 / legalBalls;
    }
}
