package in.neelporiya.elevator;

import in.neelporiya.elevator.dispatch.NearestDispatchStrategy;
import in.neelporiya.elevator.observer.ElevatorObserver;
import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * // CONCURRENCY: many threads press hall buttons at once. The per-car lock on the stop sets must
 * ensure no request is lost — after pumping the simulation, every requested floor must have been
 * served exactly once.
 */
class ElevatorConcurrencyTest {

    @Test
    void concurrentHallCallsAreAllServed() throws InterruptedException {
        Elevator car = new Elevator("A", 0, MutableClock.atEpoch());
        Set<Integer> served = ConcurrentHashMap.newKeySet();
        car.addObserver(new ElevatorObserver() {
            @Override
            public void onStop(String elevatorId, int floor, Instant at) {
                served.add(floor);
            }
        });
        ElevatorController controller = new ElevatorController(List.of(car), new NearestDispatchStrategy());

        int floors = 200;
        ExecutorService pool = Executors.newFixedThreadPool(32);
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(floors);
        for (int f = 1; f <= floors; f++) {
            int floor = f;
            pool.submit(() -> {
                try {
                    startGun.await();
                    controller.requestHallCall(floor, Direction.UP);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        startGun.countDown();
        assertTrue(done.await(10, TimeUnit.SECONDS));
        pool.shutdownNow();

        // Pump the simulation to completion and verify every requested floor was visited.
        controller.runUntilIdle(10_000);

        assertTrue(car.isIdle());
        Set<Integer> expected = ConcurrentHashMap.newKeySet();
        for (int f = 1; f <= floors; f++) {
            expected.add(f);
        }
        assertEquals(expected, served, "every requested floor must be served exactly once");
    }
}
