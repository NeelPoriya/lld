package in.neelporiya.loggingframework;

/**
 * A sink for log records: console, memory, file, async wrapper, network, etc.
 *
 * <p>// EXTENSIBILITY: Adding a database or HTTP sink only requires another implementation of this
 * interface; {@link Logger} continues to broadcast to the abstraction.
 */
public interface Appender extends AutoCloseable {

    void append(LogRecord record);

    default void flush() {
        // Most in-memory appenders have nothing to flush.
    }

    @Override
    default void close() {
        flush();
    }
}
