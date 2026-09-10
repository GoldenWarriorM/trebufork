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
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;

import com.android.internal.graphics.ColorUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * trebufork: Java port of SystemUI's MultiRippleView + RippleAnimation — the tap effect of
 * the shade/lockscreen media player. Pressing an action button plays a circle ripple
 * centered on the button that expands across the whole player and fades out
 * (MediaControlViewBinder.createTouchRippleAnimation: CIRCLE shape, 1500ms,
 * maxSize = view width * 2, opacity 100, accentPrimary color).
 */
public class ScrollableMediaRippleView extends View {

    // createTouchRippleAnimation: duration 1500L, opacity 100, sparkleStrength 0.
    private static final long RIPPLE_DURATION_MS = 1500L;
    private static final int RIPPLE_OPACITY = 100;
    private static final float MAX_SIZE_FACTOR = 2f;

    // RippleShader fade curves (RawProgress-driven, STANDARD easing applied to the size):
    // - base ring: fades in over the first 10% and out from 30%..100%
    // - center fill (the "inside" disc at 1.25x radius): only visible in the first
    //   frames (fadeInEnd=0 → immediately fading out), so the tap flashes a filled circle
    //   that quickly dissolves while the ring keeps expanding — not a uniform fading disc.
    private static final float BASE_RING_FADE_IN_END = 0.1f;
    private static final float BASE_RING_FADE_OUT_START = 0.3f;
    private static final float CENTER_FILL_FADE_OUT_START = 0f;

    private static final class Ripple {
        float x;
        float y;
        float progress;
        Animator animator;
    }

    private final List<Ripple> mRipples = new ArrayList<>();
    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int mColor = 0xFFFFFFFF;
    // RippleShader.STANDARD — applied to the size progress inside the shader.
    private final android.view.animation.Interpolator mStandardInterpolator =
            new android.view.animation.PathInterpolator(0.2f, 0f, 0f, 1f);

    public ScrollableMediaRippleView(Context context) {
        super(context);
    }

    public ScrollableMediaRippleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrollableMediaRippleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /** The ripple color; SystemUI feeds it the scheme's accentPrimary. */
    public void updateColor(int color) {
        if (mColor == color) {
            return;
        }
        mColor = color;
        invalidate();
    }

