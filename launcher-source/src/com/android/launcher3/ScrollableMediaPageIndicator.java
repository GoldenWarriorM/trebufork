/*
 * Copyright (C) 2020 The Android Open Source Project
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

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * trebufork: verbatim port of SystemUI's com.android.systemui.qs.PageIndicator — the animated
 * page dots shown under the media carousel. Each page is a 16dp cell holding a 6.4dp dot;
 * the current page's dot is "major" (opaque), the others "minor" (42% alpha). Moving between
 * adjacent pages plays the minor/major AVD cross-morph (major_a_b/major_b_c/... animation
 * drawables); positions further apart jump without an animation. Fractional locations
 * (during a swipe) map to the intermediate A/B/C states exactly like the original.
 */
public class ScrollableMediaPageIndicator extends ViewGroup {

    private static final float MINOR_ALPHA = .42f;

    private final ArrayList<Integer> mQueuedPositions = new ArrayList<>();

    private int mPageIndicatorWidth;
    private int mPageIndicatorHeight;
    private int mPageDotWidth;
    private ColorStateList mTint = ColorStateList.valueOf(0xFFFFFFFF);

    private int mPosition = -1;
    private boolean mAnimating;

    private final Animatable2.AnimationCallback mAnimationCallback =
            new Animatable2.AnimationCallback() {
                @Override
                public void onAnimationEnd(Drawable drawable) {
                    if (drawable instanceof AnimatedVectorDrawable) {
                        ((AnimatedVectorDrawable) drawable).unregisterAnimationCallback(
                                mAnimationCallback);
                    }
                    mAnimating = false;
                    if (mQueuedPositions.size() != 0) {
                        setPosition(mQueuedPositions.remove(0));
                    }
                }
            };

    public ScrollableMediaPageIndicator(Context context) {
        this(context, null);
    }

