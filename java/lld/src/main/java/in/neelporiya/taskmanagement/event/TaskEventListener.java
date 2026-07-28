package in.neelporiya.taskmanagement.event;

import in.neelporiya.taskmanagement.model.Task;
import in.neelporiya.taskmanagement.model.TaskStatus;

/**
 * // DESIGN PATTERN: Observer. The service emits these on every mutation; listeners (activity log,
 * notifications, metrics) react without the service depending on them.
 */
public interface TaskEventListener {

    default void onCreated(Task task) {
    }

    default void onStatusChanged(Task task, TaskStatus previous, TaskStatus current) {
    }

    default void onAssigned(Task task, String assigneeId) {
    }
}
