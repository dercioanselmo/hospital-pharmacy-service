-- No PrescriptionOrder is seeded here — creating one requires a live claim
-- call to hospital-clinical-service, which a migration can't make; that's
-- demonstrated via the API during verification instead (same as
-- hospital-laboratory-service's and hospital-radiology-service's seeds).
INSERT INTO medications (code, name, form, strength, on_hand_quantity)
VALUES
    ('PARA-500', 'Paracetamol', 'TABLET', '500mg', 200),
    ('AMOX-250', 'Amoxicilina', 'CAPSULE', '250mg', 100);
