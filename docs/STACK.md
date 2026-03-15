# TheOnlyStack — Golden Stack Reference

> Authoritative. Every layer. Every choice. Every reason.
> Last updated: 2026-03-15

---

## Guiding Principles

1. **One default per phase.** Start with the cheapest correct default, then graduate only when the product shape forces it.
2. **Optimise for 0→1.** Choose the simplest thing that survives to 1M users. Migrate later with real data.
3. **AI-first development.** Every tool, config, and convention is chosen to work well with Claude Code + Cloudy.
4. **Parity between platforms.** iOS and Android ship the same features simultaneously. No exceptions.
5. **Offline-first.** Every user action writes locally first. The word "offline" never appears as an error.

---

## Phase Model

TheOnlyStack has two hosting lanes and six product phases.

### Hosting lanes

| Lane | Use it for | Stack |
|------|------------|-------|
| **Lane A — Managed launch stack** | Default for nearly every new product | Railway + PostgreSQL + Redis + Meilisearch + PostHog + Cloudflare R2 + Vercel/Cloudflare Pages + Firebase |
| **Lane B — Managed scale stack** | Graduate here only when Railway becomes the bottleneck | GCP Cloud Run + Cloud SQL + Redis + worker services + Cloud Storage/CDN + Cloudflare R2 if it is still working well |

### Product phases

| Phase | Default lane | Goal |
|-------|--------------|------|
| **Local development** | Local Docker + provider sandboxes | Fast inner loop, zero external coupling |
| **Public alpha** | Lane A | Ship quickly and learn |
| **Public beta** | Lane A | Add staging/prod split, alerting, spend caps, rollback discipline |
| **Launch** | Lane A | Paid users, reliable operations, no infra heroics |
| **Under 1M users** | Lane A unless product shape forces Lane B | Scale with evidence, not fear |
| **Under 5M users** | Lane A or Lane B depending on product shape | Use the heavier lane only where it buys real control |

### Scale doctrine

- **Cloudflare R2 is a long-term default.** Do not plan to migrate away from R2 just because the product gets large.
- **Railway is the default control plane, not a forever promise.** Stay on Railway until it becomes the bottleneck.
- **Do not migrate because of vanity thresholds.** "1M users" and "5M users" are not migration triggers by themselves.
- **Migrate control planes for specific reasons:** multi-region latency, database topology, compliance, custom networking, SLO pressure, or support/observability needs.

---

## Stack Layers

### Backend API

| Choice | Reason |
|--------|--------|
| **NestJS** (TypeScript) | Decorator-based, module-per-domain, batteries included. Claude Code handles it extremely well. |
| **Prisma** (ORM) | Type-safe, migrations, excellent DX. Schema-first makes multi-service contracts clear. |
| **PostgreSQL 16** | Default. pgvector available for M2+ AI features. |
| **BullMQ + Redis** | Queue for post-action job chains (workout processing, badge evaluation, push dispatch). |
| **Meilisearch** (self-hosted on Railway) | Full-text search with typo tolerance. Deploy as a Railway service alongside the API. |
| **Node.js 20 LTS** | LTS stability. TypeScript 5.x. pnpm for package management. |

**Architecture pattern:** Module per domain (`auth`, `user`, `workout`, `arena`, `fuel`, etc.). Each module = service + controller + DTOs + spec. No cross-module imports except shared `common/`.

**API conventions:**
- Global prefix: `api/v1` (set once in `main.ts`, never in controller decorators)
- Auth: JWT guard via `@UseGuards(JwtAuthGuard)` on all protected routes
- Pagination: cursor-based (default) + offset-based (admin). Response envelope: `{ items, pagination: { totalCount, limit, hasMore, nextCursor } }`
- Validation: `class-validator` + `class-transformer` on all DTOs
- Soft deletes: `deletedAt` timestamp on all user-created records. Hard delete after 30 days via scheduled job.
- ClientId: every user-created record carries a UUID `clientId` generated on the client for idempotent sync.

---

### Mobile — iOS

| Choice | Reason |
|--------|--------|
| **Swift + SwiftUI** | Native performance. SF Symbols. Declarative UI matches AI coding patterns well. |
| **SwiftData** | Apple's native ORM for SwiftUI. No third-party dependency. |
| **iOS 17.0+ minimum** | `@Observable` macro, SwiftData, NavigationSplitView all require 17+. |
| **MVVM with `@Observable`** | Not `ObservableObject`. Simpler, less boilerplate, better performance. |
| **NavigationSplitView** | Collapses to stack on iPhone, becomes split view on iPad automatically. |

