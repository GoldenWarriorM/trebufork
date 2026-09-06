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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;

import com.android.internal.graphics.ColorUtils;

/**
 * trebufork: verbatim Java port of SystemUI's SquigglyProgress drawable
 * (com.android.systemui.media.controls.ui.drawable.SquigglyProgress) — the animated wavy
 * seek bar of the shade/lockscreen media player. When "animating" (playing), the wave phase
 * advances every frame; when paused the wave flattens back into a straight line.
 *
 * <p>Progress semantics follow the Drawable level: 0..10000. The wave occupies the progress
 * portion (tinted color), the rest is drawn as a flat line at DISABLED alpha — identical to
 * the SystemUI implementation, which uses the same ColorUtils.setAlphaComponent scheme.
 */
public class SquigglyProgress extends Drawable {

    private static final float TWO_PI = (float) (Math.PI * 2f);
    private static final int DISABLED_ALPHA = 77;

    private final Paint wavePaint = new Paint();
    private final Paint linePaint = new Paint();
    private final Path path = new Path();
    private float heightFraction = 0f;
    private ValueAnimator heightAnimator;
    private float phaseOffset = 0f;
    private long lastFrameTime = -1L;

    /* distance over which amplitude drops to zero, measured in wavelengths */
    private final float transitionPeriods = 1.5f;
    /* wave endpoint as percentage of bar when play position is zero */
    private final float minWaveEndpoint = 0.2f;
    /* wave endpoint as percentage of bar when play position matches wave endpoint */
    private final float matchedWaveEndpoint = 0.6f;

    // Horizontal length of the sine wave
    float waveLength = 0f;
    // Height of each peak of the sine wave
    float lineAmplitude = 0f;
    // Line speed in px per second
    float phaseSpeed = 0f;
    // Progress stroke width, both for wave and solid line
    float strokeWidth = 0f;

    // Enables a transition region where the amplitude of the wave is reduced linearly.
    boolean transitionEnabled = true;

    // trebufork: public no-arg constructor — required by DrawableInflater when the
    // drawable is inflated from scrollable_media_squiggly_progress.xml.
    public SquigglyProgress() {
        wavePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStyle(Paint.Style.STROKE);
        wavePaint.setStyle(Paint.Style.STROKE);
        linePaint.setAlpha(DISABLED_ALPHA);
    }

    boolean isAnimating() {
        return heightAnimator != null;
    }

