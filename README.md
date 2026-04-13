# Artem Health HMS

Production-oriented Hospital Management System built as a modular monolith with a Spring Boot 3 backend and Angular 17 frontend. The application covers patient registration, appointments, doctor onboarding, prescriptions, pharmacy inventory, billing, audit logging, and role-based access control.

## Table of Contents

- [Architecture](#architecture)
- [System Diagram](#system-diagram)
- [Sequence Diagram](#sequence-diagram)
- [Implemented Features](#implemented-features)
- [Application Flow](#application-flow)
- [Production Hardening Included](#production-hardening-included)
- [Role Workspaces](#role-workspaces)
- [Role Module APIs](#role-module-apis)
- [Local Setup](#local-setup)
- [Elasticsearch Setup](#elasticsearch-setup)
- [Configuration](#configuration)
- [Production Safety](#production-safety)
- [Security Notes](#security-notes)
- [Troubleshooting](#troubleshooting)
- [Verification](#verification)

## Architecture

- Backend: Spring Boot 3.3, Spring Security 6, Spring Data JPA, Spring Data Elasticsearch, Hibernate, MapStruct, Lombok, Bucket4j, Redis.
- Frontend: Angular 17 standalone components, PrimeNG, RxJS, route guards, HTTP interceptors.
- Database: MySQL 8 and Redis 7 (caching & rate limiting) for normal runtime.
- Search: Elasticsearch 8.11 with fuzzy search, phonetic matching, and batch reindexing.
- Security: JWT access tokens, refresh-token rotation/revocation, BCrypt password hashing, method-level authorization, security headers, and distributed Redis-backed rate limiting.
- Caching: Multi-level caching for Doctor, Patient, and Medicine entities to reduce DB load and improve response times.

## System Diagram

### High-Level Architecture

```mermaid
flowchart LR
    subgraph Actors["Hospital Actors"]
        Admin["Admin"]
        Doctor["Doctor"]
        Nurse["Nurse"]
        Receptionist["Receptionist"]
        Pharmacist["Pharmacist"]
        LabStaff["Laboratory Staff"]
        PatientUser["Patient"]
    end

    subgraph Client["Client Layer"]
        Angular["Angular 17 SPA"]
        Guards["Auth Guard<br/>Role Guard"]
        Interceptors["JWT Interceptor<br/>Error Interceptor"]
    end

    subgraph Backend["Backend Layer"]
        API["Spring Boot 3 REST API"]
        Security["Spring Security<br/>JWT, CORS, rate limits"]
        Modules["Domain Modules"]
    end

    subgraph Data["Data and Infrastructure"]
        MySQL[("MySQL 8")]
        Redis[("Redis 7")]
        Logs[("Application Logs")]
    end

    Actors --> Angular
    Angular --> Guards
    Guards --> Interceptors
    Interceptors -->|"HTTP JSON /api/v1"| API
    API --> Security
    Security --> Modules
    Modules --> MySQL
    Modules --> Redis
    Security --> Redis
    Modules --> Logs
```

### Frontend Design

```mermaid
flowchart TB
    App["Angular Application"]
    Routes["App Routes"]
    Guards["Auth Guard<br/>Guest Guard<br/>Role Guard"]
    Layout["Header and Sidebar"]
    Interceptors["JWT Interceptor<br/>Error Interceptor"]
    Services["Feature Services"]
    Pages["Feature Pages"]

    Dashboard["Dashboard"]
    Patients["Patients"]
    Appointments["Appointments"]
    Staff["Staff and Doctors"]
    Prescriptions["Prescriptions"]
    Pharmacy["Pharmacy and Inventory"]
    Billing["Billing"]
    Audit["Audit Logs"]

    App --> Routes
    Routes --> Guards
    Guards --> Layout
    Layout --> Pages
    Pages --> Dashboard
    Pages --> Patients
    Pages --> Appointments
    Pages --> Staff
    Pages --> Prescriptions
    Pages --> Pharmacy
    Pages --> Billing
    Pages --> Audit
    Pages --> Services
    Services --> Interceptors
    Interceptors -->|"API calls"| BackendAPI["Spring Boot /api/v1"]
```

### Backend Module Design

```mermaid
flowchart TB
    Controllers["REST Controllers"]
    Security["Security Layer<br/>JWT filter, method security,<br/>ownership checks"]
    Services["Service Layer<br/>business rules and validation"]
    Mappers["MapStruct Mappers"]
    Repositories["JPA Repositories"]

    Auth["Auth and Users"]
    Patient["Patients"]
    Appointment["Appointments"]
    DoctorModule["Doctors and Staff"]
    Prescription["Prescriptions"]
    Pharmacy["Pharmacy and Inventory"]
    Billing["Billing"]
    Dashboard["Dashboard"]
    Audit["Audit Logging"]
    LabNursing["Lab and Nursing APIs"]

    Controllers --> Security
    Security --> Services
    Services --> Mappers
    Services --> Repositories
    Services --> Auth
    Services --> Patient
    Services --> Appointment
    Services --> DoctorModule
    Services --> Prescription
    Services --> Pharmacy
    Services --> Billing
    Services --> Dashboard
    Services --> Audit
    Services --> LabNursing
```

### Data and Security Design

```mermaid
flowchart LR
    Request["Authenticated Request"]
    Jwt["JWT Validation"]
    RateLimit["Bucket4j Rate Limit"]
    Ownership["Role and Ownership Checks"]
    Service["Domain Service"]
    Cache["Redis Cache"]
    Buckets["Redis Rate-Limit Buckets"]
    Database[("MySQL 8<br/>hospital records")]
    Audit[("Audit Logs")]

    Request --> Jwt
    Jwt --> RateLimit
    RateLimit --> Buckets
    RateLimit --> Ownership
    Ownership --> Service
    Service --> Cache
    Service --> Database
    Service --> Audit
```

## Sequence Diagram

```mermaid
sequenceDiagram
    actor User as Hospital User
    participant UI as Angular SPA
    participant Guard as Route Guard / Interceptor
    participant API as Spring Boot API
    participant Security as Spring Security + JWT
    participant RateLimit as Bucket4j + Redis
    participant Service as Domain Service
    participant Cache as Redis Cache
    participant DB as MySQL
    participant Audit as Audit Log

    User->>UI: Open protected workspace
    UI->>Guard: Check session and role access
    Guard->>API: Send request with JWT
    API->>Security: Validate token and method access
    Security->>RateLimit: Check shared request quota
    RateLimit-->>Security: Quota accepted
    Security->>Service: Forward authorized request
    Service->>Cache: Read cached domain data
    alt Cache hit
        Cache-->>Service: Return cached data
    else Cache miss
        Service->>DB: Query or persist hospital record
        DB-->>Service: Return saved or loaded data
        Service->>Cache: Store cacheable response
    end
    Service->>Audit: Record important clinical or admin action
    Service-->>API: Return API response
    API-->>UI: Return JSON result
    UI-->>User: Render updated workspace
```

## Implemented Features

- Authentication and session management with login, registration, change password, JWT access tokens, refresh-token rotation, logout revocation, and BCrypt password hashing.
- Role-based access for ADMIN, DOCTOR, NURSE, RECEPTIONIST, PHARMACIST, LABORATORY_STAFF, and PATIENT across backend method security and Angular route/sidebar visibility.
- Dashboard workflows for operational summaries, weekly statistics, department statistics, and appointment views.
- Patient management with registration, patient directory, patient-owned `/me` access, duplicate-patient handling, and slice-based list loading.
- Appointment management with booking, appointment list views, doctor availability checks, slot conflict prevention, status tracking, and patient-owned appointment access.
- Staff and doctor onboarding with doctor registration, doctor directory, department mapping, and indexed doctor lookup paths.
- Prescription workflows with prescription creation from appointments, prescription details, patient prescription access, and prescription medicine line items.
- Pharmacy workflows with medicine catalog management, restock and dispense operations, inventory transaction logging, stock validation, and medicine slice endpoints.
- Billing workflows with bill creation, bill items, payment status and method handling, patient billing access, and patient payment action support.
- Audit logging for administrative visibility through `GET /api/v1/audit-logs/slice?page=0&size=25` and the Angular `/audit-logs` page.
- **Elasticsearch full-text search** with fuzzy search (typo tolerance), phonetic matching (similar-sounding names), and multi-field search across patients, doctors, appointments, and prescriptions.
- **Admin search management** dashboard for reindexing entities, health checks, and index management with batch processing (500 records/batch).
- **Search API endpoints** for global fuzzy search with pagination and role-based access control.
- Production-oriented platform behavior including soft delete, centralized exception responses, validation errors, CORS configuration, startup database verification, logging configuration, caching, and distributed rate limiting.

## Application Flow

1. A user signs in from the Angular application on `http://localhost:4200`.
2. The frontend stores the authenticated session through the auth service and sends API requests through the JWT interceptor.
3. Spring Security validates the token, applies role and method authorization, and enforces Redis-backed rate limits before requests reach domain modules.
4. Domain services perform validation, ownership checks, soft-delete-aware data access, audit logging, caching, and persistence through Spring Data JPA.
5. MySQL stores hospital records while Redis supports shared rate-limit buckets and cache entries for high-traffic entities.
6. Angular route guards, role-route maps, sidebar visibility, modals, and interceptors keep the user experience aligned with backend access rules.

## Production Hardening Included

- Soft delete is enabled at the base entity layer so repository deletes keep medical and operational records recoverable.
- Bucket4j rate limiting is wired into the Spring Security filter chain with Redis-backed persistence. Quotas are shared across all backend instances for consistent traffic management.
- `@PreAuthorize` rules are active through method security.
- High-traffic database access paths have entity-level indexes for users, patients, doctors, appointments, prescriptions, medicines, inventory transactions, and billing.
- Large directory-style resources expose slice endpoints as a non-breaking alternative to full-list reads:
  - `GET /api/v1/patients/slice?page=0&size=25`
  - `GET /api/v1/doctors/slice?page=0&size=25`
  - `GET /api/v1/medicines/slice?page=0&size=25`
  - `GET /api/v1/users/slice?page=0&size=25`
- Angular services include matching slice methods and handle `429 Too Many Requests` responses cleanly.
- Frontend route/sidebar role visibility is aligned for patient and staff workflows.
- Dedicated role workspaces and backend APIs are available for nursing triage, laboratory specimen handling, and patient self-service guidance.
- Startup verification logs the running project path, active profile, web port, frontend API URL, DB URL, active schema, DDL mode, and table count. In production profiles, the backend refuses to start with the default JWT secret or unsafe Hibernate DDL mutation settings.
- Admin audit trail UI is available at `/audit-logs`.

## Role Workspaces

- `ADMIN`: full operations, users, staff, pharmacy, billing, lab, and nursing oversight.
- `DOCTOR`: appointments, prescriptions, patients, and clinical dashboard workflows.
- `NURSE`: nursing workbench, patient triage, check-in handoff, patients, and appointments.
- `RECEPTIONIST`: patient registration, appointment booking, staff directory, and billing.
- `PHARMACIST`: pharmacy inventory, inventory logs, prescriptions, and patient lookup.
- `LABORATORY_STAFF`: laboratory workbench for sample and result workflow coordination.
- `PATIENT`: patient portal for appointments, bills, prescriptions, lab reports, and demographic profile access.

## Role Module APIs

- Nursing triage: `POST /api/v1/nursing/triage`, `GET /api/v1/nursing/triage/patient/{patientId}`
- Laboratory orders: `POST /api/v1/lab/orders`, `GET /api/v1/lab/orders`, `GET /api/v1/lab/orders/patient/{patientId}`
- Laboratory workflow: `PATCH /api/v1/lab/orders/{id}/status`, `PATCH /api/v1/lab/orders/{id}/result`
- Patient portal: `GET /api/v1/patient-portal/summary`
- Patient billing: `GET /api/v1/billings/me`, `PATCH /api/v1/billings/{id}/pay`
- Patient-owned APIs: `GET /api/v1/patients/me`, `GET /api/v1/appointments/me`, `GET /api/v1/prescriptions/me`, `GET /api/v1/lab/orders/me`
- Audit trail: `GET /api/v1/audit-logs/slice?page=0&size=25`

## Local Setup

### Prerequisites

- JDK 17+
- Node.js 18+
- Maven wrapper included in `backend/`
- MySQL 8+
- Redis 7+ (for caching and rate limiting)

### Backend

```bash
cd backend
set HMS_DB_URL=jdbc:mysql://localhost:1234/data?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
set HMS_DB_USERNAME=root
set HMS_DB_PASSWORD=your_password
set REDIS_HOST=localhost
set REDIS_PORT=6379
set JWT_SECRET_KEY=replace_with_a_strong_hex_secret
.\mvnw.cmd spring-boot:run
```

The backend runs on `http://localhost:8080` by default.

### Frontend

```bash
cd frontend
npm install
npm start
```

The frontend runs on `http://localhost:4200` by default.

## Elasticsearch Setup

Elasticsearch provides fast full-text search with fuzzy matching and phonetic analysis. Choose one setup method below.

### Option 1: Local Elasticsearch (Without Docker) - Windows

#### Step 1: Download Elasticsearch

1. Visit [Elasticsearch Downloads](https://www.elastic.co/downloads/elasticsearch)
2. Download Elasticsearch 8.11.0 for Windows (ZIP)
3. Extract to a location like `C:\elasticsearch-8.11.0`

#### Step 2: Disable Security (Development Only)

Edit `C:\elasticsearch-8.11.0\config\elasticsearch.yml`:

```yaml
xpack.security.enabled: false
discovery.type: single-node
```

#### Step 3: Start Elasticsearch

Open PowerShell and run:

```powershell
cd C:\elasticsearch-8.11.0\bin
.\elasticsearch.bat
```

Wait for the message: `started`

Verify it's running:

```powershell
curl http://localhost:9200
```

#### Step 4: Configure HMS Backend

Set environment variable or update `application.properties`:

```properties
elasticsearch.enabled=true
elasticsearch.host=localhost
elasticsearch.port=9200
```

### Option 2: Elasticsearch with Docker

```bash
docker run -d \
  --name elasticsearch \
  -e discovery.type=single-node \
  -e xpack.security.enabled=false \
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
  -p 9200:9200 \
  docker.elastic.co/elasticsearch/elasticsearch:8.11.0
```

### Option 3: Docker Compose (Complete Stack)

Use the included `docker-compose.yml` to run all services:

```bash
docker-compose up -d
```

This starts: Elasticsearch, MySQL, Redis (and optional Kibana for monitoring)

### Using the Search Features

1. **Access Admin Dashboard**:
   - Navigate to `http://localhost:4200/admin/elasticsearch`
   - Click "Refresh" to check Elasticsearch health status

2. **Reindex Data**:
   - Select "All Entities" from dropdown
   - Click "Start Reindex"
   - Wait for completion

3. **Search Data**:
   Use the search endpoints:
   ```
   GET /api/v1/search/patients?query=john&page=0&size=10
   GET /api/v1/search/doctors?query=cardiology&page=0&size=10
   GET /api/v1/search/appointments?query=knee&page=0&size=10
   GET /api/v1/search/prescriptions?query=diabetes&page=0&size=10
   ```

### Search Features

- **Fuzzy Search**: Typo-tolerant searching ("jon" matches "john", "smyth" matches "smith")
- **Phonetic Matching**: Similar-sounding names are matched using Metaphone analysis
- **Multi-field Search**: Searches across relevant fields (name, email, phone, specialization, etc.)
- **Pagination**: Full support for paginated results
- **Role-based Access**: User search is protected by role-based access control

### Admin Reindex Endpoints

All endpoints require ADMIN role:

```
POST   /api/v1/admin/search/reindex/patients
POST   /api/v1/admin/search/reindex/doctors
POST   /api/v1/admin/search/reindex/appointments
POST   /api/v1/admin/search/reindex/prescriptions
POST   /api/v1/admin/search/reindex/all         (Reindex everything)
DELETE /api/v1/admin/search/indices/clear-all   (Clear all indices)
GET    /api/v1/admin/search/health              (Check Elasticsearch health)
```

### Disabling Elasticsearch

If you want to run without Elasticsearch, set:

```properties
elasticsearch.enabled=false
```

The application will start successfully but search features will be unavailable.

## Configuration

Important backend settings live in `backend/src/main/resources/application.properties` and can be overridden with environment variables.

- `HMS_DB_URL`, `HMS_DB_USERNAME`, `HMS_DB_PASSWORD`: database connection.
- `JWT_SECRET_KEY`: production JWT signing secret.
- `SERVER_PORT`: backend HTTP port. Defaults to `8080` and must match Angular `environment.apiUrl`.
- `HMS_FRONTEND_API_URL`: API URL logged during startup to prevent copied-project confusion. Defaults to `http://localhost:8080/api/v1`.
- `HMS_ALLOWED_ORIGINS`: comma-separated CORS origins. Dev default is `http://localhost:4200,http://127.0.0.1:4200`.
- `hms.rate-limit.enabled`: enable or disable distributed Bucket4j rate limiting.
- `hms.rate-limit.capacity`: maximum tokens per bucket.
- `hms.rate-limit.refill-tokens`: tokens restored per interval.
- `hms.rate-limit.refill-minutes`: refill interval in minutes.
- `REDIS_HOST`, `REDIS_PORT`: Redis connection for shared rate limiting and cache.
- `HMS_LOG_PATH` or `hms.logging.path`: canonical backend log directory. `npm run backend:start` writes backend logs and backend-run stdout/stderr files under `logs/backend`.
- `elasticsearch.enabled`: enable/disable Elasticsearch search. Defaults to `true`.
- `elasticsearch.host`: Elasticsearch server hostname. Defaults to `localhost`.
- `elasticsearch.port`: Elasticsearch port. Defaults to `9200`.
- `elasticsearch.username`: Elasticsearch username (for secured clusters).
- `elasticsearch.password`: Elasticsearch password (for secured clusters).

Redis is required for consistent distributed rate limiting and optimized caching in this version.

## Production Safety

Production profile names `prod` and `production` are guarded at startup:

- `spring.jpa.hibernate.ddl-auto=update`, `create`, or `create-drop` fails startup.
- The default JWT secret fails startup.
- Use `HMS_DDL_AUTO=validate` and a strong `JWT_SECRET_KEY` before production deployment.
- Cookies should be deployed with `hms.jwt.cookieSecure=true` and `hms.jwt.cookieSameSite=Lax` or `Strict`.

## Security Notes

- Patient-facing APIs use ownership checks in the service layer, not only route/controller guards.
- Patients should use `/patient-portal` and the `/me` endpoints.
- Patient ID based APIs remain staff/admin oriented unless service ownership checks permit the exact current patient.
- Refresh tokens are stored client-side only as HttpOnly cookies; logout revokes the current token hash and increments the user token version.
- Refresh now rotates the token version and revokes the previous refresh token.

## Troubleshooting

- If a copied project shows old data, check which Java process owns port `8080`.
- The startup log must say `HMS Backend running from C:\Users\Piyush\Desktop\hms\backend`.
- If MySQL tables are empty but old users still appear, clear browser site data for `localhost` and stop any backend from `C:\Users\Piyush\Desktop\Project\backend`.
- If the frontend cannot connect, confirm `frontend/src/environments/environment*.ts` points to `http://localhost:8080/api/v1`.
- Use `docs/manual-route-validation.md` to confirm role route access after auth changes.

### Elasticsearch Troubleshooting

- **"Elasticsearch is not available"**: Check if Elasticsearch is running. On Windows, verify the `elasticsearch.bat` process is active.

  ```powershell
  # Check Elasticsearch status
  curl http://localhost:9200

  # If not running, start it
  cd C:\elasticsearch-8.11.0\bin
  .\elasticsearch.bat
  ```

- **Reindex fails or takes too long**:
  - Verify Elasticsearch has enough memory: Check `elasticsearch.yml` for `ES_JAVA_OPTS`
  - For local setup, you can increase heap size by editing the bat file or environment

- **Search returns no results**:
  - Ensure reindex was completed successfully from admin dashboard
  - Check Elasticsearch logs for errors
  - Verify indices exist: `curl http://localhost:9200/_cat/indices`

- **Memory issues with Elasticsearch**:
  - Edit `elasticsearch.yml` and reduce heap: `ES_JAVA_OPTS: "-Xms256m -Xmx512m"`
  - Or reduce batch size in `ElasticsearchReindexServiceImpl.java` (change `BATCH_SIZE` from 500)

- **Port 9200 already in use**:
  - Change Elasticsearch port in `elasticsearch.yml`: `http.port: 9201`
  - Update `application.properties`: `elasticsearch.port=9201`

## Verification

Backend:

```bash
cd backend
.\mvnw.cmd -q -DskipTests compile
```

Frontend:

```bash
cd frontend
npm run build
```

The frontend build may report CommonJS optimization warnings from PDF/canvas-related dependencies. Those warnings do not block the build.
