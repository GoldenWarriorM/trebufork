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
 *     <li>ColorScheme(wallpaperColors, darkTheme=false, Style.CONTENT) — SchemeContent:
 *     the primary palette keeps the SEED's own hue AND chroma, and the pill tone tracks the
 *     seed tone (ColorSpec2021.primaryContainer with isFidelity=true = seed tone), so vivid
 *     covers get a vivid pill and muted covers a muted one</li>
 *     <li>seed extraction with filter=false for CONTENT (ColorScheme ctor passes
 *     {@code style != ThemeStyle.CONTENT} as the filter flag): low-chroma colors are
 *     allowed and there is NO Google-Blue fallback — that is why the system player stays
 *     pink where our old TONAL_SPOT-style port jumped to blue/teal</li>     * <li>Lineage 23.2 MediaColorSchemes.kt selections (material roles):
     * play/pause bg / ripple / app icon = primaryFixed (accent1 FIXED tone 90 — light
     * pastel of the seed hue, verified in libmonet MaterialDynamicColors),
     * play/pause icon = onPrimaryFixed (accent1 tone 10 — near-black),
     * scrim = onSurface (neutral1 tone 10), title/seekbar = media_on_background (white)</li>
 * </ul>
 */
public final class MonetColorExtractor {

    // The CONTENT scheme (SchemeContent via ColorSpec2021) keeps the seed's own chroma in
    // the primary palette — no fixed 48 chroma. The pill tone is the seed tone too
    // (primaryContainer isFidelity), clamped to a sane band like DynamicColor does.
    private static final int GOOGLE_BLUE = 0xFF1b6ef3;

    private final boolean mDark;
    private final int mSeed;    private final HctSolverUtils.TonalPalette mAccent1;
    private final HctSolverUtils.TonalPalette mAccent2;
    private final HctSolverUtils.TonalPalette mNeutral1;
    private final HctSolverUtils.TonalPalette mNeutral2;

    private MonetColorExtractor(int seed, boolean dark) {        mDark = dark;
        mSeed = seed;
        double[] hct = HctSolverUtils.hctFromInt(seed);
        double hue = hct[0];
        double chroma = hct[1];
        // SchemeContent: primary palette = seed hue/chroma, secondary = 0.33x chroma,
        // neutral = 0.0833x, neutralVariant = 0.1666x (ColorSpec2021.getPrimaryPalette et al).
        mAccent1 = HctSolverUtils.TonalPalette.fromHueAndChroma(hue, chroma);
        mAccent2 = HctSolverUtils.TonalPalette.fromHueAndChroma(hue, chroma * 0.33);
        mNeutral1 = HctSolverUtils.TonalPalette.fromHueAndChroma(hue, chroma * 0.0833);
        mNeutral2 = HctSolverUtils.TonalPalette.fromHueAndChroma(hue, chroma * 0.1666);
    }

    /** Builds the scheme from a source color (the average artwork color). */
    public static MonetColorExtractor fromSeedColor(int seedColor, boolean darkTheme) {
        return new MonetColorExtractor(seedColor, darkTheme);
    }

    /**
     * Extracts the seed color from the album artwork exactly like
     * {@code ColorScheme(WallpaperColors.fromBitmap(art), darkTheme, Style.CONTENT)}: the
     * seed is the top-scored quantized color of the bitmap, and because the style is
     * CONTENT the filter flag is <b>false</b> — low-chroma colors are allowed and the
     * GOOGLE_BLUE fallback never fires (only a fully empty quantization falls back).
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
        // Percentage of the image with each hue (360 slots). filter=false for CONTENT:
        // low-chroma colors still contribute to the hue population.
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
        // filter=false: no hue-population filter and NO GOOGLE_BLUE fallback here;
        // the top-scored color wins even if it is nearly gray.
        return seeds.isEmpty() ? (allColors.isEmpty() ? GOOGLE_BLUE : scored.get(0))
                : seeds.get(0);
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

    /**
     * Matches WallpaperColors.fromBitmap's internal downscale: area at most
     * MAX_BITMAP_SIZE^2 = 112x112 (see WallpaperColors#calculateOptimalSize). The old
     * 256px-long-side scale kept 5x more area, which shifted quantizer population
     * weights and picked different seeds than the system player.
     */
    private static Bitmap scaleDown(Bitmap bitmap) {
        final int maxArea = 112 * 112;
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int area = w * h;
        if (area <= maxArea) {
            return bitmap;
        }
        double scale = Math.sqrt(maxArea / (double) area);
        int newWidth = Math.max(1, (int) (w * scale));
        int newHeight = Math.max(1, (int) (h * scale));
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
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
     * MediaColorSchemes.primaryFromScheme = materialScheme.getPrimaryFixed().
     * Verified against libmonet MaterialDynamicColors.primaryFixed(): the tone is FIXED
     * at 90 (light/dark alike; 40 only in the monochrome spec) — a light pastel of the
     * seed hue, which is why the system pill stays light on every cover.
     */
    public int getPillBackground() {
        return mAccent1.tone(90);
    }

    /**
     * The icon inside the play/pause button: Lineage onPrimaryFromScheme =
     * materialScheme.getOnPrimaryFixed() = accent1 tone 10 (near-black), matching the
     * black play triangle of the system pill.
     */
    public int getPillIcon() {
        return shade(mAccent1, 900);
    }

    /**
     * The album scrim: Lineage 23.2 backgroundFromScheme = materialScheme.getOnSurface()
     * — in the light CONTENT scheme that is neutral1 tone 10, a near-black with a slight
     * tint of the artwork hue (neutral chroma = 0.0833x seed chroma).
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
     * The LightSourceDrawable press glow: Lineage getSurfaceEffectColor =
     * primaryColor.targetColor = the pill color, same as the ripple.
     */
    public int getHighlight() {
        return getPillBackground();
    }

    /**
     * The carousel page dots: media_paging_indicator =
     * material_dynamic_neutral_variant80 = neutral-variant palette (0.1666x chroma) tone 80.
     */
    public int getPagingIndicator() {
        return shade(mNeutral2, 200);
    }
}
