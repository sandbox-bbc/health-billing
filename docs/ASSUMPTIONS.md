# Documented Assumptions

This document lists assumptions made during the implementation of the Healthcare Billing System.

## Technical Assumptions

### Storage
- **In-memory storage only** - Per assignment requirement: "without needing a database or external APIs"
- Uses `ConcurrentHashMap` for thread-safe operations
- Data is lost on application restart (acceptable for this assignment)

### Technology Choices
- **JDK 21** - Current LTS version, stable and widely supported
- **Kotlin 1.9.25** - Official Micronaut 4.x baseline (Kotlin 2.x support planned for Micronaut 5.x)
- **Micronaut 4.10.7** - Latest stable release at time of development

## Business Assumptions

### Patient
- Insurance information is **always provided** with patient creation
- Per problem statement: "patient already has valid insurance"

### Doctor
- Doctor information is **immutable** after creation
- Problem statement uses "capture" which implies one-time entry
- If correction is needed, delete and recreate the record
- Fields that don't change: NPI number, specialty, practice start date

### Appointments
- Only **COMPLETED** appointments count toward loyalty discount
- Billing can only be generated for COMPLETE appointments
- Attempting to bill a non-complete appointment returns 400 error

### Billing
- Bills are calculated **per appointment**, not in bulk
- Per problem statement: "particular consultation" (singular)
- If a bill already exists for an appointment, return the existing bill (idempotent)

### Discount Calculation
- Discount = `min(priorCompletedAppointments, 10)%`
- "Prior" means appointments completed **before** the current one
- Current appointment being billed is NOT counted in prior count

### Specialties
- Only ORTHO and CARDIO specialties are implemented
- Per problem statement: "assume more specialties as needed" - keeping simple for this scope

## Out of Scope

The following are explicitly NOT implemented:

1. **Security/Authentication** - APIs are assumed to be internal/trusted
2. **Audit Logging** - No tracking of who made what changes
3. **Metrics/Tracing** - Health endpoint only, no advanced observability
4. **Database** - In-memory only, per assignment requirement
5. **Pagination** - List endpoints return all records
6. **Search/Filtering** - Basic lookups only
