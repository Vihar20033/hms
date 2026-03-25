# Lab Module Documentation

The `lab` module manages diagnostic test orders and results.

## Components
- **LabListComponent**: Track pending and completed lab tests.

## Services
- **LabService**: Handles orders and test result uploads.

## Logic Flow: Test Ordering
```mermaid
sequenceDiagram
    participant Doc as Doctor
    participant Lab as LabListComponent
    participant Service as LabService
    participant API as API /lab

    Doc->>Lab: Order Test (e.g., Blood Panel)
    Lab->>Service: createTestOrder(data)
    Service->>API: POST /lab/orders
    API-->>Service: Test ID
    Service-->>Lab: Test Scheduled
```

## Configuration (RBAC)
- **Place Orders**: ADMIN, DOCTOR.
- **Manage Tests**: ADMIN, DOCTOR, LABORATORY_STAFF.
