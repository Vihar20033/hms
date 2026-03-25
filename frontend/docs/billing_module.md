# Billing Module Documentation

The `billing` module manages invoicing and payment workflows.

## Components
- **BillingListComponent**: View all generated bills and their payment statuses.

## Services
- **BillingService**: Handles bill generation from appointments, status updates (Paid/Unpaid), and previews.

## Logic Flow: Bill Generation
```mermaid
sequenceDiagram
    participant App as AppointmentService
    participant Bill as BillingService
    participant API as Backend

    App->>API: Complete Consultation
    API->>Bill: Trigger Bill Generation
    Bill->>API: POST /billings/generate/appointment/{id}
    API-->>Bill: Created Invoice
    Bill-->>App: Success
```

## Configuration (RBAC)
- **Access**: Restricted to ADMIN and RECEPTIONIST.
