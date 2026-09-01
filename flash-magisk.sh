#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ZIP=""
SERIAL=""
REBOOT=true
REMOTE_ZIP="/data/local/tmp/trebufork-magisk-module.zip"

usage() {
    cat <<'EOF'
Usage: ./flash-magisk.sh [MODULE.zip] [--serial DEVICE] [--no-reboot]

Flash a Magisk module through adb, wait for Android to boot, and verify the module.
When MODULE.zip is omitted, the newest build/Trebufork-magisk-*.zip is used.
EOF
}

while [ "$#" -gt 0 ]; do
    case "$1" in
        -h|--help)
            usage
            exit 0
            ;;
        --no-reboot)
            REBOOT=false
            shift
            ;;
        --serial)
            [ "$#" -ge 2 ] || { echo "Missing value for --serial" >&2; exit 2; }
            SERIAL="$2"
            shift 2
            ;;
        --serial=*)
            SERIAL="${1#*=}"
            [ -n "$SERIAL" ] || { echo "Empty --serial value" >&2; exit 2; }
            shift
            ;;
        -*)
            echo "Unknown option: $1" >&2
            usage >&2
            exit 2
            ;;
        *)
            [ -z "$ZIP" ] || { echo "Only one module ZIP may be specified" >&2; exit 2; }
            ZIP="$1"
            shift
            ;;
    esac
done

command -v adb >/dev/null || { echo "adb command not found" >&2; exit 1; }
command -v unzip >/dev/null || { echo "unzip command not found" >&2; exit 1; }

if [ -z "$ZIP" ]; then
    shopt -s nullglob
    zips=("$ROOT"/build/Trebufork-magisk-*.zip)
    shopt -u nullglob
    [ "${#zips[@]}" -gt 0 ] || {
        echo "No Magisk ZIP found in $ROOT/build" >&2
        echo "Build and package the module first." >&2
        exit 1
    }
    IFS=$'\n' zips=( $(ls -t "${zips[@]}" 2>/dev/null) )
    ZIP="${zips[0]}"
fi

[ -f "$ZIP" ] || { echo "Magisk ZIP not found: $ZIP" >&2; exit 1; }
ZIP="$(cd "$(dirname "$ZIP")" && pwd)/$(basename "$ZIP")"

module_id="$(unzip -p "$ZIP" module.prop 2>/dev/null | sed -n 's/^id=//p' | head -1 | tr -d '\r')"
[ -n "$module_id" ] || {
    echo "Cannot read module id from $ZIP (module.prop is missing or invalid)" >&2
    exit 1
}
[[ "$module_id" =~ ^[A-Za-z0-9._-]+$ ]] || {
    echo "Invalid Magisk module id: $module_id" >&2
    exit 1
}

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

root_id="$("${ADB[@]}" shell su -c id 2>/dev/null || true)"
echo "$root_id" | grep -q 'uid=0' || {
    echo "Root access through Magisk su is required on $SERIAL" >&2
    exit 1
}

module_name="$(unzip -p "$ZIP" module.prop | sed -n 's/^name=//p' | head -1 | tr -d '\r')"
echo "Device: $SERIAL"
echo "Module: ${module_name:-$module_id} ($module_id)"
echo "ZIP: $ZIP"

"${ADB[@]}" push "$ZIP" "$REMOTE_ZIP"
"${ADB[@]}" shell su -c "magisk --install-module $REMOTE_ZIP"
"${ADB[@]}" shell rm -f "$REMOTE_ZIP"

if [ "$REBOOT" = true ]; then
    echo "Rebooting device..."
    "${ADB[@]}" reboot
    "${ADB[@]}" wait-for-device

    booted=false
    for _ in $(seq 1 60); do
        boot_completed="$("${ADB[@]}" shell getprop sys.boot_completed 2>/dev/null \
                | tr -d '\r' || true)"
        if [ "$boot_completed" = "1" ]; then
            booted=true
            break
        fi
        sleep 2
    done
    [ "$booted" = true ] || {
        echo "Device did not report sys.boot_completed=1 within 120 seconds" >&2
        exit 1
    }
fi

installed_prop="$("${ADB[@]}" shell su -c "cat /data/adb/modules/$module_id/module.prop" \
        2>/dev/null | tr -d '\r' || true)"
echo "$installed_prop" | grep -q "^id=$module_id$" || {
    echo "Magisk module $module_id was not found after installation" >&2
    exit 1
}

echo "Magisk module installed successfully${REBOOT:+ and verified}."
