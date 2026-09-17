-- Effective-dated price versioning (Service Catalog Pricing, sub-phase A).
-- No existing price to backfill -- medications never had a price column at
-- all, so every catalog item starts with zero versions (honestly "not yet
-- priced") until an administrator sets one.
CREATE TABLE medication_price_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    medication_id UUID NOT NULL REFERENCES medications(id),
    price NUMERIC(12, 2) NOT NULL,
    effective_from DATE NOT NULL,
    effective_to DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_medication_price_versions_medication ON medication_price_versions(medication_id);
