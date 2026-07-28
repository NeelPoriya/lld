package in.neelporiya.taskmanagement.filter;

import in.neelporiya.taskmanagement.model.Task;

public class TagFilter implements TaskFilter {

    private final String tag;

    public TagFilter(String tag) {
        this.tag = tag;
    }

    @Override
    public boolean matches(Task task) {
        return task.getTags().contains(tag);
    }
}
