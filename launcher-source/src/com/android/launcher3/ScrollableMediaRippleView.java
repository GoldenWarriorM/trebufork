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

    private static final class Ripple {
        float x;
        float y;
        float progress;
        Animator animator;
    }

    private final List<Ripple> mRipples = new ArrayList<>();
    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int mColor = 0xFFFFFFFF;
    private Interpolator mInterpolator;

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
        Ripple ripple = new Ripple();
        ripple.x = x;
        ripple.y = y;
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(RIPPLE_DURATION_MS);
        animator.setInterpolator(getInterpolator());
        animator.addUpdateListener(animation -> {
            ripple.progress = (float) animation.getAnimatedValue();
            invalidate();
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
            float progress = ripple.progress;
            if (progress <= 0f) {
                continue;
            }
            float radius = progress * maxSize;
            // The ripple fades out as it expands; alpha ramps up only in the first
            // frames so the circle grows from nothing under the finger.
            float fade = progress < 0.1f ? progress / 0.1f : (1f - progress) / 0.9f;
            int alpha = Math.round(RIPPLE_OPACITY * Math.max(0f, fade));
            mPaint.setColor(ColorUtils.setAlphaComponent(mColor, alpha));
            canvas.drawCircle(ripple.x, ripple.y, radius, mPaint);
        }
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

    /** The linear_out_slow_in curve, like the rest of the shade player's animations. */
    private Interpolator getInterpolator() {
        if (mInterpolator == null) {
            mInterpolator = new android.view.animation.PathInterpolator(0.2f, 0f, 0f, 1f);
        }
        return mInterpolator;
    }
}
