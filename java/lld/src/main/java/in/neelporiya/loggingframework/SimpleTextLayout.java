package in.neelporiya.loggingframework;

/**
 * Small Log4j-style text layout:
 * {@code 1970-01-01T00:00:00Z [INFO] service - started}.
 */
public final class SimpleTextLayout implements Layout {

    @Override
    public String format(LogRecord record) {
        return record.getTimestamp()
                + " [" + record.getLevel() + "] "
                + record.getLoggerName()
                + " - "
                + record.getMessage();
    }
}
