# Architecture Documentation

## System Overview

Healthcare Billing Backend System built with Micronaut 4.10.7 and Kotlin.

## Technology Stack

| Component | Technology | Version |
|-----------|------------|---------|
| Runtime | JDK | 21 (LTS) |
| Language | Kotlin | 1.9.25 |
| Framework | Micronaut | 4.10.7 |
| Build | Gradle (Kotlin DSL) | 8.x |
| Testing | Kotest + MockK | - |

## Package Structure

```
com.linx.health/
├── Application.kt          # Entry point
├── controller/             # REST endpoints
├── service/                # Business logic
│   └── billing/            # Billing calculation (Strategy Pattern)
│       ├── BillingConstants.kt
│       ├── BillingCalculator.kt
│       ├── FeeStrategy.kt
│       ├── OrthoFeeStrategy.kt
│       └── CardioFeeStrategy.kt
├── repository/             # Data access (in-memory)
├── domain/                 # Entity models
├── dto/                    # Request/Response objects
├── common/                 # Shared constants
└── exception/              # Custom exceptions
```

## Layer Architecture

```mermaid
flowchart TD
    subgraph Controller["Controller Layer"]
        C1[REST endpoints]
        C2[Request/Response handling]
    end
    
    subgraph Service["Service Layer"]
        S1[Business logic]
        S2[Validation]
    end
    
    subgraph Repository["Repository Layer"]
        R1[Data access]
        R2[In-memory storage]
    end
    
    Controller --> Service --> Repository
```

## Entity Relationships

```mermaid
erDiagram
    Patient ||--|| InsuranceInfo : has
    Patient ||--o{ Appointment : books
    Doctor ||--o{ Appointment : conducts
    Appointment ||--o| Bill : generates

    Patient {
        UUID id PK
        String firstName
        String lastName
        LocalDate dob
    }
    
    InsuranceInfo {
        String binNo
        String pcnNo
        String memberId
    }
    
    Doctor {
        UUID id PK
        String firstName
        String lastName
        String npiNo UK
        Specialty specialty
        LocalDate practiceStartDate
    }
    
    Appointment {
        UUID id PK
        UUID patientId FK
        UUID doctorId FK
        LocalDate appointmentDate
        AppointmentStatus status
    }
    
    Bill {
        UUID id PK
        UUID appointmentId FK
        BigDecimal baseFee
        BigDecimal discountAmount
        BigDecimal gstAmount
        BigDecimal totalAmount
        BigDecimal insuranceAmount
        BigDecimal coPayAmount
    }
```

## Entity Lifecycles

### Patient Lifecycle

```mermaid
stateDiagram-v2
    [*] --> Created: POST /patients
    Created --> Updated: PUT /patients/{id}
    Updated --> Updated: PUT /patients/{id}
    Created --> Deleted: DELETE /patients/{id}
    Updated --> Deleted: DELETE /patients/{id}
    Deleted --> [*]
    
    note right of Created: Insurance info embedded
    note right of Updated: Can update details & insurance
```

- Patient created with insurance info (required)
- Can update personal details and insurance
- Delete removes patient (appointments may reference)

### Doctor Lifecycle

```mermaid
stateDiagram-v2
    [*] --> Created: POST /doctors
    Created --> Deleted: DELETE /doctors/{id}
    Deleted --> [*]
    
    note right of Created: Immutable - no updates allowed
```

