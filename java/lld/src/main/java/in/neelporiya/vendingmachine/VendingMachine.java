package in.neelporiya.vendingmachine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

/**
 * // DESIGN PATTERN: Facade — a single API over state, inventory, payment and change-making.
 *
 * <p>// DESIGN PATTERN: State — public methods delegate to {@link VendingMachineState}; legal
 * behavior changes by transitioning between Idle, HasMoney, Dispense and OutOfStock states.
 *
 * <p>// CONCURRENCY: The machine is physically single-user, but tests can still race method calls.
 * A single {@link ReentrantLock} guards the whole transaction commit: state transition + inventory
 * decrement + change computation + cash-box update. Without this, two concurrent {@code selectProduct}
 * calls could both observe quantity=1 and oversell the last unit.
 */
public class VendingMachine {

    private final Map<String, InventoryItem> inventory;
    private final ChangeStrategy changeStrategy;
    private final ReentrantLock lock = new ReentrantLock();
    private final List<Denomination> insertedMoney = new ArrayList<>(); // guarded by lock
    private final Map<Denomination, Integer> cashBox;                   // guarded by lock

    private VendingMachineState state;
    private Product selectedProduct;
    private int collectedBalanceCents;

    private VendingMachine(Builder builder) {
        this.inventory = new LinkedHashMap<>(builder.inventory);
        this.changeStrategy = builder.changeStrategy;
        this.cashBox = new HashMap<>(builder.cashBox);
        this.state = hasAnyStock() ? IdleState.INSTANCE : OutOfStockState.INSTANCE;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void insertMoney(Denomination money) {
        Objects.requireNonNull(money, "money");
        lock.lock();
        try {
            state.insertMoney(this, money);
        } finally {
            lock.unlock();
        }
    }

    public void insertCoin(Coin coin) {
        insertMoney(coin);
    }

    public void insertNote(Note note) {
        insertMoney(note);
    }

    public DispenseResult selectProduct(String code) {
        Objects.requireNonNull(code, "code");
        lock.lock();
        try {
            return state.selectProduct(this, code);
        } finally {
            lock.unlock();
        }
    }

    public DispenseResult dispense() {
        lock.lock();
        try {
            return state.dispense(this);
        } finally {
            lock.unlock();
        }
    }

    public RefundResult refund() {
        lock.lock();
        try {
            return state.refund(this);
        } finally {
            lock.unlock();
        }
    }

    public String getStateName() {
        lock.lock();
        try {
            return state.name();
        } finally {
            lock.unlock();
        }
    }

    public int getInsertedBalanceCents() {
        lock.lock();
        try {
            return insertedBalanceCents();
        } finally {
            lock.unlock();
        }
    }

    public int getCollectedBalanceCents() {
        lock.lock();
        try {
            return collectedBalanceCents;
        } finally {
            lock.unlock();
        }
    }

    public int getQuantity(String code) {
        lock.lock();
        try {
            return requireProduct(code) == null ? 0 : inventory.get(code).getQuantity();
        } finally {
            lock.unlock();
        }
    }

    public int getCashCount(Denomination denomination) {
        lock.lock();
        try {
            return cashBox.getOrDefault(denomination, 0);
        } finally {
            lock.unlock();
        }
    }

    public Optional<Product> findProduct(String code) {
        lock.lock();
        try {
            InventoryItem item = inventory.get(code);
            return item == null ? Optional.empty() : Optional.of(item.getProduct());
        } finally {
            lock.unlock();
        }
    }

    public List<InventoryItem> inventorySnapshot() {
        lock.lock();
        try {
            return inventory.values().stream()
                    .map(item -> new InventoryItem(item.getProduct(), item.getQuantity()))
                    .toList();
        } finally {
            lock.unlock();
        }
    }

    void addInsertedMoney(Denomination money) {
        insertedMoney.add(money);
    }

    int insertedBalanceCents() {
        return insertedMoney.stream().mapToInt(Denomination::cents).sum();
    }

    List<Denomination> drainInsertedMoney() {
        List<Denomination> refund = List.copyOf(insertedMoney);
        insertedMoney.clear();
        return refund;
    }

    Product requireProduct(String code) {
        InventoryItem item = inventory.get(code);
        if (item == null) {
            throw new InvalidSelectionException("Unknown product code: " + code);
        }
        return item.getProduct();
    }

    InventoryItem itemFor(String code) {
        return inventory.get(code);
    }

    void setSelectedProduct(Product selectedProduct) {
        this.selectedProduct = selectedProduct;
    }

    void transitionTo(VendingMachineState nextState) {
        this.state = Objects.requireNonNull(nextState, "nextState");
    }

    boolean hasAnyStock() {
        return inventory.values().stream().anyMatch(InventoryItem::isInStock);
    }

    DispenseResult completeDispense() {
        Product product = Objects.requireNonNull(selectedProduct, "selectedProduct");
        InventoryItem item = itemFor(product.code());
        if (!item.isInStock()) {
            throw new OutOfStockException("Product is out of stock: " + product.code());
        }

        int paid = insertedBalanceCents();
        if (paid < product.priceCents()) {
            throw new InsufficientFundsException("Need " + product.priceCents()
                    + " cents but only " + paid + " inserted");
        }

        int changeDue = paid - product.priceCents();
        Map<Denomination, Integer> availableCash = cashBoxPlusInsertedMoney();
        List<Denomination> change = changeStrategy.makeChange(changeDue, availableCash);

        item.decrement();
        cashBox.clear();
        cashBox.putAll(availableCash);
        change.forEach(denomination -> cashBox.compute(denomination, (key, count) -> count == null ? 0 : count - 1));
        insertedMoney.clear();
        selectedProduct = null;
        collectedBalanceCents += product.priceCents();
        transitionTo(hasAnyStock() ? IdleState.INSTANCE : OutOfStockState.INSTANCE);
        return new DispenseResult(product, change, paid);
    }

    private Map<Denomination, Integer> cashBoxPlusInsertedMoney() {
        Map<Denomination, Integer> available = new HashMap<>(cashBox);
        insertedMoney.forEach(denomination -> available.merge(denomination, 1, Integer::sum));
        return available;
    }

    /**
     * // DESIGN PATTERN: Builder — readable setup of catalog, initial change float and injected
     * strategy. // TESTABILITY: tests inject a deterministic {@link ChangeStrategy} if needed.
     */
    public static final class Builder {
        private final Map<String, InventoryItem> inventory = new LinkedHashMap<>();
        private final Map<Denomination, Integer> cashBox = new HashMap<>();
        private ChangeStrategy changeStrategy = new GreedyChangeStrategy();

        public Builder addProduct(String code, String name, int priceCents, int quantity) {
            return addProduct(new Product(code, name, priceCents), quantity);
        }

        public Builder addProduct(Product product, int quantity) {
            Objects.requireNonNull(product, "product");
            if (inventory.containsKey(product.code())) {
                throw new IllegalArgumentException("Duplicate product code: " + product.code());
            }
            inventory.put(product.code(), new InventoryItem(product, quantity));
            return this;
        }

        public Builder addChange(Denomination denomination, int count) {
            Objects.requireNonNull(denomination, "denomination");
            if (count < 0) {
                throw new IllegalArgumentException("Cash count cannot be negative");
            }
            cashBox.merge(denomination, count, Integer::sum);
            return this;
        }

        public Builder changeStrategy(ChangeStrategy changeStrategy) {
            this.changeStrategy = Objects.requireNonNull(changeStrategy, "changeStrategy");
            return this;
        }

        public VendingMachine build() {
            return new VendingMachine(this);
        }
    }
}
