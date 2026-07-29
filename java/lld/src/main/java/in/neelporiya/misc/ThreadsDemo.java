package in.neelporiya.misc;

public class ThreadsDemo {
    public static void testThreads() throws InterruptedException {
        Thread okayThisIsCool = Thread.ofVirtual().start(() -> System.out.println("Okay this is cool"));
        okayThisIsCool.join();
    }
}
