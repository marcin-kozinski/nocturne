#!/usr/bin/env bash
set -euo pipefail

# Check for uncommitted changes
if [[ -n $(git status --porcelain) ]]; then
  echo "Please commit your changes before pushing."
  echo ""
  echo "If you're aware of the uncommitted changes and don't want to commit yet,"
  echo "please stash them temporarily."
  exit 1
fi
