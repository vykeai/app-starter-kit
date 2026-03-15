# iOS E2E Tests (Maestro)

Mobile UI automation using [Maestro](https://maestro.mobile.dev). Tests run against a
debug build on Simulator via `simemu`. For starter proof flows, the app can run
entirely in runtime fixture mode with `-UITestMode`, so a local backend is not
required.

## Install

```bash
brew tap mobile-dev-inc/tap
brew install maestro
```

Verify the install:

```bash
maestro --version
```

## Run a single flow

```bash
maestro test apps/starter-ios/e2e/01_auth_flow.yaml
```

## Run all flows in order

```bash
maestro test apps/starter-ios/e2e/
```

Maestro runs YAML files in lexicographic order, so the numeric prefix controls
execution sequence.

## Run on a specific simulator with simemu

```bash
export SIMEMU_AGENT=starter
simemu status
simemu boot starter-ios
simemu maestro starter-ios apps/starter-ios/e2e/01_auth_flow.yaml
```

## CI usage (GitHub Actions)

```yaml
- name: Run Maestro E2E
  uses: mobile-dev-inc/action-maestro-cloud@v1
  with:
    api-key: ${{ secrets.MAESTRO_CLOUD_API_KEY }}
    app-file: path/to/StarterApp.app
    workspace: apps/starter-ios/e2e/
```

## Test inventory

| File | What it covers |
|------|----------------|
| `01_auth_flow.yaml` | App launch → welcome → email entry → fixture OTP verification → home |
| `02_home_screen.yaml` | Home screen presence when already authenticated, logout sheet cancel |

## Tips

- `clearState: true` in `launchApp` wipes the app's keychain and UserDefaults — use this at the
  start of auth flows to guarantee a logged-out state.
- Maestro waits up to 5 seconds for each `assertVisible` by default. If your network is slow
  in CI, increase the timeout with `waitForAnimationToEnd`.
- `01_auth_flow.yaml` uses `-UITestMode`, which enables the iOS runtime fixture transport.
- Use `simemu screenshot starter-ios -o ~/Desktop/screenshots/starter/ios_auth.png` after
  a flow to keep proof artifacts out of `/tmp`.
