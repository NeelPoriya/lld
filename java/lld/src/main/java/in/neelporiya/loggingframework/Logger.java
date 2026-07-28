package in.neelporiya.loggingframework;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Named logger facade used by application code.
 */
public final class Logger {

    private final String name;
    private final LogLevel minimumLevel;
    private final Clock clock;
    private final List<Appender> appenders;

    private Logger(Builder builder) {
        name = Objects.requireNonNull(builder.name, "name");
        minimumLevel = Objects.requireNonNull(builder.minimumLevel, "minimumLevel");
        clock = Objects.requireNonNull(builder.clock, "clock");
        appenders = new CopyOnWriteArrayList<>(builder.appenders);
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public void trace(String message) {
        log(LogLevel.TRACE, message);
    }

    public void debug(String message) {
        log(LogLevel.DEBUG, message);
    }

    public void info(String message) {
        log(LogLevel.INFO, message);
    }

    public void warn(String message) {
        log(LogLevel.WARN, message);
    }

    public void error(String message) {
        log(LogLevel.ERROR, message);
    }

    public void log(LogLevel level, String message) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(message, "message");
        // DESIGN PATTERN: level-threshold filtering is the first link in the responsibility chain.
        if (!minimumLevel.allows(level)) {
            return;
        }
        LogRecord record = new LogRecord(level, message, name, clock.instant(), Thread.currentThread().getName());
        // DESIGN PATTERN: Observer/Composite — one logger event is broadcast to every configured
        // appender, and the logger does not know whether each sink is console, memory, file, or async.
        appenders.forEach(appender -> appender.append(record));
    }

    public void addAppender(Appender appender) {
        appenders.add(Objects.requireNonNull(appender, "appender"));
    }

    public void flush() {
        appenders.forEach(Appender::flush);
    }

    public void close() {
        appenders.forEach(Appender::close);
    }

    public String getName() {
        return name;
    }

    public LogLevel getMinimumLevel() {
        return minimumLevel;
    }

    public static final class Builder {
        private final String name;
        private LogLevel minimumLevel = LogLevel.INFO;
        private Clock clock = Clock.systemUTC();
        private final List<Appender> appenders = new ArrayList<>();

        private Builder(String name) {
            this.name = Objects.requireNonNull(name, "name");
        }

        public Builder minimumLevel(LogLevel minimumLevel) {
            this.minimumLevel = Objects.requireNonNull(minimumLevel, "minimumLevel");
            return this;
        }

        public Builder clock(Clock clock) {
            this.clock = Objects.requireNonNull(clock, "clock");
            return this;
        }

        public Builder addAppender(Appender appender) {
            appenders.add(Objects.requireNonNull(appender, "appender"));
            return this;
        }

        public Logger build() {
            // DESIGN PATTERN: Builder keeps interview setup readable as more appenders/thresholds are added.
            return new Logger(this);
        }
    }
}
