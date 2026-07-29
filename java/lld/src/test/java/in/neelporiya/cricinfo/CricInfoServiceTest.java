package in.neelporiya.cricinfo;

import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CricInfoServiceTest {

    private MutableClock clock;
    private Team alpha;
    private Team beta;
    private Player alphaOne;
    private Player alphaTwo;
    private Player betaBowler;
    private Match match;

    @BeforeEach
    void setUp() {
        clock = MutableClock.atEpoch();
        alphaOne = new Player("a1", "Alpha One");
        alphaTwo = new Player("a2", "Alpha Two");
        alpha = new Team("Alpha", List.of(alphaOne, alphaTwo, new Player("a3", "Alpha Three")));
        betaBowler = new Player("b1", "Beta Bowler");
        beta = new Team("Beta", List.of(betaBowler, new Player("b2", "Beta Two"), new Player("b3", "Beta Three")));
        match = MatchFactory.limitedOvers("match-1", alpha, beta, alpha, MatchFormat.limitedOvers(1), clock);
        match.start();
    }

    @Test
    void recordingRunsUpdatesTeamAndBatsmanScore() {
        match.recordBall(alphaOne, betaBowler, BallOutcome.runs(4));

        Scorecard scorecard = match.currentScorecard();
        assertEquals(4, scorecard.getRuns());
        assertEquals(0, scorecard.getWickets());
        assertEquals("0.1", scorecard.getOvers());
        assertEquals(4, scorecard.getBattingStats(alphaOne).getRuns());
        assertEquals(1, scorecard.getBattingStats(alphaOne).getBallsFaced());
        assertEquals(400.0, scorecard.getBattingStats(alphaOne).getStrikeRate(), 0.001);
    }

    @Test
    void wideAddsExtraRunWithoutConsumingBall() {
        match.recordBall(alphaOne, betaBowler, BallOutcome.wide());

        Scorecard scorecard = match.currentScorecard();
        assertEquals(1, scorecard.getRuns());
        assertEquals("0.0", scorecard.getOvers());
        assertEquals(0, scorecard.getBattingStats(alphaOne).getBallsFaced());
        assertEquals("0.0", scorecard.getBowlingStats(betaBowler).getOvers());
        assertEquals(1, scorecard.getBowlingStats(betaBowler).getRunsConceded());
    }

    @Test
    void wicketIncrementsWicketsAndBowlerFigure() {
        match.recordBall(alphaOne, betaBowler, BallOutcome.wicketBall());

        Scorecard scorecard = match.currentScorecard();
        assertEquals(1, scorecard.getWickets());
        assertTrue(scorecard.getBattingStats(alphaOne).isOut());
        assertEquals(1, scorecard.getBowlingStats(betaBowler).getWickets());
    }

    @Test
    void sixLegalBallsCompleteAnOverAndBowlerStatsUpdate() {
        for (int i = 0; i < 6; i++) {
            match.recordBall(alphaOne, betaBowler, BallOutcome.runs(1));
        }

        Scorecard scorecard = match.currentScorecard();
        assertEquals("1.0", scorecard.getOvers());
        assertEquals(1, match.getInnings().get(0).getOversList().size());
        assertTrue(match.getInnings().get(0).getOversList().get(0).isComplete());
        assertEquals("1.0", scorecard.getBowlingStats(betaBowler).getOvers());
        assertEquals(6, scorecard.getBowlingStats(betaBowler).getRunsConceded());
    }

    @Test
    void inningsEndsAtFormatOverLimit() {
        for (int i = 0; i < 6; i++) {
            match.recordBall(alphaOne, betaBowler, BallOutcome.runs(0));
        }

        assertEquals(MatchStatus.INNINGS_BREAK, match.getStatus());
        assertEquals(InningsStatus.COMPLETED, match.getInnings().get(0).getStatus());
    }

    @Test
    void subscribersAreNotifiedForEveryBallWithDeterministicTimestamps() {
        AtomicInteger calls = new AtomicInteger();
        AtomicReference<ScoreUpdate> last = new AtomicReference<>();
        CommentaryFeed feed = new CommentaryFeed();
        match.subscribe(update -> {
            calls.incrementAndGet();
            last.set(update);
        });
        match.subscribe(feed);

        match.recordBall(alphaOne, betaBowler, BallOutcome.runs(2));
        clock.advance(Duration.ofSeconds(5));
        match.recordBall(alphaOne, betaBowler, BallOutcome.runs(1));

        assertEquals(2, calls.get());
        assertEquals(3, last.get().scorecard().getRuns());
        assertEquals(clock.instant(), last.get().timestamp());
        assertEquals(2, feed.lines().size());
    }

    @Test
    void strikeRateAndBowlerEconomyAreComputed() {
        match.recordBall(alphaOne, betaBowler, BallOutcome.runs(4));
        for (int i = 0; i < 5; i++) {
            match.recordBall(alphaOne, betaBowler, BallOutcome.runs(0));
        }

        Scorecard scorecard = match.currentScorecard();
        assertEquals(66.666, scorecard.getBattingStats(alphaOne).getStrikeRate(), 0.001);
        assertEquals(4.0, scorecard.getBowlingStats(betaBowler).getEconomyRate(), 0.001);
    }

    @Test
    void matchResultIsDeterminedAfterSecondInnings() {
        for (int i = 0; i < 6; i++) {
            match.recordBall(alphaOne, betaBowler, BallOutcome.runs(1));
        }
        match.startNextInnings();

        Player betaBatter = beta.getPlayers().get(1);
        match.recordBall(betaBatter, alphaOne, BallOutcome.runs(4));
        match.recordBall(betaBatter, alphaOne, BallOutcome.runs(3));

        assertEquals(MatchStatus.COMPLETED, match.getStatus());
        assertTrue(match.getResult().isPresent());
        assertEquals(beta, match.getResult().get().winner().orElseThrow());
        assertEquals("Beta won by 2 wickets", match.getResult().get().summary());
    }
}
