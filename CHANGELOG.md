# Changelog

The complete commit history of the original development repository is
preserved here as text, because this public repository is published as a
single squashed commit. Newest first; full message bodies included.

### 09eb87bcd04c (2026-09-01) — Scrub personal identifiers and translate docs to English

- README.md and AGENTS.md rewritten in English (migration docs removed)
- Replace hardcoded home-directory paths with $HOME in build scripts
- Mask device IP, replace GitHub owner with <owner> placeholder
- Drop stale Russian comment in update-platform.sh

Generated with Codebuff 🤖
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### 7b1b2539ee3f (2026-09-01) — Bump module version to v0.5.1 and add Magisk online-update support

module.prop now carries an updateJson pointing at magisk/update.json on main,
so Magisk's built-in update check (module tab -> Check for updates) can offer
new versions over GitHub. A release workflow (.github/workflows/release.yml)
runs on v* tag pushes: it builds the release APK, signs it with the vendored
platform key, packages the module, publishes a GitHub Release with the ZIP and
refreshes magisk/update.json on main. release.sh is a one-command helper that
bumps version/versionCode (major*1000 + minor*100 + patch), commits, tags and
pushes, triggering the workflow.

Also bumps the module from 0.5.0 to 0.5.1 and documents the release flow in
AGENTS.md.

Generated with Codebuff 🤖
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### f532234a3b9b (2026-09-01) — Never fall back to a stale task thumbnail when the cache has no valid entry

TasksRepository.removeTasks keeps task data across recents visibility changes,
so task.thumbnail may be a stale copy from a previous recents session. The
cache entry for the same task can be missing by the time getThumbnail runs:
evicted (phone cache holds only recentsThumbnailCacheSize=3 tasks and
updateIfAlreadyInCache drops pushes for evicted tasks) or invalidated when the
task key's lastActiveTime changed after the app was reopened. In both cases the
fresh snapshot the system captured at minimize time is only reachable through a
direct query, so returning task.thumbnail pinned a stale preview forever.

getThumbnail now goes cache -> system query only. The model still renders the
previous thumbnail instantly at bind time, so the async system result replaces
it within a frame or two without the white flash.

Generated with Codebuff 🤖
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### 61ebfbd94c59 (2026-08-31) — Prefer the fresh task-snapshot cache over a stale task thumbnail in recents

getThumbnail() used to return task.thumbnail first. Since the task data is
now kept across recents visibility changes (918eab6a), that stale copy could
be returned forever, so a just-minimized app showed an outdated preview and
the fresh snapshot taken at minimize time was never queried. Check the LRU
cache first it is updated on every system-pushed snapshot (including the
one captured on minimize), so recents cards stay current while still
rendering instantly on re-open.

Generated with Codebuff 🤖
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### a2d9dff8c3b9 (2026-08-30) — Fix one-frame icon blink when opening an app from the paged workspace

When the floating icon finished loading (often right at animation
composition) the original icon was hidden synchronously, but the floating
view is not laid out/drawn until the next frame - leaving a one-frame gap
where the icon vanished on the paged workspace. Defer hiding the real icon
until the floating view is actually drawn over it.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### 828fc519cd23 (2026-08-30) — Play wallpaper zoom on app open alongside the close-to-home reverse

The close-to-home reveal animates depth (wallpaper zoom-out), but the app
open path skipped it entirely when allAppsBlur is set, so the wallpaper just
sat at home scale while the app opened. Still animate stateDepth on open for
the reverse zoom, suppressing only the expensive cross-window blur during the
launch.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### 8a1cd8c86ae6 (2026-08-30) — Add top scrim gradient over scrollable home for status bar contrast

The scrollable home (desktop + apps list) draws edge-to-edge, so its content
blends with the status bar. Add a non-touch darkening gradient pinned to the
top of the screen, visible only while the scrollable home setting is enabled.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### dca5924934d5 (2026-08-30) — Collapse close-to-icon window to zero when the target icon is lost

During a scrollable-home close animation the tracked icon can scroll out of
the visible list (or jump away via the alphabet), making its final position
unreachable. Instead of freezing the flight at an on-screen point and letting
the icon pop, re-target the spring to a near-zero rect at the current center
so the window shrinks and fades out, and immediately restore the real icon's
visibility so no empty slot remains when the user scrolls back.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### 6d020a4cf941 (2026-08-30) — Fix close-to-icon flying icon drift during scrollable home scroll

