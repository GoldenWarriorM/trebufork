/*
 * Gradle-provided copy of the SystemUI compilelib constant (Soong builds
 * compilelib as src-debug/src-release variants; tracinglib references
 * Compile.IS_DEBUG). Our builds are debug, matching the src-debug variant.
 */
package com.android.systemui.util;

/** Constants that vary by compilation configuration. */
public class Compile {
    /** Whether this compilation supports debug features. */
    public static final boolean IS_DEBUG = true;
}
