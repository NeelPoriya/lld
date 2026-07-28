package in.neelporiya.taskmanagement.filter;

import in.neelporiya.taskmanagement.model.Task;

import java.util.Comparator;

/**
 * // DESIGN PATTERN: Strategy (as {@link Comparator}). Reusable sort orders for task queries.
 */
public final class TaskComparators {

    private TaskComparators() {
    }

    /** Most urgent first (URGENT → LOW). */
    public static final Comparator<Task> BY_PRIORITY_DESC =
            Comparator.comparing(Task::getPriority).reversed();

    /** Earliest due date first; tasks without a due date sort last. */
    public static final Comparator<Task> BY_DUE_DATE_ASC =
            Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()));

    /** Oldest first. */
    public static final Comparator<Task> BY_CREATED_ASC =
            Comparator.comparing(Task::getCreatedAt);
}
