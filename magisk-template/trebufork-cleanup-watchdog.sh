#!/system/bin/sh
#
# Trebufork — disable/remove watchdog (installed into /data/adb/service.d)
#
# Lives OUTSIDE the module directory, so it keeps running at every boot even
# when the module is disabled, its service.sh no longer executes, and after the
# module is fully removed (until uninstall.sh removes it too).
#
# Its single job: if the module is not active (absent, disabled via the
# "remove"/"disable" flag files, or its systemless mount of the launcher APK is
# gone) while the package manager still carries the module's `pm install -r`
# update of com.android.launcher3 — undo that update and wipe the launcher
# caches, so the device returns to the stock launcher with a clean state.
#
# Runs at each boot from Magisk's service.d runner, waits for the package
# manager, is a no-op while the module is active.

PKG="com.android.launcher3"
MODULE_ID="trebuchetmagisk"
MODDIR="/data/adb/modules/$MODULE_ID"
WATCHDOG="/data/adb/service.d/trebufork-cleanup-watchdog.sh"

# Guard against double execution (a stale copy from a previous install could
# coexist with the module's own copy). Uses noclobber (O_EXCL) instead of flock:
# toybox flock rejects arbitrary fd redirections on some devices.
LOCK="$WATCHDOG.lock"
if ! (set -o noclobber; echo $$ > "$LOCK") 2>/dev/null; then
    # A leftover lock is only honored while its owner process is alive; a lock
    # from a crashed/finished previous run (or from before a reboot) is stale
    # and gets taken over.
    lock_pid="$(cat "$LOCK" 2>/dev/null)"
    if [ -n "$lock_pid" ] && kill -0 "$lock_pid" 2>/dev/null; then
        exit 0
    fi
fi
# Stale or free lock: take it over.
echo $$ > "$LOCK" 2>/dev/null

# Wait for the package manager (same pattern as the module's service.sh).
i=0
until pm path "$PKG" >/dev/null 2>&1 || [ "$i" -ge 60 ]; do
    sleep 2
    i=$((i + 1))
done

module_active() {
    # Module dir present, not disabled, and the launcher APK is actually mounted.
    [ -d "$MODDIR" ] || return 1
    [ -f "$MODDIR/remove" ] && return 1
    [ -f "$MODDIR/disable" ] && return 1
    [ -f /system_ext/priv-app/Launcher3QuickStep/Launcher3QuickStep.apk ] || return 1
    return 0
}

if module_active; then
    # Module is alive — nothing to do. Keep the watchdog in place: the user may
    # disable the module at any time and it should still clean up next boot.
    rm -f "$LOCK"
    exit 0
fi

# --- Module is disabled or removed: restore the stock launcher state -------

tlog() {
    /system/bin/log -t TrebuforkCleanup "$1" 2>/dev/null
}

# Drop the /data/app update the module's service.sh registered. On an updated
# system app this removes exactly the /data/app copy and brings back the
# original system APK (or, if no system copy exists, uninstalls the package).
#
# Early-boot subtlety: a single `pm uninstall` right after PMS answers `pm path`
# can fail silently (package monitor still initializing), and the running
# launcher process re-creates its databases immediately afterwards. So: kill
# the launcher first, then retry the uninstall until the /data/app update is
# really gone (pm path no longer resolves into /data/app).
kill_launcher() {
    am force-stop "$PKG" >/dev/null 2>&1
}

update_gone() {
    # Update gone when the package path no longer points into /data/app.
    # (When the update is uninstalled from an updated system app, the remaining
    # system copy resolves to /system_ext/... or the package disappears.
    # NB: pm path prefixes the path with "package:" — strip it first.)
    p="$(pm path "$PKG" 2>/dev/null | head -1)"
    p="${p#package:}"
    case "$p" in
        /data/app/*) return 1 ;;
        *) return 0 ;;
    esac
}

kill_launcher
tries=0
until update_gone || [ "$tries" -ge 10 ]; do
    pm uninstall "$PKG" >/dev/null 2>&1
    sleep 3
    tries=$((tries + 1))
done
if update_gone; then
    tlog "launcher /data/app update removed (after $tries attempt(s))"
else
    tlog "WARNING: /data/app update still present after $tries attempts"
fi

# Wipe only the launcher's caches (code_cache, cache) — the user's DATA
# (databases, shared_prefs: workspace layout, settings) is deliberately KEPT,
# so re-enabling the module later restores the same workspace.
kill_launcher
for d in /data/data /data/user/*; do
    [ -d "$d/$PKG" ] || continue
    rm -rf "$d/$PKG/cache" "$d/$PKG/code_cache" 2>/dev/null
done
tlog "launcher caches wiped (user data kept)"

# Home role cleanup (best effort).
cmd role remove-role-holder --user 0 android.app.role.HOME "$PKG" >/dev/null 2>&1

# The module is gone/disabled: the watchdog has done its job and can retire.
# (If the user only disabled the module and re-enables it later, the module's
# own service.sh will re-register everything on the next active boot.)
rm -f "$WATCHDOG" "$LOCK"

exit 0
