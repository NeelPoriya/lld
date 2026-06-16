package in.neelporiya.phases.phase12concurrency;

import java.util.concurrent.locks.ReentrantLock;

public class BackAccountDemo {
    static final int THREADS = 5;
    static final int OPS = 1000;

    static class BankAccount {
        private long balance = 0;
        // TODO: add a ReentrantLock to guard the balance
        private static final ReentrantLock lock = new ReentrantLock();

        public void deposit(long amount) {
            // TODO: lock before updating, unlock after
            lock.lock();
            try {
                balance += amount;
            } catch (Exception ex) {
                System.out.println("Caught: " + ex.getMessage());
            } finally {
                lock.unlock();
            }
        }
        public void withdraw(long amount) {
            // TODO: lock before updating, unlock after
            lock.lock();
            try {
                balance -= amount;
            } catch (Exception ex) {
                System.out.println("caught: " + ex.getMessage());
            } finally {
                lock.unlock();
            }
        }
        public long getBalance() { return balance; }
    }

    public static void main(String[] args) throws InterruptedException {
        BankAccount account = new BankAccount();
        Thread[] threads = new Thread[THREADS];
        for (int t = 0; t < THREADS; t++) {
            threads[t] = new Thread(() -> {
                for (int i = 0; i < OPS; i++) {
                    account.deposit(300);
                    account.withdraw(100);
                }
            });
        }
        for (Thread th : threads) {
            th.start();
        }
        for (Thread th : threads) {
            th.join();
        }
        System.out.println(account.getBalance());
    }
}