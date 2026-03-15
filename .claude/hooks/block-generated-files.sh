#!/usr/bin/env bash
# PreToolUse — Edit|Write
# Blocks edits to sentinel-generated files. Edit schemas + run schema:generate instead.

input=$(cat)
file=$(echo "$input" | jq -r '.tool_input.file_path // empty')

[[ -z "$file" ]] && exit 0

generated=(
  "ios/StarterApp/DesignSystem/Tokens/AppTokens.swift"
  "ios/StarterApp/Core/FeatureFlags.swift"
  "ios/StarterApp/Core/Models/Models.swift"
  "ios/StarterApp/Core/Network/APIClientProtocol.swift"
  "android/app/src/main/kotlin/com/appstarterkit/app/design/tokens/AppTokens.kt"
  "android/app/src/main/res/values/strings-generated.xml"
  "android/app/src/main/kotlin/com/appstarterkit/app/core/FeatureFlags.kt"
  "android/app/src/main/kotlin/com/appstarterkit/app/core/models/Models.kt"
  "android/app/src/main/kotlin/com/appstarterkit/app/core/network/APIClient.kt"
)

for gf in "${generated[@]}"; do
  if [[ "$file" == *"$gf"* ]]; then
    echo "BLOCKED: $file is a generated file — do not edit directly." >&2
    echo "Edit the source schema in sentinel/schemas/ then run: npm run schema:generate" >&2
    exit 2
  fi
done

exit 0
