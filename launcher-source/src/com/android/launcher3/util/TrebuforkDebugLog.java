/*
 * Copyright (C) 2026 The Trebufork Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3.util;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Logs rare launcher anomalies (e.g. taps landing on invisible views) with full stack
 * traces to a persistent file so they can be pulled later for diagnosis:
 *
 * <pre>adb pull /sdcard/Android/data/com.android.launcher3/files/trebufork_events.log</pre>
 *
 * The file rotates: once it exceeds {@link #MAX_BYTES} it is moved to
 * {@code trebufork_events.log.old} (replacing any previous old file).
 */
public final class TrebuforkDebugLog {

    private static final String TAG = "TrebuforkDebugLog";
    private static final String FILE_NAME = "trebufork_events.log";
    private static final long MAX_BYTES = 2 * 1024 * 1024;

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "trebufork-debug-log");
        t.setDaemon(true);
        return t;
    });

    private TrebuforkDebugLog() {}

    /**
     * Records {@code event} with the current call stack (via the provided throwable) to the
     * log file. Safe to call from the UI thread — all disk I/O happens on a background
     * executor. Never throws.
     */
    public static void logEvent(Context context, String event, Throwable throwable) {
        if (context == null) {
            Log.e(TAG, "logEvent: no context, event=" + event);
            return;
        }
        Log.e(TAG, event, throwable);
        final Context app = context.getApplicationContext();
        final Throwable t = throwable != null
                ? throwable : new Throwable(event);
        final String entry = formatEntry(event, t);
        EXECUTOR.execute(() -> writeEntry(app, entry));
    }

    /** Convenience overload that captures the current stack automatically. */
    public static void logEvent(Context context, String event) {
        logEvent(context, event, new Throwable(event));
    }

    private static String formatEntry(String event, Throwable t) {
        StringWriter sw = new StringWriter(512);
        PrintWriter pw = new PrintWriter(sw);
        pw.print(new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US).format(new Date()));
        pw.print(" EVENT: ");
        pw.println(event);
        if (t != null) {
            t.printStackTrace(pw);
        }
        pw.println();
        pw.flush();
        return sw.toString();
    }

    private static void writeEntry(Context app, String entry) {
        try {
            File dir = app.getExternalFilesDir(null);
            if (dir == null) {
                dir = app.getFilesDir();
            }
            if (dir == null) {
                return;
            }
            File file = new File(dir, FILE_NAME);
            if (file.length() > MAX_BYTES) {
                File old = new File(dir, FILE_NAME + ".old");
                //noinspection ResultOfMethodCallIgnored
                old.delete();
                //noinspection ResultOfMethodCallIgnored
                file.renameTo(old);
            }
            try (Writer w = new OutputStreamWriter(
                    new FileOutputStream(file, true), StandardCharsets.UTF_8)) {
                w.write(entry);
            }
        } catch (IOException | RuntimeException e) {
            Log.e(TAG, "Failed to write debug log entry", e);
        }
    }
}
