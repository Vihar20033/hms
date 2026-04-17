# HMS System Design and Flow Diagrams

This document provides architecture, module, and sequence diagrams for core and advanced HMS features.

## 1) High-Level System Design

```mermaid
flowchart LR
    Users[Hospital Users\nAdmin Doctor Nurse Receptionist Pharmacist Billing] --> FE[Angular Frontend]
    FE --> API[Spring Boot Backend API]
    API --> DB[(MySQL)]
    API --> Redis[(Redis)]
    API --> ES[(Elasticsearch Optional)]
    API --> Audit[(Audit Trail)]
```

## 2) Backend Module Design

```mermaid
flowchart TB
    Security[security/auth] --> User[user]
    Security --> Patient[patient]
    Security --> Appointment[appointment]
    Security --> Prescription[prescription]
    Security --> Pharmacy[pharmacy]
    Security --> Billing[billing]
    Security --> Workflow[workflow]
    Security --> Admin[admin restore]
    Appointment --> Workflow
    Prescription --> Pharmacy
    Patient --> Appointment
    Appointment --> Billing
    User --> Audit[audit]
    Patient --> Audit
    Appointment --> Audit
    Billing --> Audit
    Workflow --> Audit
    Admin --> Audit
```

## 3) Request Processing Flow (Security + Rate Limit + Cache)

```mermaid
sequenceDiagram
    participant U as User
    participant FE as Angular App
    participant G as Security Filter Chain
    participant RL as Rate Limiter
    participant S as Service Layer
    participant C as Redis Cache
    participant D as MySQL

    U->>FE: Action (open patient details)
    FE->>G: API request with JWT
    G->>G: Validate token and roles
    G->>RL: Check request quota
    RL-->>G: Allowed
    G->>S: Forward request
    S->>C: Check cached response
    alt Cache hit
        C-->>S: Cached data
        S-->>FE: Return response
    else Cache miss
        C-->>S: Not found
        S->>D: Query database
        D-->>S: Data
        S->>C: Store cache
        S-->>FE: Return response
    end
```

## 4) Appointment to Workflow Runtime Flow

```mermaid
sequenceDiagram
    participant R as Receptionist
    participant FE as Appointment UI
    participant AP as Appointment API
    participant WF as Workflow Engine
    participant DB as MySQL

    R->>FE: Create appointment
    FE->>AP: POST /appointments
    AP->>DB: Persist appointment
    DB-->>AP: Appointment created
    AP-->>FE: Appointment response
    FE->>WF: POST /workflow/instances/start
    WF->>DB: Create workflow instance + initial tasks
    DB-->>WF: Instance created
    WF-->>FE: Instance and task status
```

## 5) Workflow Transition and Approval Sequence

```mermaid
sequenceDiagram
    participant Staff as Authorized Staff
    participant UI as Workflow Admin UI
    participant API as Workflow Controller
    participant ENG as Workflow Service
    participant DB as MySQL

    Staff->>UI: Move instance to next step
    UI->>API: POST /workflow/instances/{id}/transition
    API->>ENG: Validate transition request
    ENG->>DB: Load instance, step, transition policy
    DB-->>ENG: Current runtime data
    ENG->>ENG: Check role/approval rules
    alt Allowed
        ENG->>DB: Close old tasks and create next tasks
        ENG->>DB: Update instance current step/status
        ENG-->>API: Transition success
        API-->>UI: Updated instance response
    else Rejected
        ENG-->>API: Validation error
        API-->>UI: 4xx with reason
    end
```

## 6) Soft-Delete Restore Flow (Advanced Admin Feature)

```mermaid
sequenceDiagram
    participant A as System Admin
    participant UI as Admin UI
    participant API as Restore API
    participant SVC as Restore Service
    participant DB as MySQL
    participant AUD as Audit Module

    A->>UI: Restore deleted record
    UI->>API: POST /admin/restore/{entity}/{id}
    API->>SVC: Authorize and restore
    SVC->>DB: Find soft-deleted row
    DB-->>SVC: Row found
    SVC->>DB: Set deleted=false, restore metadata
    SVC->>AUD: Write restore event
    AUD-->>SVC: Logged
    SVC-->>API: Restore result
    API-->>UI: Success response
```

## 7) Advanced Feature Interaction Map

```mermaid
flowchart LR
    Auth[Auth and RBAC] --> Workflow[Workflow Engine]
    Auth --> Restore[System Restore]
    Auth --> Billing[Billing]
    Workflow --> Audit[Audit Trail]
    Restore --> Audit
    Billing --> Audit
    Cache[Redis Cache] --> Patient[Patient APIs]
    Cache --> Appointment[Appointment APIs]
    RateLimit[Bucket4j] --> Auth
    RateLimit --> Patient
    Search[Elasticsearch Optional] --> Patient
    Search --> Prescription
```

## 8) Feature Capability Diagram (Core + Advanced)

```mermaid
flowchart TB
    Core[Core HMS Features]
    Core --> Auth[Authentication and RBAC]
    Core --> Patient[Patient Management]
    Core --> Appointment[Appointment Management]
    Core --> Prescription[Prescription Management]
    Core --> Pharmacy[Pharmacy and Inventory]
    Core --> Billing[Billing and Payment]
    Core --> Dashboard[Dashboard and Reporting]

    Advanced[Advanced Platform Features]
    Advanced --> Workflow[Workflow Engine]
    Advanced --> Restore[Soft-Delete Restore]
    Advanced --> Audit[Audit Trail]
    Advanced --> Cache[Redis Cache Layer]
    Advanced --> RateLimit[Distributed Rate Limiting]
    Advanced --> Idempotency[Idempotency Service]
    Advanced --> Search[Elasticsearch Search]

    Auth --> Workflow
    Appointment --> Workflow
    Workflow --> Audit
    Restore --> Audit
    Billing --> Audit
    Cache --> Patient
    Cache --> Appointment
```

