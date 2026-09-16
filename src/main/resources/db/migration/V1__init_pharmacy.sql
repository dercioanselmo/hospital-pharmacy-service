CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- on_hand_quantity is a simple running total, not real batch/lot inventory
-- (deferred to a future Phase 5b — see docs/development-plan.md).
CREATE TABLE medications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    form VARCHAR(20) NOT NULL,
    strength VARCHAR(50) NOT NULL,
    on_hand_quantity INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- clinical_order_id references hospital-clinical-service's clinical_orders.id;
-- encounter_id references its encounters.id; patient_id references
-- hospital-patient-service's patients.id; ordering_staff_id/received_by_staff_id/
-- verified_by_staff_id reference hospital-staff-service's staff_members.id —
-- ALL BY ID ONLY, no cross-service FK.
CREATE TABLE prescription_orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    clinical_order_id UUID NOT NULL UNIQUE,
    encounter_id UUID NOT NULL,
    patient_id UUID NOT NULL,
    ordering_staff_id UUID NOT NULL,
    medication_id UUID NOT NULL REFERENCES medications(id),
    quantity_prescribed INTEGER NOT NULL,
    dosage_instructions TEXT NOT NULL,
    received_by_staff_id UUID NOT NULL,
    verified_by_staff_id UUID,
    status VARCHAR(20) NOT NULL DEFAULT 'RECEIVED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_prescription_orders_status ON prescription_orders(status);
CREATE INDEX idx_prescription_orders_patient ON prescription_orders(patient_id);

CREATE TABLE dispensing_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    prescription_order_id UUID NOT NULL REFERENCES prescription_orders(id) ON DELETE CASCADE,
    quantity_dispensed INTEGER NOT NULL,
    dispensed_by_staff_id UUID NOT NULL,
    dispensed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_by_staff_id UUID,
    completed_at TIMESTAMPTZ
);

CREATE INDEX idx_dispensing_records_order ON dispensing_records(prescription_order_id);
