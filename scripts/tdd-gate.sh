#!/usr/bin/env bash
# tdd-gate.sh — deterministic TDD gates for vethub.
#
# The gates live in the harness (this script + git), not in the prompt.
# An agent may claim "tests pass"; only the exit code of this script counts.
#
# Usage:
#   scripts/tdd-gate.sh status        # run backend tests, print a compact summary (never fails)
#   scripts/tdd-gate.sh red           # RED gate: new tests exist, they FAIL, no production code changed
#                                     #   -> on success the tests are snapshotted with `git add`
#   scripts/tdd-gate.sh green         # GREEN gate: `./gradlew build` passes AND tests unchanged since RED
#   scripts/tdd-gate.sh verify        # GREEN + client `bun run check`
#   scripts/tdd-gate.sh done VET-5    # verify, then move the story refined/ -> completed/<date>-<file>
#
# Exit code 0 = gate passed, 1 = gate failed (reason on stdout).

set -u

ROOT="$(git rev-parse --show-toplevel)"
TESTS="server/src/test"
MAIN="server/src/main"
cd "$ROOT" || exit 1

# Run through mise when available so the pinned Temurin 25 is used.
RUN=""
command -v mise >/dev/null 2>&1 && RUN="mise exec --"

gradle() { (cd "$ROOT/server" && $RUN ./gradlew "$@" --console=plain 2>&1); }

summary() {
  grep -E 'FAILED|error:|tests completed|BUILD (SUCCESSFUL|FAILED)|What went wrong|violation' | head -40
}

pass() { echo "GATE PASSED: $*"; exit 0; }
fail() { echo "GATE FAILED: $*"; exit 1; }

tests_unchanged_since_red() {
  if ! git diff --quiet -- "$TESTS"; then
    echo "Test files changed after RED (invariant: never modify tests to make them pass):"
    git diff --name-only -- "$TESTS"
    return 1
  fi
}

cmd_status() {
  gradle test | summary
  exit 0
}

cmd_red() {
  [ -z "$(git status --porcelain -- "$MAIN")" ] \
    || fail "production code under $MAIN changed during RED; only tests may change"
  [ -n "$(git status --porcelain -- "$TESTS")" ] \
    || fail "no new or changed tests under $TESTS"
  local out
  out="$(gradle test)"
  if echo "$out" | grep -q 'BUILD SUCCESSFUL'; then
    fail "all tests pass; RED needs at least one failing test for the story"
  fi
  echo "$out" | summary
  git add -- "$TESTS"
  pass "RED — tests fail as expected and are snapshotted (git add $TESTS)"
}

cmd_green() {
  tests_unchanged_since_red || fail "tests were modified after RED"
  local out
  out="$(gradle build)"
  echo "$out" | summary
  echo "$out" | grep -q 'BUILD SUCCESSFUL' || fail "./gradlew build is not green"
  tests_unchanged_since_red || fail "tests were modified during the build"
  echo "GREEN ok"
}

cmd_verify() {
  cmd_green || exit 1
  local out rc
  out="$(cd "$ROOT/client" && $RUN bun run check 2>&1)"; rc=$?
  echo "$out" | tail -5
  [ "$rc" -eq 0 ] || fail "client bun run check failed"
  echo "VERIFY ok"
}

cmd_done() {
  local id="${1:-}"
  [ -n "$id" ] || fail "usage: tdd-gate.sh done VET-#"
  local story
  story="$(ls .opencode/refined/"$id"-*.md 2>/dev/null | head -1)"
  [ -n "$story" ] || fail "no refined story for $id in .opencode/refined/"
  ( cmd_verify ) || fail "verify failed; story stays in refined/"
  local target
  target=".opencode/completed/$(date +%Y%m%d)-$(basename "$story")"
  mkdir -p .opencode/completed
  mv "$story" "$target"
  pass "DONE — $story -> $target"
}

case "${1:-}" in
  status) cmd_status ;;
  red)    cmd_red ;;
  green)  cmd_green && pass "GREEN — build passes, tests untouched" ;;
  verify) cmd_verify && pass "VERIFY — backend green and client typecheck ok" ;;
  done)   cmd_done "${2:-}" ;;
  *)      sed -n '2,17p' "$0"; exit 2 ;;
esac