The flying close icon landed a few pixels left / jumped left-right around
finger release when the scrollable home list scrolled mid-animation.
Root cause: the scroll re-target re-laid out the FloatingIconView container
(layout(left = leftMargin)) while the spring translation assumed the layout
position matched the margin; with a late-arriving icon bounds the margin and
actual layout diverged, so the translation math snapped the icon to the wrong
position (screen edge / stale layout) for a few frames.

Fixes:
- FloatingIconView.updatePosition now only re-targets the spring
  (mPositionOut) and no longer mutates margins or re-lays out the container
  mid-flight; the per-frame translation already places the icon at the live
  spring rect.
- ClipIconView translates relative to the container's actual getLeft()/getTop()
  instead of the margin, so the icon always tracks the spring target.
- RectFSpringAnim.start never bakes an empty target rect as the end position
  (window would fly to the top-left corner); it holds until a re-target
  anchors the real icon.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### e2f212441e8b (2026-08-30) — Kill flying close-to-icon only on launcher-registered nav swipe

Killing the close-to-icon spring on any touch-down interrupted the closing
animation on plain list scrolls, and gating it by EDGE_NAV_BAR on the
input-consumer event never fired (edge flags do not reach that path), so a
pill swipe left the icon flying while recents opened.

Move the kill to NoButtonNavbarToOverviewTouchController.onDragStart, which
the launcher only reaches after canInterceptTouch deems the touch a real
nav gesture (on the view path EDGE_NAV_BAR is reliable; a scrollable-home
list-body touch returns false so the list scrolls). A plain scroll never
starts a drag, so the close-to-icon animation keeps tracking the icon; a
pill swipe removes it before recents opens. killFlyingIcon is now public.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### 016509cf147b (2026-08-30) — Document on-device logcat capture for gesture testing

Host-side `setsid adb logcat > file` dies with the parent command and
leaves a 0-byte file; writing the log on the device via
`nohup logcat -f` survives adb drops and is pulled afterwards.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### 25da40a75716 (2026-08-30) — Scale recents-to-app reverse launch by launcher animation speed

At non-1x AnimationSpeed the recents-reverse AnimatorSet was never
duration-scaled (stock 336ms) while the concurrent open spring was slowed
via stiffness, and in the deferred/merged case the composed tree mixed
672ms window animators with a 336ms recents-view subtree - the window and
the recents cards then animate at different speeds ("double animation").

Scale the recents-view launcherAnim subtree and the whole composed tree in
composeRecentsLaunchAnimator, and make AnimationSpeed.applyDurationScale
idempotent per AnimatorSet instance (WeakHashMap guard) so
LauncherAnimationRunner.setAnimation does not re-scale the same tree.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### 3624763af8f1 (2026-08-30) — Cancel in-flight recents fade-in before hiding scrollable home

When returning home, setRecentsVisible starts duration'd alpha animators
(rows, search bar, alphabet sidebar). Re-opening overview calls setAlpha(0)
directly without cancelling them, so a lingering animator keeps driving
alpha back to 1 and re-shows all elements over overview on the second exit.
Cancel rows/search ViewPropertyAnimators and the tracked sidebar
ObjectAnimator before forcing alpha 0, and keep a handle on the sidebar
fade for reliable cancellation.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### bade7a5dbfa0 (2026-08-30) — Document Wi-Fi adb and screen-record/logcat test workflow in AGENTS.md

Add the test-device Wi-Fi adb connection (192.168.XXX.XXX:5555, static port,
IP may change) plus reconnection/boot-wait loop after a module flash reboots
the phone, and a dedicated section on capturing screen-record + logcat when
debugging visual bugs (enter/exit recents flashes and disappearing elements).

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### 00e8ddcd9790 (2026-08-30) — Fix recents-open overlap and hide scrollable home before overview

Opening recents on scrollable home played several broken transitions that
showed as a one-frame blank and overlapping animations:

