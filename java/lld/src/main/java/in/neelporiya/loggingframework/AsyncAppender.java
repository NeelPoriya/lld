package in.neelporiya.loggingframework;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Asynchronous appender wrapper with deterministic flush/close.
 *
 * <p>// DESIGN PATTERN: Decorator — wraps any {@link Appender} and adds async behavior without
 * changing the delegate's implementation.
 */
public final class AsyncAppender implements Appender {

    private final Appender delegate;
    private final LogLevel threshold;
    private final BlockingQueue<QueueItem> queue = new LinkedBlockingQueue<>();
    private final AtomicBoolean closed = new AtomicBoolean();
    private final Object lifecycleLock = new Object();
    private final Thread worker;

    public AsyncAppender(Appender delegate) {
        this(delegate, LogLevel.TRACE, "logging-framework-async-appender");
    }

    public AsyncAppender(Appender delegate, LogLevel threshold, String workerName) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.threshold = Objects.requireNonNull(threshold, "threshold");
        worker = new Thread(this::drainLoop, Objects.requireNonNull(workerName, "workerName"));
        worker.setDaemon(true);
        worker.start();
    }

    @Override
    public void append(LogRecord record) {
        Objects.requireNonNull(record, "record");
        synchronized (lifecycleLock) {
            if (closed.get()) {
                throw new IllegalStateException("async appender is closed");
            }
            // DESIGN PATTERN: threshold filtering can live at logger and appender levels; a full Log4j
            // system may model this as a richer Chain of Responsibility with filters.
            if (!threshold.allows(record.getLevel())) {
                return;
            }
            // CONCURRENCY: The lifecycle lock orders append() against close(); a record can never be
            // enqueued behind the stop marker and silently lost.
            queue.add(QueueItem.record(record));
        }
    }

    @Override
    public void flush() {
        CompletableFuture<Void> done = new CompletableFuture<>();
        synchronized (lifecycleLock) {
            if (closed.get()) {
                return;
            }
            queue.add(QueueItem.flush(done));
        }
        waitFor(done);
    }

    @Override
    public void close() {
        CompletableFuture<Void> flushed = new CompletableFuture<>();
        CompletableFuture<Void> stopped = new CompletableFuture<>();
        synchronized (lifecycleLock) {
            if (!closed.compareAndSet(false, true)) {
                return;
            }
            queue.add(QueueItem.flush(flushed));
            queue.add(QueueItem.stop(stopped));
        }
        waitFor(flushed);
        waitFor(stopped);
        try {
            worker.join(TimeUnit.SECONDS.toMillis(5));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrupted while closing async appender", e);
        }
        if (worker.isAlive()) {
            throw new IllegalStateException("async appender worker did not stop");
        }
        delegate.close();
    }

    private void drainLoop() {
        try {
            while (true) {
                QueueItem item = queue.take();
                if (item.record != null) {
                    delegate.append(item.record);
                } else if (item.flushSignal != null) {
                    delegate.flush();
                    item.flushSignal.complete(null);
                } else if (item.stopSignal != null) {
                    delegate.flush();
                    item.stopSignal.complete(null);
                    return;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (RuntimeException e) {
            // INTERVIEW INSIGHT: Production systems route this to an error handler. For an interview
            // implementation, fail queued flush/close calls so tests never wait forever.
            failPendingControls(e);
        }
    }

    private void failPendingControls(RuntimeException failure) {
        QueueItem item;
        while ((item = queue.poll()) != null) {
            if (item.flushSignal != null) {
                item.flushSignal.completeExceptionally(failure);
            }
            if (item.stopSignal != null) {
                item.stopSignal.completeExceptionally(failure);
            }
        }
    }

    private static void waitFor(CompletableFuture<Void> done) {
        try {
            done.join();
        } catch (RuntimeException e) {
            throw e;
        }
    }

    private static final class QueueItem {
        private final LogRecord record;
        private final CompletableFuture<Void> flushSignal;
        private final CompletableFuture<Void> stopSignal;

        private QueueItem(LogRecord record, CompletableFuture<Void> flushSignal, CompletableFuture<Void> stopSignal) {
            this.record = record;
            this.flushSignal = flushSignal;
            this.stopSignal = stopSignal;
        }

        private static QueueItem record(LogRecord record) {
            return new QueueItem(record, null, null);
        }

        private static QueueItem flush(CompletableFuture<Void> done) {
            return new QueueItem(null, done, null);
        }

        private static QueueItem stop(CompletableFuture<Void> done) {
            return new QueueItem(null, null, done);
        }
    }
}
