package mz.mva.pharmacy.domain;

/** v9 §32: "ISSUE = system stock is higher than physical stock; RECEIVE = physical stock is higher than system stock." */
public enum AdjustmentDirection {
    ISSUE,
    RECEIVE
}
