#!/usr/bin/env bash
set -euo pipefail

ktfmt_args=(--kotlinlang-style)

if [[ "${1:-}" == "--fix" ]]; then
  versions_toml_fmt_mode="--fix"
else
  ktfmt_args+=(--dry-run --set-exit-if-changed)
  versions_toml_fmt_mode="--dry-run"
fi

ktfmt "${ktfmt_args[@]}" .
./scripts/versions-toml-fmt.main.kts "$versions_toml_fmt_mode" gradle/libs.versions.toml
