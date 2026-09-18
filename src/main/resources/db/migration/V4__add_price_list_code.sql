-- Price lists (Service Catalog Pricing sub-phase B+): see hospital-administration-service's
-- equivalent migration for the full rationale — defaults every existing row to "STANDARD".
ALTER TABLE medication_price_versions ADD COLUMN price_list_code VARCHAR(50) NOT NULL DEFAULT 'STANDARD';
