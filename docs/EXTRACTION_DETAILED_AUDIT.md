# Onlystack Detailed Extraction Audit

This document is the sharper follow-up to [STARTER_KIT_EXTRACTION_AUDIT.md](/Users/ted/dev/onlystack/docs/STARTER_KIT_EXTRACTION_AUDIT.md).

It records what is actually strong in:

- `univiirse`
- `goala`
- `sitches`
- `fitkind`

and what should be treated as aspirational, stale, or incomplete.

`fitkind-legacy` is not currently checked out under `/Users/ted/dev`, so it is not part of the concrete extraction base yet.

## Source Summary

### Univiirse

Strongest areas:

- app-scoped deep-link routing and post-auth resume
- Sentinel route/fixture/screenshot contract model
- fixture-backed mock transport at runtime
- simulator-driven proof loops
- rich cross-platform fixture parity
- broad subscription and media surface modeling

Weak areas:

- too much historical drift around `MockAPIClient`
- some docs describe desired architecture rather than the clean current one

### Goala

Strongest areas:

- backend auth rigor for Apple and Google verification
- common backend architecture and sync thinking
- notification preference modeling and device registration
- request/response discipline

Weak areas:

- some mobile deep-link handling is still event-oriented
- offline UX has been a recurring gap even when sync architecture is strong

### Sitches

Strongest areas:

- compact starter-shaped NFR set
- native subscription/paywall verification shape
- simple readable auth flow
- good template discipline around offline/review/force-update

Weak areas:

- “magic link” is really code auth plus a partial deep-link assist
- deep-link auth handoff is not durable enough
- push is still mostly stubbed

### FitKind

Strongest areas:

- explicit cross-platform parity discipline
- best compact NFR doctrine
- offline-first product stance
- strongest web-public direction for a starter marketing site
- clear subscription and notification product language

Weak areas:

- many strongest patterns are documented doctrine, not yet live reusable code
- web-admin is mostly configuration/text scaffolding right now, not a full starter

## Domain Decisions

## 1. Auth

Use:

- `goala` backend verifier rigor for Apple and Google
- `univiirse` frontend pending-auth/deep-link orchestration
- `sitches` review/dev bypass idea, but not its verify contract

Do not copy:

- `sitches` requirement that deep-link completion depends on `email + code`
- any flow where the code is only useful if the code-entry screen is already open

Onlystack decision:

- backend supports:
  - challenge request
  - challenge verify by `code`
  - challenge verify by opaque `linkToken`
  - Apple sign-in
  - Google sign-in
  - refresh / logout / me
- app entry stores a pending auth payload
- auth UI consumes and clears that payload
- mock and review mode must support deterministic non-email login

## 2. Deep-Link Routing

Use:

- `univiirse` typed app router and pending payload model

Why:

- `NavigationRouter.swift` in Vivii is the clearest pattern for treating deep links as app state rather than throwaway events
- the Maestro deep-link proof flow is the strongest “did this actually work?” loop

Do not copy:

- `NotificationCenter`-only deep-link handoff for core auth routing
- template drift where iOS and Android schemes diverge

Onlystack decision:

- one Sentinel route registry
- one typed route payload per platform
- deep links parsed at app root
- auth/push/referral routes resume from durable state, not immediate view assumptions

## 3. Mock Transport

Use:

- `univiirse` `ViviiURLProtocol` model on iOS
- generated/mock-dispatcher thinking from Sentinel
- `fitkind` / `sitches` rule that mocking belongs in transport, not UI

Do not copy:

- large app-wide `MockAPIClient` forks as the long-term architecture

Onlystack decision:

- fixture-backed HTTP interception is the default mock/runtime proof path
- keep API clients thin and real
- fixtures validated through contracts, then consumed by iOS/Android/web

## 4. Force Update, Offline, Review

Use:

- `fitkind` as the best compact doctrine
- `sitches` as the clean starter-shaped runtime example

Onlystack decision:

- these are first-class starter systems
- all three degrade gracefully on failure
- no screen should re-implement connectivity or version logic ad hoc

## 5. Notifications And Preferences

Use:

- `goala` backend notification DTOs and preference shape
- `univiirse` richer product preference extension model
- `fitkind` notification-system thinking for rate limits / quiet hours

Onlystack decision:

- a shared preferences core should support:
  - push enabled
  - email enabled
  - marketing enabled
  - category toggles
  - quiet hours
  - per-product extension block
- backend owns device registration and filtering
- mobile owns permission prompts and token registration

## 6. Subscription And Paywall

Use:

- `sitches` native verification and paywall shape
- `fitkind` product-language clarity around tiers and gating
- `univiirse` richer entitlement and dunning surface as the upper bound

Do not copy:

- starter abstractions that assume one vendor forever
- paywall logic embedded directly into UI instead of entitlement checks

Onlystack decision:

- one entitlement core
- platform-native adapters for StoreKit and Play Billing
- optional web adapter later
- paywall is presentation; entitlement manager is the real contract

## 7. Media Upload And Read

Use:

- `univiirse` as the current strongest reusable source
- `sitches` / `fitkind` infra direction toward Cloudflare R2

Onlystack decision:

- standardize:
  - asset create
  - upload URL issue
  - upload progress
  - asset finalize
  - read URL
  - delete
- default storage guidance should assume R2-compatible object storage

## 8. Networking Library

Use:

- `goala` backend/common response discipline
- `univiirse` thin client + fixture integration direction
- starter refresh/interceptor behavior

Onlystack decision:

- network core should provide:
  - base URL/env injection
  - auth header + refresh retry
  - structured error mapping
  - request IDs where appropriate
  - mock transport compatibility

## 9. Sync And Offline Mutation

Use carefully:

- `goala` has the strongest sync-first philosophy and endpoint shape

Do not force:

- a heavy sync engine into every starter product

Onlystack decision:

- extract sync primitives and endpoint contracts
- do not make full sync-first architecture mandatory in the default starter
- products opt into heavier offline mutation models when needed

## 10. Web Surfaces

Use:

- `fitkind` and `sitches` as the strongest signal for `web-public` being Astro/static-first
- `univiirse` as the strongest signal for `web-admin` and authenticated `web-app` being Next.js

Onlystack decision:

- `starter-web-public`: Astro
- `starter-web-admin`: Next.js
- `starter-web-app`: Next.js

This is already the direction now scaffolded in `Onlystack`.

## What Should Be Starter-Core Vs Product-Only

Starter-core:

- auth runtime
- deep-link runtime
- network core
- notification core
- contracts / Sentinel schemas
- force update
- offline banner
- review manager
- push registration skeleton
- entitlement core
- media-core skeleton
- screenshot proof loop

Product-only:

- Vivii reader and storytelling systems
- Goala journaling / space semantics
- FitKind workout logging and exercise domain
- Sitches dating / radar / match concepts

## Immediate Follow-Ups

1. Keep `Onlystack` as the runnable monorepo.
2. Keep extracting internals into `libs/*`.
3. Use this repo, not abstract docs alone, as the proof surface.
4. Add `user-core`, `media-core`, and `analytics/contracts` next.
5. If `fitkind-legacy` appears locally later, run the same pass again and update this document instead of creating a parallel audit.
