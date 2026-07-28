package in.neelporiya.loggingframework;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Test-friendly appender that keeps both records and rendered lines.
 *
 * <p>// TESTABILITY: Unit tests assert on this sink instead of scraping console/file output.
 */
public final class InMemoryAppender extends AbstractAppender {

    private final List<LogRecord> records = new CopyOnWriteArrayList<>();
    private final List<String> lines = new CopyOnWriteArrayList<>();

    public InMemoryAppender() {
        this(LogLevel.TRACE, new SimpleTextLayout());
    }

    public InMemoryAppender(LogLevel threshold, Layout layout) {
        super(threshold, layout);
    }

    @Override
    protected void write(LogRecord record, String formattedLine) {
        // CONCURRENCY: CopyOnWriteArrayList gives readers a stable snapshot while many threads append.
        records.add(record);
        lines.add(formattedLine);
    }

    public List<LogRecord> getRecords() {
        return List.copyOf(records);
    }

    public List<String> getLines() {
        return List.copyOf(lines);
    }
}
