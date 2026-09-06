/*
 * Copyright (C) 2026 The trebufork Project
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
package com.android.launcher3;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * trebufork: the Monet dynamic-color engine for the media player row. Generates the exact
 * tonal palettes the SystemUI shade/lockscreen player uses (via the HCT solver port in
 * {@link HctSolverUtils}), picking colors the same way <code>MediaColorSchemes.kt</code>
 * does:
 * <ul>
 *     <li>on-surface text over the album scrim: neutral palette tone 90 (dark theme)</li>
 *     <li>primary (play-pause pill, seekbar progress): primaryFixed — primary tone 90</li>
 *     <li>on-primary (pill icon): primary tone 10</li>
 * </ul>
 * The seed is extracted from the album artwork, falling back to a fixed neutral seed when
 * the artwork is unavailable.
 */
public final class MonetColorExtractor {

    public static final int TONE_ON_SURFACE_DARK = 90;
    public static final int TONE_ON_SURFACE_LIGHT = 10;
    public static final int TONE_PRIMARY_FIXED = 90;
    public static final int TONE_ON_PRIMARY_FIXED = 10;

    // SchemeContent palette rules (libmonet scheme/SchemeContent.kt), the style SystemUI
    // uses for the media player: new ColorScheme(wallpaperColors, /* darkTheme= */ false,
    // ThemeStyle.CONTENT).
    private static final double CONTENT_PRIMARY_CHROMA_OFFSET = 32.0;
    private static final double CONTENT_PRIMARY_CHROMA_FACTOR = 0.5;
    private static final double CONTENT_NEUTRAL_CHROMA_DIVISOR = 8.0;
    private static final double CONTENT_NEUTRAL_CHROMA_MAX = 8.0;

    private final boolean mDark;
    private final HctSolverUtils.TonalPalette mPrimary;
    private final HctSolverUtils.TonalPalette mNeutral;

    private MonetColorExtractor(int seed, boolean dark) {
        mDark = dark;
        double[] hc = HctSolverUtils.hctFromInt(seed);
        double sourceChroma = hc[1];
        double primaryChroma = Math.max(sourceChroma - CONTENT_PRIMARY_CHROMA_OFFSET,
                sourceChroma * CONTENT_PRIMARY_CHROMA_FACTOR);
        double neutralChroma = Math.min(sourceChroma / CONTENT_NEUTRAL_CHROMA_DIVISOR,
                CONTENT_NEUTRAL_CHROMA_MAX);
        mPrimary = HctSolverUtils.TonalPalette.fromHueAndChroma(hc[0], primaryChroma);
        mNeutral = HctSolverUtils.TonalPalette.fromHueAndChroma(hc[0], neutralChroma);
    }

    /** Builds the scheme from a source color (the average artwork color). */
    public static MonetColorExtractor fromSeedColor(int seedColor, boolean darkTheme) {
        return new MonetColorExtractor(seedColor, darkTheme);
    }

    /**
     * Extracts a seed color from the album artwork, mirroring what
     * {@code ColorScheme(WallpaperColors.fromBitmap(...))} does in SystemUI: the dominant
     * colors of the bitmap are averaged into a seed. Falls back to a neutral gray-blue seed
     * if there is no artwork.
     */
    public static MonetColorExtractor fromArtwork(Bitmap artwork, boolean darkTheme) {
        int seed = 0xFF606573; // neutral fallback seed (grayish blue, like monet default)
        if (artwork != null && !artwork.isRecycled()) {
            seed = extractSeed(artwork);
        }
        return new MonetColorExtractor(seed, darkTheme);
    }

    /**
     * Averages the bitmap pixels with the most chroma, like WallpaperColors does for its
     * primary color: samples a downscaled grid, keeps colorful-enough pixels, and blends
     * them; if the artwork is mostly achromatic, falls back to the overall average.
     */
    private static int extractSeed(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (w <= 0 || h <= 0) {
            return 0xFF606573;
        }
        // Downscale: sample at most 24x24 points.
        int step = Math.max(1, Math.max(w, h) / 24);
        long sumR = 0, sumG = 0, sumB = 0, sumWeight = 0;
        long sumRAll = 0, sumGAll = 0, sumBAll = 0, countAll = 0;
        for (int y = 0; y < h; y += step) {
            for (int x = 0; x < w; x += step) {
                int pixel = bitmap.getPixel(x, y);
                int r = Color.red(pixel), g = Color.green(pixel), b = Color.blue(pixel);
                sumRAll += r;
                sumGAll += g;
                sumBAll += b;
                countAll++;
                // Weight colorful pixels (simple chroma proxy: max-min spread).
                int max = Math.max(r, Math.max(g, b));
                int min = Math.min(r, Math.min(g, b));
                int weight = max - min;
                if (weight > 24) {
                    sumR += (long) r * weight;
                    sumG += (long) g * weight;
                    sumB += (long) b * weight;
                    sumWeight += weight;
                }
            }
        }
        if (sumWeight > 0) {
            return Color.argb(0xFF,
                    (int) (sumR / sumWeight), (int) (sumG / sumWeight),
                    (int) (sumB / sumWeight));
        }
        return Color.argb(0xFF,
                (int) (sumRAll / Math.max(1, countAll)),
                (int) (sumGAll / Math.max(1, countAll)),
                (int) (sumBAll / Math.max(1, countAll)));
    }

    public boolean isDark() {
        return mDark;
    }

    /** Text/icon color drawn over the album art scrim (media_on_background). */
    public int getOnSurface() {
        return mNeutral.tone(mDark ? TONE_ON_SURFACE_DARK : TONE_ON_SURFACE_LIGHT);
    }

    /** Secondary (artist) text: neutral tone 80 / 30. */
    public int getOnSurfaceVariant() {
        return mNeutral.tone(mDark ? 80 : 30);
    }

    /** Play-pause pill background and seekbar progress: primaryFixed (tone 90). */
    public int getPrimaryFixed() {
        return mPrimary.tone(TONE_PRIMARY_FIXED);
    }

    /** Icon inside the play-pause pill: onPrimaryFixed (tone 10). */
    public int getOnPrimaryFixed() {
        return mPrimary.tone(TONE_ON_PRIMARY_FIXED);
    }
}
