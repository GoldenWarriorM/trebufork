#!/system/bin/sh
#
# Trebufork — uninstall hook
#
# Magisk runs this script (as root) when the module is REMOVED, right after the
# module directory is marked for deletion and before/around the reboot. The
# systemless mount is gone at the next boot, but the state service.sh created
# survives module removal — so we undo it here:
#
#   1. The "updated system app" registration: service.sh runs
#      `pm install -r /system_ext/priv-app/.../Launcher3QuickStep.apk`, which
#      copies the module APK into /data/app as an update to the (now vanished
#      again) system package. Without cleanup the device is left with an
#      orphaned launcher update: stale name/version metadata and no way to
#      return to the stock build. `pm uninstall com.android.launcher3` on an
#      updated system app removes exactly that /data/app update and restores
#      the original system APK.
#   2. The launcher's own caches and databases (icon cache app_icons.db,
#      widget previews, grid dbs, preferences) are wiped so the next launcher
#      (e.g. the stock one after a ROM update) starts from a clean state.
#   3. The disable-watchdog dropped into /data/adb/service.d (see service.sh)
#      is removed — it is no longer needed once the module is gone.
#
# Everything is best-effort: uninstall must never fail loudly.

PKG="com.android.launcher3"

ui_print "- Restoring stock launcher state..."

tlog() {
    /system/bin/log -t TrebuforkCleanup "$1" 2>/dev/null
}

# Same logic as trebufork-cleanup-watchdog.sh: a single early `pm uninstall`
# can fail silently, and a running launcher re-creates its databases. Kill
# first, retry until the /data/app update is really gone.
am force-stop "$PKG" >/dev/null 2>&1
update_gone() {
    # NB: pm path prefixes the path with "package:" — strip it first.
    p="$(pm path "$PKG" 2>/dev/null | head -1)"
    p="${p#package:}"
    case "$p" in
        /data/app/*) return 1 ;;
        *) return 0 ;;
    esac
}
tries=0
until update_gone || [ "$tries" -ge 10 ]; do
    pm uninstall "$PKG" >/dev/null 2>&1
    sleep 2
    tries=$((tries + 1))
done
if update_gone; then
    ui_print "- Launcher update removed (after $tries attempt(s))"
    tlog "launcher /data/app update removed (after $tries attempt(s))"
else
    ui_print "- WARNING: launcher update may still be present"
    tlog "WARNING: /data/app update still present after $tries attempts"
fi

# 2. Clear only the launcher's caches (code_cache, cache) — the user's DATA
#    (databases, shared_prefs: workspace layout, settings) is deliberately
#    KEPT, so a later reinstall of the module restores the same workspace.
#    All users/profiles, not just user 0.
am force-stop "$PKG" >/dev/null 2>&1
for d in /data/data /data/user/*; do
    [ -d "$d/$PKG" ] || continue
    rm -rf "$d/$PKG/cache" "$d/$PKG/code_cache" 2>/dev/null
done
tlog "launcher caches wiped (user data kept)"

# 3. Remove the disable-watchdog installed by service.sh.
rm -f /data/adb/service.d/trebufork-cleanup-watchdog.sh 2>/dev/null

# 4. Drop the HOME role if it still points at the launcher (best effort; the
#    system falls back to another HOME automatically once the package is gone).
cmd role remove-role-holder --user 0 android.app.role.HOME "$PKG" >/dev/null 2>&1

ui_print "- Done"
exit 0
