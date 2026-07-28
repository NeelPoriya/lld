package in.neelporiya.taskmanagement.service;

import in.neelporiya.taskmanagement.model.Task;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * // DESIGN PATTERN: Repository — isolates storage behind a small interface-like surface so the
 * in-memory map could later be swapped for a database without touching the service.
 *
 * <p>// CONCURRENCY: a {@link ConcurrentHashMap} gives lock-free, thread-safe put/get/remove.
 */
public class TaskRepository {

    private final Map<String, Task> tasks = new ConcurrentHashMap<>();

    public void save(Task task) {
        tasks.put(task.getId(), task);
    }

    public Optional<Task> findById(String id) {
        return Optional.ofNullable(tasks.get(id));
    }

    public List<Task> findAll() {
        return List.copyOf(tasks.values());
    }

    public boolean delete(String id) {
        return tasks.remove(id) != null;
    }

    public int size() {
        return tasks.size();
    }
}
