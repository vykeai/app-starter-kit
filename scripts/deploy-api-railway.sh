#!/usr/bin/env bash
set -euo pipefail

environment="${1:-staging}"

if ! command -v railway >/dev/null 2>&1; then
  echo "railway CLI is required: npm install -g @railway/cli" >&2
  exit 1
fi

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
api_dir="${repo_root}/apps/starter-api"

echo "Deploying Onlystack starter-api to Railway environment '${environment}'"
cd "${api_dir}"
railway up --environment "${environment}" --detach
