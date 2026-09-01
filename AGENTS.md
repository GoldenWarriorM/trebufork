# AGENTS.md

## Project

This is the Gradle build environment for Trebufork/Launcher3. The main app
module is `:launcher`, and the Launcher3 sources are pulled in from
`launcher-source/`.

## Building in this environment

Build with Java 21 and an Android SDK (its location comes from
`ANDROID_HOME`). The
system Gradle 9.6.1 is incompatible with the project's Android Gradle Plugin;
there is no `gradlew` wrapper in the repo, but the Gradle 8.13 distribution is
usually present in the `~/.gradle/wrapper/dists/` cache.

The build is **fully self-contained**: platform sources and local
Jetpack/Material Maven repositories are vendored in `platform/` (see
`platform/README.md`); no external Lineage tree is required.
`-PlineageRoot=/path/to/tree` is an optional override for live development
against a Lineage checkout.


The Magisk module replaces the system launcher `com.android.launcher3`, so it
needs a **release** APK (the debug build has the `.debug` applicationId suffix,
which prevents the PackageManager from rescanning the package and leaves the
app/settings names stale). `package-magisk.sh` signs the release APK with the
platform key itself.

After a successful build the APK is at:

```text
launcher/build/outputs/apk/release/launcher-release-unsigned.apk
```

After every source fix, always run the full verify-and-install cycle: build the
APK, create the Magisk ZIP and flash it to the connected device.

After a successful build, always create the Magisk ZIP with:

```bash
./package-magisk.sh
```

The archive is at `build/Trebufork-magisk-<version>.zip`.

To install the Magisk module on a connected root device use the skill script:

```bash
./flash-magisk.sh
```

The script automatically picks the newest Magisk ZIP from `build/`, verifies
exactly one adb device and `su` availability, pushes the archive via `adb`,
installs it with `magisk --install-module`, removes the temp file, reboots the
phone, waits for boot to complete and verifies the installed module. For an
explicit archive, a specific device, or install without reboot:

```bash
./flash-magisk.sh build/Trebufork-magisk-v0.4.0.zip --serial DEVICE
./flash-magisk.sh --no-reboot
```

For a quick Java-compilation-only check, use the same command with
`:launcher:compileReleaseJavaWithJavac` instead of `:launcher:assembleRelease`.

`versionCode` in `launcher/build.gradle` must stay `36` (matching the stock
Android 16 launcher), otherwise `pm install` rejects the replacement as a
downgrade.

### Important note about `build-gradle.sh`

`build-gradle.sh` automatically runs `sync-source.sh`, which syncs
`launcher-source/` from an external launcher source tree. Do not use this script after
local source changes unless you intentionally want to overwrite the snapshot.
To build the current checkout, use the direct Gradle command above.

`sync-prebuilts.sh` is only needed when artifacts in `prebuilts/` are missing
or stale; it expects an up-to-date Lineage Soong build. With the vendored
`platform/`, normal builds read neither `out/soong` nor the Lineage tree at
all.

To update the vendored sources (`platform/frameworks/`), use
`./update-platform.sh [BRANCH]` — it does partial clones of the LineageOS
repositories, no tree needed.

## Installing on a connected device

Check the connection:

```bash
adb devices
```

### Connecting to the test phone over Wi-Fi

The default test device is available over Wi-Fi (the adb binary comes from
`$ANDROID_HOME/platform-tools`):

```bash
export PATH=$PATH:$ANDROID_HOME/platform-tools
adb connect 192.168.XXX.XXX:5555     # port 5555 is static (magisk module requred, ask user), IP may change 
```

After flashing the module the phone reboots and the adb connection drops. Wait
for boot and reconnect in a loop:

```bash
adb disconnect 192.168.XXX.XXX:5555
for i in $(seq 1 30); do
    adb connect 192.168.XXX.XXX:5555 >/dev/null 2>&1
    if adb -s 192.168.XXX.XXX:5555 shell getprop sys.boot_completed 2>/dev/null \
            | tr -d '\r' | grep -q 1; then
        echo BOOTED; break
    fi
    sleep 5
done
```

Install a debug APK over the already-installed version keeping data:

```bash
adb install -r -d launcher/build/outputs/apk/debug/launcher-debug.apk
```

