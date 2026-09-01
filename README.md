# Trebufork

A fork of the [LineageOS Trebuchet](https://github.com/LineageOS/android_packages_apps_Launcher3)
Quickstep launcher (`com.android.launcher3`) with a **scrollable single-column
home screen**, installed systemlessly via [Magisk](https://topjohnwu.github.io/Magisk/).

Trebufork fixes some bugs from aosp launcher and adds custom scrollable design inspired by niagara launcher.
---

## Features

### Scrollable home

- **One scrollable column** instead of separate paged pages and an app drawer:
  the desktop (icons, widgets, hotseat) and the app list are a single
  continuously scrolling surface.
- **Alphabet sidebar** on the right edge — jump straight to any letter of the
  app list. Position, size and edge insets are tunable (see below).
- **Search bar on the home screen** (optional) — search apps right from the
  desktop, not just from the list.
- **Hide app names** — icon-only scrollable home for a cleaner look.
- **Folders as scrolling lists** — folder contents render as a one-column list
  instead of the classic 2×3 grid (toggle per folder).

### Animations

- **Wallpaper zoom** — a custom in-launcher wallpaper renderer (a workaround
  for WindowManager bugs with live wallpaper surfaces) animates a smooth
  zoom-in when an app opens and zoom-out when it closes.
- **Close-to-icon flight** — when an app closes, its window shrinks into the
  app icon. The flying window tracks the icon even while the list scrolls.
- **Animation speed setting** — a master launcher animation speed from 0.5× to
  2× (Settings → Animation speed). Automatically disabled when the system
  animation scale is overridden in Developer options.

### Recents

- **Always-fresh thumbnails** — recents cards show the current task snapshot;
  stale previews from previous recents sessions are replaced within a frame or
  two, without the white flash of the stock reload path.


### Hidden tuning (via adb)

A few scrollable-home parameters are not exposed in Settings and are tuned by
editing `com.android.launcher3.prefs.xml` under the app's data directory:

- `pref_sidebar_position`, `pref_sidebar_height`, `pref_sidebar_edge_inset`,
  `pref_sidebar_margin_end`, `pref_sidebar_left_inset` (left-hand alphabet
  swipe strip), `pref_sidebar_touch_width` — alphabet sidebar geometry.
- `pref_scrollable_top_inset` — top empty strip of the scrollable home
  (percent of screen height, default 20).

Example:

```bash
adb shell "sed -i 's|<float name=\"pref_scrollable_top_inset\" value=\"20.0\"|<float name=\"pref_scrollable_top_inset\" value=\"30.0\"|' \
  /data/data/com.android.launcher3/shared_prefs/com.android.launcher3.prefs.xml"
```

---

## Installation

Trebufork replaces the **stock system launcher**, so it ships as a Magisk
module and is installed systemlessly.

**Requirements**

- Rooted device with Magisk **25200+**
- Android 16 QPR 2 / LineageOS 23.2 (API 36) — the module installs into
  `/system_ext` and the app's `versionCode` stays `36` to match the stock
  Android 16 launcher, so the package manager always accepts it as an upgrade,
  never a downgrade.

**Install**

1. Download `Trebufork-magisk-<version>.zip` from the
   [releases page](https://github.com/GoldenWarriorM/trebufork/releases).
2. Open Magisk → Modules → Install from storage → pick the ZIP.
3. Reboot. Set Trebufork as the default home app if prompted.

**Update**

Updates are delivered through Magisk's built-in update check: Magisk →
Modules → Trebufork → *Check for updates* (or just wait — Magisk polls
periodically). When a new version is available, install it from the same
screen.

**Uninstall**

Remove the module in Magisk → Modules → Trebufork → trash icon. The stock
launcher is restored on reboot.

---

## Building from source

The build is **fully self-contained**: the platform sources and Jetpack/Material
Maven artifacts needed by the launcher are vendored in `platform/`, so no
LineageOS source tree is required.

**Requirements**

- JDK 21 (`JAVA_HOME`)
- Android SDK with platform 36 and build-tools 36.x (`ANDROID_HOME`)
- Gradle 8.13 (the repo intentionally ships no wrapper; use the 8.13
  distribution, newer Gradle is incompatible with the pinned AGP)

**Build**

```bash
export ANDROID_HOME=/path/to/android-sdk
export JAVA_HOME=/path/to/jdk-21

gradle :launcher:assembleRelease
./package-magisk.sh        # signs the APK with the vendored platform key and
                           # produces build/Trebufork-magisk-<version>.zip
```

For development on a connected device (Wi-Fi adb, flashing, log capture),
see `AGENTS.md`.

---

## Releases

Version bumps and releases are one command:

```bash
./release.sh 0.6.0
```

`release.sh` bumps `magisk-template/module.prop` and `magisk/update.json`,
commits, tags (`v0.6.0`) and pushes. The tag push triggers a GitHub Actions
workflow (`.github/workflows/release.yml`) that builds the release APK, signs
it with the vendored platform key, packages the module and publishes a GitHub
Release — which Magisk then offers as an update. Note: for Magisk to check for
updates, the repository must be **public**.

---

## Credits

Based on LineageOS Trebuchet / AOSP Launcher3 (Apache License 2.0).
