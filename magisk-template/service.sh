#!/system/bin/sh
#
# Trebufork Quickstep — late boot script
#
# The module replaces the system launcher APK with a systemless mount, but the
# overlay is only visible after the package manager's boot-time scan. To make the
# replacement stick (correct app name / settings labels, version metadata), we
# re-register the mounted APK as an "updated system app" once the overlay is live.
# Because it is platform-signed and shares the package name, it keeps the
# PRIVILEGED / SYSTEM_EXT flags, so widgets, recents and all launcher features
# keep working.
#
# The marker file prevents re-stomping a user-chosen default launcher on later
# boots.
#
MODDIR=${0%/*}
MARKER="$MODDIR/.home_set"
SRC_APK="/system_ext/priv-app/Launcher3QuickStep/Launcher3QuickStep.apk"

# 1. Wait for the package manager (it comes up a few seconds before boot
#    completes).
until pm path com.android.launcher3 >/dev/null 2>&1; do
    sleep 1
done

# 2. Re-register the mounted launcher so the package manager picks up the correct
#    manifest (labels, versionCode). Idempotent: a no-op once the same APK is
#    already registered (marker stores the mounted APK's size-mtime signature),
#    so normal boots never reinstall and never kill the running launcher. The
#    marker is written only on success, so a failed install is retried next boot.
#
#    A single install attempt as soon as PMS is up fails silently (the install
#    pipeline is not ready yet), so we poll until it succeeds. Retrying from
#    PMS-up instead of waiting for boot completion makes the reinstall land
#    BEFORE the launcher's home activity starts after a flash: the install-
#    triggered kill (installPackageLI) then only hits the early service process,
#    and the user never sees a restart or a doubled boot entrance animation.
if [ -f "$SRC_APK" ]; then
    APK_SIG="$(stat -c '%s-%Y' "$SRC_APK" 2>/dev/null)"
    INSTALL_MARKER="$MODDIR/.apk_installed"
    if [ -n "$APK_SIG" ] && [ "$(cat "$INSTALL_MARKER" 2>/dev/null)" = "$APK_SIG" ]; then
        : # same APK already registered, skip the kill-triggering reinstall
    else
        tries=0
        until pm install -r -d "$SRC_APK" >/dev/null 2>&1 || [ "$tries" -ge 20 ]; do
            sleep 3
            tries=$((tries + 1))
        done
        if [ "$tries" -lt 20 ]; then
            echo "$APK_SIG" > "$INSTALL_MARKER"
        fi
    fi
fi

# 3. Grant the wallpaper bitmap permissions (Android 13+) AFTER the (possible)
#    reinstall — a reinstall resets runtime grants, so granting first would be
#    wiped. Runs as soon as the package manager is up (or right after a flash
#    reinstall), which is still before the launcher's home activity attaches at
#    boot completion; WallpaperManager.getDrawable() then succeeds on the very
#    first frame (no black flash at boot).
pm grant com.android.launcher3 android.permission.READ_MEDIA_IMAGES >/dev/null 2>&1
pm grant com.android.launcher3 android.permission.READ_EXTERNAL_STORAGE >/dev/null 2>&1

# 4. Set the HOME role once (the role manager is only ready after boot
#    completion).
until [ "$(getprop sys.boot_completed)" = "1" ]; do
    sleep 1
done
if [ ! -f "$MARKER" ]; then
    cmd role add-role-holder --user 0 --silent \
        android.app.role.HOME com.android.launcher3 2>/dev/null
    touch "$MARKER"
fi

exit 0
