# Design Decisions (Architecture Decision Records)

This document records significant design decisions with context and rationale.

---

## ADR-001: Insurance Information Storage Strategy

**Date:** January 2026  
**Status:** Accepted

### Context

The system needs to store patient insurance information (BIN No., PCN No., Member ID). We needed to decide how to model this data in relation to the Patient entity.

### Options Considered

#### Option A: Flat Structure
All insurance fields directly on Patient class.

```kotlin
data class Patient(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val dob: LocalDate,
    val insuranceBinNo: String,
    val insurancePcnNo: String,
    val insuranceMemberId: String
)
```

**Pros:**
- Simplest implementation
- Single class to maintain
- Flat JSON structure

**Cons:**
- Mixes domain concerns (patient identity vs insurance)
- Fields scattered across class
- Harder to validate insurance as a unit
- Less reusable (can't pass insurance info separately)
- Gets messy if insurance fields grow

#### Option B: Nested Object (Embedded)
Insurance as a separate data class, embedded within Patient.

```kotlin
data class InsuranceInfo(
    val binNo: String,
    val pcnNo: String,
    val memberId: String
)

data class Patient(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val dob: LocalDate,
    val insurance: InsuranceInfo
)
```

**Pros:**
- Clear domain grouping
- Easy to validate insurance as unit
- Reusable (can pass InsuranceInfo to functions)
- Clean JSON structure with logical nesting
- Easy to extend insurance fields later
- Shows understanding of domain modeling

**Cons:**
- Slightly more code (one extra class)
- Nested JSON structure (minor)

#### Option C: Separate Entity
Insurance as independent entity with its own ID and lifecycle.

```kotlin
data class InsuranceInfo(
    val id: UUID,
    val patientId: UUID,
    val binNo: String,
    val pcnNo: String,
    val memberId: String
)
```

**Pros:**
- Complete separation of concerns
- Supports multiple insurance per patient
- Full CRUD independence
- Audit trail per insurance record

**Cons:**
- Overkill for this requirement (1:1 relationship)
- More complex API (separate endpoints)
- Extra join logic needed
- Problem statement treats it as part of patient

### Decision

**Chosen: Option B - Nested Object (Embedded)**

### Rationale

1. **Domain Clarity:** Insurance information is conceptually a cohesive unit. The problem statement even groups these three fields together as "Patient Insurance Info has 3 components".

2. **Validation:** Can validate all insurance fields together:
   ```kotlin
   fun validateInsurance(insurance: InsuranceInfo) {
       require(insurance.binNo.length == 6) { "BIN must be 6 digits" }
       require(insurance.memberId.isNotBlank()) { "Member ID required" }
   }
   ```

3. **Reusability:** Can pass `InsuranceInfo` to functions without passing entire Patient:
   ```kotlin
   fun verifyWithInsuranceProvider(insurance: InsuranceInfo): Boolean
   fun formatInsuranceCard(insurance: InsuranceInfo): String
   ```

4. **Future-Proof:** If we need to add fields (group number, plan type), they logically belong in InsuranceInfo, not cluttering Patient.

5. **Balance:** Option B provides structure without the complexity of Option C. Since insurance doesn't have independent lifecycle (created/deleted with patient), a separate entity is unnecessary.

6. **API Clarity:** JSON structure makes the domain model obvious:
   ```json
   {
     "id": "...",
     "firstName": "John",
     "lastName": "Doe",
     "dob": "01/15/1985",
     "insurance": {
       "binNo": "123456",
       "pcnNo": "PCN001",
       "memberId": "MEM123"
     }
   }
   ```

### Consequences

- Patient and InsuranceInfo are created/updated/deleted together
- No separate insurance endpoint needed
- Insurance is always present (per problem statement: "patient has valid insurance")

---

## ADR-002: Doctor Immutability

**Date:** January 2026  
**Status:** Accepted

### Context

Need to decide whether Doctor records should be updatable after creation.

### Options Considered

1. **Full CRUD** - Allow updates to all fields
2. **Partial Updates** - Allow some fields to be updated
3. **Immutable** - No updates allowed (delete + recreate if needed)

### Decision

**Chosen: Immutable (Option 3)**

### Rationale

1. **Problem Statement Language:** Uses "capture" for doctor info, implying one-time entry, not ongoing maintenance.

2. **Business Logic:** Key fields shouldn't change:
   - NPI Number: Regulatory identifier, never changes
   - Specialty: Doctors don't typically change specialties
   - Practice Start Date: Historical fact

3. **Simplicity:** One less endpoint to implement and test.

4. **Data Integrity:** Prevents accidental modifications to billing-critical data.

### Consequences

- If correction needed, delete and recreate the doctor record
- Appointments referencing a doctor would need handling if doctor is deleted
- For this assignment scope, we assume doctors aren't deleted while having appointments

---

## ADR-003: Per-Appointment Billing (Not Bulk)

**Date:** January 2026  
**Status:** Accepted

### Context

Need to decide whether billing should be calculated for one appointment at a time or in bulk.

### Options Considered

1. **Bulk Billing** - Calculate bills for all unbilled appointments at once
2. **Per-Appointment** - Calculate bill for single appointment

### Decision

**Chosen: Per-Appointment (Option 2)**

### Rationale

1. **Problem Statement:** Uses singular language: "bill for a particular consultation"

2. **Simplicity:** Easier to understand, implement, and test.

3. **Error Handling:** If one calculation fails, others aren't affected.

4. **Real-World Pattern:** Billing typically happens right after consultation, not in batch.

5. **Idempotency:** Can easily make endpoint idempotent (return existing bill if already generated).

### Consequences

- Endpoint: `POST /appointments/{id}/bill` (not `POST /billing/generate-all`)
- If bulk needed later, can add as separate endpoint
- Each billing request triggers one calculation

---

## ADR-004: UUID for Entity Identifiers

**Date:** January 2026  
**Status:** Accepted

### Context

Need to decide on ID generation strategy for entities.

### Options Considered

1. **Auto-increment Integer** - Sequential numbers (1, 2, 3...)
2. **UUID** - Universally unique identifiers
3. **Custom ID** - Business-specific format (e.g., PAT-001)

### Decision

**Chosen: UUID (Option 2)**

### Rationale

1. **No Collisions:** Safe for distributed systems and concurrent creation.

2. **Security:** Non-sequential IDs don't reveal business information (e.g., "you're patient #3").

3. **Standard:** Widely understood and supported by all tooling.

4. **Kotlin Support:** Built-in `java.util.UUID` class.

5. **Future-Proof:** Works if system ever scales to multiple instances.

### Consequences

- IDs are longer in URLs and JSON
- No natural ordering by ID
- Acceptable trade-offs for the benefits

---

## ADR-005: BigDecimal for Monetary Values

**Date:** January 2026  
**Status:** Accepted

### Context

Need to decide how to represent money in the billing system.

### Options Considered

1. **Double/Float** - Native floating point
2. **BigDecimal** - Arbitrary precision decimal
3. **Long (cents)** - Store as integer cents

### Decision

**Chosen: BigDecimal (Option 2)**

### Rationale

1. **Precision:** Floating point has rounding errors:
   ```kotlin
   // Double problem
   val result = 0.1 + 0.2  // = 0.30000000000000004
   
   // BigDecimal correct
   val result = BigDecimal("0.1") + BigDecimal("0.2")  // = 0.3
   ```

2. **Industry Standard:** Financial systems universally use BigDecimal or similar.

3. **Clear Intent:** Code explicitly shows we care about precision.

4. **Calculation Support:** Built-in rounding modes, scale control.

### Consequences

- Slightly more verbose than primitives
- Must use `BigDecimal("value")` not `BigDecimal(0.1)` to avoid double conversion
- JSON serialization handled automatically by Jackson

---

## ADR-006: Appointment Status and Discount Timing

**Date:** January 2026  
**Status:** Accepted

### Context

The system needs to track appointment lifecycle and calculate loyalty discounts based on "prior visits." A critical question arose: when an appointment is marked COMPLETED and then billed, should it count toward its own discount?

**The Problem:**
```
Patient has 3 prior COMPLETED appointments.
4th appointment happens → marked COMPLETED → bill generated.

Question: Is discount 3% (prior only) or 4% (including current)?
```

### Options Considered

#### Option A: Simple Statuses (3 states)
```
SCHEDULED → COMPLETED
         ↘ CANCELLED
```
- Exclude current appointment in discount calculation via code
- Bill existence proves billing happened

#### Option B: Add BILLED Status (4 states)
```
SCHEDULED → COMPLETED → BILLED
         ↘ CANCELLED
```
- Only count BILLED appointments for discount
- Status automatically excludes current

### Decision

**Chosen: Option A - Simple Statuses (3 states)**

Appointment statuses:
| Status | Meaning |
|--------|---------|
| `SCHEDULED` | Booked, consultation not yet done |
| `COMPLETED` | Consultation finished, eligible for billing |
| `CANCELLED` | Appointment was cancelled |

Discount calculation rule:
- Count all COMPLETED appointments for the patient
- **Exclude the current appointment being billed**
- Current appointment does NOT contribute to its own discount

### Rationale

1. **Problem Statement Alignment:** Uses "COMPLETE" as the final consultation state.

2. **Semantic Clarity:** "Prior visits" logically means visits BEFORE the current one. Visit #4 shouldn't get credit for Visit #4.

3. **Simplicity:** 3 statuses are easier to manage than 4.

4. **Separation of Concerns:** Bill is a separate entity - its existence proves billing happened. We don't need a status to track this.

5. **Explicit Logic:** The exclusion is clear in code:
   ```kotlin
   fun calculatePriorVisits(currentAppointmentId: UUID, patientId: UUID): Int {
       return appointmentRepository
           .findByPatientId(patientId)
           .count { it.status == COMPLETED && it.id != currentAppointmentId }
   }
   ```

### Workflow

```
1. Appointment created           → SCHEDULED
2. Patient visits, consultation
3. Staff marks appointment       → COMPLETED
4. Bill generation requested
5. System calculates discount (excluding current appointment)
6. Bill created and returned
7. Appointment stays             → COMPLETED (bill existence = billed)
```

### Consequences

- Discount logic must explicitly exclude current appointment ID
- No way to query "billed appointments" by status alone (must join with Bill entity)
- Simple state machine, fewer transitions to manage

### Example Scenarios

| Visit # | Prior COMPLETED (excl. current) | Discount |
|---------|--------------------------------|----------|
| 1st | 0 | 0% |
| 2nd | 1 | 1% |
| 3rd | 2 | 2% |
| 11th | 10 | 10% (capped) |
| 15th | 14 | 10% (capped) |

---

## ADR-007: DTOs Grouped by Domain Entity

**Date:** January 2026  
**Status:** Accepted

### Context

Need to decide how to organize DTO files (request/response classes) in the codebase.

### Options Considered

#### Option A: Single File Per Domain (Chosen)
```
dto/
├── PatientDto.kt      # CreatePatientRequest, UpdatePatientRequest, PatientResponse
├── DoctorDto.kt       # CreateDoctorRequest, DoctorResponse
├── AppointmentDto.kt  # ...
└── BillDto.kt         # ...
```

#### Option B: Separate Files Per DTO
```
dto/
├── CreatePatientRequest.kt
├── UpdatePatientRequest.kt
├── PatientResponse.kt
├── CreateDoctorRequest.kt
└── ...
```

#### Option C: Grouped by Type
```
dto/
├── request/
│   ├── CreatePatientRequest.kt
│   └── CreateDoctorRequest.kt
└── response/
    ├── PatientResponse.kt
    └── DoctorResponse.kt
```

### Decision

**Chosen: Option A - Single File Per Domain**

### Rationale

1. **Cohesion:** All Patient-related DTOs in one place.

2. **Easy navigation:** Open `PatientDto.kt` to see all Patient DTOs.

3. **Extension functions together:** Mapping functions stay with their DTOs.

4. **Appropriate for project size:** Small project doesn't need deep folder structure.

5. **Refactoring friendly:** Changes to Patient DTOs are localized.

### File Structure

```kotlin
// PatientDto.kt
data class CreatePatientRequest(...)
data class UpdatePatientRequest(...)
data class PatientResponse(...)
data class InsuranceInfoDto(...)

// Extension functions
fun Patient.toResponse() = ...
fun CreatePatientRequest.toDomain() = ...
```

---

## ADR-008: Extension Functions for DTO Mapping

**Date:** January 2026  
**Status:** Accepted

### Context

Need to decide how to convert between domain models and DTOs. Two main approaches in Kotlin:

1. **Companion object methods** - Static factory-style methods inside the DTO class
2. **Extension functions** - Methods added to source class that return target

### Options Considered

#### Option A: Companion Object
```kotlin
data class PatientResponse(...) {
    companion object {
        fun fromDomain(patient: Patient) = PatientResponse(...)
    }
}
// Usage: PatientResponse.fromDomain(patient)
```

#### Option B: Extension Function
```kotlin
fun Patient.toResponse() = PatientResponse(...)
// Usage: patient.toResponse()
```

### Decision

**Chosen: Option B - Extension Functions**

### Rationale

1. **More idiomatic Kotlin:** Extension functions are a core Kotlin feature.

2. **Cleaner chaining with null safety:**
   ```kotlin
   // Extension - clean
   patientRepository.findById(id)?.toResponse()
   
   // Companion - awkward
   patientRepository.findById(id)?.let { PatientResponse.fromDomain(it) }
   ```

3. **Natural reading order:** "Take patient, convert to response" reads left-to-right.

4. **IDE discoverability:** Type `patient.` and IDE shows `toResponse()`.

5. **Consistency:** All mappings follow same pattern (`source.toTarget()`).

### Convention Established

```kotlin
// Domain → DTO
fun Patient.toResponse() = PatientResponse(...)
fun InsuranceInfo.toDto() = InsuranceInfoDto(...)

// DTO → Domain
fun CreatePatientRequest.toDomain() = Patient(...)
fun InsuranceInfoDto.toDomain() = InsuranceInfo(...)
```

### Location

Extension functions placed at bottom of DTO file, after data class definitions.

---

## ADR-009: Unified Exception Handler

**Date:** January 2026  
**Status:** Accepted

### Context

Need to handle domain exceptions and map them to HTTP status codes. Micronaut's `ExceptionHandler<E, R>` interface is typed to specific exception types.

### Options Considered

#### Option A: One Handler Per Exception
```kotlin
class NotFoundExceptionHandler : ExceptionHandler<NotFoundException, ...>
class ConflictExceptionHandler : ExceptionHandler<ConflictException, ...>
class BadRequestExceptionHandler : ExceptionHandler<BadRequestException, ...>
```

#### Option B: Single Handler for Base Exception
```kotlin
class DomainExceptionHandler : ExceptionHandler<DomainException, ...> {
    override fun handle(...) {
        val status = when (exception) {
            is NotFoundException -> HttpStatus.NOT_FOUND
            is ConflictException -> HttpStatus.CONFLICT
            is BadRequestException -> HttpStatus.BAD_REQUEST
            else -> HttpStatus.INTERNAL_SERVER_ERROR
        }
    }
}
```

### Decision

**Chosen: Option B - Single Unified Handler**

### Rationale

1. **Less boilerplate:** One class instead of many.

2. **Centralized logic:** All exception-to-status mapping in one place.

3. **Easier to maintain:** Adding new exception type = one line in `when`.

4. **Kotlin `when` is exhaustive:** Compiler can warn if cases are missing.

5. **Consistent response format:** All exceptions produce same `ErrorResponse` structure.

### Exception Hierarchy

```kotlin
abstract class DomainException(message: String, val errorCode: String)
├── NotFoundException    → 404
├── ConflictException    → 409
└── BadRequestException  → 400
```

---

## ADR-010: Centralized Error Codes

**Date:** January 2026  
**Status:** Accepted

### Context

Error codes are used in exceptions to identify specific error scenarios. Need to decide how to manage these codes across the codebase.

### Options Considered

#### Option A: Inline Strings
```kotlin
throw NotFoundException("...", "PATIENT_NOT_FOUND")
```

#### Option B: Constants in Exception Classes
```kotlin
class NotFoundException {
    companion object {
        const val PATIENT = "PATIENT_NOT_FOUND"
    }
}
```

#### Option C: Central ErrorCodes Object
```kotlin
object ErrorCodes {
    const val PATIENT_NOT_FOUND = "PATIENT_NOT_FOUND"
    const val DOCTOR_NOT_FOUND = "DOCTOR_NOT_FOUND"
}
```

#### Option D: Enum
```kotlin
enum class ErrorCode { PATIENT_NOT_FOUND, DOCTOR_NOT_FOUND }
```

### Decision

**Chosen: Option C - Central ErrorCodes Object**

### Rationale

1. **Single source of truth:** All error codes in one file.

2. **IDE autocomplete:** Type `ErrorCodes.` to see all available codes.

3. **Prevents typos:** Compiler catches misspelled constant names.

4. **Easy discovery:** New developers can see all error codes at once.

5. **Simple refactoring:** Change code in one place.

6. **No signature changes:** Unlike enum, doesn't require changing exception constructors.

### Structure

```kotlin
object ErrorCodes {
    // Patient
    const val PATIENT_NOT_FOUND = "PATIENT_NOT_FOUND"
    const val PATIENT_HAS_APPOINTMENTS = "PATIENT_HAS_APPOINTMENTS"
    
    // Doctor (added when implementing Doctor APIs)
    // Appointment (added when implementing Appointment APIs)
    // Billing (added when implementing Billing APIs)
}
```

---

## ADR-011: No Serialization Annotations on Domain Models

**Date:** January 2026  
**Status:** Accepted

### Context

Micronaut Serialization requires `@Serdeable` annotation for classes to be serialized/deserialized. Need to decide whether domain models should have this annotation.

### Decision

**Domain models do NOT have `@Serdeable`. Only DTOs have serialization annotations.**

### Rationale

1. **Separation of concerns:** Domain models represent business logic; serialization is an API concern.

2. **Single responsibility:** DTOs handle API format, domain models handle business rules.

3. **Flexibility:** Domain can evolve independently of API format.

4. **Clean domain layer:** No framework dependencies in domain classes.

5. **Explicit mapping:** Extension functions make the mapping visible and testable.

### Structure

```
domain/           # No @Serdeable
├── Patient.kt    
├── Doctor.kt
├── Appointment.kt
├── Bill.kt
└── InsuranceInfo.kt

dto/              # @Serdeable on all DTOs
├── PatientDto.kt
├── DoctorDto.kt
├── AppointmentDto.kt
└── BillDto.kt
```

### Exception: Enums

Enums (`Specialty`, `AppointmentStatus`) don't need `@Serdeable` as Micronaut handles them natively.

---

*More decisions will be documented as the project progresses.*
