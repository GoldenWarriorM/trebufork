#!/usr/bin/env bash
# trebufork: sync-source.sh is LEGACY and detached from the old project.
#
# It used to rsync launcher-source/ from an external checkout of the old
# "trebuchet-magisk" project.
# That fork is stale (Aug 12): running the old sync here would rsync --delete
# the newer features (desktop store, groups, search, in-launcher wallpaper,
# group folders, ...) that exist ONLY in this repo's launcher-source/, which is
# now the source of truth. build-gradle.sh no longer calls this script for that
# reason.
#
# To import a DIFFERENT, current, known-good external source on purpose, pass it
# explicitly via SOURCE. The stale legacy fork is hard-refused below.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LEGACY_FORK="${LEGACY_FORK:-$HOME/Documents/trebuchet-magisk/launcher}"

SOURCE="${SOURCE:-}"

if [ -z "$SOURCE" ]; then
    echo "sync-source.sh is disabled by default: launcher-source/ is the source of" >&2
    echo "truth and is NOT pulled from any external tree anymore." >&2
    echo "  - Build with ./build-gradle.sh (never calls sync-source.sh)." >&2
    echo "  - Only run this script with SOURCE=<path> to import a current checkout." >&2
    exit 0
fi

case "$SOURCE" in
    "$LEGACY_FORK" | "$LEGACY_FORK"/*)
        echo "Refusing to sync from the stale legacy fork: $SOURCE" >&2
        echo "That tree (trebuchet-magisk) is unmaintained; syncing would delete newer" >&2
        echo "features from launcher-source/. Point SOURCE at a current checkout instead." >&2
        exit 1
        ;;
esac

[ -d "$SOURCE" ] || { echo "Source not found: $SOURCE" >&2; exit 1; }

# Kotlin 2.x compile workarounds (see comments inside each file) must survive
# re-syncs, so keep them out of the rsync --delete sweep.
PRESERVED_FILES=(
    "quickstep/src/com/android/launcher3/taskbar/PinToTaskbarShortcut.kt"
    "quickstep/src/com/android/quickstep/task/thumbnail/TaskContentView.kt"
    "quickstep/src/com/android/quickstep/views/TaskMenuView.kt"
    "quickstep/src/com/android/quickstep/views/RecentsViewUtils.kt"
)

RSYNC_ARGS=(-a --delete --exclude .git)
for f in "${PRESERVED_FILES[@]}"; do
    RSYNC_ARGS+=(--exclude "$f")
done
rsync "${RSYNC_ARGS[@]}" "$SOURCE/" "$ROOT/launcher-source/"
echo "Synced $SOURCE -> $ROOT/launcher-source (${#PRESERVED_FILES[@]} patched files preserved)"