Debug application id: `com.android.launcher3.debug`.

## Testing visual bugs: screen recording and logs

Full cycle for content/animation fixes: build → `./package-magisk.sh` →
`./flash-magisk.sh <zip>` → wait for boot (`sys.boot_completed=1`, see above) →
ask the user to reproduce the gesture → analyze the recording and logs.

Visual bugs (frame flicker, disappearing elements) are caught by screen
recording the device + logcat. Create a recording helper (screenrecord writes
the file on completion after `--time-limit`; a separate `logcat -c` call
clears the buffer):

```bash
cat > /tmp/trebufork_record.sh <<'EOF'
#!/bin/bash
ADB=$ANDROID_HOME/platform-tools/adb
DUR="$1"; OUT="$2"
"$ADB" shell rm -f "$OUT"; "$ADB" shell logcat -c
exec "$ADB" shell screenrecord --time-limit "$DUR" "$OUT"
EOF
chmod +x /tmp/trebufork_record.sh
```

Run the recording **detached** (`setsid ... &`) and return control without
blocking the flow — the user performs the gesture meanwhile (10 seconds is
enough):

```bash
adb shell date +%s            # file name tag so runs don't get mixed up
setsid bash /tmp/trebufork_record.sh 10 /data/local/tmp/rec_<tag>.mp4 \
    > /tmp/rec_run.log 2>&1 < /dev/null &
```

Important: do not mix other `adb` commands into the same line before the
spawn — the recorder reliably survives control return only on a clean launch
(a lone `adb shell ... --time-limit`, absolute adb path). Then pull the
recording and dump logs to a file (the log buffer is cleared by a device
reboot, so save it right after the test):

```bash
adb pull /data/local/tmp/rec_<tag>.mp4 /tmp/rec_<tag>.mp4
adb logcat -d > /tmp/test.logcat
```

**Capturing logs on the device (more reliable than setsid on the host).**
A detached `adb logcat > file` on the host often dies together with the parent
command (the file stays 0 bytes). Instead write the log directly on the device
via `nohup logcat -f` — it survives adb disconnects, and after the gestures
the file is pulled with `adb pull`:

```bash
adb shell "logcat -c; rm -f /data/local/tmp/cap.log; \
    nohup logcat -f /data/local/tmp/cap.log >/dev/null 2>&1 &"
# ... user performs the gesture ...
adb shell "kill \$(pidof logcat) 2>/dev/null"
adb pull /data/local/tmp/cap.log /tmp/cap.log
```

Do not rely on uncommitted edits as the "current" build — always build
release, package and flash before asking the user to test.

## Releases (Magisk online-update)

The module updates through Magisk's built-in mechanism: `module.prop` carries
`updateJson` pointing at `magisk/update.json` in the repository
(`raw.githubusercontent.com/.../main/magisk/update.json`). Magisk polls this
JSON periodically (module tab → "Check for updates") and offers the release
ZIP to download when `versionCode` is higher.

Release cycle — one command:

```bash
./release.sh 0.6.0
```

`release.sh` updates the version in `magisk-template/module.prop` (versionCode
= major*1000 + minor*100 + patch) and in `magisk/update.json`, commits, tags
`v0.6.0` and pushes. The `v*` tag push triggers the workflow
`.github/workflows/release.yml`, which on GitHub Actions:

1. builds `:launcher:assembleRelease`,
2. signs the APK with the vendored platform key and packages the module
   (`./package-magisk.sh`) — ZIP `Trebufork-magisk-v0.6.0.zip`,
3. publishes a GitHub Release with the ZIP and an auto-generated changelog,
4. updates `magisk/update.json` on `main` for the new tag.

Important: the repository (raw JSON and release assets) must be **public**,
otherwise Magisk cannot authenticate and the update check fails. The installed
module updates like any other: Magisk → module → "Update".

## Constraints

- Do not run `sync-source.sh` unless necessary: it can delete local files that
  are missing from the external snapshot.
- Do not commit generated files from `build/` and `launcher/build/`.
- Gradle/Kotlin warnings and the `Already watching path` message may appear on
  a successful build; rely on the final exit code and `BUILD SUCCESSFUL`.
