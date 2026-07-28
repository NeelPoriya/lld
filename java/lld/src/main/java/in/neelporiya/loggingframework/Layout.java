package in.neelporiya.loggingframework;

/**
 * // DESIGN PATTERN: Strategy — appenders depend on this interface, so changing text/JSON/XML
 * formatting does not change logger or appender dispatch logic.
 *
 * <p>// EXTENSIBILITY: Layouts are open for new formats while appenders stay closed for modification.
 */
public interface Layout {

    String format(LogRecord record);
}
