# Appointment Module Documentation

The `appointments` module manages the scheduling of patient visits.

## Components
- **AppointmentListComponent**: Calendar and list view for daily appointments.
- **AppointmentBookingComponent**: Wizard for selecting a doctor, date, and time slot.

## Services
- **AppointmentService**: Handles booking, status updates (Check-in, Start, Complete), and summary statistics.

## Logic Flow: Appointment Lifecycle
```mermaid
stateDiagram-v2
    [*] --> Scheduled
    Scheduled --> CheckedIn: Patient Arrives
    CheckedIn --> InConsultation: Doctor Starts
    InConsultation --> Completed: Visit Over
    InConsultation --> Cancelled: Patient Leaves Early
```

## Configuration (RBAC)
- **Booking**: ADMIN, RECEPTIONIST.
- **View Schedule**: ADMIN, DOCTOR, NURSE, RECEPTIONIST.