- setRecentsVisible hid the home rows/search bar with a 250ms fade during the
  opening gesture, so the content visibly disappeared while recents opened. It
  now hides instantly (alpha 0) the moment overview starts — already gone
  before the overview appears — and fades back on return. The alphabet sidebar
  is faded too (alpha-only ObjectAnimator in fadeSidebarRecentsAlpha, which
  never touches translationX and never cancels animateSidebar, so the
  alphabet's right inset is preserved).

- A previous gesture's close-to-icon spring can still be flying its window to
  the home icon when the next gesture opens recents (scrollable home keeps the
  spring alive to track the icon). Track the spring (sFlyingIconSpring) and
  cancel it on touch-down (killFlyingIcon) so the fly-to-icon never overlaps
  the recents-open; a RECENTS end-target safety net covers paths that miss
  touch-down. registerFlyingIconSpring clears the static when the spring ends
  or is cancelled to avoid stale state.

- onRecentsAnimationStart re-applies the frozen real-surface transform for BOTH
  the true handoff (mActive) and the deferred-close path (mDeferredClose),
  closing the single wallpaper-only frame when opening recents; the fresh-leash
  hide stays limited to the true handoff so the deferred close never leaves the
  recents leash invisible.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### be8f742f5648 (2026-08-29) — Hide fresh recents leash during the deferred reverse close

When the user swipes to close while the same task's open animation is
still running, the merge is deferred until the open finishes and then
delegated as a normal close. The delegation makes WM reparent the app
under a brand-new recents leash created at identity (full screen), which
can present one full-screen identity frame until the close spring takes
over. The existing hideHandoffRecentsLeashNow() only fired while the full
handoff was armed (mActive), which the deferred path deliberately leaves
false so the close spring starts from the gesture position.

Add LaunchHandoff.mDeferredClose, set in the deferred merge branch and
cleared once a close spring runs, when the deferred close is cancelled,
on a fresh launch superseding it, and on the recents-animation reset, so
onRecentsAnimationStart hides the fresh leash in the deferred path too.

Generated with Codebuff 🤖
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### 918eab6af0db (2026-08-29) — Keep recents task data (thumbnail/icon/title) across visibility changes

When a recents re-opens right after closing an app, removeTasks() used to
clear every task's thumbnail/icon/title, forcing an ~40ms async snapshot
re-fetch during which every card renders BackgroundOnly - the flat app
background color (near-white for light-themed apps). That was visible as
a 1-2 frame white flash over the recents cards.

Keeping the data fixes two things at once:
- cards render their cached snapshot instantly (no white flash), and
- app names keep showing, because TaskIconCache.getIcon() short-circuits
  when task.icon is already set and returns the task's current (previously
  nulled) title instead of loading the real one.

Cost is a few MB for ~17 low-res snapshots + icons + strings per task,
and the map is rebuilt from the system list on every force refresh.

Generated with Codebuff 🤖
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### c08e4afe81d0 (2026-08-29) — Place the closing floating icon below the accent scrim

The recents overview dims the home with the accent-colored ScrimView, which
sits inside the DragLayer right before the overview panel. Insert the closing
floating icon before the scrim child (above the workspace, below the accent
dim and below the overview) so the icon gets dimmed together with the home
instead of staying bright on top of the scrim.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### 3a256e5fcbed (2026-08-29) — Render the closing floating icon below the recents overview

The overview panel (LauncherRecentsView) lives inside the DragLayer, while the
close-to-home floating icon was added to the launcher root above everything,
so it drew on top of recents when overview opened mid-animation. Add closing
icons to the DragLayer right before the overview child: above the
workspace/scrim but under the overview.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### 8b2aa6454dea (2026-08-29) — Add launcher animation speed setting (0.5x-2x), disabled on dev-scale override

New "Animation speed" ListPreference in launcher settings, values
0.5/0.75/1/1.25/1.5/2 (default 1x), backed by LauncherPrefs.ANIMATION_SPEED.
A central AnimationSpeed helper scales the main launcher animations: app
open/close AnimatorSets (proportional per-child duration scaling in
LauncherAnimationRunner) and the spring-based recents/close-to-icon/taskbar
motion (stiffness scaled by speed^2 in RectFSpringAnim).

The control auto-disables (and its value is ignored) when any Developer-options
animation scale is changed away from 1x - the system override takes precedence -
tracked via a ContentObserver on the three Settings.Global animation-scale keys.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### 6b39bcb2fb21 (2026-08-29) — Detach sync/packaging from the old trebuchet-magisk project

sync-source.sh no longer rsyncs launcher-source/ from the stale
trebuchet-magisk fork (which would delete newer features); it is a no-op by
default and hard-refuses the legacy SOURCE path. The Magisk module template is
vendored into magisk-template/ so package-magisk.sh no longer depends on
$HOME/Documents/trebuchet-magisk/module.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### 2bee531f55f5 (2026-08-29) — Defer reverse close until the open animation finishes (no skipped/snapped close)

