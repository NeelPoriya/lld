package in.neelporiya.tictactoe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Facade for a single Tic Tac Toe match.
 *
 * <p>// DESIGN PATTERN: Builder assembles board size, players, and win strategies without exposing
 * construction noise to clients.
 *
 * <p>// INTERVIEW INSIGHT: The provided strategies rescan only the affected row/column/diagonal,
 * making each check O(N). A production scoreboard can keep row/column/diagonal counters per piece
 * and answer wins in O(1) per move; the Strategy seam lets that optimization replace this logic.
 *
 * <p>// CONCURRENCY: makeMove is synchronized so "validate turn → mutate board → detect status →
 * switch turn" is one atomic critical section. Two racing callers cannot both move for X's turn.
 *
 * <p>// TESTABILITY: There is no randomness or wall clock. Tests call makeMove(player, row, col) and
 * assert the returned GameStatus immediately.
 *
 * <p>// EXTENSIBILITY: Add a new WinningStrategy (for example, four-corners) and register it in the
 * builder; Game stays closed for modification.
 */
public final class Game {

    private final Board board;
    private final Player xPlayer;
    private final Player oPlayer;
    private final List<WinningStrategy> winningStrategies;

    private Player currentPlayer;
    private GameStatus status = GameStatus.IN_PROGRESS;

    private Game(Board board, Player xPlayer, Player oPlayer, List<WinningStrategy> winningStrategies) {
        this.board = Objects.requireNonNull(board, "board");
        this.xPlayer = requirePiece(xPlayer, Piece.X);
        this.oPlayer = requirePiece(oPlayer, Piece.O);
        if (winningStrategies.isEmpty()) {
            throw new IllegalArgumentException("at least one winning strategy is required");
        }
        this.winningStrategies = List.copyOf(winningStrategies);
        this.currentPlayer = this.xPlayer;
    }

    public static Game defaultGame() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public synchronized GameStatus makeMove(Player player, int row, int column) {
        Objects.requireNonNull(player, "player");
        if (status.isTerminal()) {
            throw new InvalidMoveException("game is already over with status " + status);
        }
        if (!player.equals(currentPlayer)) {
            throw new InvalidMoveException("expected " + currentPlayer + " to move next");
        }

        Move move = new Move(player, row, column);
        board.place(row, column, player.getPiece());

        if (winningStrategies.stream().anyMatch(strategy -> strategy.isWinningMove(board, move))) {
            status = player.getPiece().winningStatus();
        } else if (board.isFull()) {
            status = GameStatus.DRAW;
        } else {
            currentPlayer = nextPlayer();
        }
        return status;
    }

    public synchronized GameStatus getStatus() {
        return status;
    }

    public synchronized Player getCurrentPlayer() {
        return currentPlayer;
    }

    public Board getBoard() {
        return board;
    }

    public Player getXPlayer() {
        return xPlayer;
    }

    public Player getOPlayer() {
        return oPlayer;
    }

    private Player nextPlayer() {
        return currentPlayer.getPiece() == Piece.X ? oPlayer : xPlayer;
    }

    private static Player requirePiece(Player player, Piece piece) {
        Objects.requireNonNull(player, "player");
        if (player.getPiece() != piece) {
            throw new IllegalArgumentException("player must use piece " + piece);
        }
        return player;
    }

    public static final class Builder {

        private int size = 3;
        private Player xPlayer = new Player("Player X", Piece.X);
        private Player oPlayer = new Player("Player O", Piece.O);
        private final List<WinningStrategy> strategies = new ArrayList<>(List.of(
                new RowWinningStrategy(),
                new ColumnWinningStrategy(),
                new DiagonalWinningStrategy()
        ));

        private Builder() {
        }

        public Builder size(int size) {
            this.size = size;
            return this;
        }

        public Builder xPlayer(Player xPlayer) {
            this.xPlayer = requirePiece(xPlayer, Piece.X);
            return this;
        }

        public Builder oPlayer(Player oPlayer) {
            this.oPlayer = requirePiece(oPlayer, Piece.O);
            return this;
        }

        public Builder winningStrategies(List<WinningStrategy> strategies) {
            this.strategies.clear();
            this.strategies.addAll(Objects.requireNonNull(strategies, "strategies"));
            return this;
        }

        public Builder addWinningStrategy(WinningStrategy strategy) {
            this.strategies.add(Objects.requireNonNull(strategy, "strategy"));
            return this;
        }

        public Game build() {
            return new Game(new Board(size), xPlayer, oPlayer, strategies);
        }
    }
}

