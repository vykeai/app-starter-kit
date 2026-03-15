# Starter Auth Module

Current runtime status:

- implemented:
  - email challenge request via legacy magic-link endpoint
  - email code verification via legacy magic-link endpoint
  - refresh
  - logout
- partially implemented:
  - social auth route shape exists
- not yet fully aligned with the canonical starter standard:
  - `challengeId`
  - `linkToken`
  - separate `/auth/apple` and `/auth/google` runtime handlers
  - non-production delivery mode behavior
  - dev/review bypass handling

Canonical target:

- see [AUTH_SYSTEM.md](/Users/ted/dev/onlystack/docs/AUTH_SYSTEM.md)

Migration order:

1. add challenge model support (`challengeId`, `linkToken`, expiry metadata)
2. implement provider verifiers for Apple and Google
3. expose canonical runtime routes alongside legacy aliases
4. update mobile clients to store pending auth payloads at app scope
5. add screenshot-backed auth proof in iOS + Android E2E flows