A swipe to close an app while its launch animation is still running used to
cancel the open transition, letting WM compose a single full-screen identity
frame on the app's real surface (the flash). Falling back to dropping the swipe
entirely made the close animation be skipped instead.

Buffer the close gesture properly: hold the merged CLOSE transition and
delegate it only once the open animation has fully completed, at which point
the window is full-screen and the recents close spring runs as a normal
close-to-home with no identity-frame flash. LaunchHandoff diagnostics are
moved to a timestamped HandoffTrace log so the whole merge->spring window can
be correlated with SurfaceFlinger-presented frames.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### fb9258563ab9 (2026-08-27) — Fix full-screen flash during reverse close of in-progress app launch

When the user swipes to close an app while its opening animation is still
running, the window briefly flashed to full screen: the open transition's
finish transaction reset the real task surface to identity, and the fresh
recents leash started at full screen until the close spring's first frame.

Freeze the captured open-window state (crop/matrix/alpha) onto the app's
real task surface at the close merge, re-apply it after the open
transition finishes and after the recents reparent, and hand the state
over onto the leash the close spring drives (composed model x frozen)
before the spring's first frame. Early-cancel swipes instead hide the
surface until the reverse-to-icon spring takes over.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### 793be88c0c9b (2026-08-26) — Swipe-up close during app launch now runs the full close animation from the current window position

When the user starts closing an app while its open animation is still running, the
launch-merge guard previously reversed the open animation, which looked wrong because
the open and close animations are fundamentally different. Instead, the guard now arms
a close handoff (capturing the window's current crop/position/alpha) and delegates the
merge to the default handler, which finishes the open transition without consuming the
close transition so the recents close spring runs normally. The close spring then starts
from the captured window position instead of the gesture model's near-full-screen
position, giving a seamless full close-to-icon animation without snapping to full screen.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### 9db4e0b7069b (2026-08-26) — Fix boot entrance replaying after reinstall restart and stale widget rows

The entrance cascade now skips only when the module's pm install actually
updated the package after the previous cascade (recorded package
lastUpdateTime in the pref), so a plain reboot always replays the
animation instead of being mistaken for a reinstall restart. The skip
path also re-creates desktop widget rows as real host views so widgets
receive their data instead of staying on loading placeholders.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### c8b1fb4cf978 (2026-08-26) — Merge folder/group add actions and hide uninstall on the desktop

The app context menu offered separate "Add to folder" and "Add to
group" entries; combine them into one "Add to folder or group" item
that opens a single picker listing folders, groups with room, and the
new-container options. Also stop offering Uninstall from the scrollable
desktop context menu — it stays available in the app list only.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### 58d2b09ecee5 (2026-08-26) — Polish group Configure dialog: remove bold, add pop-in/pop-out animation

The Configure dialog for desktop groups used bold text everywhere and
appeared/disappeared instantly. Drop the bold styling from all labels
and buttons, and animate the window with a fade+scale pop-in on open
and a quick fade+scale-down on close via a TrebuforkDialogAnimation
window animation style.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### c67145ce38eb (2026-08-26) — Load wallpaper synchronously on attach and retry at boot

The wallpaper bitmap was loaded on a worker thread, so the first frames
after a launcher (re)start showed black, followed by an abrupt pop-in.
Load it synchronously when the view attaches so it renders on the first
frame, and poll (up to ~15s) for the boot case where READ_MEDIA_IMAGES
is granted by the module's service.sh a moment after the activity starts.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### 5a19daca9982 (2026-08-26) — Fix search bar reacting to another app's lingering keyboard

The search bar lifted whenever the IME inset was visible, even without
focus — so returning home while the previous app's keyboard was still
animating closed made it jump. And a tap right after such a return could
be swallowed by the keyboard teardown, leaving the field focused with no
keyboard. Only lift while the field is focused, retry show-soft-input
until the IME is actually up, and suppress the lift while the visible
IME is the previous app's (so the bar lifts once, when the real keyboard
opens).

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### e611b6cfe257 (2026-08-26) — Fix skipped open animation when relaunching an app during the close animation

