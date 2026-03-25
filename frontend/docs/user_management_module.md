# User Management Module Documentation

The `users` module allows administrators to control application security and identity.

## Components
- **UserListComponent**: View all internal app users, change roles, or deactivate accounts.

## Configuration (RBAC)
- **Access**: Strictly restricted to ADMIN.
- **Route**: Maped to `/users`.

```mermaid
graph TD
    A[Admin Access /users] --> B[Fetch All Internal Users]
    B --> C{Action?}
    C -- Edit Role --> D[PUT /api/v1/users/role]
    C -- Reset Pass --> E[POST /api/v1/users/reset]
    C -- Delete --> F[DELETE /api/v1/users]
```
