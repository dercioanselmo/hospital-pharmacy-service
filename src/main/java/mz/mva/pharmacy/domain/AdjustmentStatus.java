package mz.mva.pharmacy.domain;

/** Adds an APPROVED gate before posting, on top of GRN/Return/OpeningBalance's plain DRAFT->POSTED (v9 §32). */
public enum AdjustmentStatus {
    DRAFT,
    APPROVED,
    POSTED,
    CANCELLED
}