Scrollable home keeps the launcher touchable while the close-to-icon spring
runs, so a new launch can arrive during the still-active recents transition
and WM Shell merges it in, silently dropping the open animation. Finish the
running recents animation before re-issuing the launch (defer mechanism), and
for the same app absorb the redundant TO_FRONT merge that WM Shell creates
when re-launching a still-visible task, so the open animation plays to its
natural end instead of being cancelled ~30ms in.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### 39eb6d33c51a (2026-08-26) — Render wallpaper in-launcher (WallpaperBackgroundView) and stop syncing stale fork

The old build pipeline rsynced trebuchet-magisk/launcher (stale Aug 12 fork)
over launcher-source before every build, deleting newer features (desktop
store, groups, search). Drop that sync and build release from the current
source. Add a self-contained WallpaperBackgroundView that draws the wallpaper
bitmap directly (bypassing WM's wallpaper surface, which races and leaves a
black screen during transitions), wire zoom/offset into DepthController and
WallpaperOffsetInterpolator, remove windowShowWallpaper, and grant
READ_MEDIA_IMAGES for bitmap access.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### f5a0612951e8 (2026-08-25) — Fix stuck FloatingIconView (animation ghost icons) after rapid open/close

Three-layer defense against floating icons leaking onto the workspace:

1. FloatingIconView.onAnimationCancel: was a no-op — cancelled opening
   animations never removed the view. Now runs same cleanup as onAnimationEnd.

2. RectFSpringAnim watchdog: force-ends spring after 3s idle (re-armed on
   each re-target, so legitimate scroll tracking is not cut short).

3. FloatingIconView view-level watchdog: covers open icons driven by plain
   ValueAnimator (no spring). Removed 3s after last re-target.

4. Bulletproof finish(): removes from actual parent, not assumed parent.

Stress test: 55s aggressive open/close → 311 icons created, 0 leaked
(was 8 leaks per 8s test before fix).

Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### efe3648f558a (2026-08-23) — Add group Configure popup and fix desktop search dismissal

Groups now get a single Configure entry in their context menu opening a
Material-style popup with a rename field, an alignment selector (previews
drawn with line thickness equal to the icon-circle diameter) and a
horizontal draggable strip for reordering members. Surface colors are
resolved from the raw color resources instead of the wallpaper-tinted
theme attrs, so the popup and its name field render opaque dark instead
of translucent white. Desktop search is now ended by the nav-pill swipe /
home gesture, and the swipe-up handler only preserves the close-to-icon
spring for the HOME end target so recents/quick-switch settle cleanly.
Depth blur stays disabled while scrollable home is active.

Generated with Codebuff 🤖
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### 290288bf2f47 (2026-08-20) — Keep close-to-icon animation tracking the icon during scrollable home scroll

The window-close spring in Quickstep was cut short or aimed at a stale
position whenever the scrollable home list scrolled during the swipe-up
animation. Fixes: FloatingIconView now registers a scroll listener on the
ScrollableAppsView to re-target the spring to the live icon position,
getWindowTargetRect returns the shared live RectF instead of a snapshot,
the ScalingWorkspaceRevealAnim frame listener no longer overwrites the
target in scrollable mode, and scroll touches no longer fast-finish or
invalidate the running spring.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### 287f516dbfd3 (2026-08-19) — Reapply depth/blur when content-behind-launcher flag changes

setHasContentBehindLauncher only stored the flag, so the launcher surface
opacity could go stale when leaving recents and the wallpaper stayed hidden
behind a black background until an unrelated event re-ran the depth
controller. Reapply immediately on change to keep the surface translucent
on the home screen.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### 0c78a1272bf4 (2026-08-19) — Bump Magisk module to v0.4.0 and rename output ZIP

- Output archive is now Trebufork-magisk-v0.4.0.zip instead of
  TrebuchetQuickStep-gradle-*.zip; update the packager, flasher and docs
  to match.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### 433747c6fdf2 (2026-08-19) — Delete empty inline groups instead of leaving phantom rows

- removeFromGroup now removes the group itself once its last member is
  taken out, so an emptied group no longer lingers as an empty row.
- Drop empty groups on load and persist the cleanup, clearing phantoms
  left behind by previous builds.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### b39c3c3d1cb8 (2026-08-19) — Hide scrollable home in recents and reset search session on exit

- Fade out the scrollable-home rows and search bar while recents/overview
  is open so no desktop elements show through behind the tasks (the search
  bar sits above the scrim and previously stayed crisp).
