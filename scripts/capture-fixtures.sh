#!/usr/bin/env bash
# Capture real API responses and save them as fixture files.
#
# Fixtures must be captured real responses — not hand-written.
# Run this against a local dev backend after seeding test data.
#
# Usage:
#   TOKEN=$(curl -s -X POST http://localhost:3000/api/v1/auth/magic-link/verify \
#     -H 'Content-Type: application/json' \
#     -d '{"email":"dev@example.com","code":"12345678"}' | jq -r '.accessToken')
#
#   ./scripts/capture-fixtures.sh $TOKEN
#
# Or run without TOKEN to capture only public endpoints.

set -euo pipefail

BASE_URL="${API_BASE_URL:-http://localhost:3000/api/v1}"
TOKEN="${1:-}"
IOS_FIXTURES="ios/StarterAppTests/Fixtures"
ANDROID_FIXTURES="android/app/src/test/resources/fixtures"

REPO_ROOT=$(git rev-parse --show-toplevel 2>/dev/null || pwd)
cd "$REPO_ROOT"

mkdir -p "$IOS_FIXTURES" "$ANDROID_FIXTURES"

capture() {
  local name="$1"
  local url="$2"
  local method="${3:-GET}"
  local body="${4:-}"
  local auth="${5:-}"

  local curl_args=(-s -X "$method" "$url" -H 'Content-Type: application/json')
  [[ -n "$auth" ]] && curl_args+=(-H "Authorization: Bearer $auth")
  [[ -n "$body" ]] && curl_args+=(-d "$body")

  local response
  response=$(curl "${curl_args[@]}")
  local exit_code=$?

  if [[ $exit_code -ne 0 ]]; then
    echo "  SKIP $name — curl failed (is the backend running?)"
    return
  fi

  echo "$response" | jq . > "$IOS_FIXTURES/$name.json"
  echo "$response" | jq . > "$ANDROID_FIXTURES/$name.json"
  echo "  OK   $name"
}

echo "Capturing fixtures from $BASE_URL"
echo ""

# ── Public endpoints ──────────────────────────────────────────────────────────
capture "health"          "http://localhost:3000/health"
capture "version-check"   "$BASE_URL/app/version-check?platform=ios&version=1.0.0"

# ── Auth flow (request + verify) ─────────────────────────────────────────────
# Step 1: request a code (response is just {message})
capture "auth-request" "$BASE_URL/auth/magic-link/request" "POST" \
  '{"email":"fixture@example.com"}'

# Step 2: verify (requires the code printed by the backend console or email)
# Replace 12345678 with the actual code from your dev backend logs.
if [[ -n "$TOKEN" ]]; then
  capture "auth-verify" "$BASE_URL/auth/magic-link/verify" "POST" \
    '{"email":"fixture@example.com","code":"12345678"}'
  capture "auth-refresh" "$BASE_URL/auth/refresh" "POST" \
    "{\"refreshToken\":\"$TOKEN\"}"
else
  echo "  SKIP auth-verify and auth-refresh (no TOKEN provided)"
  echo "       Provide the refreshToken as first argument to capture these."
fi

echo ""
echo "Done. Update fixtures manually if shapes differ from real responses."
echo "Commit: git add $IOS_FIXTURES $ANDROID_FIXTURES && git commit -m 'chore: update fixtures'"
