# Starter-Kit Systems

This document records the reusable cross-cutting systems that should live in
`app-starter-kit`, the source repos they were extracted from, and the contract
they should preserve.

The goal is not identical UI across products. The goal is one reusable runtime
shape for the systems every product keeps rebuilding.

Current packaging direction:

- keep this repo as the integration host and runnable reference app
- extract reusable seams as internal libraries first:
  - `auth-core`
  - `network-core`
  - `notifications-core`
  - `contracts`
- only split repos later if multiple products consume those libraries cleanly

## Source Ranking

Primary extraction sources:

- `univiirse`
  - strongest for deep-link routing, Sentinel contracts, fixture-backed mock
    transport, simulator proof loops, rich preferences, and media upload/read
- `goala`
  - strongest for backend module rigor, notification-preference structure, and
    common API concerns like request IDs and structured error envelopes
- `fitkind`
  - strongest for compact cross-cutting NFR shape and test-friendly overrides
- `sitches`
  - strongest for native store subscription verification flow and paywall
    entitlement logic

## 1. Deep-Link Routing Conventions

Selected pattern:

- `univiirse` app-scoped pending deep-link payload
- backed by route tests, not just navigator wiring

Why:

- route payload is durable app state, not a transient event
- works for cold start, warm start, and unauthenticated entry
- avoids the `sitches` / old starter problem where the link only works if the
  user already happens to be on the right screen

Starter contract:

- parse incoming URL once at app entry
- convert to a typed payload
- store in app-scoped state
- let the current flow consume and clear it when ready
- keep one route registry in Sentinel
- require unit tests for the parser and Maestro proof for critical links

## 2. Fixture-Backed Mock Transport

Selected pattern:

- `univiirse` generated Android `MockDispatcher` + iOS `ViviiURLProtocol`
- `fitkind` test-time `MockURLProtocol`

Why:

- same API client code path in real and mock modes
- fixtures live in one contract-driven source of truth
- mock behavior supports filled, empty, and slow states for screenshots

Starter contract:

- iOS: bundle-backed `URLProtocol` interception
- Android: debug-only `MockWebServer` dispatcher
- fixture route mapping generated or validated from Sentinel
- non-email auth and screenshot runs must use this transport rather than custom
  fake view models

## 3. Screenshot Proof Loop With Simemu + Maestro

Selected pattern:

- `univiirse` Sentinel catalog + Maestro proof flows
- `fitkind` / `sitches` `simemu` discipline and screenshot storage rules

Why:

- platform parity work needs reproducible devices, not ad hoc local simulator use
- screenshots become release evidence, not side effects

Starter contract:

- `simemu` is the simulator allocation and screenshot layer
- Maestro is the navigation and state-driving layer
- Sentinel owns the catalog, expected variants, and validation
- required proof set per feature should be declared in Sentinel

## 4. Force Update

Selected pattern:

- `fitkind` and starter runtime shape

Why:

- simple version-check endpoint
- hard block + soft banner split
- launch arguments can force hard/soft update states during testing

Starter contract:

- backend endpoint: `GET /app/version-check`
- clients surface:
  - blocking hard update
  - dismissible soft update
- test overrides via launch args / injected flags
- version-check failures must never block app startup

## 5. Offline Monitor / Banner

Selected pattern:

- existing starter / `fitkind` / `sitches`

Why:

- simple, reliable, and already consistent across products

Starter contract:

- iOS: `NWPathMonitor`
- Android: `ConnectivityManager` flow
- root-level banner overlay, not per-screen logic
- network monitor is also shared with sync and retry systems

## 6. Review Prompt Manager

Selected pattern:

- existing starter / `fitkind` / `sitches`

Why:

- small shared service, zero product-specific UI dependency

Starter contract:

- iOS: `SKStoreReviewController`
- Android: Play In-App Review
- cooldown gating stored locally
- trigger input should be semantic (`first_success`, `milestone`, `custom`) not
  screen-specific

## 7. Common Backend Module

Selected pattern:

- `goala` common middleware + filters + DTO conventions
- starter backend modular structure

Why:

- `goala` has the clearest shape for request IDs, structured errors, and shared
  pagination contracts

Starter contract:

