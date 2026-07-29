package in.neelporiya.cricinfo;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

/**
 * Facade aggregate for a live cricket match.
 *
 * <p>// CONCURRENCY: recording a ball is guarded by one private score lock. The critical section does
 * validate → create delivery → mutate innings/scorecard → decide state transition → copy snapshot.
 * Observers are notified <em>after</em> the lock is released, so subscribers all receive the same
 * consistent snapshot and a slow commentary feed cannot block future score mutations forever.
 */
public class Match {

    private final String id;
    private final Team teamA;
    private final Team teamB;
    private final Team battingFirst;
    private final MatchFormat format;
    private final Clock clock;
    private final List<ScoreSubscriber> subscribers;
    private final List<Innings> innings = new ArrayList<>();
    private final Object scoreLock = new Object();

    private MatchStatus status = MatchStatus.SCHEDULED;
    private MatchResult result;

    private Match(Builder builder) {
        this.id = builder.idGenerator.get();
        this.teamA = builder.teamA;
        this.teamB = builder.teamB;
        this.battingFirst = builder.battingFirst == null ? builder.teamA : builder.battingFirst;
        this.format = builder.format;
        this.clock = builder.clock;
        this.subscribers = new CopyOnWriteArrayList<>(builder.subscribers);
    }

    public void start() {
        synchronized (scoreLock) {
            transitionTo(MatchStatus.IN_PROGRESS);
            Team bowlingFirst = otherTeam(battingFirst);
            Innings first = new Innings(1, battingFirst, bowlingFirst);
            first.start();
            innings.add(first);
        }
    }

    public void startNextInnings() {
        synchronized (scoreLock) {
            if (status != MatchStatus.INNINGS_BREAK) {
                throw new IllegalStateException("Next innings can start only during innings break");
            }
            transitionTo(MatchStatus.IN_PROGRESS);
            Innings second = new Innings(2, innings.get(0).getBowlingTeam(), innings.get(0).getBattingTeam());
            second.start();
            innings.add(second);
        }
    }

    public Delivery recordBall(Player batter, Player bowler, BallOutcome outcome) {
        Objects.requireNonNull(batter, "batter");
        Objects.requireNonNull(bowler, "bowler");
        Objects.requireNonNull(outcome, "outcome");

        ScoreUpdate update;
        Delivery delivery;
        synchronized (scoreLock) {
            if (status != MatchStatus.IN_PROGRESS || innings.isEmpty()) {
                throw new IllegalStateException("Match is not ready to record balls");
            }
            Innings current = innings.get(innings.size() - 1);
            Over currentOver = current.currentOver();
            int overNumber = current.getScorecard().getLegalDeliveries() / 6 + 1;
            int ballInOver = currentOver == null || currentOver.isComplete()
                    ? 1
                    : Math.min(6, currentOver.legalDeliveries() + 1);

            Instant timestamp = clock.instant(); // TESTABILITY: injected Clock, never Instant.now().
            delivery = new Delivery(current.getNumber(), overNumber, ballInOver, batter, bowler, outcome, timestamp);
            current.addDelivery(delivery);

            maybeCloseCurrentInnings(current);
            update = new ScoreUpdate(id, current.getNumber(), delivery, current.getScorecard().copy(), status, timestamp);
        }

        // DESIGN PATTERN: Observer — subscribers react to every ball without coupling to scoring code.
        subscribers.forEach(subscriber -> subscriber.onBallRecorded(update));
        return delivery;
    }

    private void maybeCloseCurrentInnings(Innings current) {
        if (current.getNumber() == 2 && current.getScorecard().getRuns() > innings.get(0).getScorecard().getRuns()) {
            completeMatch(current);
            return;
        }
        if (!format.isInningsComplete(current)) {
            return;
        }
        current.complete();
        if (current.getNumber() == 1) {
            transitionTo(MatchStatus.INNINGS_BREAK);
        } else {
            completeMatch(current);
        }
    }

