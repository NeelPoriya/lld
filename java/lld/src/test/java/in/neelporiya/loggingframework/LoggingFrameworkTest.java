package in.neelporiya.loggingframework;

import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * // TESTABILITY: These tests use an injected clock and in-memory appenders, so no console scraping,
 * filesystem dependency, or Thread.sleep timing is needed.
 */
class LoggingFrameworkTest {

    @Test
    void infoLoggerDropsDebugMessages() {
        InMemoryAppender memory = new InMemoryAppender();
        Logger logger = Logger.builder("orders")
                .minimumLevel(LogLevel.INFO)
                .clock(MutableClock.atEpoch())
                .addAppender(memory)
                .build();

        logger.debug("too detailed");
        logger.info("accepted");

        assertEquals(1, memory.getRecords().size());
        assertEquals(LogLevel.INFO, memory.getRecords().getFirst().getLevel());
        assertEquals("accepted", memory.getRecords().getFirst().getMessage());
    }

    @Test
    void simpleTextLayoutFormatsExactTimestampLevelLoggerAndMessage() {
        Instant instant = Instant.parse("2026-07-28T16:12:20.526Z");
        InMemoryAppender memory = new InMemoryAppender(LogLevel.TRACE, new SimpleTextLayout());
        Logger logger = Logger.builder("billing")
                .minimumLevel(LogLevel.TRACE)
                .clock(MutableClock.at(instant))
                .addAppender(memory)
                .build();

        logger.info("invoice paid");

        assertEquals("2026-07-28T16:12:20.526Z [INFO] billing - invoice paid",
                memory.getLines().getFirst());
    }

    @Test
    void multipleAppendersAllReceiveTheSameRecord() {
        InMemoryAppender first = new InMemoryAppender();
        InMemoryAppender second = new InMemoryAppender();
        Logger logger = Logger.builder("inventory")
                .minimumLevel(LogLevel.INFO)
                .clock(MutableClock.atEpoch())
                .addAppender(first)
                .addAppender(second)
                .build();

        logger.warn("stock low");

        assertEquals(1, first.getRecords().size());
        assertEquals(1, second.getRecords().size());
        assertEquals(first.getLines().getFirst(), second.getLines().getFirst());
    }

    @Test
    void asyncAppenderDeliversAllRecordsAfterFlushAndClose() {
        InMemoryAppender memory = new InMemoryAppender();
        AsyncAppender async = new AsyncAppender(memory, LogLevel.TRACE, "test-async-delivery");
        Logger logger = Logger.builder("async")
                .minimumLevel(LogLevel.TRACE)
                .clock(MutableClock.atEpoch())
                .addAppender(async)
                .build();

        for (int i = 0; i < 100; i++) {
            logger.info("message-" + i);
        }

        async.flush();
        assertEquals(100, memory.getRecords().size());

        async.close();
        assertEquals(100, memory.getRecords().size());
    }

    @Test
    void concurrentLoggingThroughAsyncAppenderDoesNotLoseRecords() throws InterruptedException {
        int threads = 16;
        int messagesPerThread = 75;
        int expected = threads * messagesPerThread;

        InMemoryAppender memory = new InMemoryAppender();
        AsyncAppender async = new AsyncAppender(memory, LogLevel.TRACE, "test-async-concurrency");
        Logger logger = Logger.builder("concurrent")
                .minimumLevel(LogLevel.TRACE)
                .clock(MutableClock.atEpoch())
                .addAppender(async)
                .build();

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        Set<String> uniqueMessages = ConcurrentHashMap.newKeySet();

        for (int t = 0; t < threads; t++) {
            int threadId = t;
            pool.submit(() -> {
                try {
                    startGun.await(); // CONCURRENCY: maximize contention on the logger and async queue.
                    for (int i = 0; i < messagesPerThread; i++) {
                        String message = "t" + threadId + "-m" + i;
                        uniqueMessages.add(message);
                        logger.info(message);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        startGun.countDown();
        assertTrue(done.await(10, TimeUnit.SECONDS), "workers did not finish in time");
        pool.shutdownNow();

        async.close();

        assertEquals(expected, uniqueMessages.size(), "test setup should create unique messages");
        assertEquals(expected, memory.getRecords().size(), "async appender must not drop records");
    }
}
