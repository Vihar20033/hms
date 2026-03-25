# Pharmacy Module Documentation

The `pharmacy` module manages drug inventory and stock movement.

## Components
- **PharmacyListComponent**: View and search medicine stock.
- **InventoryLogComponent**: Historical log of stock adjustments.

## Services
- **PharmacyService**: Handles stock updates and auditing.

## Logic Flow: Stock Update
```mermaid
sequenceDiagram
    participant User
    participant List as PharmacyListComponent
    participant Service as PharmacyService
    participant Backend as API /pharmaceuticals

    User->>List: Update Stock (Add/Remove)
    List->>Service: updateStock(id, amount)
    Service->>Backend: PATCH /stock
    Backend-->>Service: Updated Stock Object
    Service-->>List: Refresh UI
```

## Configuration (RBAC)
- **Access**: Restricted to ADMIN and PHARMACIST.
