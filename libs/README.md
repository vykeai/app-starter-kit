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

These are internal boundaries first, not published packages yet.
