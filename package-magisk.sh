#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# trebufork: the Magisk module template is now vendored in-repo (magisk-template/)
# so packaging no longer depends on the old external trebuchet-magisk
# project. Override MAGISK_TEMPLATE to use a different template if needed.
MAGISK_TEMPLATE="${MAGISK_TEMPLATE:-$ROOT/magisk-template}"
OUT="$ROOT/build/magisk-module"
ZIP_DIR="$ROOT/build"

# The Magisk module replaces the system launcher com.android.launcher3, so the
# packaged APK must carry that package name. The Gradle debug build has the
# '.debug' applicationId suffix and would leave the package manager with stale
# (mismatched) metadata, so we package the release APK and sign it with the
# platform key (same key as the stock launcher).
RELEASE_APK="$ROOT/launcher/build/outputs/apk/release/launcher-release.apk"
RELEASE_UNSIGNED="$ROOT/launcher/build/outputs/apk/release/launcher-release-unsigned.apk"
DEBUG_APK="$ROOT/launcher/build/outputs/apk/debug/launcher-debug.apk"

APK="${1:-}"

# Locate apksigner and the platform key. The test platform key is vendored in
# platform/build/... (no Lineage tree needed); LINEAGE_ROOT overrides it for
# signing with a device-specific key.
APKSIGNER="$(ls "${ANDROID_HOME:-$HOME/android-sdk}"/build-tools/*/apksigner 2>/dev/null | sort -V | tail -1)"
TREE="${LINEAGE_ROOT:-$ROOT/platform}"
# The signing key never lives in the repo: pass it via PLATFORM_PK8/PLATFORM_PEM
# (e.g. from CI secrets). Falls back to the vendored test key for local builds.
PK8="${PLATFORM_PK8:-$TREE/build/make/target/product/security/platform.pk8}"
PEM="${PLATFORM_PEM:-$TREE/build/make/target/product/security/platform.x509.pem}"

[ -d "$MAGISK_TEMPLATE" ] || { echo "Magisk template not found: $MAGISK_TEMPLATE" >&2; exit 1; }
command -v zip >/dev/null || { echo "zip command not found" >&2; exit 1; }

if [ -z "$APK" ]; then
    if [ -f "$RELEASE_APK" ]; then
        APK="$RELEASE_APK"
    elif [ -f "$RELEASE_UNSIGNED" ]; then
        # Sign the release build with the platform key before packaging.
        [ -n "$APKSIGNER" ] && [ -f "$PK8" ] && [ -f "$PEM" ] || {
            echo "apksigner/platform key not found; cannot sign the release APK" >&2
            echo "Run assembleRelease and sign it first, or pass an APK path." >&2
            exit 1
        }
        "$APKSIGNER" sign --key "$PK8" --cert "$PEM" \
            --out "$RELEASE_APK" "$RELEASE_UNSIGNED"
        APK="$RELEASE_APK"
    elif [ -f "$DEBUG_APK" ]; then
        echo "WARNING: using the debug APK (com.android.launcher3.debug)." >&2
        echo "The launcher app name/settings labels may be stale; build release instead." >&2
        APK="$DEBUG_APK"
    else
        echo "No APK found. Build :launcher:assembleRelease (or :launcher:assembleDebug) first." >&2
        exit 1
    fi
fi

[ -f "$APK" ] || { echo "Gradle APK not found: $APK" >&2; exit 1; }

rm -rf "$OUT"
mkdir -p "$OUT/system/system_ext/priv-app/Launcher3QuickStep"
cp -a "$MAGISK_TEMPLATE/." "$OUT/"
rm -f "$OUT/system/system_ext/priv-app/Launcher3QuickStep/"*.apk
cp -a "$APK" "$OUT/system/system_ext/priv-app/Launcher3QuickStep/Launcher3QuickStep.apk"

version="$(grep -m1 '^version=' "$OUT/module.prop" | cut -d= -f2)"
[ -n "$version" ] || { echo "Missing version in $OUT/module.prop" >&2; exit 1; }
# The displayed version may contain spaces (e.g. "v0.2.0 by AINN"); sanitize it
# for the ZIP file name.
zip_version="$(echo "$version" | tr ' ' '_')"
ZIP="$ZIP_DIR/Trebufork-magisk-${zip_version}.zip"
rm -f "$ZIP"
(cd "$OUT" && zip -q -r "$ZIP" .)

unzip -tq "$ZIP"
echo "APK: $APK"
echo "Magisk module: $OUT"
echo "Flashable ZIP: $ZIP"
