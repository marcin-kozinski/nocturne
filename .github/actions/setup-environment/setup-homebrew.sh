#!/usr/bin/env bash
set -euo pipefail

echo "Enabling Homebrew…"

# Enable Homebrew in current step
# See: https://github.com/actions/runner-images/blob/main/images/ubuntu/Ubuntu2404-Readme.md#homebrew-note
eval "$(/home/linuxbrew/.linuxbrew/bin/brew shellenv)"

# Export environment variables for subsequent steps
{
  echo "HOMEBREW_PREFIX=$HOMEBREW_PREFIX"
  echo "HOMEBREW_CELLAR=$HOMEBREW_CELLAR"
  echo "HOMEBREW_REPOSITORY=$HOMEBREW_REPOSITORY"
} >> "$GITHUB_ENV"

# Add Homebrew to PATH for subsequent steps
# See: https://docs.github.com/en/actions/reference/workflows-and-actions/workflow-commands#adding-a-system-path
{
  echo "$HOMEBREW_PREFIX/bin"
  echo "$HOMEBREW_PREFIX/sbin"
} >> "$GITHUB_PATH"
