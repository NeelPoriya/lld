package in.neelporiya.taskmanagement.service;

import in.neelporiya.taskmanagement.event.TaskEventListener;
import in.neelporiya.taskmanagement.exception.TaskNotFoundException;
import in.neelporiya.taskmanagement.filter.OverdueFilter;
import in.neelporiya.taskmanagement.filter.TaskComparators;
import in.neelporiya.taskmanagement.filter.TaskFilter;
import in.neelporiya.taskmanagement.model.Priority;
import in.neelporiya.taskmanagement.model.Task;
import in.neelporiya.taskmanagement.model.TaskStatus;
import in.neelporiya.taskmanagement.model.User;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

/**
 * // DESIGN PATTERN: Facade. The single entry point over the repository, event listeners and clock.
 *
 * <p>// TESTABILITY: {@link Clock} and the id {@link Supplier} are injected, so timestamps,
 * "overdue" logic and ids are all deterministic in tests.
 */
public class TaskManagementService {

    private final TaskRepository repository = new TaskRepository();
    private final List<TaskEventListener> listeners = new CopyOnWriteArrayList<>();
    private final Clock clock;
    private final Supplier<String> idGenerator;

    public TaskManagementService(Clock clock, Supplier<String> idGenerator) {
        this.clock = Objects.requireNonNull(clock, "clock");
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
    }

    public static TaskManagementService createDefault() {
        return new TaskManagementService(Clock.systemUTC(), () -> UUID.randomUUID().toString());
    }

    public void addListener(TaskEventListener listener) {
        listeners.add(listener);
    }

    public User createUser(String name) {
        return new User(idGenerator.get(), name);
    }

    public Task createTask(String title, String description, Priority priority, Set<String> tags, Instant dueDate) {
        Task task = Task.builder()
                .id(idGenerator.get())
                .title(title)
                .description(description)
                .priority(priority)
                .tags(tags)
                .dueDate(dueDate)
                .createdAt(clock.instant())
                .build();
        repository.save(task);
        listeners.forEach(listener -> listener.onCreated(task));
        return task;
    }

    public Task changeStatus(String taskId, TaskStatus next) {
        Task task = require(taskId);
        TaskStatus previous = task.changeStatus(next, clock);
        listeners.forEach(listener -> listener.onStatusChanged(task, previous, next));
        return task;
    }

    public Task assign(String taskId, String userId) {
        Task task = require(taskId);
        task.assignTo(userId, clock);
        listeners.forEach(listener -> listener.onAssigned(task, userId));
        return task;
    }

    public Task unassign(String taskId) {
        Task task = require(taskId);
        task.unassign(clock);
        return task;
    }

    public Task setPriority(String taskId, Priority priority) {
        Task task = require(taskId);
        task.setPriority(priority, clock);
        return task;
    }

    public Task setDueDate(String taskId, Instant dueDate) {
        Task task = require(taskId);
        task.setDueDate(dueDate, clock);
        return task;
    }

    public Task addTag(String taskId, String tag) {
        Task task = require(taskId);
        task.addTag(tag, clock);
        return task;
    }

    public boolean deleteTask(String taskId) {
        return repository.delete(taskId);
    }

    public Task getTask(String taskId) {
        return require(taskId);
    }

    /** Query with a composable filter and an optional sort order. */
    public List<Task> query(TaskFilter filter, Comparator<Task> sort) {
        var stream = repository.findAll().stream().filter(filter::matches);
        if (sort != null) {
            stream = stream.sorted(sort);
        }
        return stream.toList();
    }

    public List<Task> query(TaskFilter filter) {
        return query(filter, null);
    }

    /** All tasks that are past due and not done, earliest-due first. */
    public List<Task> overdueTasks() {
        return query(new OverdueFilter(clock), TaskComparators.BY_DUE_DATE_ASC);
    }

    private Task require(String taskId) {
        return repository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("No task with id " + taskId));
    }
}