    private void completeMatch(Innings current) {
        if (current.getStatus() == InningsStatus.IN_PROGRESS) {
            current.complete();
        }
        Scorecard first = innings.get(0).getScorecard();
        Scorecard second = current.getScorecard();
        if (second.getRuns() > first.getRuns()) {
            int wicketsRemaining = current.getBattingTeam().getPlayers().size() - 1 - second.getWickets();
            result = MatchResult.wonByWickets(current.getBattingTeam(), wicketsRemaining);
        } else if (first.getRuns() > second.getRuns()) {
            result = MatchResult.wonByRuns(innings.get(0).getBattingTeam(), first.getRuns() - second.getRuns());
        } else {
            result = MatchResult.tie();
        }
        transitionTo(MatchStatus.COMPLETED);
    }

    private Team otherTeam(Team team) {
        if (team == teamA) {
            return teamB;
        }
        if (team == teamB) {
            return teamA;
        }
        throw new IllegalArgumentException("Unknown team: " + team);
    }

    private void transitionTo(MatchStatus next) {
        if (!status.canTransitionTo(next)) {
            throw new IllegalStateException("Cannot move match " + id + " from " + status + " to " + next);
        }
        status = next;
    }

    public void subscribe(ScoreSubscriber subscriber) {
        subscribers.add(Objects.requireNonNull(subscriber, "subscriber"));
    }

    public void unsubscribe(ScoreSubscriber subscriber) {
        subscribers.remove(subscriber);
    }

    public Scorecard currentScorecard() {
        synchronized (scoreLock) {
            if (innings.isEmpty()) {
                throw new IllegalStateException("Match has not started");
            }
            return innings.get(innings.size() - 1).getScorecard();
        }
    }

    public List<Innings> getInnings() {
        synchronized (scoreLock) {
            return List.copyOf(innings);
        }
    }

    public Optional<MatchResult> getResult() {
        synchronized (scoreLock) {
            return Optional.ofNullable(result);
        }
    }

    public MatchStatus getStatus() {
        synchronized (scoreLock) {
            return status;
        }
    }

    public String getId() {
        return id;
    }

    public MatchFormat getFormat() {
        return format;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * // DESIGN PATTERN: Builder — match setup has many choices (format, toss, clock, subscribers),
     * so fluent construction is clearer than a telescoping constructor.
     */
    public static final class Builder {
        private Team teamA;
        private Team teamB;
        private Team battingFirst;
        private MatchFormat format = MatchFormat.t20();
        private Clock clock = Clock.systemUTC();
        private Supplier<String> idGenerator = () -> UUID.randomUUID().toString();
        private final List<ScoreSubscriber> subscribers = new ArrayList<>();

        public Builder teams(Team teamA, Team teamB) {
            this.teamA = Objects.requireNonNull(teamA, "teamA");
            this.teamB = Objects.requireNonNull(teamB, "teamB");
            return this;
        }

        public Builder battingFirst(Team battingFirst) {
            this.battingFirst = Objects.requireNonNull(battingFirst, "battingFirst");
            return this;
        }

        public Builder format(MatchFormat format) {
            this.format = Objects.requireNonNull(format, "format");
            return this;
        }

        public Builder clock(Clock clock) {
            this.clock = Objects.requireNonNull(clock, "clock");
            return this;
        }

        public Builder idGenerator(Supplier<String> idGenerator) {
            this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
            return this;
        }

        public Builder addSubscriber(ScoreSubscriber subscriber) {
            this.subscribers.add(Objects.requireNonNull(subscriber, "subscriber"));
            return this;
        }

        public Match build() {
            if (teamA == null || teamB == null) {
                throw new IllegalStateException("Both teams are required");
            }
            if (teamA == teamB) {
                throw new IllegalStateException("A match needs two different teams");
            }
            return new Match(this);
        }
    }
}
