-- v9 Delta Phase F: Pharmacy Stocking Module (v9 S24/S32) -- a pharmacy-
-- specific operational interface over a NEW batch/store model, since
-- hospital-pharmacy-service never had one (Medication.on_hand_quantity was
-- always just a running total, per that entity's own Javadoc). This is
-- pharmacy's OWN batch/lot system, separate from
-- hospital-medical-inventory-service's Product/Batch (AGENTS.md S20/S21:
-- pharmacy is a distinct bounded context from hospital-wide consumables --
-- no shared entities, no shared database, even though the shape mirrors it).
--
-- Deliberately scoped down from the full spec: supplier is a plain string
-- (no Supplier master-data entity, unlike medical-inventory-service's
-- fuller model); GRN and Supplier Return are multi-line (the true "bulk"
-- documents v9 emphasizes); Opening Balance and Stock Adjustment are
-- single-line per document (still real, still posted with a full audit
-- trail, just not bulk multi-item this pass).

CREATE SEQUENCE grn_number_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE supplier_return_number_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE opening_balance_number_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE stock_adjustment_number_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE pharmacy_stores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO pharmacy_stores (code, name) VALUES
    ('PRINCIPAL', 'Farmácia Principal'),
    ('AMBULATORIO', 'Farmácia Ambulatório');

-- The authoritative stock record: quantity_available per (medication, store, batch).
CREATE TABLE medication_batches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    medication_id UUID NOT NULL REFERENCES medications(id),
    store_id UUID NOT NULL REFERENCES pharmacy_stores(id),
    batch_number VARCHAR(100) NOT NULL,
    expiry_date DATE,
    quantity_available INTEGER NOT NULL DEFAULT 0,
    cost_price NUMERIC(12, 2),
    sale_price NUMERIC(12, 2),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (medication_id, store_id, batch_number)
);

CREATE INDEX idx_medication_batches_medication ON medication_batches(medication_id);
CREATE INDEX idx_medication_batches_store ON medication_batches(store_id);

-- The append-only ledger -- "the system must always be able to answer why
-- stock changed, which document caused it, who posted it ... the
-- before/after quantity" (v9 S32).
CREATE TABLE pharmacy_inventory_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_type VARCHAR(30) NOT NULL,
    medication_id UUID NOT NULL,
    store_id UUID NOT NULL,
    batch_number VARCHAR(100) NOT NULL,
    quantity INTEGER NOT NULL,
    quantity_before INTEGER NOT NULL,
    quantity_after INTEGER NOT NULL,
    reference_type VARCHAR(30),
    reference_id UUID,
    reason TEXT,
    staff_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_pharmacy_tx_medication ON pharmacy_inventory_transactions(medication_id);
CREATE INDEX idx_pharmacy_tx_reference ON pharmacy_inventory_transactions(reference_type, reference_id);

CREATE TABLE goods_received_notes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    grn_number VARCHAR(30) NOT NULL UNIQUE,
    store_id UUID NOT NULL REFERENCES pharmacy_stores(id),
    supplier_name VARCHAR(200) NOT NULL,
    receipt_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_by_staff_id UUID NOT NULL,
    posted_by_staff_id UUID,
    posted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE grn_lines (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    grn_id UUID NOT NULL REFERENCES goods_received_notes(id) ON DELETE CASCADE,
    medication_id UUID NOT NULL REFERENCES medications(id),
    batch_number VARCHAR(100) NOT NULL,
    expiry_date DATE,
    quantity INTEGER NOT NULL,
    free_quantity INTEGER NOT NULL DEFAULT 0,
    cost_price NUMERIC(12, 2) NOT NULL,
    sale_price NUMERIC(12, 2) NOT NULL
);

CREATE INDEX idx_grn_lines_grn ON grn_lines(grn_id);

CREATE TABLE supplier_returns (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    return_number VARCHAR(30) NOT NULL UNIQUE,
    original_grn_id UUID REFERENCES goods_received_notes(id),
    store_id UUID NOT NULL REFERENCES pharmacy_stores(id),
    return_date DATE NOT NULL,
    return_type VARCHAR(50),
    reason TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_by_staff_id UUID NOT NULL,
    posted_by_staff_id UUID,
    posted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE supplier_return_lines (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    supplier_return_id UUID NOT NULL REFERENCES supplier_returns(id) ON DELETE CASCADE,
    medication_id UUID NOT NULL REFERENCES medications(id),
    batch_number VARCHAR(100) NOT NULL,
    quantity INTEGER NOT NULL
);

CREATE INDEX idx_supplier_return_lines_return ON supplier_return_lines(supplier_return_id);

-- Elevated authorization, distinct transaction type, never disguised as a
-- fake supplier purchase (v9 S32).
CREATE TABLE opening_balances (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    balance_number VARCHAR(30) NOT NULL UNIQUE,
    store_id UUID NOT NULL REFERENCES pharmacy_stores(id),
    medication_id UUID NOT NULL REFERENCES medications(id),
    batch_number VARCHAR(100) NOT NULL,
    expiry_date DATE,
    quantity INTEGER NOT NULL,
    cost_price NUMERIC(12, 2),
    reason TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_by_staff_id UUID NOT NULL,
    posted_by_staff_id UUID,
    posted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Physical-vs-system reconciliation, two directions (v9 S32). Requires
-- approval before posting -- ordinary pharmacy users must not be able to
-- freely change stock.
CREATE TABLE stock_adjustments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    adjustment_number VARCHAR(30) NOT NULL UNIQUE,
    store_id UUID NOT NULL REFERENCES pharmacy_stores(id),
    medication_id UUID NOT NULL REFERENCES medications(id),
    batch_number VARCHAR(100) NOT NULL,
    direction VARCHAR(20) NOT NULL,
    quantity INTEGER NOT NULL,
    reason TEXT NOT NULL,
    quantity_before INTEGER,
    quantity_after INTEGER,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_by_staff_id UUID NOT NULL,
    approved_by_staff_id UUID,
    approved_at TIMESTAMPTZ,
    posted_by_staff_id UUID,
    posted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
