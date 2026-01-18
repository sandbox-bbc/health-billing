# Healthcare Billing System

![CI](https://github.com/sandbox-bbc/health-billing/actions/workflows/ci.yml/badge.svg)

A backend system for managing healthcare billing, built with **Kotlin** and **Micronaut**.

## Tech Stack

| Component | Technology | Version |
|-----------|------------|---------|
| Runtime | JDK | 21 |
| Language | Kotlin | 1.9.25 |
| Framework | Micronaut | 4.10.7 |
| Build | Gradle (Kotlin DSL) | 8.x |
| Testing | Kotest + MockK | - |

## Quick Start

### Prerequisites
- JDK 21+
- Gradle 8.x (or use included wrapper)

### Run the Application

```bash
./gradlew run
```

The server starts at `http://localhost:8080`

### Run Tests

```bash
./gradlew test
```

### Health Check

```bash
curl http://localhost:8080/health
```

### Demo UI

A web-based demo interface is included to showcase all features:

```bash
./gradlew run
# Open http://localhost:8080 in your browser
```

The UI is served as a static resource from the same server - no separate frontend setup needed!

**Demo Features:**
- 🚀 Interactive step-by-step billing workflow
- 👤 Patient management (CRUD)
- 🩺 Doctor management
- 📅 Appointment scheduling & status updates
- 💰 Bill generation with detailed breakdown

## API Endpoints

### Patients

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/patients` | Create patient with insurance |
| GET | `/patients/{id}` | Get patient by ID |
| GET | `/patients` | List all patients |
| PUT | `/patients/{id}` | Update patient |
| DELETE | `/patients/{id}` | Delete patient |

### Doctors

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/doctors` | Create doctor |
| GET | `/doctors/{id}` | Get doctor by ID |
| GET | `/doctors` | List all doctors |
| DELETE | `/doctors/{id}` | Delete doctor |

> **Note:** Doctors are immutable - no update endpoint.

### Appointments

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/appointments` | Schedule appointment |
| GET | `/appointments/{id}` | Get appointment by ID |
| GET | `/appointments` | List all appointments |
| PUT | `/appointments/{id}/status` | Update status (COMPLETED/CANCELLED) |
| DELETE | `/appointments/{id}` | Delete appointment |

### Billing

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/bills/generate/{appointmentId}` | Generate bill for completed appointment |
| GET | `/bills/{id}` | Get bill by ID |
| GET | `/bills` | List all bills |
| GET | `/bills?appointmentId={id}` | Get bill by appointment ID |

## Billing Calculation

### Fee Table

| Specialty | 0-19 yrs exp | 20-30 yrs exp | 31+ yrs exp |
|-----------|--------------|---------------|-------------|
| ORTHO | $800 | $1,000 | $1,500 |
| CARDIO | $1,000 | $1,500 | $2,000 |

### Calculation Flow

```
Base Fee (from table)
    ↓
- Loyalty Discount (min(priorVisits, 10)%)
    ↓
+ GST (12%)
    ↓
= Total Amount
    ↓
Split: Insurance (90%) / Co-pay (10%)
```

### Example

```
Doctor: CARDIO, 26 years experience → $1,500 base fee
Patient: 5 prior completed visits → 5% discount

Base Fee:        $1,500.00
Discount (5%):   -  $75.00
Discounted:      $1,425.00
GST (12%):       + $171.00
Total:           $1,596.00
Insurance (90%): $1,436.40
Co-pay (10%):    $  159.60
```

## Sample API Calls

### Create Patient

```bash
curl -X POST http://localhost:8080/patients \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "dob": "01/15/1990",
    "insurance": {
      "binNo": "123456",
      "pcnNo": "PCN001",
      "memberId": "MEM12345"
    }
  }'
```

### Create Doctor

```bash
curl -X POST http://localhost:8080/doctors \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Smith",
    "npiNo": "NPI123456",
    "specialty": "CARDIO",
    "practiceStartDate": "01/01/2000"
  }'
```

### Schedule Appointment

```bash
curl -X POST http://localhost:8080/appointments \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": "<patient-uuid>",
    "doctorId": "<doctor-uuid>",
    "appointmentDate": "01/20/2026"
  }'
```

### Complete Appointment

```bash
curl -X PUT http://localhost:8080/appointments/<appointment-uuid>/status \
  -H "Content-Type: application/json" \
  -d '{"status": "COMPLETED"}'
```

### Generate Bill

```bash
curl -X POST http://localhost:8080/bills/generate/<appointment-uuid> \
  -H "Content-Type: application/json"
```

## Project Structure

```
src/main/kotlin/com/linx/health/
├── Application.kt           # Entry point
├── controller/              # REST endpoints
├── service/                 # Business logic
│   └── billing/             # Billing calculation (Strategy Pattern)
├── repository/              # In-memory data access
├── domain/                  # Entity models
├── dto/                     # Request/Response objects
├── common/                  # Shared constants
└── exception/               # Error handling
```

## Documentation

- [Architecture](docs/ARCHITECTURE.md) - System design and diagrams
- [Design Decisions](docs/DESIGN_DECISIONS.md) - ADRs with rationale
- [Assumptions](docs/ASSUMPTIONS.md) - Business and technical assumptions

## Design Highlights

- **Strategy Pattern** for specialty-based fee calculation (extensible)
- **Extension Functions** for DTO ↔ Domain mapping
- **Unified Exception Handler** for consistent error responses
- **In-memory Storage** with `ConcurrentHashMap` (thread-safe)

## Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| PATIENT_NOT_FOUND | 404 | Patient does not exist |
| DOCTOR_NOT_FOUND | 404 | Doctor does not exist |
| APPOINTMENT_NOT_FOUND | 404 | Appointment does not exist |
| APPOINTMENT_NOT_COMPLETED | 400 | Cannot bill non-completed appointment |
| BILL_ALREADY_EXISTS | 409 | Bill already generated for appointment |
| DUPLICATE_NPI | 409 | Doctor with NPI already exists |
| PATIENT_HAS_APPOINTMENTS | 409 | Cannot delete patient with appointments |
| DOCTOR_HAS_APPOINTMENTS | 409 | Cannot delete doctor with appointments |

## Development Workflow

### Branching Strategy

```
main (protected)
  │
  └── feat/implementation ──► PR ──► main
```

| Branch | Purpose |
|--------|---------|
| `main` | Production-ready code, protected |
| `feat/*` | Feature development branches |

### Git Workflow

```bash
# 1. Create feature branch
git checkout -b feat/my-feature

# 2. Make changes and commit
git add .
git commit -m "feat: add new feature"

# 3. Push to remote
git push origin feat/my-feature

# 4. Create Pull Request on GitHub
# 5. CI runs automatically
# 6. Merge when CI passes
```

### Commit Convention

We use [Conventional Commits](https://www.conventionalcommits.org/):

| Prefix | Purpose | Example |
|--------|---------|---------|
| `feat:` | New feature | `feat: add billing endpoint` |
| `fix:` | Bug fix | `fix: correct discount calculation` |
| `docs:` | Documentation | `docs: update API examples` |
| `refactor:` | Code restructure | `refactor: extract fee strategy` |
| `test:` | Tests | `test: add billing tests` |
| `chore:` | Maintenance | `chore: update dependencies` |

### CI Pipeline

GitHub Actions runs automatically on:
- Push to `main` or `feat/*` branches
- Pull requests targeting `main`

```
Push/PR ──► Checkout ──► JDK 21 ──► Cache ──► Build ──► Test ──► ✅/❌
```

**What CI checks:**
1. Code compiles successfully
2. All tests pass (38 tests)
3. Test reports uploaded as artifacts

**View CI results:** GitHub repo → Actions tab

## License

Private - Assignment submission