# HMS Module Working Guide

This document explains how each backend and frontend module works, what business flow it handles, which APIs it uses/exposes, and which advanced platform features are applied.

## 1) End-to-End Runtime Pattern

1. Frontend page calls a feature service.
2. Service sends request to backend API under /api/v1.
3. Security filters validate JWT and role policies.
4. Rate limiter may throttle requests (Redis-backed when enabled).
5. Controller validates input and delegates to service layer.
6. Service performs business logic with repository access.
7. Optional advanced logic runs: cache, distributed lock, idempotency, audit.
8. API returns standardized response envelope.

## 2) Backend Modules

## 2.1 Auth Module

Purpose:

- User registration, login, refresh, password change, logout.

Working:

- Login/refresh issue auth response and set refresh token cookie.
- Logout revokes/clears session state.

Controller:

- com.hms.auth.controller.AuthController

Endpoints:

- POST /api/v1/auth/register
- POST /api/v1/auth/login
- POST /api/v1/auth/refresh
- POST /api/v1/auth/change-password
- POST /api/v1/auth/logout

Advanced features used:

- Cookie-based refresh token handling
- JWT authentication integration
- Standard ApiResponse envelope

## 2.2 User Module

Purpose:

- Admin-only user listing and deactivation/removal.

Working:

- Supports searchable slice for scalable user table rendering.

Controller:

- com.hms.user.controller.UserController

Endpoints:

- GET /api/v1/users
- GET /api/v1/users/slice
- DELETE /api/v1/users/{id}

Advanced features used:

- Role-based authorization with method-level guards
- Pagination via SliceResponse

## 2.3 Patient Module

Purpose:

- Patient registration and profile lifecycle.

Working:

- Create/update/delete for operational staff.
- Searchable and paginated listing for large datasets.

Controller:

- com.hms.patient.controller.PatientController

Endpoints:

- POST /api/v1/patients
- GET /api/v1/patients
- GET /api/v1/patients/slice
- GET /api/v1/patients/{id}
- PUT /api/v1/patients/{id}
- DELETE /api/v1/patients/{id}

Advanced features used:

- Service-level caching (patients cache)
- Role-aware endpoint policies
- Search + slice optimization

## 2.4 Doctor (Staff) Module

Purpose:

- Doctor onboarding, listing, updates, and department filtering.

Working:

- Admin can onboard/update/delete doctors.
- All authorized staff can browse doctors and filters.

Controller:

- com.hms.doctor.controller.DoctorController

Endpoints:

- GET /api/v1/doctors/{id}/appointment-count
- GET /api/v1/doctors
- GET /api/v1/doctors/slice
- GET /api/v1/doctors/{id}
- POST /api/v1/doctors
- PUT /api/v1/doctors/{id}
- DELETE /api/v1/doctors/{id}
- GET /api/v1/doctors/department/{department}

Advanced features used:

- Service-level caching (doctors cache)
- Search + slice optimization

## 2.5 Appointment Module

Purpose:

- Appointment booking and visit-status progression.

Working:

- Create/list/update/reassign appointments.
- Queue endpoints support doctor queue operations.
- Idempotent status transitions prevent duplicate state changes.

Controller:

- com.hms.appointment.controller.AppointmentController

Endpoints:

- GET /api/v1/appointments/summary
- POST /api/v1/appointments
- GET /api/v1/appointments
- GET /api/v1/appointments/me
- GET /api/v1/appointments/today
- GET /api/v1/appointments/{id}
- PUT /api/v1/appointments/{id}
- DELETE /api/v1/appointments/{id}
- PATCH /api/v1/appointments/{id}/check-in
- PATCH /api/v1/appointments/{id}/start
- PATCH /api/v1/appointments/{id}/complete
- PATCH /api/v1/appointments/reassign
- GET /api/v1/appointments/queue
- GET /api/v1/appointments/queue/next

Advanced features used:

- Idempotency key enforcement for transition endpoints
- Redis counter usage for token sequencing in service implementation
- Role-specific views (staff vs patient)

## 2.6 Prescription Module

Purpose:

- Clinical prescription creation and retrieval.

Working:

