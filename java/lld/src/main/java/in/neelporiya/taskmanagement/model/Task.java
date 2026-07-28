package in.neelporiya.taskmanagement.model;

import in.neelporiya.taskmanagement.exception.InvalidTransitionException;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * The task aggregate. It owns and protects its own mutable state.
 *
 * <p>// CONCURRENCY: all mutations go through {@code synchronized} methods, so a change is atomic:
 * validate → update field(s) → bump {@code updatedAt} and {@code version}. Two concurrent status
 * changes can't both succeed against the same precondition. Mutable fields are {@code volatile} so
 * reader threads (e.g. queries) see the latest values without taking the lock.
 *
 * <p>// INTERVIEW INSIGHT: {@code version} is the hook for optimistic concurrency — an API can reject
 * an update whose expected version no longer matches, preventing lost updates between two editors.
 */
public class Task {

    private final String id;
    private final String title;
    private final Instant createdAt;
    private final Set<String> tags = new CopyOnWriteArraySet<>();

    private volatile String description;
    private volatile Priority priority;
    private volatile TaskStatus status;
    private volatile String assigneeId;   // nullable
    private volatile Instant dueDate;     // nullable
    private volatile Instant updatedAt;
    private volatile Instant completedAt; // nullable
    private volatile int version;

    private Task(Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.description = builder.description;
        this.priority = builder.priority;
        this.status = TaskStatus.TODO;
        this.dueDate = builder.dueDate;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.createdAt;
        this.tags.addAll(builder.tags);
    }

    public synchronized TaskStatus changeStatus(TaskStatus next, Clock clock) {
        if (!status.canTransitionTo(next)) {
            throw new InvalidTransitionException("Cannot move task " + id + " from " + status + " to " + next);
        }
        TaskStatus previous = status;
        status = next;
        if (next == TaskStatus.DONE) {
            completedAt = clock.instant();
        }
        touch(clock);
        return previous;
    }

    public synchronized void assignTo(String userId, Clock clock) {
        this.assigneeId = Objects.requireNonNull(userId, "userId");
        touch(clock);
    }

    public synchronized void unassign(Clock clock) {
        this.assigneeId = null;
        touch(clock);
    }

    public synchronized void setPriority(Priority priority, Clock clock) {
        this.priority = Objects.requireNonNull(priority, "priority");
        touch(clock);
    }

    public synchronized void setDueDate(Instant dueDate, Clock clock) {
        this.dueDate = dueDate;
        touch(clock);
    }

    public synchronized void addTag(String tag, Clock clock) {
        tags.add(tag);
        touch(clock);
    }

    /** Bump the audit fields. Must be called while holding the lock (from a synchronized method). */
    private void touch(Clock clock) {
        this.updatedAt = clock.instant();
        this.version++;
    }

    public boolean isOverdue(Instant now) {
        Instant due = this.dueDate;
        return due != null && status != TaskStatus.DONE && now.isAfter(due);
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Priority getPriority() {
        return priority;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public String getAssigneeId() {
        return assigneeId;
    }

    public Instant getDueDate() {
        return dueDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public int getVersion() {
        return version;
    }

    public Set<String> getTags() {
        return Set.copyOf(tags);
    }

    public static Builder builder() {
        return new Builder();
    }

    /** // DESIGN PATTERN: Builder — a task has many optional fields; the builder keeps construction readable. */
    public static final class Builder {
        private String id;
        private String title;
        private String description = "";
        private Priority priority = Priority.MEDIUM;
        private Instant dueDate;
        private Instant createdAt = Instant.EPOCH;
        private Set<String> tags = Set.of();

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder priority(Priority priority) {
            this.priority = priority;
            return this;
        }

        public Builder dueDate(Instant dueDate) {
            this.dueDate = dueDate;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder tags(Set<String> tags) {
            this.tags = Set.copyOf(tags);
            return this;
        }

        public Task build() {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(title, "title");
            return new Task(this);
        }
    }
}
