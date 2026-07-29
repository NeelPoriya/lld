package in.neelporiya.cricinfo;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Live score plus batting/bowling tables for one innings.
 *
 * <p>// CONCURRENCY: Match serializes calls to {@link #record(Delivery)} with its score lock. This
 * class also synchronizes public readers so a UI/subscriber that holds a live scorecard sees a
 * consistent runs/wickets/overs snapshot rather than half of a ball update.
 */
public class Scorecard {

    private final Team battingTeam;
    private final Team bowlingTeam;
    private final Map<Player, BattingStats> batting = new LinkedHashMap<>();
    private final Map<Player, BowlingStats> bowling = new LinkedHashMap<>();

    private int runs;
    private int wickets;
    private int legalDeliveries;

    public Scorecard(Team battingTeam, Team bowlingTeam) {
        this.battingTeam = Objects.requireNonNull(battingTeam, "battingTeam");
        this.bowlingTeam = Objects.requireNonNull(bowlingTeam, "bowlingTeam");
        battingTeam.getPlayers().forEach(player -> batting.put(player, new BattingStats()));
        bowlingTeam.getPlayers().forEach(player -> bowling.put(player, new BowlingStats()));
    }

    private Scorecard(Team battingTeam, Team bowlingTeam, int runs, int wickets, int legalDeliveries,
                      Map<Player, BattingStats> batting, Map<Player, BowlingStats> bowling) {
        this.battingTeam = battingTeam;
        this.bowlingTeam = bowlingTeam;
        this.runs = runs;
        this.wickets = wickets;
        this.legalDeliveries = legalDeliveries;
        this.batting.putAll(batting);
        this.bowling.putAll(bowling);
    }

    synchronized void record(Delivery delivery) {
        BallOutcome outcome = delivery.outcome();
        runs += outcome.totalRuns();
        if (outcome.isLegalDelivery()) {
            legalDeliveries++;
        }
        if (outcome.wicket()) {
            wickets++;
        }
        batting.computeIfAbsent(delivery.batter(), player -> new BattingStats()).apply(outcome);
        bowling.computeIfAbsent(delivery.bowler(), player -> new BowlingStats()).apply(outcome);
    }

    public synchronized Scorecard copy() {
        Map<Player, BattingStats> battingCopy = new LinkedHashMap<>();
        batting.forEach((player, stats) -> battingCopy.put(player, stats.copy()));
        Map<Player, BowlingStats> bowlingCopy = new LinkedHashMap<>();
        bowling.forEach((player, stats) -> bowlingCopy.put(player, stats.copy()));
        return new Scorecard(battingTeam, bowlingTeam, runs, wickets, legalDeliveries, battingCopy, bowlingCopy);
    }

    public Team getBattingTeam() {
        return battingTeam;
    }

    public Team getBowlingTeam() {
        return bowlingTeam;
    }

    public synchronized int getRuns() {
        return runs;
    }

    public synchronized int getWickets() {
        return wickets;
    }

    public synchronized int getLegalDeliveries() {
        return legalDeliveries;
    }

    public synchronized String getOvers() {
        return (legalDeliveries / 6) + "." + (legalDeliveries % 6);
    }

    public synchronized BattingStats getBattingStats(Player player) {
        return batting.computeIfAbsent(player, ignored -> new BattingStats());
    }

    public synchronized BowlingStats getBowlingStats(Player player) {
        return bowling.computeIfAbsent(player, ignored -> new BowlingStats());
    }

    public synchronized Map<Player, BattingStats> battingScore() {
        return Map.copyOf(batting);
    }

    public synchronized Map<Player, BowlingStats> bowlingScore() {
        return Map.copyOf(bowling);
    }
}
