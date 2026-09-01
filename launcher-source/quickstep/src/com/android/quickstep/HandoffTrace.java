/*
 * trebufork: verbose, timestamped trace for the open/close handoff (swipe to close or cancel an
 * app whose OPEN animation is still running). Every launcher operation logs through here so the
 * whole window from merge -> spring can be replayed from logcat and correlated with the frames
 * SurfaceFlinger actually presents.
 *
 * Each plain log line carries:
 *   u=  - android.os.SystemClock.uptimeMillis() (monotonic, matches `logcat` wall order)
 *   f=  - Choreographer.getFrameTimeNanos() in ms, i.e. the vsync of the CURRENT frame the
 *         launcher UI thread is producing. SurfaceFlinger latches system-app/launcher surfaces
 *         on vsync boundaries, so `f` is directly comparable to the latch timestamps reported by
 *         the transaction-completed callback below and to `dumpsys SurfaceFlinger --latency`.
 *
 * For transactions we also register a transaction-completed listener; when SurfaceFlinger
 * latches our transaction into a frame it reports the latch time. If the latch time for a given
 * op is EARLIER than the current launcher frame, at least one frame was presented with the
 * pre-op surface state (e.g. the full-screen identity frame WM queued for a fresh leash) -
 * i.e. the flash is a SurfaceFlinger composition of WM's state, not a launcher-logic ordering
 * bug. If the listener cannot be registered (non-privileged callers get SecurityException), we
 * log that once per op and fall back to a plain apply.
 */
package com.android.quickstep;

import android.os.SystemClock;
import android.util.Log;
import android.view.Choreographer;
import android.view.SurfaceControl;

import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class HandoffTrace {

    public static final String TAG = "TrebuforkRev";

    private static final Executor sExecutor = Executors.newSingleThreadExecutor();

    private HandoffTrace() {
    }

    /**
     * Timestamped log line. {@code f} carries the Choreographer frame time for the current
     * frame so line order can be mapped onto SurfaceFlinger vsync boundaries.
     */
    public static void log(String msg) {
        try {
            Log.d(TAG, String.format(Locale.US, "[u=%d f=%d] %s",
                    SystemClock.uptimeMillis(),
                    Choreographer.getInstance().getFrameTimeNanos() / 1_000_000L,
                    msg));
        } catch (Throwable e) {
            Log.d(TAG, "[u=" + SystemClock.uptimeMillis() + "] " + msg);
        }
    }

    /**
     * Applies {@code t} and, once SurfaceFlinger latches it into a frame, logs which present
     * time the frame carrying this transaction got. Compared with the launcher's current frame
     * time it shows whether SurfaceFlinger composed at least one frame with the PRE-op surface
     * state (e.g. the full-screen identity frame WM queued for a fresh leash) before our
     * transaction landed - i.e. whether the flash is a SurfaceFlinger composition of WM's state
     * rather than a launcher-logic ordering issue.
     */
    public static void applyAndTrace(String op, SurfaceControl.Transaction t) {
        final long issuedFrameMs = frameTimeMs();
        // trebufork: pin the transaction to the current vsync so SurfaceFlinger's completed-
        // callback can actually associate it with a frame and give us a present fence signal
        // that fires when the frame carries the TRANSACTION state is presented. Without this a
        // bare transaction is merged opportunistically and its fence never signals
        // (latchedAt/getSignalTime = 0/INVALID on this platform).
        try {
            t.setFrameTimelineVsync(Choreographer.getInstance().getFrameTimeNanos());
        } catch (Throwable ignore) {
            // best-effort; may not exist in older SDKs
        }
        try {
            t.addTransactionCompletedListener(sExecutor, stats -> {
                final long issuedMs = issuedFrameMs;
                final android.hardware.SyncFence fence = stats.getPresentFence();
                long presentMs = LONG_INVALID_MS;
                if (fence != null) {
                    try {
                        final boolean signalled = fence.await(java.time.Duration.ofMillis(1000));
                        final long tNanos = fence.getSignalTime();
                        presentMs = (signalled && tNanos > 0)
                                ? tNanos / 1_000_000L : LONG_INVALID_MS;
                    } catch (Throwable ignore) {
                        // presentMs stays invalid
                    } finally {
                        try {
                            fence.close();
                        } catch (Throwable ignore) {
                        }
                    }
                }
                if (presentMs == LONG_INVALID_MS) {
                    log("SF present: " + op + " issuedAt=" + issuedMs
                            + "ms present=<invalid> (cannot correlate frame)");
                } else {
                    long delta = presentMs - issuedMs;
                    log(String.format(Locale.US,
                            "SF present: " + op + " issuedAt=%dms presented=%dms ahead=%dms",
                            issuedMs, presentMs, delta));
                }
                stats.close();
            });
        } catch (Throwable e) {
            log("SF callback unavailable for " + op + ": "
                    + (e.getClass().getSimpleName()) + " (falling back to plain apply)");
        }
        t.apply();
    }

    private static final long LONG_INVALID_MS = Long.MIN_VALUE;

    /**
     * Renders an object array compactly for tracing (avoids Array.toString() noise).
     */
    public static String render(Object... parts) {
        return Arrays.toString(parts);
    }

    private static long frameTimeMs() {
        try {
            return Choreographer.getInstance().getFrameTimeNanos() / 1_000_000L;
        } catch (Throwable e) {
            return SystemClock.uptimeMillis();
        }
    }
}