    public ScrollableMediaPageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources res = context.getResources();
        mPageIndicatorWidth =
                res.getDimensionPixelSize(R.dimen.scrollable_media_page_indicator_width);
        mPageIndicatorHeight =
                res.getDimensionPixelSize(R.dimen.scrollable_media_page_indicator_height);
        mPageDotWidth =
                res.getDimensionPixelSize(R.dimen.scrollable_media_page_indicator_dot_width);
    }

    public void setNumPages(int numPages) {
        setVisibility(numPages > 1 ? View.VISIBLE : View.GONE);
        int childCount = getChildCount();
        if (numPages == childCount) {
            return;
        }
        while (numPages < getChildCount()) {
            removeViewAt(getChildCount() - 1);
        }
        while (numPages > getChildCount()) {
            ImageView v = new ImageView(mContext);
            v.setImageResource(R.drawable.scrollable_media_page_minor_a_b);
            v.setImageTintList(mTint);
            addView(v, new LayoutParams(mPageIndicatorWidth, mPageIndicatorHeight));
        }
        // Refresh state.
        setIndex(mPosition >> 1);
        requestLayout();
    }

    /** Sets the dot color (the Monet scheme's paging indicator). */
    public void setTintList(ColorStateList color) {
        if (color == null || color.equals(mTint)) {
            return;
        }
        mTint = color;
        final int n = getChildCount();
        for (int i = 0; i < n; i++) {
            View v = getChildAt(i);
            if (v instanceof ImageView) {
                ((ImageView) v).setImageTintList(mTint);
            }
        }
    }

    /**
     * Location is the page index plus a fractional part for the in-between swipe state,
     * e.g. 1.5 is halfway between page 1 and 2 (SystemUI's PageIndicator.setLocation).
     */
    public void setLocation(float location) {
        int index = (int) location;
        int position = index << 1 | ((location != index) ? 1 : 0);

        int lastPosition = mPosition;
        if (mQueuedPositions.size() != 0) {
            lastPosition = mQueuedPositions.get(mQueuedPositions.size() - 1);
        }
        if (position == lastPosition) {
            return;
        }
        if (mAnimating) {
            mQueuedPositions.add(position);
            return;
        }
        setPosition(position);
    }

    private void setPosition(int position) {
        if (isShown() && Math.abs(mPosition - position) == 1) {
            animate(mPosition, position);
        } else {
            setIndex(position >> 1);
        }
        mPosition = position;
    }

    private void setIndex(int index) {
        final int n = getChildCount();
        for (int i = 0; i < n; i++) {
            ImageView v = (ImageView) getChildAt(i);
            // Clear out any animation positioning.
            v.setTranslationX(0);
            v.setImageResource(R.drawable.scrollable_media_page_major_a_b);
            v.setAlpha(getAlpha(i == index));
        }
    }

    private void animate(int from, int to) {
        int fromIndex = from >> 1;
        int toIndex = to >> 1;

        // Set the position of everything, then manually control the two views involved.
        setIndex(fromIndex);

        boolean fromTransition = (from & 1) != 0;
        boolean isAState = fromTransition ? from > to : from < to;
        int firstIndex = Math.min(fromIndex, toIndex);
        int secondIndex = Math.max(fromIndex, toIndex);
        if (secondIndex == firstIndex) {
            secondIndex++;
        }
        ImageView first = (ImageView) getChildAt(firstIndex);
        ImageView second = (ImageView) getChildAt(secondIndex);
        if (first == null || second == null) {
            return;
        }
        // Lay the two views on top of each other.
        second.setTranslationX(first.getX() - second.getX());

        playAnimation(first, getTransition(fromTransition, isAState, false));
        first.setAlpha(getAlpha(false));

        playAnimation(second, getTransition(fromTransition, isAState, true));
        second.setAlpha(getAlpha(true));

        mAnimating = true;
    }

    private float getAlpha(boolean isMajor) {
        return isMajor ? 1 : MINOR_ALPHA;
    }

    private void playAnimation(ImageView imageView, int res) {
        final AnimatedVectorDrawable avd =
                (AnimatedVectorDrawable) getContext().getDrawable(res);
        imageView.setImageDrawable(avd);
        avd.forceAnimationOnUI();
        avd.registerAnimationCallback(mAnimationCallback);
        avd.start();
    }

    private int getTransition(boolean fromB, boolean isMajorAState, boolean isMajor) {
        if (isMajor) {
            if (fromB) {
                if (isMajorAState) {
                    return R.drawable.scrollable_media_page_major_b_a_animation;
                } else {
                    return R.drawable.scrollable_media_page_major_b_c_animation;
                }
            } else {
                if (isMajorAState) {
                    return R.drawable.scrollable_media_page_major_a_b_animation;
                } else {
                    return R.drawable.scrollable_media_page_major_c_b_animation;
                }
            }
        } else {
            if (fromB) {
                if (isMajorAState) {
                    return R.drawable.scrollable_media_page_minor_b_c_animation;
                } else {
                    return R.drawable.scrollable_media_page_minor_b_a_animation;
                }
            } else {
                if (isMajorAState) {
                    return R.drawable.scrollable_media_page_minor_a_b_animation;
                } else {
                    return R.drawable.scrollable_media_page_minor_c_b_animation;
                }
            }
        }
    }

    private int calculateWidth(int numPages) {
        return (mPageIndicatorWidth - mPageDotWidth) * (numPages - 1) + mPageDotWidth;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int n = getChildCount();
        if (n == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        final int widthChildSpec =
                MeasureSpec.makeMeasureSpec(mPageIndicatorWidth, MeasureSpec.EXACTLY);
        final int heightChildSpec =
                MeasureSpec.makeMeasureSpec(mPageIndicatorHeight, MeasureSpec.EXACTLY);
        for (int i = 0; i < n; i++) {
            getChildAt(i).measure(widthChildSpec, heightChildSpec);
        }
        int width = calculateWidth(n);
        setMeasuredDimension(width, mPageIndicatorHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int n = getChildCount();
        if (n == 0) {
            return;
        }
        for (int i = 0; i < n; i++) {
            int left = (mPageIndicatorWidth - mPageDotWidth) * i;
            getChildAt(i).layout(left, 0, mPageIndicatorWidth + left, mPageIndicatorHeight);
        }
    }
}
