package in.neelporiya.phases.phase12concurrency;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

// This is the demo for condition variable...
public class PizzaShopSimulation {
    public static class PizzaShop {
        private int pizza = 0;
        private int MAX_PIZZAS = 1;

        ReentrantLock kitchenKey = new ReentrantLock();
        Condition pizzaReadyCondition = kitchenKey.newCondition();
        Condition counterEmptyCondition = kitchenKey.newCondition();

        public void bakePizza() {
            kitchenKey.lock();

            try {
                while (pizza == MAX_PIZZAS) {
                    // wait for counter to become empty
                    counterEmptyCondition.await();
                }

                System.out.println("[Chef] Baking a delicious pizza, and putting it on counter");
                Thread.sleep(2000);
                pizza++;
                pizzaReadyCondition.signal();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                kitchenKey.unlock();
            }
        }

        public void deliverPizza() {
            kitchenKey.lock();

            try {
                while (pizza == 0) {
                    // wait for pizza to be ready
                    pizzaReadyCondition.await();
                }
                System.out.println("[Delivery Guy] Delivering the pizza...");
                Thread.sleep(2000);
                pizza--;
                counterEmptyCondition.signal();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                kitchenKey.unlock();
            }
        }
    }

    static void main() throws InterruptedException {
        PizzaShop shop = new PizzaShop();

        Thread deliveryGuy = new Thread(() -> {
            for (int i = 0; i < 3; ++i) {
                shop.deliverPizza();
            }
        });

        Thread chef = new Thread(() -> {
            for (int i = 0; i < 3; ++i) {
                shop.bakePizza();
            }
        });

        deliveryGuy.start();

        try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        chef.start();

        deliveryGuy.join();
        chef.join();
    }
}
