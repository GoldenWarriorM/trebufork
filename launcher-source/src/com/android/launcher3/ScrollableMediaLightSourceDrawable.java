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
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

import com.android.internal.graphics.ColorUtils;

import org.xmlpull.v1.XmlPullParser;

/**
 * trebufork: Java port of SystemUI's LightSourceDrawable (the "Illumination" glow) — the
 * press effect of the shade/lockscreen media player buttons. While the finger is down a
 * small highlight sits under it; on release the highlight expands to {@code rippleMaxSize}
 * while fading out, exactly like the shade player. Tinted with the scheme highlight color
 * through {@link #setHighlightColor(int)}.
 */
public class ScrollableMediaLightSourceDrawable extends Drawable {

    private static final long RIPPLE_ANIM_DURATION = 800L;
    private static final float RIPPLE_IDLE_PROGRESS = 0f;
    private static final float RIPPLE_DOWN_START_PROGRESS = 0.05f;
    private static final long RIPPLE_CANCEL_DURATION = 200L;
    private static final float[] GRADIENT_STOPS = {0.2f, 1f};

    private boolean mPressed = false;
    private int[] mThemeAttrs = null;
    private final RippleData mRippleData = new RippleData();
    private final Paint mPaint = new Paint();

    private int mHighlightColor = Color.WHITE;

    private float mMinSize;
    private float mMaxSize;

    private Animator mRippleAnimation;
    private boolean mActive;

    private static final class RippleData {
        float x;
        float y;
        float alpha;
        float progress;
        float minSize;
        float maxSize;
    }

    public ScrollableMediaLightSourceDrawable() {
    }

    /** Color of the glow; SystemUI sets the scheme's media player highlight color here. */
    public void setHighlightColor(int color) {
        if (mHighlightColor == color) {
            return;
        }
        mHighlightColor = color;
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        float radius = lerp(mRippleData.minSize, mRippleData.maxSize, mRippleData.progress);
        if (radius <= 0f || mRippleData.alpha <= 0f) {
            return;
        }
        int centerColor = ColorUtils.setAlphaComponent(
                mHighlightColor, (int) (mRippleData.alpha * 255f));
        mPaint.setShader(new RadialGradient(
                mRippleData.x, mRippleData.y, radius,
                new int[] {centerColor, Color.TRANSPARENT},
                GRADIENT_STOPS, Shader.TileMode.CLAMP));
        canvas.drawCircle(mRippleData.x, mRippleData.y, radius, mPaint);
    }

    @Override
    public void getOutline(Outline outline) {
        // No bounds: the parent view clips the glow, same as SystemUI.
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }

