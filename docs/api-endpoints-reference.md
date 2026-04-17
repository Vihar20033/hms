# HMS API Endpoints Reference

This is the backend API reference grouped by module, including role notes and idempotency requirements.

Base path:

- /api/v1

Response contract:

- Success responses are wrapped with ApiResponse
- Slice/pagination responses use SliceResponse

## 1) Auth

- POST /api/v1/auth/register
- POST /api/v1/auth/login
- POST /api/v1/auth/refresh
- POST /api/v1/auth/change-password
- POST /api/v1/auth/logout

Auth notes:

- Refresh token is managed through cookie utilities.

## 2) Users

- GET /api/v1/users (ADMIN)
- GET /api/v1/users/slice (ADMIN)
- DELETE /api/v1/users/{id} (ADMIN)

## 3) Patients

- POST /api/v1/patients (ADMIN, RECEPTIONIST)
- GET /api/v1/patients (ADMIN, DOCTOR, RECEPTIONIST, PHARMACIST)
- GET /api/v1/patients/slice (ADMIN, DOCTOR, RECEPTIONIST, PHARMACIST)
- GET /api/v1/patients/{id} (ADMIN, DOCTOR, RECEPTIONIST, PHARMACIST)
- PUT /api/v1/patients/{id} (ADMIN, DOCTOR, RECEPTIONIST)
- DELETE /api/v1/patients/{id} (ADMIN, RECEPTIONIST)

## 4) Doctors

- GET /api/v1/doctors
- GET /api/v1/doctors/slice
- GET /api/v1/doctors/{id}
- GET /api/v1/doctors/department/{department}
- GET /api/v1/doctors/{id}/appointment-count (ADMIN)
- POST /api/v1/doctors (ADMIN)
- PUT /api/v1/doctors/{id} (ADMIN)
- DELETE /api/v1/doctors/{id} (ADMIN)

## 5) Appointments

- GET /api/v1/appointments/summary (ADMIN, RECEPTIONIST, DOCTOR)
- POST /api/v1/appointments (ADMIN, RECEPTIONIST, DOCTOR)
- GET /api/v1/appointments (ADMIN, RECEPTIONIST, DOCTOR)
- GET /api/v1/appointments/me (PATIENT)
- GET /api/v1/appointments/today (ADMIN, RECEPTIONIST, DOCTOR)
- GET /api/v1/appointments/{id} (ADMIN, DOCTOR, RECEPTIONIST, PATIENT)
- PUT /api/v1/appointments/{id} (ADMIN, RECEPTIONIST)
- DELETE /api/v1/appointments/{id} (ADMIN, RECEPTIONIST)
- PATCH /api/v1/appointments/{id}/check-in (ADMIN, RECEPTIONIST)
- PATCH /api/v1/appointments/{id}/start (ADMIN, DOCTOR)
- PATCH /api/v1/appointments/{id}/complete (ADMIN, DOCTOR)
- PATCH /api/v1/appointments/reassign (ADMIN, RECEPTIONIST)
- GET /api/v1/appointments/queue?doctorId={doctorId} (ADMIN, DOCTOR)
- GET /api/v1/appointments/queue/next?doctorId={doctorId} (ADMIN, DOCTOR)

Idempotency header required:

- PATCH /api/v1/appointments/{id}/check-in
- PATCH /api/v1/appointments/{id}/start
- PATCH /api/v1/appointments/{id}/complete

Required header:

- X-Idempotency-Key: unique-per-operation-key

## 6) Prescriptions

- POST /api/v1/prescriptions (ADMIN, DOCTOR)
- GET /api/v1/prescriptions/slice (ADMIN, DOCTOR, PHARMACIST)
- GET /api/v1/prescriptions/patient/{patientId} (ADMIN, DOCTOR, PHARMACIST)
- GET /api/v1/prescriptions/{id} (ADMIN, DOCTOR, PHARMACIST)
- DELETE /api/v1/prescriptions/{id} (ADMIN, DOCTOR)