**Key patterns:**
- State: `@Observable` classes, `@Environment` for DI
- Async: async/await everywhere, no Combine for new code
- Adaptive layout: `@Environment(\.horizontalSizeClass)` — `.compact` (iPhone) vs `.regular` (iPad)
- `UILaunchScreen` key in Info.plist: `<key>UILaunchScreen</key><dict/>` — NEVER remove, prevents letterboxing on modern iPhones

---

### Mobile — Android

| Choice | Reason |
|--------|--------|
| **Kotlin + Jetpack Compose** | Modern, declarative, coroutines-first. |
| **Room** | Android's official ORM. Generates type-safe queries, supports migrations. |
| **Hilt** | Official DI. Works with `@HiltViewModel`, `@AndroidEntryPoint`. |
| **API 26 (Android 8.0) minimum** | 95%+ device coverage. Below API 26 not worth supporting. |
| **MVVM with ViewModel + StateFlow** | Standard Android architecture. Works well with Compose. |
| **AdaptiveNavigationSuiteScaffold** | Bottom nav (phone) → Rail (medium) → Drawer (expanded) automatically. |

**Key patterns:**
- Async: Coroutines + Flow
- Adaptive layout: `WindowSizeClass` from `calculateWindowSizeClass()`
- `ListDetailPaneScaffold` for master-detail on tablets

---

### Web — Public Site

| Choice | Reason |
|--------|--------|
| **Astro 5** | Static-first, zero JS by default. Perfect for marketing pages and SEO. |
| **Tailwind CSS v4** | Via `@tailwindcss/vite` Vite plugin (not `@astrojs/tailwind`). |
| **@fontsource-variable/inter** | No Google Fonts CDN. Font self-hosted with zero network dependency. |
| **React islands** | Only for interactive components (pricing toggle, beta form, platform detect). Everything else is pure HTML. |
| **Static output** | `output: 'static'` — no Node adapter. Deploy to Railway static or Cloudflare Pages. |

---

### Web — Admin Panel / App

| Choice | Reason |
|--------|--------|
| **Next.js 15** | App Router. React Server Components for data-heavy pages. |
| **shadcn/ui** | Unstyled, copy-paste components. Tailwind-based, easily customised. |
| **Tailwind CSS v4** | Consistent with public site. |

---

### Storage — Files and Media

| Choice | Reason |
|--------|--------|
| **Cloudflare R2** | **Zero egress fees.** S3-compatible SDK (drop-in). At 1M users: ~$150/mo vs ~$1,130/mo on S3. |

SDK: `@aws-sdk/client-s3` with custom endpoint pointing to R2. No code changes when you already use AWS SDK.

**Longevity rule:** R2 is not just a launch choice. Keep it by default even at large scale unless you hit a concrete technical or compliance reason to move.

---

### Hosting

| Service | Hosts | Phase |
|---------|-------|-------|
| **Railway** | NestJS API + PostgreSQL + Redis + Meilisearch + PostHog | Default through launch and often beyond |
| **Cloudflare Pages** | Astro public sites (optional — Railway static also works) | Default |
| **Vercel** | Next.js admin/app | Default |
| **GCP Cloud Run + Cloud SQL** | API + workers + managed scale control plane | Graduation lane only |

**Why Railway first:** the operational drag is lower, the setup path is faster, and it keeps the team focused on product instead of platform work.

**When GCP becomes correct:** move when the control plane needs stricter topology, stronger SLOs, advanced networking, or multi-region strategy. Do not move just to look serious.

**Infrastructure as Code:** Skip Pulumi/Terraform until you have multiple environments that need to stay in sync. Railway's GUI + Railway CLI covers 0→Phase 2 entirely.

---

### Auth

| Choice | Reason |
|--------|--------|
| **Magic link (email OTP)** | No passwords to manage. Works universally. Less friction than OAuth for fitness/wellness apps. |
| **JWT + refresh tokens** | Short-lived access tokens (15 min). Refresh tokens (30 days, rotating). Stored in Keychain (iOS) / EncryptedSharedPreferences (Android). |
| **Resend** | Email delivery. Simple API, generous free tier, great deliverability. |

---

### Push Notifications

| Choice | Reason |
|--------|--------|
| **Firebase Cloud Messaging (FCM)** | Free. Works for both iOS (via APNs bridge) and Android. One SDK for both platforms. |

