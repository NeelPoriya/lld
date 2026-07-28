package in.neelporiya.loggingframework;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Global access point for named loggers.
 *
 * <p>// DESIGN PATTERN: Singleton via initialization-on-demand holder. // TESTABILITY: the core
 * {@link Logger} still has a public builder with injected {@link Clock} and appenders, so tests and
 * libraries do not have to depend on this global registry.
 */
public final class LogManager {

    private final ConcurrentMap<String, Logger> loggers = new ConcurrentHashMap<>();
    private final List<Appender> rootAppenders = new CopyOnWriteArrayList<>();
    private volatile LogLevel rootLevel = LogLevel.INFO;
    private volatile Clock clock = Clock.systemUTC();

    private LogManager() {
        rootAppenders.add(new ConsoleAppender());
    }

    public static LogManager getInstance() {
        return Holder.INSTANCE;
    }

    public Logger getLogger(String name) {
        Objects.requireNonNull(name, "name");
        return loggers.computeIfAbsent(name, this::createLogger);
    }

    public void setRootLevel(LogLevel rootLevel) {
        this.rootLevel = Objects.requireNonNull(rootLevel, "rootLevel");
        loggers.clear();
    }

    public void setClock(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
        loggers.clear();
    }

    public void replaceRootAppenders(List<Appender> appenders) {
        rootAppenders.clear();
        rootAppenders.addAll(Objects.requireNonNull(appenders, "appenders"));
        loggers.clear();
    }

    private Logger createLogger(String name) {
        Logger.Builder builder = Logger.builder(name)
                .minimumLevel(rootLevel)
                .clock(clock);
        rootAppenders.forEach(builder::addAppender);
        return builder.build();
    }

    private static final class Holder {
        private static final LogManager INSTANCE = new LogManager();
    }
}