Idempotency header required:

- POST /api/v1/prescriptions

## 7) Medicines and Inventory

- POST /api/v1/medicines (ADMIN, PHARMACIST)
- PUT /api/v1/medicines/{id} (ADMIN, PHARMACIST)
- DELETE /api/v1/medicines/{id} (ADMIN, PHARMACIST)
- GET /api/v1/medicines/{id} (ADMIN, DOCTOR, PHARMACIST)
- GET /api/v1/medicines (ADMIN, DOCTOR, PHARMACIST)
- GET /api/v1/medicines/slice (ADMIN, DOCTOR, PHARMACIST)
- GET /api/v1/medicines/active (ADMIN, DOCTOR, PHARMACIST)
- GET /api/v1/medicines/category/{category} (ADMIN, DOCTOR, PHARMACIST)
- GET /api/v1/medicines/check-code/{code}
- POST /api/v1/medicines/dispense (PHARMACIST)
- PATCH /api/v1/medicines/{id}/restock (ADMIN, PHARMACIST)
- GET /api/v1/pharmacy/inventory-log (ADMIN, PHARMACIST)

## 8) Billings

- POST /api/v1/billings (ADMIN, RECEPTIONIST)
- GET /api/v1/billings (ADMIN, RECEPTIONIST)
- GET /api/v1/billings/paged (ADMIN, RECEPTIONIST)
- GET /api/v1/billings/slice (ADMIN, RECEPTIONIST)
- GET /api/v1/billings/patient/{patientId} (ADMIN, RECEPTIONIST)
- GET /api/v1/billings/me (PATIENT)
- GET /api/v1/billings/preview-appointment/{appointmentId} (ADMIN, RECEPTIONIST)
- GET /api/v1/billings/{id} (ADMIN, RECEPTIONIST)
- PATCH /api/v1/billings/{id}/status?status={PaymentStatus} (ADMIN, RECEPTIONIST)
- PATCH /api/v1/billings/{id}/pay (PATIENT)
- POST /api/v1/billings/generate/appointment/{appointmentId} (ADMIN, RECEPTIONIST)
- DELETE /api/v1/billings/{id} (ADMIN)

Idempotency header required:

- POST /api/v1/billings/generate/appointment/{appointmentId}

## 9) Dashboard

- GET /api/v1/dashboard/summary (ADMIN, DOCTOR, RECEPTIONIST, PHARMACIST)

## 10) Audit

- GET /api/v1/audit-logs/slice (ADMIN)
- GET /api/v1/audit/history/{entityType}/{id}

Entity history supported entityType values:

- patient
- billing
- appointment
- prescription

## 11) Admin Recovery

- POST /api/v1/admin/system/restore/patient/{id} (ADMIN)
- POST /api/v1/admin/system/restore/doctor/{id} (ADMIN)
- POST /api/v1/admin/system/restore/user/{id} (ADMIN)
- POST /api/v1/admin/system/restore/billing/{id} (ADMIN)

## 12) Workflows

- POST /api/v1/workflows/definitions (ADMIN)
- PUT /api/v1/workflows/definitions/{definitionKey}/versions/{versionNumber}/activate (ADMIN)
- GET /api/v1/workflows/definitions (ADMIN, DOCTOR, RECEPTIONIST)
- POST /api/v1/workflows/instances (ADMIN, DOCTOR, RECEPTIONIST)
- POST /api/v1/workflows/instances/{instanceId}/transition (ADMIN, DOCTOR, RECEPTIONIST)
- GET /api/v1/workflows/instances/{instanceId} (ADMIN, DOCTOR, RECEPTIONIST)
- GET /api/v1/workflows/instances/{instanceId}/tasks (ADMIN, DOCTOR, RECEPTIONIST)

## 13) Operational Endpoints (Non /api/v1)

- GET /actuator/health
