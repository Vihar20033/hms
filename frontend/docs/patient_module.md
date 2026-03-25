# Patient Module Documentation

The `patients` module handles the registration and lifecycle of patients in the HMS.

## Components
- **PatientListComponent**: Displays a paginated list of all patients with search filters (Name, Email, Blood Group).
- **PatientRegistrationComponent**: Form for registering new patients or editing existing ones.

## Services
- **PatientService**: Manages patient data synchronisation with the backend.

## Logic Flow: Patient Search
```mermaid
sequenceDiagram
    participant UI as PatientListComponent
    participant Service as PatientService
    participant Backend as API /patients

    UI->>Service: search(params)
    Service->>Backend: GET /patients?name=...
    Backend-->>Service: PagedResponse<Patient>
    Service-->>UI: Update List View
```

## Configuration (RBAC)
- **View List**: ADMIN, DOCTOR, NURSE, RECEPTIONIST, PHARMACIST.
- **Register Patient**: ADMIN, RECEPTIONIST.
