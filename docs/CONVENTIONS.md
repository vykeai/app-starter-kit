# Conventions

Shared conventions across all TheOnlyStack projects. Consistency here means Claude Code works correctly across every repo without re-teaching it the rules.

---

## Naming

### Directories

| Platform | Root dir | Language |
|----------|----------|---------|
| iOS/iPadOS | `apple/` | Swift |
| Android | `google/` | Kotlin |
| Backend API | `backend/` | TypeScript |
| Web Admin | `web-admin/` | TypeScript |
| Web Public | `web-public/` | TypeScript |

Never use `ios/`, `android/`, `server/`, `api/`, or `website/` as directory names.

### Code

| Context | Style |
|---------|-------|
| Swift properties / methods | camelCase |
| Swift types | PascalCase |
| Kotlin properties / methods | camelCase |
| Kotlin types | PascalCase |
| TypeScript / JS | camelCase for variables, PascalCase for classes |
| API JSON fields | camelCase |
| Database columns | snake_case (Prisma maps to camelCase) |
| Env vars | SCREAMING_SNAKE_CASE |

### Git Branches

```
feat/14-workout-logging-backend
fix/33-rest-timer-vibration
docs/spec-updates
chore/upgrade-nestjs
```

Always include the issue number when branching from an issue.

---

## Commits

Conventional Commits format. Required:

```
feat(backend): add workout logging endpoints — closes #14
fix(android): hydration ring stroke width
test(backend): workout processing integration tests
chore(deps): upgrade prisma to 5.x
docs(spec): add body tracking acceptance criteria
refactor(ios): extract fire score gauge to component
```

**Prefixes:**
- `feat:` — new feature
- `fix:` — bug fix
- `test:` — adding or updating tests
- `chore:` — maintenance (deps, config, tooling)
- `docs:` — documentation only
- `refactor:` — code change that's neither feat nor fix
- `perf:` — performance improvement

Never commit: `.env` files, secrets, `node_modules`, `*.xcuserstate`, `*.pbxuser`, `dist/` (unless the package requires it).

---

## Backend API Conventions

### Module Structure

```
backend/src/<domain>/
├── <domain>.module.ts
├── <domain>.service.ts
├── <domain>.controller.ts
├── <domain>.service.spec.ts
└── dto/
    ├── create-<domain>.dto.ts
    └── update-<domain>.dto.ts
```

### Controller Prefix

Never include `v1` in controller prefixes. The global prefix `api/v1` is set once in `main.ts`.

```typescript
@Controller('fuel')        // ✓ — becomes /api/v1/fuel
@Controller('api/v1/fuel') // ✗ — double prefix
```

### Error Response Shape

```json
{
  "statusCode": 404,
  "error": "NOT_FOUND",
  "message": "Workout not found",
  "code": "WORKOUT_NOT_FOUND"
}
```

Standard HTTP codes:

| Situation | Status |
|-----------|--------|
| Validation error | 400 |
| Missing/invalid auth | 401 |
| Valid auth, insufficient permissions | 403 |
| Resource not found | 404 |
| Duplicate clientId (idempotency) | 409 |
| Subscription required | 403 + `SUBSCRIPTION_REQUIRED` code |

### Pagination Response Envelope

```json
{
  "items": [...],
  "pagination": {
    "totalCount": 142,
    "limit": 50,
    "hasMore": true,
    "nextCursor": "base64-opaque-cursor",
    "offset": 0
  }
}
```

Default page size: 50. Max: 250. Cursor is opaque base64 — clients never parse it.

---

## Data Model Conventions

### ClientId

Every user-created record carries a `clientId` (UUID v4, generated client-side). Enables idempotent sync — duplicate submissions are detected by `clientId`, not by the server.

Models with clientId: Workout, ExerciseLog, BodyMeasurement, HydrationEntry, and any user-generated record that travels through the sync queue.

### Soft Deletes

All deletes set `deletedAt` timestamp. Never hard-delete user data directly. Schedule hard deletes after 30 days via BullMQ job.

```prisma
deletedAt  DateTime?
```

### Unit Storage

Always store the raw value + the unit it was entered in. Never convert on write, convert on display.

```json
{ "value": 100, "inputUnit": "lbs" }
```

Conversion constants:
- 1 kg = 2.20462 lbs
- 1 km = 0.621371 miles
- 1 cm = 0.393701 inches

---

## Platform Parity Rule

iOS and Android ship the same features in the same commit. No exceptions.

Permitted differences:
- Navigation chrome (iOS tab bar vs Android bottom nav)
- Typography (SF Pro vs Roboto)
- Platform-native components (DatePicker, share sheet, alerts)
- Shadow implementation (iOS shadowColor vs Android elevation)

Everything else is a bug. Fix immediately.

---

## Testing Conventions

### Backend

- Every `*.service.ts` has a `*.service.spec.ts`
- Unit tests mock Prisma — no real DB in unit tests
- E2E tests use real test PostgreSQL (`docker-compose` in CI)
- Run unit tests: `cd backend && pnpm test --config jest.config.js`
- Run E2E: `cd backend && pnpm test:e2e`

### iOS

- Every ViewModel has a `*ViewModelTests` file
- Snapshot tests for design system components (light + dark)
- XCUITest for all user-facing flows

### Android

- Every ViewModel has a `*Test` file in `src/test/`
- Compose UI tests in `src/androidTest/components/`
- Espresso/Compose E2E for critical flows in `src/androidTest/flows/`

---

## Simulator / Emulator

Use `simemu` exclusively. Never call `xcrun simctl` or `adb` directly.

```bash
export SIMEMU_AGENT=<project>-agent  # set before every session

simemu screenshot <slug> -o /tmp/<name>.png
simemu install <slug> path/to/app
simemu launch <slug> <bundle-id>
```

Prove every UI change with a screenshot before committing. Never report a visual change as done without seeing it.

---

## AI Tooling

### CLAUDE.md

Every project must have a `CLAUDE.md` at the repo root. Run `npx runecode` on any new project to generate it automatically. Keep it accurate — Claude Code reads it at the start of every session.

### Cloudy Specs

A good Cloudy spec task:
- Has binary pass/fail acceptance criteria (command outputs X, file exists, endpoint returns Y)
- Lists the exact files to create or modify
- References context files the agent needs to read first
- Has no TODO/TBD markers (Cloudy's post-build validation blocks the run)

### Sentinel

Run `npm run schema:validate` before and after any schema change. Run `npm run schema:generate` after editing any schema file — always commit generated files alongside schema changes.
