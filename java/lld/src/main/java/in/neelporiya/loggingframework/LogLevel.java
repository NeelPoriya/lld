package in.neelporiya.loggingframework;

/**
 * Ordered from most verbose to most severe so threshold checks are a simple ordinal comparison.
 */
public enum LogLevel {
    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR;

    public boolean allows(LogLevel candidate) {
        return candidate.ordinal() >= ordinal();
    }
}
