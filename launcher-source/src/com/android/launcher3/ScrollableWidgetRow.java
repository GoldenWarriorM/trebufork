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
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.Nullable;

/**
 * trebufork: container for a widget on the scrollable desktop. The row always spans the full
 * list width; the widget child is sized from the user-configurable width/height scales and its
 * natural aspect ratio, and is laid out at a persisted horizontal position (see
 * {@link #setPositionX(float)}), so a widget that does not fill the row can be moved left/right.
 */
public class ScrollableWidgetRow extends FrameLayout {

    /** Height / width ratio of the widget (from provider min sizes). */
    private float mAspectRatio = 1f;
    /** Width relative to the full list width (1f = full width). */
    private float mWidthScale = 1f;
    /** Height multiplier applied on top of the natural aspect-ratio height (1f = natural). */
    private float mHeightScale = 1f;
    /** Horizontal position as a fraction of the free space (0 = left, 1 = right). */
    private float mPositionX = 0f;

    private ImageView mDragHandle;

    public ScrollableWidgetRow(Context context) {
        this(context, null);
    }

    public ScrollableWidgetRow(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollableWidgetRow(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mDragHandle = findViewById(R.id.scroll_widget_drag_handle);
    }

    /** The widget host view child, or null when not yet bound. */
    @Nullable
    public View getWidgetView() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child != mDragHandle) {
                return child;
            }
        }
        return null;
    }

    public void setAspectRatio(float aspectRatio) {
        if (aspectRatio <= 0f) {
            aspectRatio = 1f;
        }
        if (mAspectRatio != aspectRatio) {
            mAspectRatio = aspectRatio;
            requestLayout();
        }
    }

    /**
     * trebufork: sets the widget size. {@code widthScale} is relative to the full list width
     * (1f = full width), {@code heightScale} multiplies the natural aspect-ratio height.
     */
    public void setScales(float widthScale, float heightScale) {
        if (widthScale <= 0f) {
            widthScale = 1f;
        }
        if (heightScale <= 0f) {
            heightScale = 1f;
        }
        if (mWidthScale != widthScale || mHeightScale != heightScale) {
            mWidthScale = widthScale;
            mHeightScale = heightScale;
            requestLayout();
        }
    }

    /** trebufork: sets the horizontal position (0..1 of the free space) and re-lays out. */
    public void setPositionX(float positionX) {
        positionX = Math.max(0f, Math.min(1f, positionX));
        if (mPositionX != positionX) {
            mPositionX = positionX;
            requestLayout();
        }
    }

    public float getPositionX() {
        return mPositionX;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        View widget = getWidgetView();
        if (widget == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        int widgetWidth = Math.round(width * mWidthScale);
        int widgetHeight = Math.round(widgetWidth * mAspectRatio * mHeightScale);
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == widget) {
                child.measure(
                        MeasureSpec.makeMeasureSpec(widgetWidth, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(widgetHeight, MeasureSpec.EXACTLY));
            } else if (child == mDragHandle) {
                child.measure(
                        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            }
        }
        setMeasuredDimension(width, widgetHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int rowWidth = r - l;
        int widgetWidth = Math.round(rowWidth * mWidthScale);
        int widgetHeight = Math.round(widgetWidth * mAspectRatio * mHeightScale);
        int freeSpace = Math.max(0, rowWidth - widgetWidth);
        int offsetX = Math.round(freeSpace * mPositionX);

        View widget = getWidgetView();
        if (widget != null) {
            widget.layout(offsetX, 0, offsetX + widgetWidth, widgetHeight);
        }
        if (mDragHandle != null) {
            int hw = mDragHandle.getMeasuredWidth();
            int hh = mDragHandle.getMeasuredHeight();
            mDragHandle.layout(offsetX + widgetWidth - hw, 0, offsetX + widgetWidth, hh);
        }
    }
}