- Fully end the search session when leaving search: drop focus (which
  restores the alphabet sidebar), reset the bar's keyboard lift, and reuse
  this path for the clear button and back key.
- Leave the sidebar out of the recents fade so its own search/reorder
  alpha+translationX animation is not cancelled, preserving the alphabet's
  right inset.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### 65f9a64e8288 (2026-08-19) — Add inline app groups, container-removal prompts and desktop search

- Inline app groups use the wallpaper/icon-shape (BubbleTextView cells),
  cap members by the actual row width so the last icon is never clipped,
  and the app-close animation lands on the exact member icon.
- Removing a folder or group now asks whether to keep its members on the
  home screen or remove them along with the container.
- Closing a folder while renaming dismisses the keyboard.
- New "Search bar on home screen" setting (off by default) that keeps the
  app search field visible on the scrollable desktop.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### d17061f9826b (2026-08-19) — Animate search bar hide during alphabet drag and narrow its resting width

Hide the search bar with a slide-down while dragging the alphabet, but
instant-hide it on the desktop-to-apps switch where it was never shown.
Also widen the resting side margins from 32dp to 64dp for a narrower bar.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### 824357c564e3 (2026-08-19) — Bump alphabet sidebar letter size to 14dp

Larger letters pack tighter within the same block height so the index stays
readable without changing its overall footprint.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### f5d76be58d9f (2026-08-19) — Add a light drop shadow to scrollable-home text

Apply a subtle text shadow to app and folder labels, section headers, the
no-results message and the alphabet sidebar so text stays readable over
wallpaper.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### b2d0c018436a (2026-08-19) — Add App info to folder member menu and round list-item highlight

Show the stock App info shortcut at the top of the folder member menu for
apps, and give the list-mode member row the same rounded ripple background
as desktop rows.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### 320497e1f199 (2026-08-19) — Remove reorder drag handles and match folder reorder to desktop

Drop the 3-dot drag-handle indicator from desktop rows and folder members;
reordering is now started by long-pressing a row/member in reorder mode, and
the folder Done button text picks a readable color for the accent pill.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### feffbd82faff (2026-08-19) — Add grid/list toggle, inline rename and keyboard-aware folder popup

Add a grid/list layout toggle inside the folder popup, an inline rename
via a pencil button that raises the popup above the keyboard, and align
the three header buttons to a single color.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### cff28a5972ac (2026-08-19) — Open the scrollable-home folder popup anchored over its desktop row

The folder card used to open at a fixed 12%-from-top position regardless of
where the tapped folder row is. It now measures the card first and vertically
centers it on the tapped row (clamped to the status bar and the bottom edge),
so the popup appears exactly where the folder lives. Folders created from the
menu still use the default position until their row is on screen.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### d4c276ac232d (2026-08-19) — Vendor platform sources for a standalone build and add scrollable-home folders

- platform/: vendored LineageOS framework sources, Jetpack/Material Maven
  repos and the platform signing key, so the build no longer needs a full
  tree checkout (default lineageRoot = platform/, -PlineageRoot stays as
  an override for live-tree development)
- migrate displaylib + view_capture (+view_capture_proto) from vendored
  Soong jars to source modules launcher-displaylib / launcher-viewcapture;
  only SysUiStatsLog remains packaged from a prebuilt jar
- add update-platform.sh: partial (blob:none + sparse) clones of the two
  LineageOS repos to refresh platform/frameworks for routine branch updates
- scrollable home folders: overlay grid, rename, reorder handles,
  add-to/remove-from-folder, long-press menu items
- restore .gitignore and update README/AGENTS/build scripts

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### b82971e43ca6 (2026-08-19) — Center the apps-list search bar and stretch it when the keyboard opens

The search field is now centered horizontally and slightly narrower at
rest; when the keyboard appears it animates wider so it stays comfortable
to use while typing.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### aef7d140e8ae (2026-08-19) — Add widget open/close animations and search top inset for scrollable home

- Tag scrollable widget host views with LauncherAppWidgetInfo so Quickstep
  sets a launch cookie and predictive-back close lands back on the widget
- Honor the widget developer's shared-element launch animation protocol
- Add a status-bar-sized top inset while searching and reorder Customize
  top inset above Home settings in the desktop menu

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### b7b1c9f3cf5d (2026-08-18) — Add apps-list search bar and hide alphabet sidebar while searching

