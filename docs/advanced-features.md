# Advanced Features: Problem, Purpose, Trade-offs, and Techniques

This document explains each advanced HMS feature in engineering terms:

- Problem solved
- Purpose
- Before vs after impact
- Advantages and disadvantages
- Techniques/methods used
- Dependencies

---

## 1) System Admin Recovery (Soft-Delete Restore)

### Problem Solved

Accidental deletions create operational downtime and DB-level recovery overhead.

### Purpose

Enable safe, auditable recovery of soft-deleted records from UI.

### Before vs After

- Before: manual SQL restore or re-entry, higher risk and delay.
- After: one-click controlled restore by entity ID through secured APIs.

### Advantages

- Faster operational recovery
- Lower data-loss risk
- Better auditability than ad-hoc DB scripts

### Disadvantages

- Requires strict access control
- Needs consistent soft-delete semantics across entities

### Techniques/Methods

- Soft-delete model in entities
- Admin restore endpoints in backend
- Admin UI with confirmation flow and status feedback

### Dependencies

- Spring Security roles
- JPA soft-delete strategy
- Angular admin service/page

---

## 2) Workflow Engine (Definition + Runtime)

### Problem Solved

Clinical process steps are often implicit, inconsistent, and hard to monitor.

### Purpose

Make patient/process flow explicit, configurable, and observable.

### Before vs After

- Before: process logic scattered in services and staff memory.
- After: centralized workflow definitions, transitions, instances, and task tracking.

### Advantages

- Standardized process execution
- Traceability of current step and task ownership
- Easier future expansion (OPD/IPD/Surgery/Discharge)

### Disadvantages

- Added modeling complexity
- Requires strong validation to avoid invalid transitions

### Techniques/Methods

- Definition versioning with activation
- Step/transition modeling
- Runtime instance + task generation
- Transition guards and approval checks

### Dependencies

- Workflow entities/repositories/services/controllers
- Role model and authorization
- UI controls for definition and runtime actions

---

## 3) Backend Security and Access Model

### Problem Solved

Uncontrolled endpoint access can expose protected data and actions.

### Purpose

Guarantee role-aware and ownership-aware API access.

### Before vs After

- Before: coarse route restrictions only.
- After: layered enforcement at route, token, and method levels.

### Advantages

- Stronger policy enforcement
- Reduced privilege escalation risk

### Disadvantages

- More policy maintenance overhead
- Requires coordinated frontend and backend role maps

### Techniques/Methods

- JWT access + refresh lifecycle
- `@PreAuthorize` method guards
- Route guards/interceptors in Angular

### Dependencies

- Spring Security
- JWT libraries (`jjwt-*`)
- Angular auth guards and interceptors

---

## 4) Rate Limiting and Resilience

### Problem Solved

Burst traffic and abuse can degrade response times or availability.

### Purpose

Protect backend availability under load.

### Before vs After

- Before: no fairness or request throttling controls.
- After: distributed rate limits with shared bucket state.

### Advantages

- Better survivability under spikes
- Consistent quota behavior across nodes

### Disadvantages

- Potential user friction when quotas are low
- Tuning effort required per endpoint profile

### Techniques/Methods

- Bucket4j filters
- Redis-backed bucket storage

### Dependencies

- `bucket4j-core`, `bucket4j-redis`
- Redis service

---

## 5) Caching and Performance Optimization

### Problem Solved

Repeated reads on hot entities increase DB pressure and latency.

### Purpose

Reduce p95 latency and DB workload for common read paths.

### Before vs After

- Before: repeated direct DB reads.
- After: cache-assisted reads and reduced repeated queries.

### Advantages

- Faster API responses
- Lower DB resource usage

### Disadvantages

- Cache invalidation complexity
- Potential stale data if invalidation misses occur

### Techniques/Methods

- Spring Cache abstraction
- Redis as distributed cache
- Optional local L1 patterns (Caffeine)

### Dependencies

- `spring-boot-starter-cache`
- `spring-boot-starter-data-redis`
- `caffeine`

---

## 6) Audit Trail and Compliance Visibility

### Problem Solved

Difficult to reconstruct who changed what and when.

### Purpose

Provide operational and security traceability.

### Before vs After

- Before: fragmented logs, hard to query operationally.
- After: centralized audit records + paginated audit UI.

### Advantages

- Better governance and investigation support
- Faster issue triage

### Disadvantages

- Additional storage footprint
- Requires sensitive-detail redaction strategy

### Techniques/Methods

- Audit event persistence
- Slice endpoints + table UI for review

### Dependencies

- Audit module storage + APIs
- Angular audit workspace

---

## 7) Search and Discovery (Elasticsearch-ready)

### Problem Solved

Relational exact-match queries are weak for typo-tolerant operational search.

### Purpose

Enable fast fuzzy discovery across clinical/admin records.

### Before vs After

- Before: exact or narrow searches only.
- After: typo-tolerant, broader query capabilities (when enabled).

### Advantages

- Better usability for real-world data entry variability
- Faster lookup in large datasets

### Disadvantages

- Additional infra and index maintenance cost
- Reindexing lifecycle to manage

### Techniques/Methods

- Elasticsearch index and query model
- Reindex operations and health checks

### Dependencies

- Elasticsearch service
- Search module config and APIs

---

## 8) Frontend Consistency and UX Hardening

### Problem Solved

Inconsistent page typography/layout increases cognitive load and errors.

### Purpose

Standardize admin and data-heavy workflows for usability.

### Before vs After

- Before: per-page visual drift in heading scales and table/form rhythm.
- After: shared layout partials, unified spacing/typography, consistent affordances.

### Advantages

- Faster user comprehension
- Lower training overhead

### Disadvantages

- Requires design discipline for new pages

### Techniques/Methods

- Shared SCSS partials
- Feature-level style normalization
- Pattern reuse for header, cards, and controls

### Dependencies

- Angular component styles
- PrimeNG theming baseline

---

## Dependency Summary Matrix

| Feature               | Key Backend Dependencies                          | Key Frontend Dependencies            |
| --------------------- | ------------------------------------------------- | ------------------------------------ |
| System Admin Recovery | Spring Security, JPA entities/services            | Admin services/components            |
| Workflow Engine       | JPA workflow entities, service layer, role checks | Workflow admin page/service/models   |
| Security              | Spring Security, jjwt                             | Guards, interceptors                 |
| Rate Limiting         | Bucket4j, Redis                                   | 429 handling UX                      |
| Caching               | Spring Cache, Redis/Caffeine                      | Optimized list/detail fetch patterns |
| Audit Trail           | Audit persistence + slice APIs                    | PrimeNG table + filtering/pagination |
| Search                | Elasticsearch modules                             | Search UI/admin controls             |
| UX Consistency        | N/A                                               | Shared SCSS and component patterns   |

---

## Recommended Next Engineering Milestones

1. Enforce workflow starts from backend domain events (not only UI-triggered).
2. Add role-based workflow task inbox and assignment operations.
3. Add SLA/escalation scheduler with notifications.
4. Add transition policy contracts and stronger validation.
5. Add integration tests for multi-step workflow lifecycle.
