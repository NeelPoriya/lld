package in.neelporiya.connectionpool;

/**
 * // DESIGN PATTERN: Factory. Decouples the pool from HOW a resource is created, health-checked and
 * torn down. Injecting this is the test seam — tests supply fake connections to assert reuse,
 * validation and eviction behaviour precisely.
 */
public interface ResourceFactory<T> {

    T create();

    /** @return true if the resource is still usable. Default: always valid. */
    default boolean validate(T resource) {
        return true;
    }

    void close(T resource);
}
