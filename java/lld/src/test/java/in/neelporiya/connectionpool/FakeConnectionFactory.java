package in.neelporiya.connectionpool;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/** A {@link ResourceFactory} of {@link FakeConnection}s that records what it created/closed. */
class FakeConnectionFactory implements ResourceFactory<FakeConnection> {

    final AtomicInteger createdCount = new AtomicInteger();
    final List<FakeConnection> created = new CopyOnWriteArrayList<>();

    @Override
    public FakeConnection create() {
        FakeConnection c = new FakeConnection("c" + createdCount.incrementAndGet());
        created.add(c);
        return c;
    }

    @Override
    public boolean validate(FakeConnection resource) {
        return resource.valid;
    }

    @Override
    public void close(FakeConnection resource) {
        resource.closed = true;
    }
}
