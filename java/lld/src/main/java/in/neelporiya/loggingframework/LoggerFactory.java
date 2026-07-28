package in.neelporiya.loggingframework;

/**
 * SLF4J-style static facade over the singleton {@link LogManager}.
 */
public final class LoggerFactory {

    private LoggerFactory() {
    }

    public static Logger getLogger(String name) {
        return LogManager.getInstance().getLogger(name);
    }

    public static Logger getLogger(Class<?> type) {
        return getLogger(type.getName());
    }
}
