#!/usr/bin/env bash
# Rename StarterApp to your app name across all platforms.
# Run once after cloning, before your first commit.
#
# Usage (interactive):
#   ./scripts/rename.sh
#
# Usage (non-interactive):
#   ./scripts/rename.sh "MyApp" "com.mycompany.myapp"
#   ./scripts/rename.sh "FitKind" "com.fitkind.app"
#
# What it changes:
#   iOS    — StarterApp → NewName in sources, xcconfig, project.yml; renames source dirs; regenerates xcodeproj
#   Android — com.starter.app → new package; StarterApp → NewName; moves source tree
#   Backend — app name in Swagger docs
#   Docs    — README, CLAUDE.md references
#   Root    — package.json name

set -euo pipefail

REPO_ROOT=$(git rev-parse --show-toplevel 2>/dev/null || pwd)
cd "$REPO_ROOT"

# ── Inputs ────────────────────────────────────────────────────────────────────

NEW_NAME="${1:-}"
NEW_BUNDLE="${2:-}"

if [[ -z "$NEW_NAME" ]]; then
  echo ""
  echo "App rename — app-starter-kit"
  echo "────────────────────────────"
  read -rp "New app name (PascalCase, e.g. FitKind):       " NEW_NAME
fi

if [[ -z "$NEW_BUNDLE" ]]; then
  # Suggest a bundle ID from the name
  SUGGESTED=$(echo "$NEW_NAME" | tr '[:upper:]' '[:lower:]' | tr -cd '[:alnum:]')
  read -rp "Bundle / package ID (e.g. com.${SUGGESTED}.app): " NEW_BUNDLE
fi

if [[ -z "$NEW_NAME" || -z "$NEW_BUNDLE" ]]; then
  echo "Error: both app name and bundle ID are required." >&2
  exit 1
fi

# ── Derived values ────────────────────────────────────────────────────────────

OLD_NAME="StarterApp"
OLD_BUNDLE_IOS="starter.app"       # used in xcconfig PRODUCT_BUNDLE_IDENTIFIER prefix
OLD_PACKAGE_ANDROID="com.starter.app"
OLD_KEBAB="app-starter-kit"
OLD_SWAGGER_TITLE="App Starter Kit API"

# PascalCase — as provided
NEW_PASCAL="$NEW_NAME"

# Lowercase (no separators) — for kebab derivation
NEW_LOWER=$(echo "$NEW_NAME" | tr '[:upper:]' '[:lower:]' | tr -cd '[:alnum:]')

# Kebab case — try to split on uppercase boundaries (FitKind → fit-kind)
NEW_KEBAB=$(echo "$NEW_NAME" | sed 's/\([A-Z]\)/-\1/g' | tr '[:upper:]' '[:lower:]' | sed 's/^-//')

# Android package path (com.starter.app → com/starter/app)
OLD_PKG_PATH=$(echo "$OLD_PACKAGE_ANDROID" | tr '.' '/')
NEW_PKG_PATH=$(echo "$NEW_BUNDLE" | tr '.' '/')

# iOS bundle ID prefix — everything up to last segment (com.starter.app → starter.app → new.bundle)
# We replace the xcconfig placeholder `starter.app` with the full bundle ID (minus last segment is tricky)
# Simpler: just replace full bundle strings
OLD_IOS_BUNDLE_PREFIX="com.starter.app"  # full prefix in xcconfigs

echo ""
echo "Renaming:"
echo "  iOS target:       $OLD_NAME          → $NEW_PASCAL"
echo "  iOS bundle ID:    $OLD_IOS_BUNDLE_PREFIX → $NEW_BUNDLE"
echo "  Android package:  $OLD_PACKAGE_ANDROID → $NEW_BUNDLE"
echo "  kebab name:       $OLD_KEBAB → $NEW_KEBAB"
echo ""
read -rp "Continue? [y/N] " CONFIRM
[[ "$CONFIRM" =~ ^[Yy]$ ]] || { echo "Aborted."; exit 0; }

echo ""

# ── Helper: replace in files ──────────────────────────────────────────────────

replace_in_files() {
  local old="$1"
  local new="$2"
  local pattern="${3:-}"

  # Build the find command
  local find_args=(-type f)
  if [[ -n "$pattern" ]]; then
    find_args+=(-name "$pattern")
  else
    find_args+=(
      \( -name "*.swift" -o -name "*.kt" -o -name "*.kts"
         -o -name "*.xml" -o -name "*.plist" -o -name "*.xcconfig"
         -o -name "*.yaml" -o -name "*.yml" -o -name "*.json"
         -o -name "*.md" -o -name "*.ts" -o -name "*.js"
         -o -name "*.sh" -o -name "*.txt" \)
    )
  fi

  find_args+=(
    ! -path "*/.git/*"
    ! -path "*/node_modules/*"
    ! -path "*/.gradle/*"
    ! -path "*/build/*"
    ! -path "*/DerivedData/*"
    ! -path "*/dist/*"
  )

  local count=0
  while IFS= read -r -d '' file; do
    if grep -qF "$old" "$file" 2>/dev/null; then
      # Use perl for portable in-place replacement (works on macOS + Linux)
      perl -pi -e "s/\Q$old\E/$new/g" "$file"
      count=$((count + 1))
    fi
  done < <(find . "${find_args[@]}" -print0)
  echo "  $old → $new  ($count files)"
}

