package in.neelporiya.taskmanagement.filter;

import in.neelporiya.taskmanagement.model.Task;
import in.neelporiya.taskmanagement.model.TaskStatus;

public class StatusFilter implements TaskFilter {

    private final TaskStatus status;

    public StatusFilter(TaskStatus status) {
        this.status = status;
    }

    @Override
    public boolean matches(Task task) {
        return task.getStatus() == status;
    }
}
