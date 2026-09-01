#!/usr/bin/env bash
# update-platform.sh — refresh the vendored platform sources in platform/frameworks/
# from the LineageOS git repositories (no full tree checkout required).
#
# Usage:
#   ./update-platform.sh [BRANCH]
#
#   BRANCH defaults to lineage-23.2. Examples:
#     ./update-platform.sh                 # refresh sources on the current branch
#     ./update-platform.sh lineage-24.0    # switch the vendored sources to a newer branch
#
# Env:
#   PLATFORM_BRANCH      default branch (same as the positional arg)
#   PLATFORM_CACHE_DIR   cache dir for the git mirrors (default ~/.cache/trebufork-platform)
#   PLATFORM_SKIP_CACHE  set to 1 to use a fresh temp dir and discard the cache after
#
# What it updates: only the framework/SystemUI sources compiled into the APK
# (the launcher-* modules read them from platform/frameworks/...). It does NOT
# touch the maven repositories (platform/prebuilts/sdk/...), framework.jar,
# framework-res.apk or the prebuilts/soong jars — those are tied to the Android
# platform version, not to branch updates. For a major platform bump (e.g.
# Android 16 -> 17) also update the maven repositories, framework.jar and the
# platform prebuilts; see the README build section.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BRANCH="${1:-${PLATFORM_BRANCH:-lineage-23.2}}"
CACHE="${PLATFORM_CACHE_DIR:-${XDG_CACHE_HOME:-$HOME/.cache}/trebufork-platform}"
FRAMEWORKS_URL="https://github.com/LineageOS/android_frameworks_base.git"
SYSTEMUI_URL="https://github.com/LineageOS/android_frameworks_libs_systemui.git"

# ---- paths to keep under platform/frameworks (directories, cone-mode sparse) ----
BASE_PATHS="
packages/SystemUI/animation
packages/SystemUI/plugin_core
packages/SystemUI/plugin
packages/SystemUI/shared
packages/SystemUI/utils
packages/SystemUI/unfold
packages/SystemUI/log
packages/SystemUI/common/src/com/android/systemui/common/buffer
packages/SystemUI/pods/src/com/android/systemui/util/kotlin
packages/SystemUI/pods/src/com/android/systemui/util/time
packages/SystemUI/pods/src/com/android/systemui/log/table
packages/SystemUI/pods/src/com/android/systemui/rotation
packages/SettingsLib/SettingsTheme
libs/WindowManager/Shell/src
libs/WindowManager/Shell/shared/src
libs/WindowManager/Shell/shared/res
"
SYSTEMUI_PATHS="
animationlib/src
animationlib/res
contextualeducationlib/src
iconloaderlib/src
iconloaderlib/res
mechanics/src
msdllib/src
tracinglib/core/src
displaylib/src
viewcapturelib/src
"

# Fetch (or update) a partial clone with sparse checkout of the needed dirs.
# Echoes the checked-out commit hash on stdout.
sync_repo() {
    local url="$1" dir="$2" branch="$3"
    shift 3
    if [ ! -d "$dir/.git" ]; then
        echo "  cloning $url ($branch)..." >&2
        git clone --filter=blob:none --no-checkout -b "$branch" "$url" "$dir"
    else
        echo "  fetching $url ($branch)..." >&2
        git -C "$dir" fetch origin "$branch"
    fi
    git -C "$dir" sparse-checkout set --cone "$@"
    git -C "$dir" checkout -q --force "$branch"
    git -C "$dir" rev-parse HEAD
}

copy_tree() { # src dst
    local src="$1" dst="$2"
    rm -rf "$dst"
    mkdir -p "$(dirname "$dst")"
    cp -r "$src" "$dst"
}

if [ "${PLATFORM_SKIP_CACHE:-0}" = "1" ]; then
    CACHE="$(mktemp -d)"
    trap 'rm -rf "$CACHE"' EXIT
fi
mkdir -p "$CACHE"

echo "==> Updating vendored platform sources to branch '$BRANCH'"

# ---- frameworks/base (SystemUI, SettingsTheme, WMShell) ----
BASE_DIR="$CACHE/frameworks_base"
BASE_COMMIT=$(sync_repo "$FRAMEWORKS_URL" "$BASE_DIR" "$BRANCH" $BASE_PATHS)
echo "  frameworks/base @ ${BASE_COMMIT:0:12}" >&2
for p in $BASE_PATHS; do
    [ -d "$BASE_DIR/$p" ] || { echo "ERROR: $p missing in frameworks/base" >&2; exit 1; }
    copy_tree "$BASE_DIR/$p" "$ROOT/platform/frameworks/base/$p"
done

# ---- frameworks/libs/systemui ----
SU_DIR="$CACHE/frameworks_libs_systemui"
SU_COMMIT=$(sync_repo "$SYSTEMUI_URL" "$SU_DIR" "$BRANCH" $SYSTEMUI_PATHS)
echo "  frameworks/libs/systemui @ ${SU_COMMIT:0:12}" >&2
for p in $SYSTEMUI_PATHS; do
    [ -d "$SU_DIR/$p" ] || { echo "ERROR: $p missing in frameworks/libs/systemui" >&2; exit 1; }
    copy_tree "$SU_DIR/$p" "$ROOT/platform/frameworks/libs/systemui/$p"
done

# ---- verify: every frameworks/... path referenced by build.gradle exists ----
MISSING=0
while read -r p; do
    [ -z "$p" ] && continue
    case "$p" in
        # false positives from comments / soong-jar paths
        *Android.bp*|*android.os.flags*|*com.android.window.flags*|*core/java.*|*frameworks/libs/modules*|*dagger/*|*SystemUI-statsd*|*shared/SystemUI*) ;;
        *)
            if [ ! -e "$ROOT/platform/$p" ]; then
                echo "WARN: referenced path missing: $p" >&2
                MISSING=1
            fi ;;
    esac
done < <(grep -rhoE "frameworks/[A-Za-z0-9_./]+" "$ROOT"/launcher*/build.gradle | grep -v "\.jar$" | sort -u)
[ "$MISSING" = "0" ] || { echo "ERROR: some build.gradle paths are missing under platform/" >&2; exit 1; }

# ---- record the sync in platform/README.md ----
printf '\nLast synced by update-platform.sh: branch %s\n- frameworks/base @ %s\n- frameworks/libs/systemui @ %s\n' \
    "$BRANCH" "$BASE_COMMIT" "$SU_COMMIT" >> "$ROOT/platform/README.md"

echo "==> Done. Vendored sources updated to branch '$BRANCH'."
echo "    frameworks/base             @ ${BASE_COMMIT:0:12}"
echo "    frameworks/libs/systemui    @ ${SU_COMMIT:0:12}"
echo "    Rebuild: gradle :launcher:assembleRelease"
