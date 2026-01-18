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
- **Age is calculated from DOB**, not stored separately
- API returns calculated age in responses for convenience
- Future enhancement: could accept age at creation and convert to DOB (not implementing now)

### Doctor
- Doctor information is **immutable** after creation
- Problem statement uses "capture" which implies one-time entry
- If correction is needed, delete and recreate the record
- Fields that don't change: NPI number, specialty, practice start date

### Experience Brackets
- Experience calculated from `practiceStartDate` to current date
- **0-19 years**: `experienceYears < 20`
- **20-30 years**: `experienceYears >= 20 && experienceYears <= 30`
- **31+ years**: `experienceYears >= 31`

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
- "Prior" means COMPLETED appointments **excluding** the current one being billed
- Current appointment does NOT contribute to its own discount
- Example: 1st visit = 0%, 2nd visit = 1%, 11th visit = 10% (capped)

### Specialties
- Only ORTHO and CARDIO specialties are implemented
- Per problem statement: "assume more specialties as needed" - keeping simple for this scope

### Deletion Rules
- **Cannot delete Doctor** with existing appointments (returns 409 Conflict)
- **Cannot delete Patient** with existing appointments (returns 409 Conflict)
- Assignment focuses on billing; complex cascade/orphan handling is out of scope

### Validation
- Basic required field checks only
- No strict format validation for NPI, BIN, PCN (treated as strings)
- DOB must not be in the future

## Development Approach

### Testing Strategy (Hybrid)

**Philosophy:** Test *behavior and business rules*, not trivial code.

| Layer | Tested? | Rationale |
|-------|---------|-----------|
| Domain models | ❌ No | Data classes with no logic - nothing to verify |
| Repositories | ❌ No | Thin wrappers over `ConcurrentHashMap` - testing Java's map |
| Fee Strategies | ✅ Yes | Business rules (fee brackets by experience) |
| BillingCalculator | ✅ Yes | Core logic (discount, GST, insurance split) |
| BillingService | ✅ Yes | Validation + orchestration of billing flow |
| AppointmentService | ✅ Partial | Status transitions (state machine logic) |
| PatientService | ❌ No | Simple CRUD + delete constraint (similar to Doctor) |
| DoctorService | ❌ No | Simple CRUD + NPI check (covered by integration) |
| Controllers | ❌ No | HTTP layer tested manually; business logic covered by service tests |

**Why not 100% coverage?**
- Assignment focuses on **billing calculation** - that's where bugs matter most
- CRUD operations are straightforward - low bug risk
- Integration tests cover happy paths end-to-end
- Time is better spent on **meaningful tests** than hitting coverage metrics

## Out of Scope

The following are explicitly NOT implemented:

1. **Security/Authentication** - APIs are assumed to be internal/trusted
2. **Audit Logging** - No tracking of who made what changes
3. **Metrics/Tracing** - Health endpoint only, no advanced observability
4. **Database** - In-memory only, per assignment requirement
5. **Pagination** - List endpoints return all records
6. **Search/Filtering** - Basic lookups only
7. **Payment Processing** - This system calculates bills only; actual payment collection, payment gateway integration, and payment status tracking are not implemented. The bill shows what is owed (co-pay) and what insurance covers, but doesn't process transactions.