- include:
  - request ID middleware
  - structured global exception filter
  - shared pagination DTO / response helpers
  - queue module
  - health module
  - notification module skeleton
- all API errors should return:
  - `statusCode`
  - `error`
  - `message`
  - `timestamp`
  - `path`
  - `requestId`

## 8. Push Notification Skeleton

Selected pattern:

- `goala` for device registration + typed notification categories
- starter for minimal FCM/APNs stub

Why:

- `goala` has a real preference-aware notification model, not just a token sink

Starter contract:

- backend:
  - register device
  - unregister device
  - read/update notification preferences
  - send typed notifications with category-aware filtering
- clients:
  - permission request
  - token registration
  - foreground/background payload handling
  - deep-link handoff from push payload

## 9. Subscription / Paywall Skeleton

Selected pattern:

- `sitches` for native StoreKit/BillingClient verification shape
- starter / `fitkind` for compact entitlement manager surface

Why:

- `sitches` shows a more complete native purchase and backend verification path
- starter should not force one billing vendor

Starter contract:

- one product-facing entitlement manager interface
- default adapters:
  - native StoreKit / BillingClient
  - optional third-party adapter later if a team wants it
- feature gates resolve against entitlement state, not against paywall screens
- paywall stays a presentation layer over the entitlement manager

## 10. Sentinel Contract Layer

Selected pattern:

- `univiirse`

Why:

- strongest schema-first parity layer
- already ties together routes, fixtures, flows, screenshots, and generated code

Starter contract:

- Sentinel is source of truth for:
  - design tokens
  - strings
  - feature flags
  - models
  - endpoint contracts
  - navigation
  - mock transport config
  - screenshot proof requirements
- product repos should add product features on top of this layer, not bypass it

## 11. Notification Preferences Data Structure

Selected pattern:

- `goala` category-based preferences + quiet hours
- `univiirse` product-specific toggles

Why:

- `goala` has the right structural base
- `univiirse` shows the need for product-level sub-toggles

Starter contract:

- base fields:
  - `pushEnabled`
  - `emailEnabled`
  - `enabledCategories`
  - `quietHoursEnabled`
  - `quietHoursStart`
  - `quietHoursEnd`
  - `quietHoursWeekendStart`
  - `quietHoursWeekendEnd`
  - `urgentBreaksQuietHours`
  - `batchSoonNotifications`
- products may extend with domain toggles like `notifyOnChapterReady`
- keep the base structure stable across all apps

## 12. Media Upload / Read

Selected pattern:

- `univiirse`

Why:

- strongest end-to-end pattern for media:
  - local optimization
  - background upload
  - tier gating
  - local persistence
  - foreground + background paths

Starter contract:

- upload API supports:
  - prepare/upload
  - read/list
  - delete
  - progress events
- iOS foreground service and Android background worker should share the same
  domain contract
- media reads should use stable asset metadata objects, not raw URL strings alone

## 13. Networking Library

Selected pattern:

- starter / `fitkind` thin client shape
- `univiirse` Android debug base-url swap and mock injection
- `goala` error envelope conventions

Why:

- the same core pattern keeps reappearing because it is the right size:
  thin client, auth interceptor, token refresh, typed errors, debug mock swap

Starter contract:

- iOS:
  - actor-based client
  - coalesced refresh task
  - pluggable `URLSession`
- Android:
  - Retrofit + OkHttp
  - auth interceptor
  - authenticator
  - injected base URL
- both:
  - typed error layer
  - request ID support
  - mock transport compatibility
  - no product-specific endpoints inside the core client

## Adoption Order

1. Sentinel contract layer
2. deep-link routing
3. mock transport
4. screenshot proof loop
5. networking library
6. notification preferences + push skeleton
7. force update / offline / review
8. subscription / paywall
9. media upload / read
10. backend common module hardening

## Decision

These systems should become part of `app-starter-kit` by default. Product repos
should customize presentation and domain rules, but should stop rebuilding the
underlying runtime shape from scratch.

## Next Extraction Candidates

After the current runtime wave, the next strong starter-lib candidates are:

- analytics / event contract
- feature-flag bootstrap
- common pending / empty / error state kit
- background sync and job-runner glue
- observability hooks for correlation IDs, request IDs, and client error reporting
