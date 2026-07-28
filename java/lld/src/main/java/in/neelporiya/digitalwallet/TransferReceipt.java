package in.neelporiya.digitalwallet;

/** The two legs of a completed transfer. */
public record TransferReceipt(String reference, WalletTransaction out, WalletTransaction in) {
}
