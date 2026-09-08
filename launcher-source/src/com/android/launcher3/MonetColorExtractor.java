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
 * trebufork: the Monet dynamic-color engine for the media player row. Reproduces the exact
 * scheme the SystemUI shade/lockscreen player uses (frameworks/libs/systemui monet +
 * MediaColorSchemes.kt):
 * <ul>
 *     <li>ColorScheme(wallpaperColors, darkTheme=false, Style.TONAL_SPOT) with seed
 *     extraction from the artwork (filter=true: GOOGLE_BLUE when chroma &lt; 5)</li>
 *     <li>TONAL_SPOT palette chromas: accent1 36, accent2 16, neutral1 6, neutral2 8</li>
 *     <li>SystemUI shade mapping: shade N = tone((1000 - N) / 10)</li>
 *     <li>Lineage 23.2 MediaColorSchemes.kt selections (material roles): play/pause bg /
 *     ripple / app icon = primaryFixed (accent1 t90), play/pause icon = onPrimaryFixed
 *     (accent1 t10), scrim = onSurface (neutral1 t10), seekbar wave/thumb + prev/next =
 *     media_on_background (white)</li>
 * </ul>
 */
public final class MonetColorExtractor {

    // TONAL_SPOT palette chromas (ColorSpec2021 get*Palette).
    private static final double CHROMA_ACCENT1 = 36.0;
    private static final double CHROMA_ACCENT2 = 16.0;
    private static final double CHROMA_NEUTRAL1 = 6.0;
    private static final double CHROMA_NEUTRAL2 = 8.0;

    // ColorScheme.ACCENT1_CHROMA / MIN_CHROMA: seed with chroma < 5 falls back to blue
    // because the player always passes filter=true (ColorScheme(WallpaperColors, darkTheme)).
    private static final double MIN_CHROMA = 5.0;
    private static final int GOOGLE_BLUE = 0xFF1b6ef3;

    private final boolean mDark;
    private final HctSolverUtils.TonalPalette mAccent1;
    private final HctSolverUtils.TonalPalette mAccent2;
    private final HctSolverUtils.TonalPalette mNeutral1;
    private final HctSolverUtils.TonalPalette mNeutral2;

    private MonetColorExtractor(int seed, boolean dark) {
        mDark = dark;
        double[] hct = HctSolverUtils.hctFromInt(seed);
        if (hct[1] < MIN_CHROMA) {
            hct = HctSolverUtils.hctFromInt(GOOGLE_BLUE);
        }
        double hue = hct[0];
        mAccent1 = HctSolverUtils.TonalPalette.fromHueAndChroma(hue, CHROMA_ACCENT1);
        mAccent2 = HctSolverUtils.TonalPalette.fromHueAndChroma(hue, CHROMA_ACCENT2);
        mNeutral1 = HctSolverUtils.TonalPalette.fromHueAndChroma(hue, CHROMA_NEUTRAL1);
        mNeutral2 = HctSolverUtils.TonalPalette.fromHueAndChroma(hue, CHROMA_NEUTRAL2);
    }

    /** Builds the scheme from a source color (the average artwork color). */
    public static MonetColorExtractor fromSeedColor(int seedColor, boolean darkTheme) {
        return new MonetColorExtractor(seedColor, darkTheme);
    }

    /**
     * Extracts the seed color from the album artwork exactly like
     * {@code ColorScheme(WallpaperColors.fromBitmap(art), darkTheme)}: the seed is the
     * top-scored quantized color of the bitmap (a faithful port of
     * {@code ColorScheme.getSeedColors(wallpaperColors, filter=true)}). Falls back to the
     * monet neutral seed when there is no artwork.
     */
    public static MonetColorExtractor fromArtwork(Bitmap artwork, boolean darkTheme) {
        int seed = 0xFF606573; // neutral fallback seed
        if (artwork != null && !artwork.isRecycled()) {
            seed = extractSeed(artwork);
        }
        return new MonetColorExtractor(seed, darkTheme);
    }

    /**
     * Faithful port of {@code ColorScheme.getSeedColor(WallpaperColors, filter=true)}:
     * scores every quantized color by hue population and chroma, then picks the first color
     * that is hue-distinct from the higher-scored ones, iteratively relaxing the required
     * hue distance (90° down to 15°).
     */
    private static int extractSeed(Bitmap bitmap) {
        WallpaperColors colors = WallpaperColors.fromBitmap(scaleDown(bitmap));
        Map<Integer, Integer> allColors = colors.getAllColors();
        if (allColors == null || allColors.isEmpty()) {
            // Population meaningless (colors didn't come from quantization): trust the
            // ordering of the provided main colors.
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
        double chromaScore = hc[1] < 48.0
                ? 0.1 * (hc[1] - 48.0)
                : 0.3 * (hc[1] - 48.0);
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

    // ---------------------------------------------------------------------
    // SystemUI TonalPalette shade mapping: shade N = tone((1000 - N) / 10).
    // ---------------------------------------------------------------------

    private static int shade(HctSolverUtils.TonalPalette palette, int shade) {
        return palette.tone((1000 - shade) / 10);
    }

    /**
     * The play/pause button background / ripple / app-icon color: Lineage 23.2
     * MediaColorSchemes.primaryFromScheme = materialScheme.getPrimaryFixed() = accent1
     * tone 90 (fixed, same for the light scheme the player uses).
     */
    public int getPillBackground() {
        return shade(mAccent1, 100);
    }

    /**
     * The icon inside the play/pause button: Lineage 23.2
     * onPrimaryFromScheme = materialScheme.getOnPrimaryFixed() = accent1 tone 10
     * (a dark tone of the SAME hue as the button background).
     */
    public int getPillIcon() {
        return shade(mAccent1, 900);
    }

    /**
     * The album scrim: Lineage 23.2 MediaColorSchemes.backgroundFromScheme =
     * materialScheme.getOnSurface() = neutral1 tone 10 — a neutral near-black, NOT an
     * accent tone (a colored scrim reads as a vignette).
     */
    public int getScrim() {
        return shade(mNeutral1, 900);
    }

    /**
     * Title text and seekbar wave/thumb: ColorSchemeTransition.textPrimary =
     * neutral1.s50 (nearly white).
     */
    public int getTextPrimary() {
        return shade(mNeutral1, 50);
    }

    /**
     * Artist text: ColorSchemeTransition.textSecondary = neutral2.s200.
     */
    public int getTextSecondary() {
        return shade(mNeutral2, 200);
    }

    /**
     * The un-played rest of the seekbar: ColorSchemeTransition.textTertiary =
     * neutral2.s400.
     */
    public int getSeekbarRest() {
        return shade(mNeutral2, 400);
    }

    /**
     * The LightSourceDrawable press glow: ColorSchemeTransition multiRipple/turbulence
     * updates use accentPrimary = accent1.s100, same as the pill.
     */
    public int getHighlight() {
        return getPillBackground();
    }
}
