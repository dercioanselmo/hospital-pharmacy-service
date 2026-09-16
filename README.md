# hospital-pharmacy-service

The third receiving department — mirrors `hospital-laboratory-service` (Phase 4a) and `hospital-radiology-service` (Phase 4b) exactly. Owns the medication catalog, prescription fulfillment, and dispensing. Fulfills orders that `hospital-clinical-service` routes to it — Clinical owns order routing/status, Pharmacy owns fulfillment detail (see `hospital-platform-docs/docs/service-boundaries.md`).

## Prerequisites

```bash
createdb mva-pharmacy
```

## Run locally

```bash
mvn spring-boot:run
```

Flyway seeds a small medication catalog ("Paracetamol 500mg", "Amoxicilina 250mg").

## The Work Queue integration

Same pattern as Laboratory/Radiology: `mz.mva.pharmacy.client.ClinicalOrderClient` calls `hospital-clinical-service` directly via Eureka discovery (bypassing the gateway), forwarding the caller's own bearer token.

```text
Doctor places a PRESCRIPTION ClinicalOrder targeting this department (in Clinical)
      ↓
GET  /api/clinical/orders?targetDepartmentId=<pharmacy-dept>&status=PLACED   (Clinical's queue view)
      ↓
POST /prescription-orders/receive {clinicalOrderId, staffId, medicationId, quantityPrescribed, dosageInstructions}
      → calls Clinical: POST /orders/{id}/claim   (PLACED -> ACCEPTED)
      → creates a local PrescriptionOrder (status RECEIVED)
      ↓
POST /prescription-orders/{id}/verify {staffId}                          (this service)
      → calls Clinical: POST /orders/{id}/ready   (ACCEPTED -> READY)
      → PrescriptionOrder -> VERIFIED (pharmacist confirmed dosage/safety)
      ↓
POST /prescription-orders/{id}/start-dispensing                          (this service)
      → calls Clinical: POST /orders/{id}/start   (READY -> IN_PROGRESS)
      → PrescriptionOrder -> DISPENSING (being prepared/counted)
      ↓
POST /prescription-orders/{id}/dispensing-records {quantityDispensed, staffId}   (this service)
      → decrements Medication.onHandQuantity (409 INSUFFICIENT_STOCK if not enough)
      → PrescriptionOrder -> DISPENSED (Clinical untouched — completion is separate)
      ↓
POST /dispensing-records/{id}/complete {staffId}                         (this service)
      → calls Clinical: POST /orders/{id}/complete (IN_PROGRESS -> COMPLETED)
      → PrescriptionOrder -> COMPLETED
```

If the call to Clinical fails, the whole action fails with `502 CLINICAL_SERVICE_UNAVAILABLE`.

## Design note: inventory

`Medication.onHandQuantity` is a simple running total decremented on dispensing — not real batch/lot inventory. Full Medical Inventory (product catalog, suppliers, batches, expiration, FEFO issuing, transfers, consumption/returns per AGENTS.md §20-30) is deferred to a future Phase 5b, same reasoning as Laboratory/Radiology deferring PACS/DICOM and shared catalogs. Dispensing more than what's on hand fails with `409 INSUFFICIENT_STOCK` — a genuine business rule, not a placeholder.

## Endpoints

All require `Authorization: Bearer <token>`.

- `GET/POST /medications` — `PHARMACY_ORDER_VIEW` / `PHARMACY_MEDICATION_MANAGE`
- `GET /prescription-orders?status=`, `GET /prescription-orders/patient/{patientId}`, `GET /prescription-orders/{id}` — `PHARMACY_ORDER_VIEW`
- `POST /prescription-orders/receive` — `PHARMACY_ORDER_RECEIVE`
- `POST /prescription-orders/{id}/verify` — `PHARMACY_ORDER_VERIFY`
- `POST /prescription-orders/{id}/start-dispensing` — `PHARMACY_DISPENSE`
- `GET/POST /prescription-orders/{id}/dispensing-records` — `PHARMACY_ORDER_VIEW` / `PHARMACY_DISPENSE`
- `POST /dispensing-records/{id}/complete` — `PHARMACY_DISPENSE_COMPLETE`

## Tests

```bash
mvn test
```

`ClinicalOrderClient` is mocked in integration tests (`@MockBean`) — this service's own test suite never makes a real network call to Clinical.
# hospital-pharmacy-service
