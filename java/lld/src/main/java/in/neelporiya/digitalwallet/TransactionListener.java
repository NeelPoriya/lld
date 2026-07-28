package in.neelporiya.digitalwallet;

/**
 * // DESIGN PATTERN: Observer — fraud checks, notifications and metrics can react to every
 * transaction without the wallet/service knowing about them.
 */
@FunctionalInterface
public interface TransactionListener {
    void onTransaction(WalletTransaction transaction);
}
