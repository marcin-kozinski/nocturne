#!/usr/bin/env bash
set -euo pipefail

## Usage: /scripts/check-code-format.sh [--fix] [FILE...]
#  Check if all files in working directory are well formatted.
#  If not, print violating files and set non-0 exit.
#
#  --fix   Optional: Format files instead of just checking
#  FILE... Optional: Run only on the specified files

ensure_kotlin_installed() {
  if ! command -v kotlin &> /dev/null; then
    echo "Error: kotlin is not installed."
    echo "Install it with: brew install kotlin"
    exit 1
  fi
}

ktfmt_args=(--kotlinlang-style)
ktfmt_files=()
versions_toml_fmt_files=()

if [[ "${1:-}" == "--fix" ]]; then
  versions_toml_fmt_mode="--fix"
  shift
else
  ktfmt_args+=(--dry-run --set-exit-if-changed)
  versions_toml_fmt_mode="--dry-run"
fi

if [[ $# -gt 0 ]]; then
  for file in "$@"; do
    if [[ "$file" == *.kt || "$file" == *.kts ]]; then
      ktfmt_files+=("$file")
    elif [[ "$file" == "gradle/libs.versions.toml" ]]; then
      versions_toml_fmt_files+=("$file")
    fi
  done
else
  # Default: format all Kotlin files and version catalog
  ktfmt_files+=(".")
  versions_toml_fmt_files+=("gradle/libs.versions.toml")
fi

if [[ ${#ktfmt_files[@]} -gt 0 ]]; then
  ensure_kotlin_installed
  ./scripts/ktfmt.main.kts "${ktfmt_args[@]}" "${ktfmt_files[@]}"
fi

if [[ ${#versions_toml_fmt_files[@]} -gt 0 ]]; then
  ensure_kotlin_installed
  ./scripts/versions-toml-fmt.main.kts "$versions_toml_fmt_mode" "${versions_toml_fmt_files[@]}"
fi
