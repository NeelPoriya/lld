package in.neelporiya.stockbrokerage;

/**
 * // DESIGN PATTERN: Observer — trade confirmations and rejections are pushed to subscribers (email,
 * audit log, UI) without the brokerage depending on any of them.
 */
public interface OrderListener {

    default void onFilled(Order order) {
    }

    default void onRejected(Order order, String reason) {
    }

    default void onCancelled(Order order) {
    }
}
