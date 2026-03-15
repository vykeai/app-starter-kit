# Internal Starter Libraries

This repo stays runnable as the starter reference app, but the reusable seams
inside it should converge on a small set of internal libraries before any repo
split happens.

Current boundaries:

- `auth-core`
  - backend auth module
  - auth DTOs
  - deep-link auth payload contract
  - client auth state-machine pattern
- `network-core`
  - API client base
  - token refresh behavior
  - request ID behavior
  - runtime fixture transport hooks
- `notifications-core`
  - preference model
  - device registration contract
  - push registration manager shape
- `contracts`
  - Sentinel schemas
  - route contracts
  - fixture and screenshot proof expectations
- `user-core`
  - current-user profile fetch/update
  - lightweight preference summary
  - app-shell user projection
- `media-core`
  - upload preparation/finalization
  - media asset metadata
  - list/delete operations
- `billing-core`
  - entitlement state
  - native purchase verification contract
  - feature-gating inputs
- `analytics-core`
  - event ingest contract
  - event naming conventions
  - starter summary shape

These are internal boundaries first, not published packages yet.
