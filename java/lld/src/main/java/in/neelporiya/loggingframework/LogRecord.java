package in.neelporiya.loggingframework;

import java.time.Instant;
import java.util.Objects;

/**
 * Immutable value captured at the call site before dispatching to appenders.
 *
 * <p>// TESTABILITY: The timestamp is supplied by the logger's injected {@code Clock}, not by
 * {@code Instant.now()}, so unit tests can assert exact output without sleeping.
 */
public final class LogRecord {

    private final LogLevel level;
    private final String message;
    private final String loggerName;
    private final Instant timestamp;
    private final String threadName;

    public LogRecord(LogLevel level, String message, String loggerName, Instant timestamp, String threadName) {
        this.level = Objects.requireNonNull(level, "level");
        this.message = Objects.requireNonNull(message, "message");
        this.loggerName = Objects.requireNonNull(loggerName, "loggerName");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp");
        this.threadName = Objects.requireNonNull(threadName, "threadName");
    }

    public LogLevel getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getThreadName() {
        return threadName;
    }
}
