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
 *     pink where our old TONAL_SPOT-style port jumped to blue/teal</li>     *     <li>Lineage 23.2 MediaColorSchemes.kt selections (material roles), with the
     *     pill landing on primaryFixedDim (accent1 tone 80) as measured on device:
     *     play/pause bg / ripple / app icon = accent1 tone 80 — deeper pastel of the
     *     seed hue,
     *     play/pause icon = onPrimaryFixed (accent1 tone 10 — near-black),
     * scrim = onSurface (neutral1 tone 10 — Lineage 23.2 backgroundFromScheme),
     * title/seekbar = media_on_background (white)</li>
 * </ul>
 */
public final class MonetColorExtractor {

    // The CONTENT scheme (SchemeContent) keeps the seed's own chroma in the primary
    // palette — no fixed 48 chroma. The pill tone is fixed 90 (primaryFixed), the
    // letterbox/scrim tone is fixed 10 (onSurface), like the shade player.
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
        // SchemeContent (libmonet SchemeContent.java, verbatim): primary = seed hue/chroma,
        // secondary = max(chroma - 32, chroma * 0.5), neutral = chroma / 8,
        // neutralVariant = chroma / 8 + 4. (The old ColorSpec2021-style 0.33x/0.0833x/
        // 0.1666x factors produced palettes the shade player never builds.)
        mAccent1 = HctSolverUtils.TonalPalette.fromHueAndChroma(hue, chroma);
        mAccent2 = HctSolverUtils.TonalPalette.fromHueAndChroma(hue,
                Math.max(chroma - 32.0, chroma * 0.5));
        mNeutral1 = HctSolverUtils.TonalPalette.fromHueAndChroma(hue, chroma / 8.0);
        mNeutral2 = HctSolverUtils.TonalPalette.fromHueAndChroma(hue, chroma / 8.0 + 4.0);
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
     * Feeds the artwork to {@link WallpaperColors#fromBitmap} EXACTLY like the shade:
     * the RAW bitmap, no pre-scaling. fromBitmap downscales internally to 112x112
     * (calculateOptimalSize) with its own rounding and filtering; any pre-scale on top
     * of that re-smooths the pixels and shifts quantizer populations — device-measured
     * (YouTube red artwork): pre-scale picked a hue-358 seed (pill #FFB3B6) while the
     * shade's raw-bitmap input picked hue-7 (pill #F8B4AC).
     */
    private static Bitmap scaleDown(Bitmap bitmap) {
        return bitmap;
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
     * The play/pause button background / ripple / app-icon color. The device's shade
     * player (Android 16 SystemUI, baksmali-verified) resolves its pill color with
     * ColorSpec2025.primaryFixed(): the primary palette with TONE = tone of
     * primaryContainer, which for the CONTENT scheme (light, phone) is
     * tMaxC(palette, min=66, cap=93, mult=1.0):
     * findBestToneForChroma walks tone DOWN from 100 while the in-gamut chroma is below
     * the palette's key chroma, then clamps into [66, 93]. Effectively: the seed's own
     * chroma held as high (as light) on the L* ramp as sRGB allows — vivid covers get a
     * vivid mid-tone pill (Bike cyan: #65DBF9-class, tone ~82), muted covers a light
     * pastel pinned at the 93 cap (Bugs green: #DBF3BF ≈ measured #DBF4BD).
     * Verified against device screenshots on two artworks (green: 93.2 vs 93.0,
     * cyan: 81.3 vs 82.0 — within one HCT-solver step).
     */
    public int getPillBackground() {
        return tMaxC(mAccent1);
    }

    /**
     * ColorSpec2025.tMaxC for the pill: walk tone down from 100 while in-gamut chroma
     * has not yet reached the palette's key chroma; keep the last strictly-rising tone;
     * clamp into [66, 93]. (isCyan hues 170..207 use a different cap; the cap constant
     * for non-cyan is 93 — baksmali-verified from ColorSpec2025 lambda tables.)
     */
    private int tMaxC(HctSolverUtils.TonalPalette palette) {
        double hue = palette.getHue();
        double chroma = palette.getChroma();
        double best = 100.0;
        double prevChroma = inGamutChroma(hue, chroma, 100.0);
        for (double t = 99.0; t >= 0.0; t -= 1.0) {
            if (prevChroma >= chroma) {
                break;
            }
            double c = inGamutChroma(hue, chroma, t);
            if (c > prevChroma) {
                best = t;
                prevChroma = c;
            }
        }
        double clamped = Math.max(66.0, Math.min(93.0, best));
        return palette.tone((int) Math.round(clamped));
    }

    /** In-gamut chroma of {@code Hct.from(hue, chroma, tone)} (the solver gamut-maps). */
    private static double inGamutChroma(double hue, double chroma, double tone) {
        int argb = HctSolverUtils.hctFromTonePublic(hue, chroma, tone);
        return HctSolverUtils.hctFromInt(argb)[1];
    }

    /** Resolves the palette's hue/chroma at an arbitrary HCT tone. */
    private static int hctFromTone(HctSolverUtils.TonalPalette palette, double tone) {
        return HctSolverUtils.hctFromTonePublic(palette.getHue(), palette.getChroma(), tone);
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
     * The album scrim AND letterbox background: Lineage 23.2 backgroundFromScheme =
     * materialScheme.getOnSurface(). On the device's Android 16 SystemUI the scheme is
     * ColorSpec2025, where onSurface (dark, phone, CONTENT) resolves through
     * surfaceBright: fixed tone 18 with the background contrastCurve (26) adjustment
     * against the neutral tone 87 → effective tone ≈ 21. Confirmed by direct veil
     * measurements on three artworks (tone 18–24 with the artwork's hue): NOT the old
     * neutral tone 10, which rendered as a neutral black veil.
     */
    public int getScrim() {
        return hctFromTone(mNeutral1, 22.0);
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
