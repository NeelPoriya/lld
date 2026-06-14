package in.neelporiya.phases.phase12concurrency;

import in.neelporiya.runner.Concept;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class CompletableFutureDemo implements Concept {
    @Override
    public String title() {
        return "CompletableFuture in Java";
    }

    @Override
    public String description() {
        return "Future<V> is blocking, but CompletableFuture lets you chain what happens when result arrives";
    }

    public String fetch(String s) {
        return s + " fetched.";
    }

    @Override
    public void run() {
        CompletableFuture<Integer> f = CompletableFuture
                .supplyAsync(() -> fetch("A"))
                .thenApply(String::length)
                .thenApply(len -> len * 2);
        System.out.println(f.join());

        CompletableFuture<String> a = CompletableFuture.supplyAsync(() -> fetch("A"));
        CompletableFuture<String> b = CompletableFuture.supplyAsync(() -> fetch("B"));

        a.thenCombine(b, (ra, rb) -> ra + ", " + rb)
                .thenAccept(System.out::println);
        CompletableFuture.allOf(a, b).join();

        // virtual threads
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 20; ++i) {
                int id = i;
                executor.execute(() -> System.out.println("task " + id + " on " + Thread.currentThread()));
            }
        }
    }
}
