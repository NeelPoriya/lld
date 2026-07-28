package in.neelporiya.loggingframework;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Objects;

/**
 * Minimal file-like appender backed by an injected {@link Writer}.
 */
public final class FileAppender extends AbstractAppender {

    private final Writer writer;

    public FileAppender(Writer writer) {
        this(writer, LogLevel.TRACE, new SimpleTextLayout());
    }

    public FileAppender(Writer writer, LogLevel threshold, Layout layout) {
        super(threshold, layout);
        this.writer = Objects.requireNonNull(writer, "writer");
    }

    @Override
    protected void write(LogRecord record, String formattedLine) {
        try {
            writer.write(formattedLine);
            writer.write(System.lineSeparator());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public synchronized void flush() {
        try {
            writer.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public synchronized void close() {
        try {
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
