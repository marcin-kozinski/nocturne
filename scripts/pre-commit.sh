#!/usr/bin/env bash
set -euo pipefail

staged=()
while IFS= read -r -d '' file; do
  staged+=("$file")
done < <(git diff --cached --diff-filter=ACMRT --name-only -z)

if [[ ${#staged[@]} -gt 0 ]]; then
  ./scripts/check-code-format.sh --fix "${staged[@]}"
fi