- Prescription creation uses idempotency to avoid duplicate writes.
- Slice-first listing prevents unbounded data fetch.

Controller:

- com.hms.prescription.controller.PrescriptionController

Endpoints:

- POST /api/v1/prescriptions
- GET /api/v1/prescriptions/slice
- GET /api/v1/prescriptions/patient/{patientId}
- GET /api/v1/prescriptions/{id}
- DELETE /api/v1/prescriptions/{id}

Advanced features used:

- Idempotency service for create operation
- Pagination and bounded query strategy

## 2.7 Pharmacy Module

Purpose:

- Medicine master data, dispense, restock, and inventory logs.

Working:

- Medicine CRUD with category/active filters.
- Dispense and restock update inventory and create log history.
- Inventory log endpoint is paginated.

Controllers:

- com.hms.pharmacy.controller.MedicineController
- com.hms.pharmacy.controller.InventoryController

Endpoints:

- POST /api/v1/medicines
- PUT /api/v1/medicines/{id}
- DELETE /api/v1/medicines/{id}
- GET /api/v1/medicines/{id}
- GET /api/v1/medicines
- GET /api/v1/medicines/slice
- GET /api/v1/medicines/active
- GET /api/v1/medicines/category/{category}
- GET /api/v1/medicines/check-code/{code}
- POST /api/v1/medicines/dispense
- PATCH /api/v1/medicines/{id}/restock
- GET /api/v1/pharmacy/inventory-log

Advanced features used:

- Service-level medicines cache
- Slice pagination for inventory and medicine listings

## 2.8 Billing Module

Purpose:

- Invoice generation, payment state transitions, and patient bill tracking.

Working:

- Supports manual create and appointment-based generation.
- Uses idempotency for billing generation from appointment.
- Includes patient self-pay flow and preview endpoint.

Controller:

- com.hms.billing.controller.BillingController

Endpoints:

- POST /api/v1/billings
- GET /api/v1/billings
- GET /api/v1/billings/paged
- GET /api/v1/billings/slice
- GET /api/v1/billings/patient/{patientId}
- GET /api/v1/billings/me
- GET /api/v1/billings/preview-appointment/{appointmentId}
- GET /api/v1/billings/{id}
- PATCH /api/v1/billings/{id}/status
- PATCH /api/v1/billings/{id}/pay
- POST /api/v1/billings/generate/appointment/{appointmentId}
- DELETE /api/v1/billings/{id}

Advanced features used:

- Idempotency for generate endpoint
- Slice pagination and query support

## 2.9 Dashboard Module

Purpose:

- Consolidated operational summary for role dashboards.

Controller:

- com.hms.dashboard.controller.DashboardController

Endpoints:

- GET /api/v1/dashboard/summary

Advanced features used:

- Aggregated read model from dashboard service/repository layer

## 2.10 Audit Module

Purpose:

- System-level action traceability and revision history lookup.

Working:

- Admin can query audit log slices and entity history timelines.

Controllers:

- com.hms.common.audit.AuditLogController
- com.hms.common.audit.EntityHistoryController

Endpoints:

- GET /api/v1/audit-logs/slice
- GET /api/v1/audit/history/{entityType}/{id}

Advanced features used:

- Searchable audit log pagination
- Entity revision history adapter

## 2.11 Admin Recovery Module

Purpose:

- Restore soft-deleted records safely from admin console.

Controller:

- com.hms.admin.controller.SystemAdminController

Endpoints:

- POST /api/v1/admin/system/restore/patient/{id}
- POST /api/v1/admin/system/restore/doctor/{id}
- POST /api/v1/admin/system/restore/user/{id}
- POST /api/v1/admin/system/restore/billing/{id}

Advanced features used:

- Role-locked restore actions
- Audit-compatible recovery path

## 2.12 Workflow Module

Purpose:

- Runtime process orchestration across clinical/admin domain events.

Working:

- Admin defines and activates workflows.
- Operational roles start instances and perform transitions.
- Instance/task APIs expose current process state.

Controller:

- com.hms.workflow.controller.WorkflowEngineController

Endpoints:

