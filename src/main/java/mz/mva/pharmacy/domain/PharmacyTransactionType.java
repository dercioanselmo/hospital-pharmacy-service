package mz.mva.pharmacy.domain;

/**
 * v9 §32: "the following business operations must remain identifiable even when mapped to generic
 * inventory transaction categories." One type per posting action across the four document types.
 */
public enum PharmacyTransactionType {
    GRN_RECEIPT,
    OPENING_BALANCE,
    SUPPLIER_RETURN,
    STOCK_ADJUSTMENT_ISSUE,
    STOCK_ADJUSTMENT_RECEIVE
}
