# Starter-Kit Extraction Audit

Cross-repo pass over:
- `univiirse`
- `goala`
- `sitches`
- `fitkind`

Goal: identify systems that appear repeatedly enough to belong in `app-starter-kit`.

## Should Be In Starter Kit

### 1. Auth System

Already common everywhere. Needs the stronger shared standard documented in [AUTH_SYSTEM.md](/Users/ted/dev/app-starter-kit/docs/AUTH_SYSTEM.md).

Why:
- repeated across all repos
- repeated mistakes around deep-link resume
- repeated social sign-in setup work

### 2. Deep-Link Routing Pattern

Not just auth.

Starter kit should include:
- app-entry URL parser
- pending-route storage
- post-auth resume support
- shared route naming guidance

Why:
- `univiirse` proved this matters beyond auth
- `sitches`/`fitkind` show how easy it is for template drift to break it

### 3. Fixture-Backed Mock Transport

Starter kit should standardize:
- iOS `URLProtocol`
- Android `MockWebServer`/dispatcher
- fixture directories
- fixture validation

Why:
- present everywhere
- easy to drift without one rule set

### 4. Screenshot Proof Layer

Starter kit should officially include:
- `simemu` setup guidance
- Maestro flows for critical journeys
- screenshot naming/storage rules

Why:
- this is the fastest way to catch parity and deep-link regressions
- all repos are converging on this anyway

### 5. Force Update

This is already common enough to be standard starter-kit infrastructure.

Include:
- backend version-check endpoint
- iOS hard/soft update UI
- Android hard/soft update UI
- config examples

### 6. Offline Indicator + Connectivity Monitor

Already a repeated cross-product concern.

Include:
- platform monitor abstraction
- offline banner
- recovery behavior guidance

### 7. Review Prompt Manager

Common, low-risk, worth centralizing.

Include:
- cadence rules
- platform wrappers
- analytics hook points

### 8. Backend Common Module

Starter kit should explicitly own:
- correlation IDs
- logging interceptor
- pagination helpers
- auth guard patterns
- soft delete extension
- error envelope standard

This is already one of the strongest reusable parts.

### 9. Push Notification Skeleton

Worth keeping in the starter kit as a baseline, not a full product system.

Include:
- token registration
- environment-safe setup
- permission prompts
- notification tap routing hooks

### 10. Subscription/Paywall Skeleton

Worth keeping as starter-kit scaffolding.

Include:
- entitlement model
- gate checks
- starter paywall structure
- StoreKit / Play Billing integration points

Do not over-productize the starter kit here.

### 11. Sentinel Contract Layer

This should remain a core part of the starter-kit operating model.

Include:
- navigation schema
- endpoint schema
- feature flags
- mock config
- fixture validation guidance

### 12. Notification Preferences Data Structure

Worth extracting as a shared contract.

Include:
- base push/email toggles
- category-based preferences
- quiet hours
- optional product-specific sub-toggles

Why:
- `goala` has the right structural base
- `univiirse` shows why products still need a small extension surface

### 13. Media Upload / Read

Worth extracting as starter-kit infrastructure.

Include:
- upload/read/delete asset contract
- progress reporting
- background upload hooks
- stable media asset metadata

Why:
- `univiirse` already has the strongest reusable pattern here

### 14. Networking Library

Worth extracting as a first-class starter system.

Include:
- thin typed API client
- auth interceptor / refresh handling
- base URL injection
- mock transport compatibility
- structured error mapping

Why:
- every repo keeps rebuilding the same thin client shape
- `univiirse`, `fitkind`, `sitches`, and starter all converge on it already

## Maybe In Starter Kit

### 1. Onboarding Shell

Useful as a structural pattern, but product-specific enough that it should be lightweight.

### 2. Admin/Web-Admin Shell

Useful when a product clearly needs operator tooling, but not mandatory for every starter app.

### 3. Sync Engine

Worth extracting only if the chosen pattern is clear.

Current state:
- `goala` strongly emphasizes sync-first
- starter kit already has sync primitives
- not every product needs full offline mutation sync immediately

Recommendation:
- keep sync primitives in starter kit
- do not force every product into a heavy sync-first architecture by default

## Should Not Be Shared As Starter-Kit Core

### 1. Product-Specific UI

Do not share:
- actual auth screens
- branded layouts
- product-specific onboarding copy
- business-domain cards and views

Share handling, not visual identity.

### 2. Domain-Specific Engines

Do not push these into starter kit:
- Vivii reader/story systems
- Goala wellness domain logic
- FitKind workout/exercise systems
- Sitches social/radar/chat specifics

### 3. Large Web Product Surfaces

`web-admin` and `web-public` can have starter templates, but not one mandatory shared product shell.

## Immediate Recommendation

The next extraction wave for `app-starter-kit` should be:

1. auth
2. deep-link routing
3. fixture-backed mock transport
4. screenshot proof workflow
5. force update
6. offline indicator
7. review manager
8. backend common module hardening
9. notification preferences contract
10. media upload/read
11. networking library

That gives new repos a strong operational base without turning the starter kit into a monolith.