Adds a bottom search field to the scrollable apps list that filters
results, ranks prefix matches first, highlights the first result, and
opens it on Enter. The field lifts above the IME with an animation and
uses an opaque pill background. The alphabet sidebar fades/slides out
while the search is active (keyboard open or a query present) and comes
back when it is cleared; the clear button now dismisses the keyboard so
the alphabet reappears instead of staying stuck hidden.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### f9a795ba0151 (2026-08-18) — Fix scrollable widget resize: no edge-resize on long-press, keep grid after release

A long-press near a widget border no longer arms the resize handle from the
same gesture (only the horizontal body move is available); the resize grid
now stays visible after releasing a handle and only dismisses on an
empty-space tap.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### b5e03962b867 (2026-08-18) — Add customizable top inset overlay and fix scrollable widget resize gestures

Add a "Customize top inset" option (long-press desktop/apps header) that opens a full-screen overlay with a drag handle in the Done-button pill style, Reset/Done buttons, fade in/out animation, and live persistence to pref_scrollable_top_inset with a 5% minimum clamp.

Fix the widget resize frame on the scrollable home: one tap dismisses the menu and grid together, a long-press drag on the widget is taken over mid-gesture so vertical swipes no longer scroll the list and horizontal moves work from the same press, the menu hides on handle grab while the grid stays, and the grid closes when the list scrolls instead of hanging at a stale position. Rename the widget menu item to "Remove".

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### 59e7db9b432c (2026-08-18) — Polish alphabet sidebar: no boot dimming and smooth highlight fade

Letters render at full opacity while the apps list is still loading, dimming
only returns for sections that truly have no apps; the selection highlight now
fades in/out with the wave and glides between letters while dragging.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### ac3d289b24e4 (2026-08-18) — Fix boot cascade: reveal widgets with apps and decouple horizontal widget resize

Rows bound before the entrance cascade stay hidden and their alpha is forced
back to 1 after the window closes so recycled widget holders never stay
invisible; horizontal widget resizing no longer changes the height.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### 56bd195db2c4 (2026-08-18) — Coordinate boot entrance animation with widget loading and remove dimming on light nav-pill swipe

Widget host listening now gates the boot cascade so widgets and icons fade in
together, and the HintState/controller no longer dims the screen when a light
swipe swaps the alphabet list for the desktop.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### 3f381637b04a (2026-08-17) — Polish scrollable home: boot entrance animation and nav-pill swipe to desktop

- Play the staggered fade-in once after boot completes, not on home return
  or in the alphabet apps list
- Light nav-pill swipe (released without opening recents) closes the
  alphabet list and lands on the desktop (star)
- Defer widget refresh until the view is attached and make widget view
  re-parenting safe across full rebinds
- Let scrollable rows render past the top edge instead of vanishing early

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### 7b37f39d2bf3 (2026-08-17) — Add 1/3-screen bottom inset to scrollable home lists

Add an empty footer row at the end of both the desktop and the apps list
in the scrollable home, sized to one third of the screen so the last
rows stay within comfortable one-handed reach. Reorder mode excludes the
footer from valid drop positions.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### 962088700452 (2026-08-17) — Fix scrollable home widgets stuck on loading after reboot

Desktop widgets are bound before LauncherWidgetHolder finishes
startListening(), so they were created as PendingAppWidgetHostView
placeholders. Those carry no LauncherAppWidgetInfo tag, so the stock
reInflate() path never refreshed them and they stayed stuck on the
loading placeholder until the launcher was manually restarted.

Add addOnListeningStartedCallback() to LauncherWidgetHolder, fired on
the main thread once the host transitions into the listening state, and
use it to re-inflate the scrollable home's widget rows with real host
views that receive updates.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### d33c9051aa81 (2026-08-17) — Disable homeScreenEditImprovements to match stock resize timing

The gradle build resolves launcher flags through FeatureFlagsImpl, whose
homeScreenEditImprovements defaulted to true (and the device aconfig store
does not provide a valid override), so the widget resize frame popped up on
long-press instead of after the drop. Force it off to match the stock
LineageOS Soong product default.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### a421ff2b6da0 (2026-08-15) — Add widget resize frame and package release APK via Magisk

