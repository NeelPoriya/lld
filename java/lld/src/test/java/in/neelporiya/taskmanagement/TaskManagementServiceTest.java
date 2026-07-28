package in.neelporiya.taskmanagement;

import in.neelporiya.taskmanagement.event.ActivityLog;
import in.neelporiya.taskmanagement.exception.InvalidTransitionException;
import in.neelporiya.taskmanagement.exception.TaskNotFoundException;
import in.neelporiya.taskmanagement.filter.AssigneeFilter;
import in.neelporiya.taskmanagement.filter.StatusFilter;
import in.neelporiya.taskmanagement.filter.TagFilter;
import in.neelporiya.taskmanagement.filter.TaskComparators;
import in.neelporiya.taskmanagement.filter.TaskFilter;
import in.neelporiya.taskmanagement.model.Priority;
import in.neelporiya.taskmanagement.model.Task;
import in.neelporiya.taskmanagement.model.TaskStatus;
import in.neelporiya.taskmanagement.model.User;
import in.neelporiya.taskmanagement.service.TaskManagementService;
import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskManagementServiceTest {

    private static final TaskFilter ALL = task -> true;

    private final MutableClock clock = MutableClock.atEpoch();
    private TaskManagementService service;
    private ActivityLog activityLog;

    @BeforeEach
    void setUp() {
        AtomicInteger seq = new AtomicInteger();
        service = new TaskManagementService(clock, () -> "T" + seq.incrementAndGet());
        activityLog = new ActivityLog();
        service.addListener(activityLog);
    }

    @Test
    void createTaskAppliesDefaultsAndTimestamps() {
        Task task = service.createTask("title", "desc", Priority.HIGH, Set.of("work"), null);

        assertEquals("T1", task.getId());
        assertEquals(TaskStatus.TODO, task.getStatus());
        assertEquals(Instant.EPOCH, task.getCreatedAt());
        assertEquals(0, task.getVersion());
        assertTrue(activityLog.entries().get(0).startsWith("CREATED T1"));
    }

    @Test
    void legalStatusTransitionSucceedsAndIsLogged() {
        Task task = service.createTask("t", "d", Priority.MEDIUM, Set.of(), null);
        service.changeStatus(task.getId(), TaskStatus.IN_PROGRESS);

        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
        assertTrue(activityLog.entries().contains("STATUS T1 TODO->IN_PROGRESS"));
    }

    @Test
    void illegalTransitionIsRejected() {
        Task task = service.createTask("t", "d", Priority.MEDIUM, Set.of(), null);
        // TODO -> DONE is not a legal jump.
        assertThrows(InvalidTransitionException.class,
                () -> service.changeStatus(task.getId(), TaskStatus.DONE));
    }

    @Test
    void doneIsTerminalAndSetsCompletedAt() {
        Task task = service.createTask("t", "d", Priority.MEDIUM, Set.of(), null);
        service.changeStatus(task.getId(), TaskStatus.IN_PROGRESS);
        clock.advance(Duration.ofHours(1));
        service.changeStatus(task.getId(), TaskStatus.DONE);

        assertEquals(Instant.EPOCH.plus(Duration.ofHours(1)), task.getCompletedAt());
        assertThrows(InvalidTransitionException.class,
                () -> service.changeStatus(task.getId(), TaskStatus.IN_PROGRESS));
    }

    @Test
    void assignmentAndAssigneeFilter() {
        User alice = service.createUser("alice");
        Task task = service.createTask("t", "d", Priority.MEDIUM, Set.of(), null);
        service.assign(task.getId(), alice.id());

        assertEquals(List.of(task), service.query(new AssigneeFilter(alice.id())));
        assertTrue(activityLog.entries().stream().anyMatch(e -> e.startsWith("ASSIGNED T")));
    }

    @Test
    void overdueDerivesFromInjectedClock() {
        Instant due = Instant.EPOCH.plus(Duration.ofHours(1));
        Task task = service.createTask("t", "d", Priority.MEDIUM, Set.of(), due);

        assertTrue(service.overdueTasks().isEmpty(), "not overdue yet");

        clock.advance(Duration.ofHours(2));
        assertEquals(List.of(task), service.overdueTasks());

        // Finishing it removes it from overdue.
        service.changeStatus(task.getId(), TaskStatus.IN_PROGRESS);
        service.changeStatus(task.getId(), TaskStatus.DONE);
        assertTrue(service.overdueTasks().isEmpty());
    }

    @Test
    void composableAndFilter() {
        Task match = service.createTask("a", "d", Priority.MEDIUM, Set.of("x"), null);
        service.createTask("b", "d", Priority.MEDIUM, Set.of("y"), null); // wrong tag
        Task moved = service.createTask("c", "d", Priority.MEDIUM, Set.of("x"), null);
        service.changeStatus(moved.getId(), TaskStatus.IN_PROGRESS); // wrong status

        TaskFilter filter = new StatusFilter(TaskStatus.TODO).and(new TagFilter("x"));
        assertEquals(List.of(match), service.query(filter));
    }

    @Test
    void sortingByPriorityDescending() {
        service.createTask("low", "d", Priority.LOW, Set.of(), null);
        service.createTask("urgent", "d", Priority.URGENT, Set.of(), null);
        service.createTask("medium", "d", Priority.MEDIUM, Set.of(), null);

        List<Priority> order = service.query(ALL, TaskComparators.BY_PRIORITY_DESC)
                .stream().map(Task::getPriority).toList();

        assertEquals(List.of(Priority.URGENT, Priority.MEDIUM, Priority.LOW), order);
    }

    @Test
    void versionIncrementsOnEachMutation() {
        Task task = service.createTask("t", "d", Priority.MEDIUM, Set.of(), null);
        assertEquals(0, task.getVersion());
        service.changeStatus(task.getId(), TaskStatus.IN_PROGRESS);
        assertEquals(1, task.getVersion());
        service.assign(task.getId(), "someone");
        assertEquals(2, task.getVersion());
    }

    @Test
    void deleteRemovesTask() {
        Task task = service.createTask("t", "d", Priority.MEDIUM, Set.of(), null);
        assertTrue(service.deleteTask(task.getId()));
        assertThrows(TaskNotFoundException.class, () -> service.getTask(task.getId()));
    }

    @Test
    void unknownTaskThrows() {
        assertThrows(TaskNotFoundException.class, () -> service.getTask("nope"));
        assertNotNull(service);
        assertFalse(service.deleteTask("nope"));
    }
}
