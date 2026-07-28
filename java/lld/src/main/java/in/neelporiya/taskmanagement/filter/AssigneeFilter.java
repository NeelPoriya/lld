package in.neelporiya.taskmanagement.filter;

import in.neelporiya.taskmanagement.model.Task;

import java.util.Objects;

public class AssigneeFilter implements TaskFilter {

    private final String assigneeId;

    public AssigneeFilter(String assigneeId) {
        this.assigneeId = assigneeId;
    }

    @Override
    public boolean matches(Task task) {
        return Objects.equals(task.getAssigneeId(), assigneeId);
    }
}
