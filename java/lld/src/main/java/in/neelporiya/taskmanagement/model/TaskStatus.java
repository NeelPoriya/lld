package in.neelporiya.taskmanagement.model;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * The task workflow, with legal transitions encoded as data.
 *
 * <p>// INTERVIEW INSIGHT: encoding the state machine as an allowed-transitions map (rather than
 * scattered {@code if/else}) keeps the workflow correct and in one place. {@code DONE} is terminal:
 * its allowed-set is empty, so the system can never silently reopen a finished task.
 */
public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    BLOCKED,
    DONE;

    private static final Map<TaskStatus, Set<TaskStatus>> ALLOWED = new EnumMap<>(TaskStatus.class);

    static {
        ALLOWED.put(TODO, EnumSet.of(IN_PROGRESS, BLOCKED));
        ALLOWED.put(IN_PROGRESS, EnumSet.of(BLOCKED, DONE, TODO));
        ALLOWED.put(BLOCKED, EnumSet.of(IN_PROGRESS, TODO));
        ALLOWED.put(DONE, EnumSet.noneOf(TaskStatus.class)); // terminal
    }

    public boolean canTransitionTo(TaskStatus next) {
        return ALLOWED.get(this).contains(next);
    }
}