Per-widget resize handles on the scrollable home (ScrollableWidgetResizeFrame
with persisted width/height scales), and package the platform-signed release
APK (com.android.launcher3, versionCode 36) instead of the .debug build so the
package name matches the system launcher and app/settings labels resolve
correctly. Rename app_name to "Trebufork".

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### d01cdff2fcc5 (2026-08-14) — Polish scrollable home: floating Done, sidebar animation, drag fix, configurable top inset

- Replace the header Done button with a floating accent pill above the DragLayer;
  reorder mode now fades the alphabet sidebar out (and back in) and shows the
  floating Done, so drag handles are never obstructed.
- Hide the sidebar and restore it on exiting reorder mode via the new
  setReorderOverlays wiring in Launcher.
- Fix drag-and-drop breaking on the paged workspace after toggling scrollable
  home: isDropEnabled now returns false while the scrollable view is GONE so its
  stale full-screen hit rect no longer swallows drops.
- Add a 30%→20% top inset to the desktop, apps list and alphabet block so the
  first rows and top letters sit in thumb range; letter jumps land at the same
  inset line. The inset is adb-configurable via pref_scrollable_top_inset
  (percent, default 20) and applies live without a launcher restart.
- Rename the desktop menu labels to "Add/Remove from home screen" and drop the
  desktop header title/hint and the Add widget button (widgets still added via
  long-press or the widget picker).

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### f198042cdf1a (2026-08-14) — Add desktop with context menus and reorder to scrollable home

The scrollable home gets a desktop mode: a persistent, freely ordered
list of user-picked apps and widgets backed by ScrollableDesktopStore.
Dropping a widget from the picker lands on the desktop, and the sidebar
star/letters switch between the desktop and the full apps list.

Long-pressing a desktop app or widget now opens the same context menu as
the paged workspace (remove, widget settings, app info, uninstall) instead
of the per-row X buttons. A Reorder entry switches to reorder mode with
drag handles on every row and a Done button in the header. The widget
long-press is wired onto the widget host view (the row never sees its
touches), and the widget touch is cancelled when the menu opens so
clickable widget content does not fire a click on release.

Also adds the hide-app-labels privacy preference.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### 5b574f34f3d3 (2026-08-14) — Make the alphabet touch strip width adb-configurable

Add pref_sidebar_touch_width (default 2/3 of the original 64dp) and read it in
the sidebar so the right-alphabet trigger zone can be tuned without a rebuild.
The app row tap target margin follows the same width.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### e9908491a7a9 (2026-08-14) — Refine scrollable-home sidebar and app row tap target

- Fix alphabet position travel range so a compressed alphabet can slide to the
  full extent of the screen instead of being capped by the edge inset.
- Make the current device values the sidebar defaults and drop the Settings UI
  (position/inset/margin/height are now adb-configurable via shared prefs).
- Add pref_sidebar_left_inset (default 32dp): an empty left strip that scrolls
  the alphabet for left-hand use, with the letter wave bulging at the edge
  rather than stretching to the finger.
- Bound the app row tap target with rounded corners and a consistent width
  that ends exactly where the alphabet touch strip begins.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### 96be5b7466c9 (2026-08-14) — Keep alphabet sidebar swipes from opening the notification shade

Disallow DragLayer touch interception while the alphabet strip is active, so
a downward swipe over it scrolls the alphabet instead of being stolen by the
status bar to open Quick Settings.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### 20cb585fd7d4 (2026-08-14) — Add alphabet height setting and float input fields for sidebar

Replace the sidebar sliders with decimal text inputs and add an
"Alphabet height" setting that compresses the letter block vertically.
Sidebar prefs migrate to floats and legacy integer values are converted
in place so the launcher keeps working after the upgrade.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### ec17bd8a13f4 (2026-08-14) — Animate alphabet wave-in and hide unselected section headers

The alphabet semicircle now grows in smoothly on press (matching the
release collapse), and section header letters for non-selected sections
hide during dragging and fade back in on release, like the app rows.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### eb2cbad38ba1 (2026-08-13) — Add alphabet section separators and drag-reveal to scrollable apps

Show a bold letter separator before each new app section and, while the
sidebar is dragged, fade out rows of other sections while preserving their
height; restoring them with a smooth alpha animation on release.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>

---

### a9bd2f7f69d6 (2026-08-13) — Initialize Trebufork Gradle workspace.

Generated with Codebuff 🤖
Co-Authored-By: Codebuff <noreply@codebuff.com>