# ── Step 1: Replace file contents ─────────────────────────────────────────────

echo "[1/5] Replacing content in source files..."

# App name (PascalCase) — most important
replace_in_files "$OLD_NAME" "$NEW_PASCAL"

# Bundle IDs
replace_in_files "$OLD_IOS_BUNDLE_PREFIX" "$NEW_BUNDLE"
replace_in_files "$OLD_PACKAGE_ANDROID" "$NEW_BUNDLE"

# Kebab name (package.json, README, CLAUDE.md)
replace_in_files "$OLD_KEBAB" "$NEW_KEBAB"

# Swagger title
replace_in_files "$OLD_SWAGGER_TITLE" "$NEW_PASCAL API"

# ── Step 2: Rename iOS source directories ─────────────────────────────────────

echo "[2/5] Renaming iOS directories..."

ios_dirs=(
  "ios/${OLD_NAME}Tests:ios/${NEW_PASCAL}Tests"
  "ios/${OLD_NAME}UITests:ios/${NEW_PASCAL}UITests"
  "ios/${OLD_NAME}:ios/${NEW_PASCAL}"
)

for entry in "${ios_dirs[@]}"; do
  old_dir="${entry%%:*}"
  new_dir="${entry##*:}"
  if [[ -d "$old_dir" && "$old_dir" != "$new_dir" ]]; then
    mv "$old_dir" "$new_dir"
    echo "  $old_dir → $new_dir"
  fi
done

# Rename xcodeproj directory
if [[ -d "ios/${OLD_NAME}.xcodeproj" && "${OLD_NAME}" != "${NEW_PASCAL}" ]]; then
  mv "ios/${OLD_NAME}.xcodeproj" "ios/${NEW_PASCAL}.xcodeproj"
  echo "  ios/${OLD_NAME}.xcodeproj → ios/${NEW_PASCAL}.xcodeproj"
fi

# Update project.yml name field (was already content-replaced above, but double-check)
# project.yml `name: StarterApp` is handled by replace_in_files above.

# ── Step 3: Move Android package directories ───────────────────────────────────

echo "[3/5] Moving Android package directories..."

android_src_roots=(
  "android/app/src/main/kotlin"
  "android/app/src/test/java"
  "android/app/src/androidTest/java"
)

for src_root in "${android_src_roots[@]}"; do
  old_pkg_dir="$src_root/$OLD_PKG_PATH"
  new_pkg_dir="$src_root/$NEW_PKG_PATH"

  if [[ -d "$old_pkg_dir" && "$old_pkg_dir" != "$new_pkg_dir" ]]; then
    mkdir -p "$(dirname "$new_pkg_dir")"
    mv "$old_pkg_dir" "$new_pkg_dir"
    echo "  $old_pkg_dir → $new_pkg_dir"
  fi
done

# ── Step 4: Regenerate iOS xcodeproj ─────────────────────────────────────────

echo "[4/5] Regenerating iOS xcodeproj..."

if command -v xcodegen &>/dev/null; then
  (cd ios && xcodegen generate --quiet)
  echo "  xcodeproj regenerated"
else
  echo "  xcodegen not found — install with: brew install xcodegen"
  echo "  Then run: cd ios && xcodegen generate"
fi

# ── Step 5: Clean up any stale Gradle caches ─────────────────────────────────

echo "[5/5] Cleaning Android build cache..."
if [[ -f "android/gradlew" ]]; then
  (cd android && ./gradlew clean --quiet 2>/dev/null || true)
  echo "  Gradle clean done"
fi

# ── Done ──────────────────────────────────────────────────────────────────────

echo ""
echo "Rename complete."
echo ""
echo "Next steps:"
echo "  1. Open ios/${NEW_PASCAL}.xcodeproj in Xcode — verify targets build"
echo "  2. Update AppTokens.swift / AppTokens.kt with your brand colours"
echo "  3. Replace YOUR_APP_ID in HardUpdateView.swift / ForceUpdateComponents.kt"
echo "     with your real App Store / Play Store IDs"
echo "  4. Implement email sending in backend/src/email/email.processor.ts"
echo "  5. Update API URLs in ios/Configs/*.xcconfig and android/app/build.gradle.kts"
echo "  6. git add -A && git commit -m 'chore: rename StarterApp → ${NEW_PASCAL}'"
echo ""
