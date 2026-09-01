#!/system/bin/sh
#
# Trebuchet Quickstep — installer
# Runs in Magisk's installer mode (MAGISK=true): functional-topolog bridges
# are not needed; we just prepare the tree and drop a conflicting user copy.
#
SKIPUNZIP=0

ui_print "******************************"
ui_print " Trebuchet Launcher Quickstep"
ui_print "******************************"

# Remove a conflicting USER-installed copy of com.android.launcher3 if present.
# A user app must not shadow the systemless priv-app with the same package name.
# Only real user copies (`pm list packages -3`) are touched: uninstalling the
# system/systemless launcher would drop its /data/app update and wipe the
# wallpaper permission grants on every module flash.
if pm list packages -3 2>/dev/null | grep -q '^package:com.android.launcher3$'; then
    ui_print "- Removing conflicting user copy of com.android.launcher3"
    pm uninstall com.android.launcher3 >/dev/null 2>&1
fi

ui_print "- Files will be mounted into /system_ext"
ui_print "- Done"

# Keep the boot image optimization: no-op marker to honor SKIPUNZIP
true