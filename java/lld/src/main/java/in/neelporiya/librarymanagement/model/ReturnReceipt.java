package in.neelporiya.librarymanagement.model;

import java.time.Instant;

public record ReturnReceipt(String loanId, String barcode, Instant returnedAt, long fineCents) {
}
