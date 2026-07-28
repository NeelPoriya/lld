package in.neelporiya.taskmanagement.filter;

import in.neelporiya.taskmanagement.model.Task;

import java.util.List;

/**
 * // DESIGN PATTERN: Strategy + Composite.
 *
 * <p>A filter is a predicate over a {@link Task}. The {@link #and} default method composes two
 * filters into an {@link AndFilter}, so callers build arbitrarily complex queries by combining
 * simple, independently-testable pieces — without the service knowing any concrete filter.
 */
@FunctionalInterface
public interface TaskFilter {

    boolean matches(Task task);

    default TaskFilter and(TaskFilter other) {
        return new AndFilter(List.of(this, other));
    }
}
