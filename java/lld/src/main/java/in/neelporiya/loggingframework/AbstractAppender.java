package in.neelporiya.loggingframework;

import java.util.Objects;

/**
 * Shared level filtering + layout rendering for concrete appenders.
 */
public abstract class AbstractAppender implements Appender {

    private final LogLevel threshold;
    private final Layout layout;

    protected AbstractAppender() {
        this(LogLevel.TRACE, new SimpleTextLayout());
    }

    protected AbstractAppender(LogLevel threshold, Layout layout) {
        this.threshold = Objects.requireNonNull(threshold, "threshold");
        this.layout = Objects.requireNonNull(layout, "layout");
    }

    @Override
    public final synchronized void append(LogRecord record) {
        Objects.requireNonNull(record, "record");
        // DESIGN PATTERN: Chain-of-Responsibility-style threshold filtering — records below this
        // appender's minimum level stop here and are not passed to the concrete sink.
        if (!threshold.allows(record.getLevel())) {
            return;
        }
        // CONCURRENCY: Rendering and the actual write happen while holding this appender's lock, so
        // two caller threads cannot interleave half-lines in a Writer/PrintStream.
        write(record, layout.format(record));
    }

    protected abstract void write(LogRecord record, String formattedLine);

    public LogLevel getThreshold() {
        return threshold;
    }

    public Layout getLayout() {
        return layout;
    }
}