Rate limit: max 5 push notifications per user per day. Quiet hours: 22:00–07:00 local time (configurable).

---

### Analytics

| Choice | Reason |
|--------|--------|
| **PostHog self-hosted** | One Railway service. Multiple projects = multiple API keys in one instance. No per-event billing. GDPR-friendly (data stays on your infra). |

Mobile: log events to both Firebase Analytics (automatic retention events) and PostHog (custom product analytics) via a single `AnalyticsManager.track()` call.

---

### Error Tracking

| Platform | Choice |
|----------|--------|
| Mobile (iOS + Android) | Firebase Crashlytics (free, integrated with FCM project) |
| Backend | Railway logs + Sentry (free tier) |

---

### Search

| Choice | Reason |
|--------|--------|
| **Meilisearch** (self-hosted on Railway) | Typo-tolerant, fast, simple to configure. Hosted as a Railway service ($5–15/mo). |

---

### In-App Payments

| Platform | Choice | Why |
|----------|--------|-----|
| iOS | **StoreKit 2** (in-house) | No RevenueCat. StoreKit 2 is first-party, handles everything. |
| Android | **Play Billing 6+** (in-house) | Same reasoning. Google Play Billing Library handles everything. |
| Web | **Stripe** | Standard. Use when you need web subscriptions or enterprise billing. |

**App store fees:** Register for Apple Small Business Program + Google Play Small Business once revenue hits. Reduces fee from 30% → 15% for <$1M/year revenue. This one decision saves more money than all infrastructure optimisation combined.

---

### Schema / Contracts

| Choice | Reason |
|--------|--------|
| **Sentinel** | Shared schema layer across iOS, Android, and backend. Generates typed tokens, strings, feature flags, and API contracts from JSON schemas. Enforces platform parity. |

---

### AI Development Tooling

| Tool | Purpose |
|------|---------|
| **Claude Code** | Primary AI coding assistant |
| **Cloudy** | Autonomous multi-task execution — decomposes specs into task graphs, runs in parallel with worktree isolation |
| **runecode** | One-command Claude Code setup — detects stack, generates `CLAUDE.md`, installs hooks and skills |
| **keel** | Task and decision tracking for solo+AI teams |
| **simemu** | Simulator/emulator management — iOS and Android from one CLI, headless by default |

---

## Cost Model at Scale

| Users | Monthly infra cost |
|-------|--------------------|
| 0–10K | ~$50–100/mo |
| 10K–100K | ~$150–300/mo |
| 100K–1M | ~$450–650/mo |

The real cost at 10K paying subscribers isn't infra — it's **App Store / Play Store fees (15–30%)**, which alone exceed all infrastructure combined. Qualifying for the 15% reduced rate is the highest-leverage financial decision at that stage.

---

## Product Fit Examples

These are reference mappings, not vanity commitments.

| Product | Shape | Local | Alpha/Beta/Launch | Under 1M | Under 5M |
|---------|-------|-------|-------------------|----------|----------|
| **Sitches** | Mobile-first dating app, chat, push, subscriptions, media | Local Docker | Lane A | Lane A until realtime/media pressure forces Lane B | Likely Lane B |
| **FitKind** | Fitness app, sync, search, admin/public web, lower realtime pressure | Local Docker | Lane A | Lane A | Lane A or Lane B depending on ops needs |
| **Goala** | AI productivity, OCR, parsing, collaboration, async jobs | Local Docker + provider sandboxes | Lane A with model routing, caching, and spend caps | Lane A or Lane B if workflow concurrency gets heavy | Likely Lane B |
| **UNIVIIRSE** | Web + mobile + AI generation + payments + social + realtime/media | Local Docker + provider mocks | Lane B earlier than the others | Lane B | Lane B |

Use this rule when in doubt:

- If the product is mostly CRUD, sync, search, and moderate background jobs, stay on Lane A.
- If the product is heavy on realtime, media pipelines, multi-service async processing, or stricter reliability constraints, graduate to Lane B.

---

## What This Stack Is NOT

- No React Native or Flutter — native iOS and Android only
- No RevenueCat — StoreKit 2 and Play Billing 6+ are sufficient in-house
- No Kubernetes — Railway handles orchestration until you need multi-region
- No IaC until Phase 3 — Railway CLI + GUI covers 0→Phase 2
- No separate CDN setup — Cloudflare R2 includes CDN via Cloudflare's network
- No AWS — R2 is S3-compatible and hosted on Cloudflare; no AWS account needed
