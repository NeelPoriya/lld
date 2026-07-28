package in.neelporiya.taskmanagement.filter;

import in.neelporiya.taskmanagement.model.Task;

import java.time.Clock;

/**
 * Matches tasks that are past their due date and not yet done.
 *
 * <p>// TESTABILITY: it reads "now" from an injected {@link Clock} at match time, so a test can make
 * tasks overdue by advancing a {@code MutableClock} instead of waiting for real time to pass.
 */
public class OverdueFilter implements TaskFilter {

    private final Clock clock;

    public OverdueFilter(Clock clock) {
        this.clock = clock;
    }

    @Override
    public boolean matches(Task task) {
        return task.isOverdue(clock.instant());
    }
}
