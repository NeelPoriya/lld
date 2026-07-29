package in.neelporiya.onlineshopping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/** Stock per product, with atomic multi-item reservation for checkout. */
public class Inventory {

    private final Map<String, Stock> stockByProductId = new ConcurrentHashMap<>();

    public void addStock(String productId, int quantity) {
        Objects.requireNonNull(productId, "productId");
        if (quantity < 0) {
            throw new IllegalArgumentException("quantity must be >= 0");
        }
        Stock stock = stockByProductId.computeIfAbsent(productId, ignored -> new Stock());
        stock.lock.lock();
        try {
            stock.quantity += quantity;
        } finally {
            stock.lock.unlock();
        }
    }

    /**
     * Atomically reserves every requested product, or reserves nothing.
     *
     * <p>// CONCURRENCY: checkout's crux is "all-or-nothing" stock decrement. We lock only the
     * requested product rows, always in sorted product-id order to avoid deadlock. Phase 1 checks every
     * quantity while all locks are held; Phase 2 decrements only after the whole cart is known to be
     * available. Therefore 100 buyers racing for the last unit can never oversell or leave partial
     * reservations behind.
     */
    public boolean tryReserve(Map<String, Integer> requested) {
        Objects.requireNonNull(requested, "requested");
        List<String> productIds = sortedProductIds(requested);
        List<Stock> locked = lockAll(productIds);
        try {
            for (String productId : productIds) {
                int need = requested.get(productId);
                if (need <= 0) {
                    throw new IllegalArgumentException("requested quantity must be > 0");
                }
                if (stockByProductId.get(productId).quantity < need) {
                    return false;
                }
            }
            for (String productId : productIds) {
                stockByProductId.get(productId).quantity -= requested.get(productId);
            }
            return true;
        } finally {
            unlockReverse(locked);
        }
    }

    public void restock(Map<String, Integer> quantities) {
        Objects.requireNonNull(quantities, "quantities");
        List<String> productIds = sortedProductIds(quantities);
        List<Stock> locked = lockAll(productIds);
        try {
            for (String productId : productIds) {
                int quantity = quantities.get(productId);
                if (quantity <= 0) {
                    throw new IllegalArgumentException("restock quantity must be > 0");
                }
                stockByProductId.get(productId).quantity += quantity;
            }
        } finally {
            unlockReverse(locked);
        }
    }

    public int quantityOf(String productId) {
        Stock stock = stockByProductId.computeIfAbsent(productId, ignored -> new Stock());
        stock.lock.lock();
        try {
            return stock.quantity;
        } finally {
            stock.lock.unlock();
        }
    }

    private List<String> sortedProductIds(Map<String, Integer> quantities) {
        List<String> productIds = new ArrayList<>(quantities.keySet());
        Collections.sort(productIds);
        return productIds;
    }

    private List<Stock> lockAll(List<String> productIds) {
        List<Stock> locked = new ArrayList<>();
        for (String productId : productIds) {
            Stock stock = stockByProductId.computeIfAbsent(productId, ignored -> new Stock());
            stock.lock.lock();
            locked.add(stock);
        }
        return locked;
    }

    private void unlockReverse(List<Stock> locked) {
        for (int i = locked.size() - 1; i >= 0; i--) {
            locked.get(i).lock.unlock();
        }
    }

    private static final class Stock {
        private final ReentrantLock lock = new ReentrantLock();
        private int quantity;
    }
}
