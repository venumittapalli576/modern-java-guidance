#!/bin/sh
# Claude Code PostToolUse hook for modern-java-guidance.
#
# Runs the scanner over the file the agent just wrote and reports legacy patterns on the
# lines it changed. The scanner is one Java source file; it is compiled once into a cache
# directory so that later runs start in a fraction of a second.
#
# This hook never fails the tool call for its own reasons: no JDK, no cache directory or a
# compile error all end in a silent exit 0.

ROOT="${CLAUDE_PLUGIN_ROOT:-$(cd "$(dirname "$0")/.." && pwd)}"
SKILL="$ROOT/skills/modern-java-guidance"
SRC="$SKILL/scripts/LegacyScan.java"

command -v java >/dev/null 2>&1 || exit 0
[ -f "$SRC" ] || exit 0

JAVA_FLAGS="-XX:TieredStopAtLevel=1 -XX:+UseSerialGC -Xshare:auto"

if command -v javac >/dev/null 2>&1 && command -v cksum >/dev/null 2>&1; then
    KEY=$(cksum < "$SRC" | tr -c '0-9\n' '-')
    CACHE="${XDG_CACHE_HOME:-$HOME/.cache}/modern-java-guidance/$KEY"
    if [ ! -f "$CACHE/LegacyScan.class" ]; then
        # --release 17 keeps the cached classes runnable if a different JDK is on PATH next time
        mkdir -p "$CACHE" 2>/dev/null && javac --release 17 -nowarn -d "$CACHE" "$SRC" >/dev/null 2>&1
    fi
    if [ -f "$CACHE/LegacyScan.class" ]; then
        # shellcheck disable=SC2086
        exec java $JAVA_FLAGS -cp "$CACHE" LegacyScan --guides "$SKILL/guides" hook
    fi
fi

# Fallback: run from source (slower, but needs nothing writable).
# shellcheck disable=SC2086
exec java $JAVA_FLAGS "$SRC" --guides "$SKILL/guides" hook
