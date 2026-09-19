package mz.mva.pharmacy.domain;

/**
 * Shared save-then-post lifecycle for GRN/Supplier Return/Opening Balance (v9 §32: "no stock
 * change on save-only operations... posting is explicit and idempotent"). Stock Adjustment adds an
 * APPROVED state in between (see {@link StockAdjustment}), so it does not reuse this enum.
 */
public enum DocumentStatus {
    DRAFT,
    POSTED,
    CANCELLED
}
