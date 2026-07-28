package in.neelporiya.taskmanagement.filter;

import in.neelporiya.taskmanagement.model.Task;

import java.util.List;

/** // DESIGN PATTERN: Composite — matches only if every child filter matches (logical AND). */
public class AndFilter implements TaskFilter {

    private final List<TaskFilter> filters;

    public AndFilter(List<TaskFilter> filters) {
        this.filters = List.copyOf(filters);
    }

    @Override
    public boolean matches(Task task) {
        return filters.stream().allMatch(filter -> filter.matches(task));
    }
}
