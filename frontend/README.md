# Frontend (Angular 17)

Frontend SPA for Artem Health HMS.

## Purpose

Delivers role-based hospital workspaces with secure navigation, clear operational flows, and responsive data handling.

## Stack

- Angular 17 standalone components
- PrimeNG + PrimeIcons
- RxJS
- Angular Router guards
- HTTP interceptors

## Major UX Workspaces

- Dashboard
- Users
- Patients
- Appointments
- Staff/Doctors
- Prescriptions
- Pharmacy
- Billing
- Audit Trail
- System Admin
- Workflow Admin

## Cross-Cutting Frontend Patterns

- Route-level role protection
- Centralized auth/session service
- JWT request interceptor
- Error interception and modal feedback
- Shared layout shell (`header`, `sidebar`)
- Feature-scoped services and models

## Run

```bash
cd frontend
npm install
npm start
```

App URL: `http://localhost:4200`

## Build

```bash
npm run build
```

## Lint and Format

```bash
npm run lint
npm run lint:fix
npm run format
```

## Dependency Notes

- `primeng`, `primeicons`: data-heavy enterprise UI
- `chart.js`: dashboard visualizations
- `jspdf`, `pdfmake`, `xlsx`: export/reporting tooling

## Architecture Notes

- Routes and role maps are the first security UX boundary; backend remains source of truth.
- New features should follow the existing pattern:
  - `models` -> `services` -> `pages/components`
  - explicit API contracts through typed interfaces
  - centralized feedback (success/error/warning)
