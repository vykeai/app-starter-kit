# Starter Auth System

Canonical auth standard for products built from `Onlystack`.

This document merges the strongest parts of:
- `univiirse` frontend auth orchestration and screenshot-backed proof
- `goala` backend auth hardening and provider verification
- `sitches` and `fitkind` starter flow simplicity

It is the source of truth for:
- backend auth contract
- frontend auth handling
- deep-link semantics
- mock-mode auth
- non-email environment auth

## Goals

- One backend auth module shape across products
- One frontend auth state-machine shape across platforms
- Native UI per product, but identical auth handling rules
- Email sign-in supports both:
  - manual code entry
  - one-tap deep-link resume
- Apple Sign In supported where relevant
- Google Sign In supported everywhere applicable
- Mock mode never leaks into UI code
- Simulator screenshots prove auth works in practice

## Standard Backend Contract

Canonical endpoints:

- `POST /api/v1/auth/challenge/request`
  - body: `{ email, purpose }`
  - response: `{ challengeId, expiresAt, resendAt, delivery }`
- `POST /api/v1/auth/challenge/verify`
  - body:
    - manual: `{ challengeId, code }`
    - one-tap: `{ linkToken }`
  - response: `{ accessToken, refreshToken, user, nextStep }`
- `POST /api/v1/auth/apple`
  - body: `{ identityToken, name? }`
- `POST /api/v1/auth/google`
  - body: `{ idToken }`
- `POST /api/v1/auth/refresh`
  - body: `{ refreshToken }`
- `POST /api/v1/auth/logout`
  - body: `{ refreshToken }`
- `DELETE /api/v1/auth/session`
  - revokes all sessions for current user
- `GET /api/v1/auth/me`
  - returns current session user

`nextStep` is required in auth success responses. Minimum enum:
- `authenticated`
- `needs_onboarding`
- `needs_registration`

## Email Challenge Rules

Do not model email auth as `email + code only`.

Every challenge must produce:
- a visible short code for manual entry
- an opaque `linkToken` for deep-link resume
- a server-side `challengeId`

The email body should contain:
- the short code for typing
- a valid `Open App` link carrying `linkToken`

Deep links must not rely on the app already remembering the email address.

## Security Rules

- Codes are short-lived and single-use
- Link tokens are opaque, single-use, and short-lived
- Refresh tokens are server-stored and revocable
- Failed verification attempts are rate-limited and lock out abusive retries
- Social sign-in tokens are always verified with Apple/Google, never trusted directly from client claims
- Production never supports auth bypass

## Frontend Auth State Machine

All clients should implement the same logical states:

- `signed_out`
- `collecting_email`
- `awaiting_challenge`
- `consuming_link`
- `verifying_code`
- `social_signing_in`
- `session_established`
- `post_auth_gate`
- `authenticated`
- `error`

All clients should support the same events:

- `submit_email`
- `challenge_sent`
- `link_received`
- `code_entered`
- `verify_succeeded`
- `verify_failed`
- `social_started`
- `social_succeeded`
- `social_failed`
- `refresh_succeeded`
- `logout`

Critical rule:

- Deep-link auth payload is stored at app scope first
- Auth UI consumes pending auth payload when ready
- Incoming auth links must not be dropped just because the app is on the wrong screen

## Deep-Link Contract

Canonical auth deep-link shape:

- Universal/App link: `https://<app-domain>/auth/verify?linkToken=...`
- Custom-scheme fallback: `<app-scheme>://auth/verify?linkToken=...`

Optional diagnostic/dev shape:

- `<app-scheme>://auth/verify?challengeId=...&code=...`

Platform rule:

- iOS and Android must use the same custom-scheme semantics
- Template names like `appstarterkit://` must be replaced during project setup

## Mock Mode

Mock mode is infrastructure, not UI behavior.

Allowed:
- fixture-backed API interception
- deterministic auth fixtures
- pre-authenticated launch arguments for screenshot/test flows
- test-only deep-link payloads

Not allowed:
- UI code branching on fake users
- fake auth states embedded directly in screens
- product logic depending on mock-only response shapes

Recommended mock auth strategies:

1. `fixture_challenge`
- request endpoint returns a deterministic `challengeId`
- verify endpoint accepts fixture code and fixture link token

2. `pre_authenticated_launch`
- test launch argument or environment seeds a known session
- used only for flows that are not about auth itself

3. `deep_link_resume`
- test opens a mock auth deep link and verifies post-auth route

Mock mode must still exercise the real frontend state machine.

## Non-Email Environments

Some environments cannot or should not send real email:
- local development
- preview environments
- CI
- store review

Allowed delivery modes:

- `email`
  - real transactional email provider
- `console`
  - backend logs the code and link token
- `disabled`
  - challenge creation works, delivery is not attempted

Required non-production strategy:

- `AUTH_DELIVERY_MODE=console` for local/CI by default
- optional allowlisted dev bypass credentials
- optional store-review credentials

Recommended dev bypass variables:

- `AUTH_DEV_BYPASS_ENABLED`
- `AUTH_DEV_BYPASS_EMAIL`
- `AUTH_DEV_BYPASS_CODE`
- `AUTH_DEV_BYPASS_LINK_TOKEN`

Rules:

- only active outside production
- only for explicit allowlisted emails
- must still return normal auth envelopes
- must be visible in logs/docs so teams know they are using bypass mode

Example local/review configuration:

```bash
AUTH_DELIVERY_MODE=console
AUTH_LINK_BASE_URL=appstarterkit://auth/verify
AUTH_DEV_BYPASS_ENABLED=true
AUTH_DEV_BYPASS_EMAIL=reviewer@yourapp.com
AUTH_DEV_BYPASS_CODE=11112222
AUTH_DEV_BYPASS_LINK_TOKEN=dev-review-link-token
```

Expected runtime behavior:

- `email`
  - backend sends real email containing a code plus a deep link
- `console`
  - backend logs and returns `code`, `linkToken`, and `linkUrl`
- `disabled`
  - backend creates the challenge without attempting delivery; useful for tightly controlled fixture tests

Mock/non-email pass strategy:

- use the same auth UI and endpoints
- type the returned code manually, or
- open the returned deep link, or
- use the allowlisted bypass credentials in review/demo environments

## Screenshot-Backed Verification

Starter-kit auth is not complete without simulator proof.

Required proof set:

1. Welcome screen
2. Email entry
3. Code entry
4. Auth deep-link open
5. Post-auth destination

Recommended execution loop:

- `simemu` for simulator allocation and screenshots
- Maestro for flow driving
- fixture-backed auth for deterministic runs

Proof storage:

- `~/Desktop/screenshots/<repo-name>/`

## Current Starter-Kit Gaps

Current repo issues to address over time:

- runtime endpoints still use legacy `magic-link/request` and `magic-link/verify`
- Apple/Google provider verification is not fully implemented in starter-kit backend
- challenge IDs are still not first-class in the runtime DTOs yet

This document defines the target standard to converge toward.

## Adoption Order

1. Update schemas and docs
2. Implement provider verification in backend
3. Introduce `challengeId` + `linkToken` model
4. Upgrade iOS/Android deep-link handling to pending auth payloads
5. Add screenshot-backed auth proof flows
6. Remove template drift and legacy custom-scheme names
