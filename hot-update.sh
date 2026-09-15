#!/usr/bin/env bash
#
# Trebufork — hot launcher update (no reboot)
#
# The Magisk module's overlay (systemless mount) is only applied at boot, but
# the *package* does not need a reboot: the platform-signed APK can be
# registered live with `pm install -r -d`, exactly what the module's service.sh
# does at every boot. PMS kills the running launcher process during the
# install and the HOME role restarts it automatically — the user only sees a
# brief launcher relaunch.
#
# This script therefore:
#   1. pushes the new (platform-signed) APK to the device,
#   2. `pm install -r -d` — the launcher process restarts on its own,
#   3. re-grants the runtime permissions the update resets,
#   4. syncs the APK into the installed module dir and refreshes the
#      `.apk_installed` marker, so the next real boot does NOT reinstall the
#      stale mounted APK over the fresh one (service.sh skips when marker ==
#      size-mtime of the module APK and PMS resolves into /data/app).
#
# Usage: ./hot-update.sh [APK] [--serial DEVICE]
# APK defaults to the signed launcher-release.apk (signs the unsigned build
# with the platform key first, same as package-magisk.sh).

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SERIAL=""
APK=""
REMOTE_APK=/data/local/tmp/trebufork-hotupdate.apk

PKG="com.android.launcher3"
MODULE_ID="trebuchetmagisk"
MODULE_APK="/data/adb/modules/$MODULE_ID/system/system_ext/priv-app/Launcher3QuickStep/Launcher3QuickStep.apk"
MODULE_DIR="/data/adb/modules/$MODULE_ID"
# Permissions re-granted after the update (service.sh does the same at boot).
GRANTS=(
    android.permission.READ_MEDIA_IMAGES
    android.permission.READ_EXTERNAL_STORAGE
)

usage() {
    cat <<'EOF'
Usage: ./hot-update.sh [APK] [--serial DEVICE]

Update the launcher on the connected device WITHOUT a reboot.
APK defaults to launcher/build/outputs/apk/release/launcher-release.apk
(the unsigned build is signed with the platform key automatically).
EOF
}

while [ "$#" -gt 0 ]; do
    case "$1" in
        -h|--help) usage; exit 0 ;;
        --serial)
            [ "$#" -ge 2 ] || { echo "Missing value for --serial" >&2; exit 2; }
            SERIAL="$2"; shift 2 ;;
        --serial=*)
            SERIAL="${1#*=}"; [ -n "$SERIAL" ] || { echo "Empty --serial value" >&2; exit 2; }
            shift ;;
        -*)
            echo "Unknown option: $1" >&2; usage >&2; exit 2 ;;
        *)
            [ -z "$APK" ] || { echo "Only one APK may be specified" >&2; exit 2; }
            APK="$1"; shift ;;
    esac
done

# ---------------------------------------------------------------- APK choice -
RELEASE_APK="$ROOT/launcher/build/outputs/apk/release/launcher-release.apk"
RELEASE_UNSIGNED="$ROOT/launcher/build/outputs/apk/release/launcher-release-unsigned.apk"

if [ -z "$APK" ]; then
    if [ -f "$RELEASE_APK" ]; then
        APK="$RELEASE_APK"
    elif [ -f "$RELEASE_UNSIGNED" ]; then
        APK="$RELEASE_UNSIGNED"
    else
        echo "No APK found. Build :launcher:assembleRelease first (or pass a path)." >&2
        exit 1
    fi
fi
[ -f "$APK" ] || { echo "APK not found: $APK" >&2; exit 1; }
APK="$(cd "$(dirname "$APK")" && pwd)/$(basename "$APK")"

# Sign the unsigned release build with the platform key (same as package-magisk.sh).
if [ "$APK" = "$RELEASE_UNSIGNED" ]; then
    APKSIGNER="$(ls "${ANDROID_HOME:-$HOME/android-sdk}"/build-tools/*/apksigner 2>/dev/null | sort -V | tail -1)"
    TREE="${LINEAGE_ROOT:-$ROOT/platform}"
    PK8="${PLATFORM_PK8:-$TREE/build/make/target/product/security/platform.pk8}"
    PEM="${PLATFORM_PEM:-$TREE/build/make/target/product/security/platform.x509.pem}"
    [ -n "$APKSIGNER" ] && [ -f "$PK8" ] && [ -f "$PEM" ] || {
        echo "apksigner/platform key not found; cannot sign $APK" >&2
        exit 1
    }
    echo "Signing $(basename "$APK") with the platform key..."
    "$APKSIGNER" sign --key "$PK8" --cert "$PEM" \
        --out "$RELEASE_APK" "$RELEASE_UNSIGNED"
    APK="$RELEASE_APK"
