# Clinical Module (Vitals) Documentation

The `clinical` module focuses on the collection and tracking of patient medical data.

## Components
- **VitalsListComponent**: Shows a list of recent vitals recorded across the hospital.
- **VitalsRecordComponent**: Form used to record BP, Temperature, Pulse, etc., linked to a specific Appointment.

## Services
- **VitalsService**: Handles CRUD operations for vital signs.

## Logic Flow: Recording Vitals
```mermaid
sequenceDiagram
    participant Nurse
    participant Form as VitalsRecordComponent
    participant Service as VitalsService
    participant API as Backend

    Nurse->>Form: Enter Vitals Data
    Form->>Service: createVitals(data)
    Service->>API: POST /clinical/vitals
    API-->>Service: Created Object
    Service-->>Form: Success
    Form-->>Nurse: Record Saved!
```

## Configuration
- **Access**: Restricted to ADMIN and NURSE.