    /** Plays (animate = true) or flattens (animate = false) the wave, as SystemUI does. */
    void setAnimate(boolean animate) {
        if (this.animate == animate) {
            return;
        }
        this.animate = animate;
        if (animate) {
            lastFrameTime = SystemClock.uptimeMillis();
        }
        if (heightAnimator != null) {
            heightAnimator.cancel();
        }
        heightAnimator = ValueAnimator.ofFloat(heightFraction, animate ? 1f : 0f);
        heightAnimator.setStartDelay(animate ? 60 : 0);
        heightAnimator.setDuration(animate ? 800 : 550);
        // EMPHASIZED_DECELERATE / STANDARD_DECELERATE approximations.
        heightAnimator.setInterpolator(animate
                ? new android.view.animation.DecelerateInterpolator(1.2f)
                : new android.view.animation.DecelerateInterpolator());
        heightAnimator.addUpdateListener(animation -> {
            heightFraction = (float) animation.getAnimatedValue();
            invalidateSelf();
        });
        heightAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                heightAnimator = null;
            }
        });
        heightAnimator.start();
    }

    private boolean animate = false;

    @Override
    public void draw(Canvas canvas) {
        if (animate) {
            invalidateSelf();
            long now = SystemClock.uptimeMillis();
            if (lastFrameTime >= 0) {
                phaseOffset += (now - lastFrameTime) / 1000f * phaseSpeed;
                phaseOffset %= waveLength;
            }
            lastFrameTime = now;
        }

        float progress = getLevel() / 10_000f;
        float totalWidth = getBounds().width();
        float totalProgressPx = totalWidth * progress;
        float waveProgressPx = totalWidth
                * (!transitionEnabled || progress > matchedWaveEndpoint ? progress
                : lerp(minWaveEndpoint, matchedWaveEndpoint,
                        lerpInv(0f, matchedWaveEndpoint, progress)));

        // Build wiggly path.
        float waveStart = -phaseOffset - waveLength / 2f;
        float waveEnd = transitionEnabled ? totalWidth : waveProgressPx;

        // helper function, computes amplitude for wave segment
        ComputeAmplitude computeAmplitude = (x, sign) -> {
            if (transitionEnabled) {
                float length = transitionPeriods * waveLength;
                float coeff = lerpInvSat(waveProgressPx + length / 2f,
                        waveProgressPx - length / 2f, x);
                return sign * heightFraction * lineAmplitude * coeff;
            } else {
                return sign * heightFraction * lineAmplitude;
            }
        };

        path.rewind();
        path.moveTo(waveStart, 0f);

        // Build the wave, incrementing by half the wavelength each time
        float currentX = waveStart;
        float waveSign = 1f;
        float currentAmp = computeAmplitude.compute(currentX, waveSign);
        float dist = waveLength / 2f;
        while (currentX < waveEnd) {
            waveSign = -waveSign;
            float nextX = currentX + dist;
            float midX = currentX + dist / 2;
            float nextAmp = computeAmplitude.compute(nextX, waveSign);
            path.cubicTo(midX, currentAmp, midX, nextAmp, nextX, nextAmp);
            currentAmp = nextAmp;
            currentX = nextX;
        }

        // translate to the start position of the progress bar for all draw commands
        float clipTop = lineAmplitude + strokeWidth;
        canvas.save();
        canvas.translate(getBounds().left, getBounds().centerY());

        // Draw path up to progress position
        canvas.save();
        canvas.clipRect(0f, -1f * clipTop, totalProgressPx, clipTop);
        canvas.drawPath(path, wavePaint);
        canvas.restore();

        if (transitionEnabled) {
            // If there's a smooth transition, we draw the rest of the path in a different
            // color (using different clip params)
            canvas.save();
            canvas.clipRect(totalProgressPx, -1f * clipTop, totalWidth, clipTop);
            canvas.drawPath(path, linePaint);
            canvas.restore();
        } else {
            // No transition, just draw a flat line to the end of the region.
            // The discontinuity is hidden by the progress bar thumb shape.
            canvas.drawLine(totalProgressPx, 0f, totalWidth, 0f, linePaint);
        }

        // Draw round line cap at the beginning of the wave
        float startAmp = (float) Math.cos(Math.abs(waveStart) / waveLength * TWO_PI);
        canvas.drawPoint(0f, startAmp * lineAmplitude * heightFraction, wavePaint);

        canvas.restore();
    }

    private interface ComputeAmplitude {
        float compute(float x, float sign);
    }

    private static float lerp(float start, float stop, float amount) {
        return (1 - amount) * start + amount * stop;
    }

    private static float lerpInv(float a, float b, float value) {
        if (a == b) {
            return 0f;
        }
        return (value - a) / (b - a);
    }

    private static float lerpInvSat(float a, float b, float value) {
        return Math.max(0f, Math.min(1f, lerpInv(a, b, value)));
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        wavePaint.setColorFilter(colorFilter);
        linePaint.setColorFilter(colorFilter);
    }

    @Override
    public void setAlpha(int alpha) {
        updateColors(wavePaint.getColor(), alpha);
    }

    @Override
    public int getAlpha() {
        return wavePaint.getAlpha();
    }

    @Override
    public boolean setState(int[] stateSet) {
        boolean changed = super.setState(stateSet);
        if (changed) {
            // Match SeekBar behavior: disabled state dims the wave.
            updateColors(wavePaint.getColor(), getAlpha());
            invalidateSelf();
        }
        return changed;
    }

    @Override
    public boolean onLevelChange(int level) {
        invalidateSelf();
        return true;
    }

    @Override
    public void setTint(int tintColor) {
        updateColors(tintColor, getAlpha());
    }

    @Override
    public void setTintList(android.content.res.ColorStateList tint) {
        if (tint == null) {
            return;
        }
        updateColors(tint.getDefaultColor(), getAlpha());
    }

    private void updateColors(int tintColor, int alpha) {
        int a = alpha == 255 && Color.alpha(tintColor) < 255
                ? Color.alpha(tintColor) : alpha;
        wavePaint.setColor(ColorUtils.setAlphaComponent(tintColor, a));
        linePaint.setColor(ColorUtils.setAlphaComponent(tintColor,
                (int) (DISABLED_ALPHA * (a / 255f))));
    }
}