- Doctor created once with all info
- No updates (NPI, specialty, start date don't change)
- If correction needed: delete and recreate

### Appointment Lifecycle

```mermaid
stateDiagram-v2
    [*] --> SCHEDULED: POST /appointments
    SCHEDULED --> COMPLETED: PUT /appointments/{id}/status
    SCHEDULED --> CANCELLED: PUT /appointments/{id}/status
    COMPLETED --> [*]: Eligible for billing
    CANCELLED --> [*]: No billing possible
    
    note right of COMPLETED: Counts toward loyalty discount
```

- Created as SCHEDULED
- Transitions to COMPLETED after consultation
- Can be CANCELLED (no billing possible)
- COMPLETED appointments count toward loyalty discount

### Bill Lifecycle

```mermaid
stateDiagram-v2
    [*] --> Generated: POST /bills?appointmentId={id}
    Generated --> [*]: Immutable
    
    note right of Generated
        Only for COMPLETED appointments
        Idempotent (returns existing if exists)
    end note
```

- Generated only for COMPLETED appointments
- Idempotent: returns existing bill if already generated
- Immutable once created

## Application Workflow

### Typical Consultation Flow

```mermaid
flowchart TD
    subgraph SETUP["SETUP PHASE"]
        A1["1. Register Doctor"]
        A2["2. Register Patient"]
        A1 --> A2
    end
    
    subgraph APPT["APPOINTMENT PHASE"]
        B1["3. Schedule Appointment"]
        B2["4. Consultation happens"]
        B3["5. Mark Complete"]
        B1 --> B2 --> B3
    end
    
    subgraph BILL["BILLING PHASE"]
        C1["6. Generate Bill"]
        C2["7. Calculate amounts"]
        C3["8. Return Bill"]
        C1 --> C2 --> C3
    end
    
    SETUP --> APPT --> BILL
```

**Workflow Details:**

| Step | Action | API |
|------|--------|-----|
| 1 | Register Doctor | `POST /doctors` |
| 2 | Register Patient with insurance | `POST /patients` |
| 3 | Schedule Appointment | `POST /appointments` → SCHEDULED |
| 4 | Consultation happens | (outside system) |
| 5 | Mark Complete | `PUT /appointments/{id}/status` → COMPLETED |
| 6 | Generate Bill | `POST /bills?appointmentId={id}` |
| 7 | System calculates | base fee, discount, GST, insurance split |
| 8 | Return Bill | Bill with all amounts |

### API Endpoints Summary

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/patients` | Create patient with insurance |
| GET | `/patients/{id}` | Get patient details |
| PUT | `/patients/{id}` | Update patient |
| DELETE | `/patients/{id}` | Delete patient |
| POST | `/doctors` | Create doctor |
| GET | `/doctors/{id}` | Get doctor details |
| DELETE | `/doctors/{id}` | Delete doctor |
| POST | `/appointments` | Schedule appointment |
| GET | `/appointments/{id}` | Get appointment details |
| PUT | `/appointments/{id}/status` | Update status |
| POST | `/bills?appointmentId={id}` | Generate bill |
| GET | `/bills/{id}` | Get bill details |

## Key Design Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Storage | In-memory (ConcurrentHashMap) | Per assignment requirement |
| ID Generation | UUID | Globally unique, no collisions |
| Money Type | BigDecimal | Precision for financial calculations |
| Date Format | MM/DD/YYYY (API), LocalDate (internal) | Per assignment requirement |
| Doctor Updates | Not allowed (immutable) | Problem statement says "capture" |
| Insurance Info | Embedded in Patient | Domain clarity, validation grouping |

> 📖 **Detailed reasoning:** See [DESIGN_DECISIONS.md](./DESIGN_DECISIONS.md) for full ADRs with context and alternatives considered.

## Billing Calculation Flow

```mermaid
flowchart TD
    A["1. Get Appointment"] --> B{"Status = COMPLETED?"}
    B -->|No| E["Error: Cannot bill"]
    B -->|Yes| C["2. Get Doctor info"]
    C --> D["3. Calculate experience years"]
    D --> F["4. Lookup base fee from Fee Table"]
    F --> G["5. Count prior COMPLETED visits"]
    G --> H["6. Calculate discount - max 10%"]
    H --> I["7. Apply discount to base fee"]
    I --> J["8. Calculate GST at 12%"]
    J --> K["9. Calculate total amount"]
    K --> L["10. Split: Insurance 90% / Co-pay 10%"]
    L --> M["Return Bill"]
```

## Fee Table

| Specialty | 0-19 yrs | 20-30 yrs | 31+ yrs |
|-----------|----------|-----------|---------|
| ORTHO | $800 | $1,000 | $1,500 |
| CARDIO | $1,000 | $1,500 | $2,000 |

> Experience = years since `practiceStartDate`

## Extension Points

The codebase is designed for easy extension following the **Open/Closed Principle**.

### 1. Adding a New Specialty

**Location:** `service/billing/`

**Steps:**
1. Add enum value to `domain/Specialty.kt`
2. Create new strategy class (e.g., `NeuroFeeStrategy.kt`)
3. Add case to `BillingCalculator.getStrategy()`

**Example:**
```kotlin
// Step 1: Add enum
enum class Specialty { ORTHO, CARDIO, NEURO }

// Step 2: Create strategy
@Singleton
@Named("NEURO")
class NeuroFeeStrategy : BaseFeeStrategy() {
    override fun getJuniorFee() = BigDecimal("900")
    override fun getMidFee() = BigDecimal("1200")
    override fun getSeniorFee() = BigDecimal("1800")
}

// Step 3: Add to calculator
fun getStrategy(specialty: Specialty) = when (specialty) {
    Specialty.ORTHO -> orthoStrategy
    Specialty.CARDIO -> cardioStrategy
    Specialty.NEURO -> neuroStrategy  // Add this
}
```

### 2. Changing Fee Amounts

**Location:** Individual strategy classes (e.g., `OrthoFeeStrategy.kt`)

**Steps:** Modify the constants in the strategy's companion object.

### 3. Changing Rates (GST, Insurance, Discount Cap)

**Location:** `service/billing/BillingConstants.kt`

```kotlin
object BillingConstants {
    val GST_RATE = BigDecimal("0.12")       // Change here
    val INSURANCE_RATE = BigDecimal("0.90") // Change here
    const val MAX_DISCOUNT_PERCENT = 10     // Change here
}
```

### 4. Changing Experience Brackets

**Location:** `service/billing/BillingConstants.kt`

```kotlin
const val JUNIOR_MAX_YEARS = 19  // 0-19 → Junior
const val MID_MAX_YEARS = 30     // 20-30 → Mid
// 31+ → Senior
```

### 5. Adding New Appointment Status

**Location:** `domain/AppointmentStatus.kt`, `service/AppointmentService.kt`

**Steps:**
1. Add enum value
2. Update status transition validation in service

### 6. Adding New Entity Fields

**Steps:**
1. Add field to domain model
2. Add field to DTO (request/response)
3. Update extension functions for mapping
4. Update repository if needed

### 7. Adding Pagination

**Location:** Controllers and Repositories

**Steps:**
1. Add `Pageable` parameter to controller
2. Return `Page<T>` instead of `List<T>`
3. Update repository to support pagination

---

## Design Patterns Used

| Pattern | Location | Purpose |
|---------|----------|---------|
| **Strategy** | `service/billing/FeeStrategy` | Specialty-based fee calculation |
| **Repository** | `repository/` | Data access abstraction |
| **DTO** | `dto/` | API request/response separation |
| **Extension Functions** | `dto/*.kt` | Domain ↔ DTO mapping |
| **Factory Method** | `BillingCalculator.getStrategy()` | Strategy resolution |
