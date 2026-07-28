package in.neelporiya.connectionpool;

/** Test double for a pooled resource, with flags tests can flip to exercise validation/eviction. */
class FakeConnection {
    final String id;
    volatile boolean valid = true;
    volatile boolean closed = false;

    FakeConnection(String id) {
        this.id = id;
    }
}
