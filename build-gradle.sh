#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# AGP 8.9.1 needs Gradle 8.x (system gradle here is 9.6.1 and is incompatible).
# Prefer the cached 8.13 wrapper distribution, then the PATH entry.
if [ -z "${GRADLE_BIN:-}" ]; then
    GRADLE_BIN=$(ls -d "$HOME/.gradle/wrapper/dists/gradle-8.13-bin"/*/gradle-8.13/bin/gradle 2>/dev/null | head -1)
    GRADLE_BIN="${GRADLE_BIN:-gradle}"
fi
LINEAGE_ROOT="${LINEAGE_ROOT:-}"
ANDROID_HOME="${ANDROID_HOME:-$HOME/android-sdk}"
ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-$ANDROID_HOME}"
JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/java-21-openjdk}"

export ANDROID_HOME ANDROID_SDK_ROOT JAVA_HOME

cd "$ROOT"
# NOTE: sync-source.sh is intentionally NOT called here. It rsyncs the stale
# trebuchet-magisk/launcher fork (Aug 12) over launcher-source, deleting all
# newer features (desktop store, groups, search). launcher-source in this git
# repo is the source of truth. Run it explicitly only if you know what you're doing.
# The build is fully standalone (sources + Maven repos vendored in platform/);
# -PlineageRoot is only needed to build against a live Lineage checkout.
if [ -n "$LINEAGE_ROOT" ]; then
    "$GRADLE_BIN" -PlineageRoot="$LINEAGE_ROOT" :launcher:assembleRelease "$@"
else
    "$GRADLE_BIN" :launcher:assembleRelease "$@"
fi
"$ROOT/package-magisk.sh"