## 9) Activity Diagram (Patient Visit End-to-End)

```mermaid
flowchart TD
    Start([Start]) --> Register[Register or Verify Patient]
    Register --> Book[Book Appointment]
    Book --> Consult[Doctor Consultation]
    Consult --> Decision{Prescription Required?}
    Decision -- Yes --> WriteRx[Create Prescription]
    Decision -- No --> BillOnly[Proceed to Billing]
    WriteRx --> Dispense[Pharmacy Dispense]
    Dispense --> Bill[Generate Invoice]
    BillOnly --> Bill
    Bill --> Pay{Payment Successful?}
    Pay -- Yes --> Close[Close Visit and Audit Event]
    Pay -- No --> Retry[Retry or Mark Pending]
    Retry --> Close
    Close --> End([End])
```

## 10) ER Diagram (Database Working Model)

```mermaid
erDiagram
    USER ||--o{ APPOINTMENT : creates
    PATIENT ||--o{ APPOINTMENT : has
    DOCTOR ||--o{ APPOINTMENT : attends

    APPOINTMENT ||--o{ PRESCRIPTION : generates
    PRESCRIPTION ||--o{ PRESCRIPTION_ITEM : contains
    MEDICINE ||--o{ PRESCRIPTION_ITEM : referenced_by

    APPOINTMENT ||--o{ INVOICE : billed_as
    INVOICE ||--o{ PAYMENT : paid_by

    WORKFLOW_DEFINITION ||--o{ WORKFLOW_STEP : defines
    WORKFLOW_STEP ||--o{ WORKFLOW_TRANSITION : routes
    WORKFLOW_DEFINITION ||--o{ WORKFLOW_INSTANCE : runs_as
    WORKFLOW_INSTANCE ||--o{ WORKFLOW_TASK : creates

    PATIENT ||--o{ AUDIT_EVENT : affects
    APPOINTMENT ||--o{ AUDIT_EVENT : affects
    INVOICE ||--o{ AUDIT_EVENT : affects
    WORKFLOW_INSTANCE ||--o{ AUDIT_EVENT : affects

    USER {
        bigint id PK
        string username
        string role
        bool active
    }
    PATIENT {
        bigint id PK
        string mrn
        string full_name
        date dob
        bool deleted
    }
    DOCTOR {
        bigint id PK
        string full_name
        string department
        bool active
    }
    APPOINTMENT {
        bigint id PK
        bigint patient_id FK
        bigint doctor_id FK
        datetime appointment_time
        string status
    }
    PRESCRIPTION {
        bigint id PK
        bigint appointment_id FK
        bigint patient_id FK
        datetime issued_at
    }
    PRESCRIPTION_ITEM {
        bigint id PK
        bigint prescription_id FK
        bigint medicine_id FK
        string dosage
        int days
    }
    MEDICINE {
        bigint id PK
        string name
        int stock_qty
        decimal unit_price
    }
    INVOICE {
        bigint id PK
        bigint appointment_id FK
        bigint patient_id FK
        decimal total_amount
        string status
    }
    PAYMENT {
        bigint id PK
        bigint invoice_id FK
        decimal amount
        string method
        datetime paid_at
    }
    WORKFLOW_DEFINITION {
        bigint id PK
        string domain
        string status
        int version
    }
    WORKFLOW_STEP {
        bigint id PK
        bigint definition_id FK
        string step_key
        string assignee_role
    }
    WORKFLOW_TRANSITION {
        bigint id PK
        bigint from_step_id FK
        bigint to_step_id FK
        string transition_key
    }
    WORKFLOW_INSTANCE {
        bigint id PK
        bigint definition_id FK
        string entity_type
        bigint entity_id
        string status
    }
    WORKFLOW_TASK {
        bigint id PK
        bigint instance_id FK
        string task_name
        string task_status
        string assignee_role
    }
    AUDIT_EVENT {
        bigint id PK
        string entity_type
        bigint entity_id
        string action
        bigint actor_user_id
        datetime created_at
    }
```

## 11) Redis and Idempotency Working Diagrams

### 11.1 Redis Runtime Responsibilities

```mermaid
flowchart LR
    API[Backend API] --> C1[Cache Keys]
    API --> C2[Rate Limit Buckets]
    API --> C3[Idempotency Records]
    API --> C4[Distributed Locks]

    C1 --> Redis[(Redis)]
    C2 --> Redis
    C3 --> Redis
    C4 --> Redis
```

### 11.2 Idempotency Sequence (Safe Retries)

```mermaid
sequenceDiagram
    participant Client as Client App
    participant FE as Angular UI
    participant API as Spring API
    participant ID as Idempotency Service
    participant Redis as Redis
    participant DB as MySQL

    Client->>FE: Submit payment/create action
    FE->>API: POST with Idempotency-Key
    API->>ID: Check key status
    ID->>Redis: GET idem:{key}

    alt Key exists (already processed)
        Redis-->>ID: Stored response metadata
        ID-->>API: Return cached result
        API-->>FE: 200/201 replay response
    else Key missing
        Redis-->>ID: Not found
        ID->>Redis: SETNX idem:{key}=IN_PROGRESS
        API->>DB: Execute transaction
        DB-->>API: Commit success
        API->>Redis: SET idem:{key}=SUCCESS + response hash + TTL
        API-->>FE: First success response
    end
```

## 12) Notes for Contributors

- Keep sequence diagrams updated when endpoint contracts change.
- Keep module map aligned with package refactors.
- Add new advanced feature diagrams in this file and link from root README.
