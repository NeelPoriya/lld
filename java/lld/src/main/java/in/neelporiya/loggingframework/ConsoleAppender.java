package in.neelporiya.loggingframework;

import java.io.PrintStream;
import java.util.Objects;

/**
 * Thread-safe console sink.
 */
public final class ConsoleAppender extends AbstractAppender {

    private final PrintStream out;

    public ConsoleAppender() {
        this(System.out, LogLevel.TRACE, new SimpleTextLayout());
    }

    public ConsoleAppender(PrintStream out, LogLevel threshold, Layout layout) {
        super(threshold, layout);
        this.out = Objects.requireNonNull(out, "out");
    }

    @Override
    protected void write(LogRecord record, String formattedLine) {
        out.println(formattedLine);
    }

    @Override
    public synchronized void flush() {
        out.flush();
    }
}
