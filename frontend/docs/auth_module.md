# Auth Module Documentation

The `auth` feature handles user authentication flows and identity management.

## Components
- **LoginComponent**: Collects credentials and initiates session.
- **RegisterComponent**: Handles user creation.
- **ChangePasswordComponent**: Manages mandatory or user-initiated password updates.
- **UnauthorizedComponent**: Static page shown when a user lacks route permission.

## Services
- **AuthService**: (See `core_module.md`)

## Logic Flow: Login Process
```mermaid
sequenceDiagram
    participant User as Frontend User
    participant Login as LoginComponent
    participant Auth as AuthService
    participant API as Backend API

    User->>Login: Submit Credentials
    Login->>Auth: login(request)
    Auth->>API: POST /auth/login
    API-->>Auth: JWT + User Metadata
    Auth->>Auth: Store in SessionStorage
    Auth->>Auth: Update Signals (currentUser)
    Auth->>Login: Success Response
    Login-->>User: Redirect to /dashboard
```

## Configuration (RBAC)
- **Login/Register**: Wrapped in `guestGuard`.
- **Change Password**: Wrapped in `authGuard`.
