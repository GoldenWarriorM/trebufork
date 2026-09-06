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

import android.app.WallpaperColors;
import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * Extracts the seed color from the album artwork exactly like SystemUI does for the
     * media player: {@code new ColorScheme(WallpaperColors.fromBitmap(...), darkTheme=false,
     * ThemeStyle.CONTENT)} — the seed is the top-scored quantized color of the bitmap
     * (a faithful port of {@code ColorScheme.getSeedColors(wallpaperColors, filter=false)};
     * CONTENT passes filter=false). Falls back to a neutral gray-blue seed if there is no
     * artwork.
     */
    public static MonetColorExtractor fromArtwork(Bitmap artwork, boolean darkTheme) {
        int seed = 0xFF606573; // neutral fallback seed (grayish blue, like monet default)
        if (artwork != null && !artwork.isRecycled()) {
            seed = extractSeed(artwork);
        }
        return new MonetColorExtractor(seed, darkTheme);
    }

    private static final double ACCENT1_CHROMA = 48.0;
    private static final int GOOGLE_BLUE = 0xFF1b6ef3;

    /**
     * Faithful port of {@code ColorScheme.getSeedColor(WallpaperColors, filter=false)}:
     * scores every quantized color by hue population and chroma, then picks the first color
     * that is hue-distinct from the higher-scored ones, iteratively relaxing the required
     * hue distance (90° down to 15°).
     */
    private static int extractSeed(Bitmap bitmap) {
        WallpaperColors colors = WallpaperColors.fromBitmap(scaleDown(bitmap));
        Map<Integer, Integer> allColors = colors.getAllColors();
        if (allColors == null || allColors.isEmpty()) {
            // Population meaningless (colors didn't come from quantization): trust the
            // ordering of the provided main colors (filter=false: no chroma filter).
            for (Color mainColor : colors.getMainColors()) {
                return mainColor.toArgb();
            }
            return GOOGLE_BLUE;
        }
        double totalPopulation = 0;
        for (int population : allColors.values()) {
            totalPopulation += population;
        }
        if (totalPopulation <= 0) {
            return GOOGLE_BLUE;
        }

        Map<Integer, double[]> intToHc = new HashMap<>();
        // Percentage of the image with each hue (360 slots).
        double[] hueProportions = new double[360];
        for (Map.Entry<Integer, Integer> entry : allColors.entrySet()) {
            double[] hc = HctSolverUtils.hctFromInt(entry.getKey());
            intToHc.put(entry.getKey(), hc);
            int hue = (int) Math.round(hc[0]) % 360;
            hueProportions[hue] += entry.getValue() / totalPopulation;
        }
        // Map each color to the percentage of the image with hues within ±15° of its own.
        Map<Integer, Double> intToHueProportion = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : allColors.entrySet()) {
            int hue = (int) Math.round(intToHc.get(entry.getKey())[0]) % 360;
            double proportion = 0.0;
            for (int i = hue - 15; i <= hue + 15; i++) {
                proportion += hueProportions[wrapDegrees(i)];
            }
            intToHueProportion.put(entry.getKey(), proportion);
        }
        // Sort the colors by score, from high to low.
        List<Integer> scored = new ArrayList<>(allColors.keySet());
        scored.sort((a, b) -> Double.compare(
                score(intToHc.get(b), intToHueProportion.get(b)),
                score(intToHc.get(a), intToHueProportion.get(a))));

        // Go through the colors, from high score to low, requiring hue distinctness that
        // iteratively decreases, thus maximizing the difference between the picked colors.
        int minimumHueDistance = 15;
        List<Integer> seeds = new ArrayList<>();
        for (int i = 90; i >= minimumHueDistance; i--) {
            seeds.clear();
            for (Integer currentColor : scored) {
                double currentHue = intToHc.get(currentColor)[0];
                boolean existingSeedNearby = false;
                for (int seed : seeds) {
                    if (hueDiff(currentHue, intToHc.get(seed)[0]) < i) {
                        existingSeedNearby = true;
                        break;
                    }
                }
                if (existingSeedNearby) {
                    continue;
                }
                seeds.add(currentColor);
                if (seeds.size() >= 4) {
                    break;
                }
            }
            if (!seeds.isEmpty()) {
                break;
            }
        }
        return seeds.isEmpty() ? GOOGLE_BLUE : seeds.get(0);
    }

    private static double score(double[] hc, double proportion) {
        double proportionScore = 0.7 * 100.0 * proportion;
        double chromaScore = hc[1] < ACCENT1_CHROMA
                ? 0.1 * (hc[1] - ACCENT1_CHROMA)
                : 0.3 * (hc[1] - ACCENT1_CHROMA);
        return chromaScore + proportionScore;
    }

    private static int wrapDegrees(int degrees) {
        if (degrees < 0) {
            return (degrees % 360) + 360;
        } else if (degrees >= 360) {
            return degrees % 360;
        }
        return degrees;
    }

    private static double hueDiff(double a, double b) {
        double diff = Math.abs(a - b);
        if (diff > 180.0) {
            diff = 360.0 - diff;
        }
        return diff;
    }

    /** Downscales to at most 256px on the long side for fast quantization. */
    private static Bitmap scaleDown(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int maxSide = Math.max(w, h);
        if (maxSide <= 256) {
            return bitmap;
        }
        float scale = 256f / maxSide;
        return Bitmap.createScaledBitmap(bitmap,
                Math.max(1, Math.round(w * scale)), Math.max(1, Math.round(h * scale)), true);
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