    @Override
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs,
            Resources.Theme theme) {
        final TypedArray a = obtainAttributes(r, theme, attrs,
                R.styleable.ScrollableMediaLightSource);
        mThemeAttrs = a.extractThemeAttrs();
        updateStateFromTypedArray(a);
        a.recycle();
    }

    private void updateStateFromTypedArray(TypedArray a) {
        if (a.hasValue(R.styleable.ScrollableMediaLightSource_rippleMinSize)) {
            mRippleData.minSize =
                    a.getDimension(R.styleable.ScrollableMediaLightSource_rippleMinSize, 0f);
            mMinSize = mRippleData.minSize;
        }
        if (a.hasValue(R.styleable.ScrollableMediaLightSource_rippleMaxSize)) {
            mRippleData.maxSize =
                    a.getDimension(R.styleable.ScrollableMediaLightSource_rippleMaxSize, 0f);
            mMaxSize = mRippleData.maxSize;
        }
    }

    @Override
    public boolean canApplyTheme() {
        return (mThemeAttrs != null && mThemeAttrs.length > 0) || super.canApplyTheme();
    }

    @Override
    public void applyTheme(Resources.Theme t) {
        super.applyTheme(t);
        if (mThemeAttrs != null) {
            TypedArray a = t.resolveAttributes(mThemeAttrs,
                    R.styleable.ScrollableMediaLightSource);
            updateStateFromTypedArray(a);
            a.recycle();
        }
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        throw new UnsupportedOperationException("Color filters are not supported");
    }

    @Override
    public void setAlpha(int alpha) {
        if (alpha == mPaint.getAlpha()) {
            return;
        }
        mPaint.setAlpha(alpha);
        invalidateSelf();
    }

    /** Draws an animated ripple that expands while fading away (finger released). */
    private void illuminate() {
        mRippleData.alpha = 1f;
        invalidateSelf();

        if (mRippleAnimation != null) {
            mRippleAnimation.cancel();
        }
        AnimatorSet set = new AnimatorSet();
        ValueAnimator fade = ValueAnimator.ofFloat(1f, 0f);
        fade.setStartDelay(133);
        fade.setDuration(RIPPLE_ANIM_DURATION - 133);
        fade.setInterpolator(getLinearOutSlowIn());
        fade.addUpdateListener(animation -> {
            mRippleData.alpha = (float) animation.getAnimatedValue();
            invalidateSelf();
        });
        ValueAnimator grow = ValueAnimator.ofFloat(mRippleData.progress, 1f);
        grow.setDuration(RIPPLE_ANIM_DURATION);
        grow.setInterpolator(getLinearOutSlowIn());
        grow.addUpdateListener(animation -> {
            mRippleData.progress = (float) animation.getAnimatedValue();
            invalidateSelf();
        });
        set.playTogether(fade, grow);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mRippleData.progress = 0f;
                mRippleAnimation = null;
                invalidateSelf();
            }
        });
        mRippleAnimation = set;
        set.start();
    }

    private Interpolator mLinearOutSlowIn;

    /** The linear_out_slow_in curve (0.2, 0, 0, 1), same as SystemUI's Interpolators. */
    private Interpolator getLinearOutSlowIn() {
        if (mLinearOutSlowIn == null) {
            mLinearOutSlowIn = new android.view.animation.PathInterpolator(0.2f, 0f, 0f, 1f);
        }
        return mLinearOutSlowIn;
    }

    @Override
    public void setHotspot(float x, float y) {
        mRippleData.x = x;
        mRippleData.y = y;
        if (mActive) {
            invalidateSelf();
        }
    }

    @Override
    public boolean isStateful() {
        return true;
    }

    @Override
    public boolean hasFocusStateSpecified() {
        return true;
    }

    @Override
    public boolean isProjected() {
        return true;
    }

    @Override
    public Rect getDirtyBounds() {
        float radius = lerp(mRippleData.minSize, mRippleData.maxSize, mRippleData.progress);
        Rect bounds = new Rect(
                (int) (mRippleData.x - radius),
                (int) (mRippleData.y - radius),
                (int) (mRippleData.x + radius),
                (int) (mRippleData.y + radius));
        bounds.union(super.getDirtyBounds());
        return bounds;
    }

    @Override
    protected boolean onStateChange(int[] stateSet) {
        boolean changed = super.onStateChange(stateSet);

        boolean wasPressed = mPressed;
        boolean enabled = false;
        mPressed = false;
        boolean focused = false;
        boolean hovered = false;

        for (int state : stateSet) {
            if (state == android.R.attr.state_enabled) {
                enabled = true;
            } else if (state == android.R.attr.state_focused) {
                focused = true;
            } else if (state == android.R.attr.state_pressed) {
                mPressed = true;
            } else if (state == android.R.attr.state_hovered) {
                hovered = true;
            }
        }

        setActive(enabled && (mPressed || focused || hovered));
        if (wasPressed && !mPressed) {
            illuminate();
        }
        return changed;
    }

    /** Draws a small highlight under the finger before expanding (or cancelling) it. */
    private void setActive(boolean value) {
        if (value == mActive) {
            return;
        }
        mActive = value;
        if (mRippleAnimation != null) {
            mRippleAnimation.cancel();
            mRippleAnimation = null;
        }
        if (value) {
            mRippleData.alpha = 1f;
            mRippleData.progress = RIPPLE_DOWN_START_PROGRESS;
        } else {
            ValueAnimator cancel = ValueAnimator.ofFloat(mRippleData.alpha, 0f);
            cancel.setDuration(RIPPLE_CANCEL_DURATION);
            cancel.setInterpolator(getLinearOutSlowIn());
            cancel.addUpdateListener(animation -> {
                mRippleData.alpha = (float) animation.getAnimatedValue();
                invalidateSelf();
            });
            cancel.addListener(new AnimatorListenerAdapter() {
                private boolean mCancelled;

                @Override
                public void onAnimationCancel(Animator animation) {
                    mCancelled = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mCancelled) {
                        return;
                    }
                    mRippleData.progress = RIPPLE_IDLE_PROGRESS;
                    mRippleData.alpha = 0f;
                    mRippleAnimation = null;
                    invalidateSelf();
                }
            });
            mRippleAnimation = cancel;
            cancel.start();
        }
        invalidateSelf();
    }

    private static float lerp(float start, float stop, float amount) {
        return start + (stop - start) * amount;
    }
}
