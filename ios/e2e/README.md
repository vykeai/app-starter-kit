# iOS E2E Tests (Maestro)

Mobile UI automation using [Maestro](https://maestro.mobile.dev). Tests run against a
debug build on Simulator or a real device. The backend must be running locally
(or pointed at staging via env) before executing flows that hit the network.

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
maestro test ios/e2e/01_auth_flow.yaml
```

## Run all flows in order

```bash
maestro test ios/e2e/
```

Maestro runs YAML files in lexicographic order, so the numeric prefix controls
execution sequence.

## Run on a specific simulator

```bash
# List available simulators
xcrun simctl list devices

# Boot a device, then run
maestro --device <UDID> test ios/e2e/01_auth_flow.yaml
```

## CI usage (GitHub Actions)

```yaml
- name: Run Maestro E2E
  uses: mobile-dev-inc/action-maestro-cloud@v1
  with:
    api-key: ${{ secrets.MAESTRO_CLOUD_API_KEY }}
    app-file: path/to/AppStarterKit.app
    workspace: ios/e2e/
```

## Test inventory

| File | What it covers |
|------|----------------|
| `01_auth_flow.yaml` | App launch → welcome → email entry → OTP screen → back navigation |
| `02_home_screen.yaml` | Home screen presence when already authenticated, logout sheet cancel |

## Tips

- `clearState: true` in `launchApp` wipes the app's keychain and UserDefaults — use this at the
  start of auth flows to guarantee a logged-out state.
- Maestro waits up to 5 seconds for each `assertVisible` by default. If your network is slow
  in CI, increase the timeout with `waitForAnimationToEnd`.
- The 8-digit OTP is printed to the NestJS dev console (`[AuthService] OTP for ...`). Copy it
  into the `inputText` step during manual runs, or configure your test email account with an
  IMAP scraper for fully automated flows.