- POST /api/v1/workflows/definitions
- PUT /api/v1/workflows/definitions/{definitionKey}/versions/{versionNumber}/activate
- GET /api/v1/workflows/definitions
- POST /api/v1/workflows/instances
- POST /api/v1/workflows/instances/{instanceId}/transition
- GET /api/v1/workflows/instances/{instanceId}
- GET /api/v1/workflows/instances/{instanceId}/tasks

Advanced features used:

- Definition versioning and controlled activation
- Step/transition runtime task orchestration

## 2.13 Cross-Cutting Security and Platform Module

Purpose:

- Shared reliability, security, and consistency infrastructure.

Main implementation areas:

- Security config and JWT filters
- Rate limiting filter and Bucket4j Redis proxy manager
- Cache manager and cache annotations in services
- Redisson/Redis lock and counter implementations
- Idempotency service with Redis-backed key records

Advanced features used:

- JWT + role policy enforcement
- Redis-backed distributed rate limiting
- Redis cache manager and Caffeine fallback path
- Redis-backed idempotency records with pending/done lifecycle
- Distributed lock/counter abstractions

## 3) Frontend Modules

## 3.1 Auth Module

Main files:

- features/auth/pages/login
- features/auth/pages/register
- features/auth/pages/change-password
- features/auth/services/auth.service.ts

Working:

- Handles login/register/refresh/logout/password-change flows.
- Maintains in-memory current user and access token state.
- Uses withCredentials for refresh/logout cookie path.

APIs consumed:

- POST /api/v1/auth/login
- POST /api/v1/auth/register
- POST /api/v1/auth/refresh
- POST /api/v1/auth/logout
- POST /api/v1/auth/change-password

Advanced features used:

- Guard-driven protected route flow
- Session bootstrap with refresh fallback

## 3.2 Dashboard Module

Main files:

- features/dashboard/pages/dashboard.component.ts
- features/dashboard/services/dashboard.service.ts

APIs consumed:

- GET /api/v1/dashboard/summary

Advanced features used:

- Role-based route access

## 3.3 Users Module

Main files:

- features/users/pages/user-list
- features/users/services/user.service.ts

APIs consumed:

- GET /api/v1/users
- GET /api/v1/users/slice
- DELETE /api/v1/users/{id}

Advanced features used:

- Server-side slice pagination and search

## 3.4 Patients Module

Main files:

- features/patients/pages/patient-list
- features/patients/pages/patient-registration
- features/patients/services/patient.service.ts

APIs consumed:

- GET /api/v1/patients
- GET /api/v1/patients/slice
- GET /api/v1/patients/{id}
- POST /api/v1/patients
- PUT /api/v1/patients/{id}
- DELETE /api/v1/patients/{id}

Advanced features used:

- Form validation and slice-based listing

## 3.5 Appointments Module

Main files:

- features/appointments/pages/appointment-list
- features/appointments/pages/appointment-booking
- features/appointments/services/appointment.service.ts

APIs consumed:

- GET /api/v1/appointments/summary
- GET /api/v1/appointments
- GET /api/v1/appointments/{id}
- POST /api/v1/appointments
- PUT /api/v1/appointments/{id}
- PATCH /api/v1/appointments/{id}/check-in
- PATCH /api/v1/appointments/{id}/start
- PATCH /api/v1/appointments/{id}/complete
- PATCH /api/v1/appointments/reassign
- GET /api/v1/appointments/today
- DELETE /api/v1/appointments/{id}

Advanced features used:

- Client-generated X-Idempotency-Key for transition operations
- Workflow kick-off integration from booking flow

## 3.6 Staff (Doctor) Module

Main files:

- features/staff/pages/doctor-list
- features/staff/pages/doctor-registration
- features/staff/services/doctor.service.ts

APIs consumed:

- GET /api/v1/doctors
- GET /api/v1/doctors/slice
- GET /api/v1/doctors/{id}
- GET /api/v1/doctors/department/{department}
- POST /api/v1/doctors
- PUT /api/v1/doctors/{id}
- DELETE /api/v1/doctors/{id}
- GET /api/v1/doctors/{id}/appointment-count

Advanced features used:

- Slice pagination and admin-managed onboarding

## 3.7 Prescription Module

Main files:

