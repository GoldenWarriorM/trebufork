/*
 * Gradle-only stub for the framework aconfig flags class
 * android.view.accessibility.Flags (view.accessibility.flags).
 *
 * The java variant of this aconfig module is not part of the Lineage Soong
 * build graph, so no jar is produced for it. The launcher only needs the two
 * flag getters below (both declared in accessibility_flags.aconfig). The stub
 * is packaged in the APK; on platforms that do expose the real framework class
 * the bootclasspath shadows this one, and on older platforms the calls simply
 * report the flag as disabled.
 */
package android.view.accessibility;

public final class Flags {

    private Flags() {}

    public static boolean launcherAppDisplayProgressUpdateOnVisibilityChange() {
        return false;
    }

    public static boolean navbarFlipOrderOption() {
        return false;
    }
}
