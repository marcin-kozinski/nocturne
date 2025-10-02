#!/usr/bin/env bash
set -euo pipefail

if ! command -v ktfmt &> /dev/null; then
  echo "Error: ktfmt is not installed."
  echo "Install it with: brew install ktfmt"
  exit 1
fi

if ! command -v kotlin &> /dev/null; then
  echo "Error: kotlin is not installed."
  echo "Install it with: brew install kotlin"
  exit 1
fi

./scripts/check-code-format.sh --fix
