package in.neelporiya.taskmanagement.model;

/** Ordered by urgency (LOW &lt; MEDIUM &lt; HIGH &lt; URGENT via ordinal) so comparators can sort by it. */
public enum Priority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}
