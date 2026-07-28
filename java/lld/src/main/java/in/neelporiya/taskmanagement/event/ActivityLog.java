package in.neelporiya.taskmanagement.event;

import in.neelporiya.taskmanagement.model.Task;
import in.neelporiya.taskmanagement.model.TaskStatus;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A simple audit trail that records a human-readable line for every task event.
 *
 * <p>// CONCURRENCY: backed by a {@link CopyOnWriteArrayList} so many worker threads can append
 * events while readers iterate a stable snapshot.
 */
public class ActivityLog implements TaskEventListener {

    private final List<String> entries = new CopyOnWriteArrayList<>();

    @Override
    public void onCreated(Task task) {
        entries.add("CREATED " + task.getId() + " (" + task.getTitle() + ")");
    }

    @Override
    public void onStatusChanged(Task task, TaskStatus previous, TaskStatus current) {
        entries.add("STATUS " + task.getId() + " " + previous + "->" + current);
    }

    @Override
    public void onAssigned(Task task, String assigneeId) {
        entries.add("ASSIGNED " + task.getId() + " -> " + assigneeId);
    }

    public List<String> entries() {
        return List.copyOf(entries);
    }
}
