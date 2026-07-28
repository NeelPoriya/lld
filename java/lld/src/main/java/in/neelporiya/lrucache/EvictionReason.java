package in.neelporiya.lrucache;

/** Why an entry left the cache — handed to {@link EvictionListener}. */
public enum EvictionReason {
    /** Removed to make room for a new entry (capacity exceeded). */
    CAPACITY,
    /** Removed because its TTL elapsed. */
    EXPIRED,
    /** Removed by an explicit {@code remove}/{@code clear}. */
    EXPLICIT
}