- features/prescription/pages/prescription-list
- features/prescription/pages/prescription-create
- features/prescription/pages/prescription-detail
- features/prescription/services/prescription.service.ts

APIs consumed:

- GET /api/v1/prescriptions/slice
- GET /api/v1/prescriptions/{id}
- GET /api/v1/prescriptions/patient/{patientId}
- POST /api/v1/prescriptions
- DELETE /api/v1/prescriptions/{id}

Advanced features used:

- Client-generated X-Idempotency-Key for create operation

## 3.8 Pharmacy Module

Main files:

- features/pharmacy/pages/pharmacy-list
- features/pharmacy/pages/inventory-log
- features/pharmacy/services/pharmacy.service.ts

APIs consumed:

- GET /api/v1/medicines
- GET /api/v1/medicines/slice
- GET /api/v1/medicines/active
- GET /api/v1/medicines/{id}
- POST /api/v1/medicines
- PUT /api/v1/medicines/{id}
- PATCH /api/v1/medicines/{id}/restock
- DELETE /api/v1/medicines/{id}
- GET /api/v1/pharmacy/inventory-log

Advanced features used:

- Paginated inventory logs and stock operations

## 3.9 Billing Module

Main files:

- features/billing/billing-list
- features/billing/components/\*
- features/billing/services/billing.service.ts

APIs consumed:

- GET /api/v1/billings/slice
- GET /api/v1/billings/{id}
- GET /api/v1/billings/patient/{patientId}
- GET /api/v1/billings/me
- POST /api/v1/billings
- PATCH /api/v1/billings/{id}/status
- PATCH /api/v1/billings/{id}/pay
- POST /api/v1/billings/generate/appointment/{appointmentId}
- GET /api/v1/billings/preview-appointment/{appointmentId}
- DELETE /api/v1/billings/{id}

Advanced features used:

- Client-generated X-Idempotency-Key for generate operation
- Slice pagination and filtered listing

## 3.10 Audit Module

Main files:

- features/audit/audit-log-page.component.ts
- features/audit/services/audit-log.service.ts

APIs consumed:

- GET /api/v1/audit-logs/slice

Advanced features used:

- Search + pagination for large audit data

## 3.11 Admin Module

Main files:

- features/admin/system-admin
- features/admin/services/admin.service.ts

APIs consumed:

- GET /actuator/health
- POST /api/v1/admin/system/restore/patient/{id}
- POST /api/v1/admin/system/restore/doctor/{id}
- POST /api/v1/admin/system/restore/user/{id}
- POST /api/v1/admin/system/restore/billing/{id}

Advanced features used:

- Protected admin-only operations
- Recovery tooling for soft-deleted entities

## 3.12 Workflow Admin Module

Main files:

- features/admin/workflow-admin
- features/admin/services/workflow-admin.service.ts

APIs consumed:

- POST /api/v1/workflows/definitions
- PUT /api/v1/workflows/definitions/{definitionKey}/versions/{versionNumber}/activate
- GET /api/v1/workflows/definitions
- POST /api/v1/workflows/instances
- POST /api/v1/workflows/instances/{instanceId}/transition
- GET /api/v1/workflows/instances/{instanceId}
- GET /api/v1/workflows/instances/{instanceId}/tasks

Advanced features used:

- Runtime workflow transition and task introspection

## 3.13 Nursing Module (Current Frontend State)

Main files:

- features/nursing/services/nursing.service.ts

Frontend API expectation:

- POST /api/v1/nursing/triage

Status note:

- No backend controller currently exposes /api/v1/nursing/triage.
- Keep this module behind route/feature-flag until backend endpoint is implemented.

## 4) Integration and Consistency Notes

1. All feature services use environment.apiUrl and shared ApiResponse models.
2. Route access is guarded via authGuard and roleGuard with ROUTE_ROLES mapping.
3. Backend source of truth for endpoint contracts is controller mappings.
4. Known contract drifts to review:
   - Frontend AppointmentService.updateStatus calls PATCH /appointments/{id}/status, while backend exposes check-in/start/complete endpoints instead.
   - Frontend PharmacyService.getLowStock calls GET /medicines/low-stock, but backend MedicineController does not currently expose this endpoint.
