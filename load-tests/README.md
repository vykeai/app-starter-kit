# StarterApp Load Tests

k6-based load testing suite covering smoke, load, and stress scenarios.

## Prerequisites

Install k6 via Homebrew (macOS) or from https://k6.io/docs/getting-started/installation/:

```bash
brew install k6
```

## Running Tests

All tests default to `http://localhost:3000`. Start the backend before running:

```bash
cd backend && npm run start:dev
```

### Smoke test — 1 VU, 30 seconds

Verifies the critical path does not crash under minimal traffic. Run this first
to confirm the environment is healthy before escalating.

```bash
k6 run load-tests/smoke.js
```

### Load test — 50 VUs, 5 minutes

Simulates a realistic steady-state load (ramp up → hold → ramp down).

```bash
k6 run load-tests/load.js
```

### Stress test — up to 200 VUs

Finds the breaking point. No strict thresholds — the goal is to observe where
latency degrades and errors begin to appear.

```bash
k6 run load-tests/stress.js
```

## Targeting a Different Environment

Pass `BASE_URL` as an environment variable:

```bash
BASE_URL=https://api-staging.yourapp.com k6 run load-tests/smoke.js
```

Or use the convenience scripts in `load-tests/package.json`:

```bash
cd load-tests
npm run smoke
npm run load
npm run stress
npm run smoke:staging
```

## Understanding Results

k6 prints a summary table after each run. Key metrics:

| Metric | Threshold (smoke + load) | What to watch for |
|--------|--------------------------|-------------------|
| `http_req_duration p(95)` | < 500 ms | API latency |
| `http_req_duration p(99)` | < 1000 ms | Tail latency |
| `http_req_failed` | < 1% | Error rate |

A PASSED threshold appears in green; a FAILED threshold appears in red and
exits with a non-zero code (useful for CI gates).

## Adding New Scenarios

1. Create a new file in `load-tests/`, e.g. `load-tests/auth-soak.js`.
2. Import shared config from `./config.js`.
3. Run: `k6 run load-tests/auth-soak.js`.
