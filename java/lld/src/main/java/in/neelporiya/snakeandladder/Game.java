package in.neelporiya.snakeandladder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Facade for a turn-based Snake and Ladder match.
 *
 * <p>// DESIGN PATTERN: Facade + Builder + State machine. Clients talk to one Game object, construct
 * it fluently, and observe explicit {@link GameStatus} transitions.
 *
 * <p>// INTERVIEW INSIGHT: Apply snakes/ladders after movement, not before. The turn order is
 * "roll → advance → resolve jump → check exact win → rotate turn".
 *
 * <p>// CONCURRENCY: {@link #playTurn()} is synchronized, so rolling, moving, applying a jump,
 * checking the winner, and advancing the turn index happen as one atomic critical section. Concurrent
 * callers cannot corrupt turn order or move two players for the same turn.
 *
 * <p>// TESTABILITY: Dice is injected. A scripted dice makes a full multi-player game deterministic,
 * exactly like an injected Clock makes time-based code deterministic.
 *
 * <p>// EXTENSIBILITY: board setup, overshoot policy, dice, and listeners are all injected; new rules
 * fit behind those seams without changing the core turn algorithm.
 */
public final class Game {

    private final Board board;
    private final Dice dice;
    private final OvershootPolicy overshootPolicy;
    private final List<Player> players;
    private final List<GameEventListener> listeners;

    private GameStatus status = GameStatus.NOT_STARTED;
    private int currentPlayerIndex;
    private Player winner;

    private Game(Builder builder) {
        this.board = Objects.requireNonNull(builder.board, "board");
        this.dice = Objects.requireNonNull(builder.dice, "dice");
        this.overshootPolicy = Objects.requireNonNull(builder.overshootPolicy, "overshootPolicy");
        this.players = List.copyOf(builder.players);
        if (players.size() < 2) {
            throw new IllegalStateException("at least two players are required");
        }
        ensureUniquePlayers(players);
        players.forEach(Player::reset);
        this.listeners = new CopyOnWriteArrayList<>(builder.listeners);
    }

    public static Game defaultGame() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public synchronized MoveResult playTurn() {
        if (status == GameStatus.FINISHED) {
            throw new IllegalStateException("game is already finished");
        }
        if (status == GameStatus.NOT_STARTED) {
            status = GameStatus.RUNNING;
        }

        Player player = players.get(currentPlayerIndex);
        int roll = dice.roll();
        if (roll < 1) {
            throw new IllegalStateException("dice roll must be positive: " + roll);
        }

        int start = player.getPosition();
        int attempted = start + roll;
        boolean overshot = attempted > board.getSize();
        int landing = overshootPolicy.landingCell(start, roll, board.getSize());
        Optional<Jump> jump = overshot ? Optional.empty() : board.jumpAt(landing);
        int finalPosition = jump.map(Jump::apply).orElse(landing);

        player.moveTo(finalPosition);
        if (finalPosition == board.getSize()) {
            status = GameStatus.FINISHED;
            winner = player;
        } else {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        }

        MoveResult result = new MoveResult(
                player, roll, start, attempted, landing, finalPosition, jump, overshot, status);
        listeners.forEach(listener -> listener.onMove(result));
        if (status == GameStatus.FINISHED) {
            GameSnapshot snapshot = snapshot();
            listeners.forEach(listener -> listener.onWin(snapshot));
        }
        return result;
    }

    public synchronized GameSnapshot snapshot() {
        Map<String, Integer> positions = new LinkedHashMap<>();
        for (Player player : players) {
            positions.put(player.getId(), player.getPosition());
        }
        return new GameSnapshot(status, players.get(currentPlayerIndex), Optional.ofNullable(winner), positions);
    }

    public synchronized GameStatus getStatus() {
        return status;
    }

    public synchronized Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public synchronized Optional<Player> getWinner() {
        return Optional.ofNullable(winner);
    }

    public Board getBoard() {
        return board;
    }

    public List<Player> getPlayers() {
        return players;
    }

    private static void ensureUniquePlayers(List<Player> players) {
        Set<String> ids = new HashSet<>();
        for (Player player : players) {
            if (!ids.add(player.getId())) {
                throw new IllegalArgumentException("duplicate player id: " + player.getId());
            }
        }
    }

    /**
     * // DESIGN PATTERN: Builder — wires board, dice Strategy, players, policy, and observers.
     */
    public static final class Builder {

        private Board board = Board.standardBoard();
        private Dice dice = new RandomDice();
        private OvershootPolicy overshootPolicy = OvershootPolicy.EXACT_ROLL_REQUIRED;
        private final List<Player> players = new ArrayList<>(List.of(
                new Player("P1", "Player 1"),
                new Player("P2", "Player 2")
        ));
        private final List<GameEventListener> listeners = new ArrayList<>();

        private Builder() {
        }

        public Builder board(Board board) {
            this.board = Objects.requireNonNull(board, "board");
            return this;
        }

        public Builder dice(Dice dice) {
            this.dice = Objects.requireNonNull(dice, "dice");
            return this;
        }

        public Builder overshootPolicy(OvershootPolicy overshootPolicy) {
            this.overshootPolicy = Objects.requireNonNull(overshootPolicy, "overshootPolicy");
            return this;
        }

        public Builder players(List<Player> players) {
            this.players.clear();
            this.players.addAll(Objects.requireNonNull(players, "players"));
            return this;
        }

        public Builder addPlayer(Player player) {
            if (players.size() == 2
                    && "P1".equals(players.get(0).getId())
                    && "P2".equals(players.get(1).getId())) {
                players.clear();
            }
            this.players.add(Objects.requireNonNull(player, "player"));
            return this;
        }

        public Builder addListener(GameEventListener listener) {
            this.listeners.add(Objects.requireNonNull(listener, "listener"));
            return this;
        }

        public Game build() {
            return new Game(this);
        }
    }
}