fi

# ----------------------------------------------------------------- adb setup -
command -v adb >/dev/null || { echo "adb command not found" >&2; exit 1; }

if [ -n "$SERIAL" ]; then
    ADB=(adb -s "$SERIAL")
else
    mapfile -t devices < <(adb devices | awk 'NR > 1 && $2 == "device" { print $1 }')
    [ "${#devices[@]}" -eq 1 ] || {
        echo "Expected exactly one ready adb device; found ${#devices[@]}" >&2
        adb devices >&2
        echo "Use --serial DEVICE when multiple devices are connected." >&2
        exit 1
    }
    SERIAL="${devices[0]}"
    ADB=(adb -s "$SERIAL")
fi

sh() { "${ADB[@]}" shell "$@"; }

root_id="$(sh "su -c id" 2>/dev/null || true)"
echo "$root_id" | grep -q 'uid=0' || {
    echo "Root access through Magisk su is required on $SERIAL" >&2
    exit 1
}

module_dir_exists="$(sh "su -c \"[ -d '$MODULE_DIR' ] && echo yes\" 2>/dev/null || true" | tr -d '\r')"
[ "$module_dir_exists" = "yes" ] || {
    echo "Module $MODULE_ID is not installed on $SERIAL; flash it once with ./flash-magisk.sh first." >&2
    exit 1
}

echo "Device: $SERIAL"
echo "APK:    $APK"

# ------------------------------------------------------- 1. push + pm install -
echo "Pushing APK..."
"${ADB[@]}" push "$APK" "$REMOTE_APK" >/dev/null

echo "Installing update (live, launcher will restart itself)..."
# -r: replace; -d: allow same/lower versionCode (versionCode is pinned to 36).
if ! sh "pm install -r -d $REMOTE_APK"; then
    echo "pm install failed; device state unchanged (old launcher still active)." >&2
    sh "rm -f $REMOTE_APK"
    exit 1
fi

# ---------------------------------------------- 2. re-grant runtime permissions
for perm in "${GRANTS[@]}"; do
    sh "pm grant $PKG $perm" >/dev/null 2>&1 || true
done

# ------------------------------------ 3. sync module dir + installation marker
# Replace the module APK (rm + cp: a fresh inode — the old one may still be
# pinned by the boot-time bind mount, which is fine; after the next reboot
# Magisk mounts the new file) and refresh the .apk_installed marker so
# service.sh does not reinstall the stale APK over this fresh one.
echo "Syncing module overlay + marker..."
sh "su -c '
    rm -f \"$MODULE_APK\"
    cp \"$REMOTE_APK\" \"$MODULE_APK\"
    chown 0:0 \"$MODULE_APK\" 2>/dev/null || true
    chmod 644 \"$MODULE_APK\" 2>/dev/null || true
    restorecon \"$MODULE_APK\" 2>/dev/null || true
    sig=\$(stat -c \"%s-%Y\" \"$MODULE_APK\")
    echo \"\$sig\" > \"$MODULE_DIR/.apk_installed\"
    rm -f \"$REMOTE_APK\"
'" >/dev/null

# ----------------------------------------------------------------- 4. verify -
sleep 2
new_path="$(sh "pm path $PKG" 2>/dev/null | head -1 | tr -d '\r')"
new_ver="$(sh "dumpsys package $PKG" 2>/dev/null | grep -m1 versionName | tr -d '\r')"
marker="$(sh "su -c \"cat $MODULE_DIR/.apk_installed\"" 2>/dev/null | tr -d '\r')"

echo
echo "Update applied without reboot:"
echo "  package path: ${new_path#package:}"
echo "  $new_ver"
echo "  module marker: ${marker:-<missing>}"
case "$new_path" in
    */data/app/*) ;;
    *) echo "WARNING: package did not register into /data/app — check signatures/versionCode." >&2 ;;
esac
[ -n "$marker" ] || echo "WARNING: marker not written — next boot will reinstall the module APK." >&2
