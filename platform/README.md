# platform/

Vendored subset of the LineageOS platform tree needed to build the launcher
standalone (no `/home/gwm/android/lineage` checkout required).

## Contents

- `frameworks/` — AOSP/LineageOS sources that the `launcher-*` Gradle modules
  compile together with the launcher code (dexed into the APK):
  - `frameworks/libs/systemui/{animationlib, contextualeducationlib, iconloaderlib,
    mechanics, msdllib, tracinglib, displaylib, viewcapturelib}` — SystemUI
    shared libraries
  - `frameworks/base/packages/SystemUI/{animation, plugin_core, plugin, shared,
    utils, unfold, log, common, pods}` — SystemUI sources used by the launcher
  - `frameworks/base/libs/WindowManager/Shell` — WMShell shared sources + AIDL
  - `frameworks/base/packages/SettingsLib/SettingsTheme` — settings theme sources

Refresh these sources with `./update-platform.sh [BRANCH]` (sparse clones of
`LineageOS/android_frameworks_base` and
`LineageOS/android_frameworks_libs_systemui`, no full tree needed).
- `prebuilts/sdk/current/androidx/m2repository` and
  `prebuilts/sdk/current/extras/material-design-x` — local Maven repositories
  with the same Jetpack/Material artifacts that Lineage's Soong build uses.
  The Gradle build resolves from these FIRST so the resource/theme graph
  matches the platform exactly (mixing release Maven versions would change
  the PreferenceTheme resource graph and runtime styles).

Only the exact source/res directories referenced by `build.gradle` files are
included; everything else (Soong output, `.repo`, device/vendor trees) is NOT
needed and lives outside this repo.

## Origin & license

Sources come from the LineageOS android-16 (lineage-23.2 "diting") tree and
are licensed under the Apache License 2.0, as all AOSP code. See
https://source.android.com/docs/setup/start/licenses and
https://lineageos.org/ for details. No modifications were made to the
vendored sources; they are updated by re-running the copy when the platform
version changes.

Last synced by update-platform.sh: branch lineage-23.2
- frameworks/base @ 094652839e07653d9c20e5cd0d68f46a8747b372
- frameworks/libs/systemui @ f33cffb935cf4f1a0a27bb3178aa1cf4621efc70
