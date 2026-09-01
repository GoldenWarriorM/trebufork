#!/usr/bin/env bash
# Vendors the platform-level Soong artifacts that a standalone Gradle build
# cannot rebuild itself (framework hidden-API jar, SDK module stubs, Lineage
# SDK jar and the framework/SystemUI resource packages used as AAPT2 -I).
#
# Run this once after a Soong build of the Lineage tree, or whenever the
# platform version changes. Afterwards the Gradle build only needs the
# sources in launcher-source/ and the lineage tree sources referenced by the
# launcher-* Gradle modules; it no longer reads out/soong/.intermediates.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LINEAGE_ROOT="${LINEAGE_ROOT:-$HOME/android/lineage}"
ANDROID_HOME="${ANDROID_HOME:-$HOME/android-sdk}"
OUT="$LINEAGE_ROOT/out/soong/.intermediates"
DEST="$ROOT/prebuilts"
mkdir -p "$DEST"

# Merge the full framework jar with the SDK stubs that the launcher compiles
# against (Android.bp sets platform_apis: true, i.e. bootclasspath + the
# system SDK): framework-minus-apex carries the hidden APIs but lacks a few
# classes hosted in APEXes (e.g. android.app.role.RoleManager) that only exist in
# the public SDK stubs, and the system SDK adds @SystemApi-only classes like
# android.provider.DeviceConfig. Extracting sdk_system_current first, then
# android.jar, then framework.jar (each with overwrite) keeps framework's
# versions of every class present in multiple jars and adds the missing ones.
# The merged jar replaces android.jar on the compile classpath, so javac/Kotlin
# see a single android.* hierarchy (hidden members included, no duplicate
# classes across jars).
SDK_ANDROID_JAR="${SDK_ANDROID_JAR:-$ANDROID_HOME/platforms/android-36/android.jar}"
SDK_SYSTEM_CURRENT="$OUT/prebuilts/sdk/sdk_system_current_android/android_common/combined/sdk_system_current_android.jar"
CORE_OJ="$OUT/libcore/core-oj/android_common_apex31/javac/core-oj.jar"
MERGE_TMP="$(mktemp -d)"
trap 'rm -rf "$MERGE_TMP"' EXIT
# core-oj comes AFTER the SDK stubs: the real core library classes
# (java.lang.Thread with the hidden get/setUncaughtExceptionPreHandler, etc.)
# that the SDK stubs omit. The launcher's Soong build compiled against the
# full bootclasspath including these; without them hidden core-library APIs
# are invisible to javac/Kotlin. framework-minus-apex (last) wins for the
# framework's own classes.
unzip -q -o "$SDK_SYSTEM_CURRENT" -d "$MERGE_TMP/merged"
unzip -q -o "$SDK_ANDROID_JAR" -d "$MERGE_TMP/merged"
unzip -q -o "$CORE_OJ" -d "$MERGE_TMP/merged"
unzip -q -o "$OUT/frameworks/base/framework-minus-apex/android_common/combined/framework.jar" \
    -d "$MERGE_TMP/merged"
( cd "$MERGE_TMP/merged" && zip -q -r "$DEST/framework.jar" . )
rm -rf "$MERGE_TMP"; trap - EXIT
cp -v "$OUT/frameworks/base/api/android_module_lib_stubs_current/android_common/combined/android_module_lib_stubs_current.jar" \
    "$DEST/android_module_lib_stubs_current.jar"
cp -v "$OUT/frameworks/base/api/android-non-updatable.stubs.test.from-text/android_common/android-non-updatable.stubs.test.from-text/android-non-updatable.stubs.test.from-text.jar" \
    "$DEST/android-non-updatable.stubs.test.from-text.jar"
cp -v "$OUT/packages/modules/StatsD/framework/framework-statsd.stubs.module_lib/android_common/turbine-combined/framework-statsd.stubs.module_lib.jar" \
    "$DEST/framework-statsd.stubs.module_lib.jar"

