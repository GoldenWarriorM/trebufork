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

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.HorizontalScrollView;

/**
 * trebufork: verbatim port of SystemUI's MediaScrollView (media controls carousel) — a
 * HorizontalScrollView that hosts one full player card per media session and doesn't limit
 * its scrolling to its children's bounds. The rounded-corner outline provider clips the
 * sliding cards exactly like the shade carousel (MediaCarouselScrollHandler sets the same
 * outline on it).
 */
public class ScrollableMediaScrollView extends HorizontalScrollView {

    /** Touch hook mirroring SystemUI's Gefingerpoken contract. */
    public interface TouchListener {
        boolean onTouchEvent(MotionEvent ev);

        boolean onInterceptTouchEvent(MotionEvent ev);
    }

    private ViewGroup mContentContainer;
    private TouchListener mTouchListener;
    // trebufork: the row's height scale (user resize) applied on top of the natural height;
    // the cards keep their natural measured size and are clipped by the viewport.
    private float mHeightScale = 1f;
    private int mCornerRadius;

    public ScrollableMediaScrollView(Context context) {
        this(context, null);
    }

    public ScrollableMediaScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollableMediaScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOverScrollMode(View.OVER_SCROLL_NEVER);
        // The player content is LTR (tape direction, like the shade forces the seekbar).
        setLayoutDirection(LAYOUT_DIRECTION_LTR);
        setHorizontalScrollBarEnabled(false);
        mCornerRadius = getResources().getDimensionPixelSize(
                R.dimen.scrollable_media_corner_radius);
        // MediaCarouselScrollHandler.init: the outline clips the sliding pages to the
        // carousel's rounded rect while a partial scroll is in progress.
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, android.graphics.Outline outline) {
                outline.setRoundRect(0, 0, getWidth(), getHeight(), mCornerRadius);
            }
        });
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContentContainer = (ViewGroup) getChildAt(0);
    }

    /** The LinearLayout where the player cards are added. */
    public ViewGroup getContentContainer() {
        return mContentContainer;
    }

    public void setTouchListener(TouchListener listener) {
        mTouchListener = listener;
    }

    /**
     * Converts between the absolute (left-to-right) and relative (start-to-end) scrollX of
     * the carousel (MediaScrollView.transformScrollX). The layout direction is forced LTR,
     * so the transform is the identity — kept for fidelity with the original.
     */
    private int transformScrollX(int scrollX) {
        return isLayoutRtl() ? (mContentContainer == null ? 0 : mContentContainer.getWidth())
                - getWidth() - scrollX : scrollX;
    }

    /** Layout-direction-relative scroll X position of the carousel. */
    public int getRelativeScrollX() {
        return transformScrollX(getScrollX());
    }

    public void setRelativeScrollX(int value) {
        setScrollX(transformScrollX(value));
    }

    /** Allow all scrolls to go through, use base implementation (MediaScrollView.scrollTo). */
    @Override
    public void scrollTo(int x, int y) {
        if (mScrollX != x || mScrollY != y) {
            int oldX = mScrollX;
            int oldY = mScrollY;
            mScrollX = x;
            mScrollY = y;
            invalidateParentCaches();
            onScrollChanged(mScrollX, mScrollY, oldX, oldY);
            if (!awakenScrollBars()) {
                postInvalidateOnAnimation();
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercept = mTouchListener != null && mTouchListener.onInterceptTouchEvent(ev);
        return super.onInterceptTouchEvent(ev) || intercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean touch = mTouchListener != null && mTouchListener.onTouchEvent(ev);
        return super.onTouchEvent(ev) || touch;
    }

    /** Cancel the current touch event going on (MediaScrollView.cancelCurrentScroll). */
    public void cancelCurrentScroll() {
        long now = SystemClock.uptimeMillis();
        MotionEvent event = MotionEvent.obtain(now, now, MotionEvent.ACTION_CANCEL, 0f, 0f, 0);
        event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        super.onTouchEvent(event);
        event.recycle();
    }

    /** Sets the vertical scale applied to the natural measured height (user resize). */
    public void setHeightScale(float heightScale) {
        mHeightScale = heightScale <= 0f ? 1f : heightScale;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mHeightScale != 1f) {
            setMeasuredDimension(getMeasuredWidth(),
                    Math.round(getMeasuredHeight() * mHeightScale));
        }
    }

    /** MediaCarouselScrollHandler.updateClipToOutline: clip only mid-swipe. */
    public void updateClipToOutline(boolean clip) {
        setClipToOutline(clip);
    }
}