    /**
     * Plays a ripple expanding from ({@code x}, {@code y}) (this view's coordinates,
     * the button center in SystemUI) to cover the whole player.
     */
    public void playRipple(float x, float y) {
        // trebufork: a card rebuilt right before the tap (widget/session switch) may not
        // be laid out yet — getWidth() == 0 would make the ripple radius 0 and invisible.
        // Replay once the view has a size.
        if (getWidth() == 0 || getHeight() == 0) {
            post(() -> {
                if (getWidth() > 0 && getHeight() > 0) {
                    playRipple(x, y);
                }
            });
            return;
        }
        Ripple ripple = new Ripple();
        ripple.x = x;
        ripple.y = y;
        // Linear animator, like RippleAnimation.play: rawProgress stays linear and the
        // STANDARD curve is applied exactly once, inside onDraw (RippleShader.progress).
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(RIPPLE_DURATION_MS);
        animator.addUpdateListener(animation -> {
            ripple.progress = (float) animation.getAnimatedValue();
            // trebufork: postInvalidateOnAnimation instead of invalidate. After the card
            // was swiped out of the carousel viewport and back, a plain invalidate from a
            // re-attached view can be coalesced away and the ripple never appears —
            // scheduling with the animation callback guarantees a frame every tick.
            postInvalidateOnAnimation();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            private boolean mCancelled;

            @Override
            public void onAnimationCancel(Animator animation) {
                mCancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mRipples.remove(ripple);
                invalidate();
            }
        });
        ripple.animator = animator;
        mRipples.add(ripple);
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mRipples.isEmpty()) {
            return;
        }
        float maxSize = getWidth() * MAX_SIZE_FACTOR;
        for (int i = 0; i < mRipples.size(); i++) {
            Ripple ripple = mRipples.get(i);
            float raw = ripple.progress;
            if (raw <= 0f) {
                continue;
            }
            // STANDARD easing on the size (RippleShader.progress = STANDARD(raw)).
            float eased = mStandardInterpolator.getInterpolation(raw);
            float radius = eased * maxSize / 2f;
            if (radius <= 0f) {
                continue;
            }

            // RippleShader softens every edge with soften() = smoothstep(-blur/2, blur/2, d)
            // where d is the circle SDF normalized by the radius and blur = lerp(1.25, 0.5,
            // eased) — so the soft half-width in pixels is blur * radius / 2: wide and
            // diffuse at the start, sharpening as the ripple expands.
            float blur = 1.25f - 0.75f * eased;
            float edge = blur * radius * 0.5f;

            // Ring: fades in 0..0.1 and out 0.3..1 (baseRingFadeParams).
            float ringFade = Math.min(
                    subProgress(0f, BASE_RING_FADE_IN_END, raw),
                    1f - subProgress(BASE_RING_FADE_OUT_START, 1f, raw));
            // Center fill: a filled disc at 1.25x the ring radius, gone almost
            // immediately (centerFillFadeParams fadeInEnd=0, fadeOutStart=0).
            float fillFade = 1f - subProgress(0f, CENTER_FILL_FADE_OUT_START, raw);
            if (fillFade > 0f && fillFade < 1f) {
                // fadeInEnd == fadeOutStart == 0: subProgress(0,0)=step; emulate the
                // shader's single-frame flash with a fast 6% decay.
                fillFade = Math.max(0f, 1f - raw / 0.06f);
            }

            // Stock ring geometry (SdfShaderLibrary.circleRing): a band between radius and
            // radius * 1.25 — the same outer edge as the center fill disc.
            float outer = radius * 1.25f;
            float gradientRadius = outer + edge;

            if (ringFade > 0f) {
                // Emulate the shader's smoothstep edges with a radial gradient: transparent
                // up to radius-edge, ramping to full over the soft edge, flat across the
                // band, then ramping back to transparent at outer+edge.
                int ringColor = ColorUtils.setAlphaComponent(mColor,
                        Math.round(RIPPLE_OPACITY * ringFade));
                int transparent = ColorUtils.setAlphaComponent(mColor, 0);
                float inner0 = clamp01((radius - edge) / gradientRadius);
                float inner1 = Math.max(inner0, clamp01((radius + edge) / gradientRadius));
                float outer0 = Math.max(inner1, clamp01((outer - edge) / gradientRadius));
                RadialGradient gradient = new RadialGradient(ripple.x, ripple.y,
                        gradientRadius,
                        new int[]{transparent, transparent, ringColor, ringColor, transparent},
                        new float[]{0f, inner0, inner1, outer0, 1f},
                        Shader.TileMode.CLAMP);
                mPaint.setShader(gradient);
                canvas.drawCircle(ripple.x, ripple.y, gradientRadius, mPaint);
            }

            if (fillFade > 0f) {
                int fillAlpha = Math.round(RIPPLE_OPACITY * fillFade);
                int fillColor = ColorUtils.setAlphaComponent(mColor, fillAlpha);
                int transparent = ColorUtils.setAlphaComponent(mColor, 0);
                float outer0 = clamp01((outer - edge) / gradientRadius);
                RadialGradient gradient = new RadialGradient(ripple.x, ripple.y,
                        gradientRadius,
                        new int[]{fillColor, fillColor, transparent},
                        new float[]{0f, outer0, 1f},
                        Shader.TileMode.CLAMP);
                mPaint.setShader(gradient);
                canvas.drawCircle(ripple.x, ripple.y, gradientRadius, mPaint);
            }
        }
        mPaint.setShader(null);
    }

    private static float clamp01(float value) {
        return Math.min(Math.max(value, 0f), 1f);
    }

    private static float subProgress(float start, float end, float progress) {
        if (start == end) {
            return progress > start ? 1f : 0f;
        }
        float sub = Math.min(Math.max(progress, Math.min(start, end)), Math.max(start, end));
        return (sub - start) / (end - start);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        for (int i = 0; i < mRipples.size(); i++) {
            if (mRipples.get(i).animator != null) {
                mRipples.get(i).animator.cancel();
            }
        }
        mRipples.clear();
    }
}