# Soong library jars the launcher and the migrated launcher-* modules compile
# against (tracinglib, mechanics, msdl, contextualeducation, iconloader,
# plugin-core, animation libs, SystemUI shared/statsd, WMShell shared,
# displaylib, unfold, settings theme, aconfig flags and the combined
# Launcher3QuickStepLib). The path list lives in the build.gradle files
# (launcher/build.gradle + launcher-*/build.gradle), so parse all of them to
# keep a single source of truth.
SOONG_JARS=$(grep -hoE "'out/soong/[^']+\.jar'" "$ROOT"/launcher*/build.gradle | tr -d "'" | sort -u)
for rel in $SOONG_JARS; do
    src="$LINEAGE_ROOT/$rel"
    [ -f "$src" ] || { echo "WARN: missing $src" >&2; continue; }
    mkdir -p "$DEST/soong/$(dirname "$rel")"
    cp -v "$src" "$DEST/soong/$rel"
done
cp -v "$OUT/lineage-sdk/org.lineageos.platform/android_common/javac/org.lineageos.platform.jar" \
    "$DEST/org.lineageos.platform.jar"
cp -v "$OUT/frameworks/base/core/res/framework-res/android_common/framework-res.apk" \
    "$DEST/framework-res.apk"
cp -v "$OUT/frameworks/base/packages/SystemUI/SystemUI/android_common/package-res.apk" \
    "$DEST/SystemUI-package-res.apk"

# Refresh the aconfig flag sources committed in launcher-flags /
# launcher-flags-framework from Soong's generated srcjars (the exact output of
# the aconfig tool), so the module sources stay in sync with the tree's aconfig
# declarations. At Gradle build time only the committed sources are used.
FLAG_SRCJARS="
frameworks/base/libs/WindowManager/Shell/aconfig/com_android_wm_shell_flags_lib:launcher-flags
frameworks/libs/systemui/aconfig/com_android_systemui_shared_flags_lib:launcher-flags
frameworks/base/packages/SystemUI/aconfig/com_android_systemui_flags_lib:launcher-flags
packages/apps/Launcher3/aconfig/com_android_launcher3_flags_lib:launcher-flags
frameworks/base/android.companion.virtualdevice.flags-aconfig-java:launcher-flags-framework
frameworks/base/android.hardware.devicestate.feature.flags-aconfig-java:launcher-flags-framework
frameworks/base/com.android.window.flags.window-aconfig-java:launcher-flags-framework
frameworks/base/android.os.flags-aconfig-java:launcher-flags-framework
frameworks/base/android.companion.flags-aconfig-java:launcher-flags-framework
frameworks/base/android.multiuser.flags-aconfig-java:launcher-flags-framework
frameworks/base/android.appwidget.flags-aconfig-java:launcher-flags-framework
frameworks/base/aconfig_settingstheme_exported_flags_java_lib:launcher-flags-framework
"
declare -A FLAG_SRC_CLEANED
for entry in $FLAG_SRCJARS; do
    src="${entry%%:*}"; dst="${entry##*:}"
    jar=$(find "$OUT/$src/android_common/gen" -maxdepth 1 -name '*.srcjar' 2>/dev/null | head -1)
    if [ -z "$jar" ]; then
        echo "WARN: no generated srcjar for $src (skipping flag source refresh)" >&2
        continue
    fi
    # Wipe the module source dir only once per module (each package comes from
    # its own srcjar; wiping per entry would drop the previously extracted ones).
    if [ -z "${FLAG_SRC_CLEANED[$dst]:-}" ]; then
        rm -rf "$ROOT/$dst/src/main/java"
        mkdir -p "$ROOT/$dst/src/main/java"
        FLAG_SRC_CLEANED[$dst]=1
    fi
    unzip -q -o "$jar" -d "$ROOT/$dst/src/main/java"
done

echo "Platform prebuilts vendored into $DEST"
