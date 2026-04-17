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

## 8) Notes for Contributors

- Keep sequence diagrams updated when endpoint contracts change.
- Keep module map aligned with package refactors.
- Add new advanced feature diagrams in this file and link from root README.
