# Backend (Spring Boot)

Backend service for Artem Health HMS.

## Purpose

Provides secure, auditable, and role-aware APIs for hospital operations:

- Clinical flows (patients, appointments, prescriptions)
- Administrative flows (users, billing, audit)
- Operational control (workflow engine, system recovery)

## Tech Stack

- Spring Boot 3.3
- Spring Security 6
- Spring Data JPA (MySQL)
- Spring Data Redis
- Bucket4j (rate limiting)
- Redisson (distributed lock support)
- MapStruct + Lombok
- Hibernate Envers

## Key Capabilities

- JWT authentication and role authorization
- Method-level access controls
- Soft-delete aware operations and restoration endpoints
- Workflow definition and runtime instance APIs
- Standardized API response model
- Audit-friendly write paths

## Run Locally

```bash
cd backend
.\mvnw.cmd clean spring-boot:run
```

## Build

```bash
cd backend
.\mvnw.cmd clean -DskipTests compile
```

## Main Modules

- `auth`, `security`: identity/session controls
- `user`: user and role operations
- `patient`: patient domain
- `appointment`: scheduling and status lifecycle
- `doctor`/`staff`: doctor management
- `prescription`: treatment orders
- `pharmacy`: medicine and inventory logs
- `billing`: invoice and payment workflows
- `audit`: operational audit records
- `workflow`: runtime process orchestration
- `admin`: system admin restore controls

## API Conventions

- Base path: `/api/v1/...`
- Success envelope: `ApiResponse<T>`
- Role guards via `@PreAuthorize`

## Performance and Reliability Patterns

- Redis-backed distributed rate limit buckets
- Cache-first read paths for selected resources
- Idempotency support for state-changing operations
- Defensive validation in service layer

## Dependencies (Operational)

- MySQL 8+
- Redis 7+
- Elasticsearch (optional, when search enabled)

## Security Notes

- Never run production with default JWT secrets
- Restrict CORS origins in production
- Use profile-specific secure settings (`prod`)
