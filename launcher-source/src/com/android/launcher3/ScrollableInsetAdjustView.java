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
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import com.android.launcher3.util.Themes;
import com.android.launcher3.views.BaseDragLayer;

/**
 * trebufork: full-screen overlay for dragging the scrollable-home top inset. A filled oval
 * handle sits on a short rounded line (inset from the screen edges) at the current inset
 * height; dragging it up or down re-sizes the empty strip above the desktop/apps list live and
 * persists it to {@link LauncherPrefs#SCROLLABLE_TOP_INSET}. Reset (top-left) restores the
 * default 20%, Done (top-right) closes the overlay — both in the same pill style as the
 * reorder-mode Done button. The handle position is remembered: it always re-opens at the
 * currently stored inset.
 */
public class ScrollableInsetAdjustView extends AbstractFloatingView {

    // trebufork: default top inset percent (matches LauncherPrefs.SCROLLABLE_TOP_INSET).
    private static final float DEFAULT_INSET_PERCENT = 20f;
    // Clamp the draggable handle between MIN_TOP_INSET_FRACTION and 50% of the screen.
    private static final float MAX_INSET_FRACTION = 0.5f;

    private final Paint mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mHandlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Launcher mLauncher;
    private ScrollableAppsView mAppsView;
    private FrameLayout mButtonBar;
    private TextView mResetButton;
    private TextView mDoneButton;

    // Current inset as a fraction of the screen height. Persisted on every drag; the overlay
    // re-opens at the stored value (the handle never spawns somewhere else).
    private float mInsetFraction;
    // True while a handle drag is in progress.
    private boolean mDragging;
    private float mDownY;
    private float mDownFraction;
    private int mTouchSlop;
    private float mDensity;

    public ScrollableInsetAdjustView(Context context) {
        this(context, null);
    }

    public ScrollableInsetAdjustView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollableInsetAdjustView(Context context, @Nullable AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        mDensity = getResources().getDisplayMetrics().density;
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        int accent = Themes.getAttrColor(context, android.R.attr.colorAccent);
        mLinePaint.setColor(accent);
        mLinePaint.setStrokeWidth(3 * mDensity);
        mLinePaint.setStrokeCap(Paint.Cap.ROUND);
        mHandlePaint.setColor(accent);
        mHandlePaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mButtonBar = findViewById(R.id.scroll_inset_button_bar);
        mResetButton = findViewById(R.id.scroll_inset_reset);
        mDoneButton = findViewById(R.id.scroll_inset_done);
        int accent = Themes.getAttrColor(getContext(), android.R.attr.colorAccent);
        int textColor = ColorUtils.calculateLuminance(accent) > 0.5f
                ? android.graphics.Color.BLACK : android.graphics.Color.WHITE;
        if (mResetButton != null) {
            mResetButton.setOnClickListener(v -> resetToDefault());
            mResetButton.setTextColor(textColor);
        }
        if (mDoneButton != null) {
            mDoneButton.setOnClickListener(v -> close(true));
            mDoneButton.setTextColor(textColor);
        }
    }

    /**
     * Shows the inset-adjustment overlay for the given scrollable home. Closes any
     * previously open overlay.
     */
    public static ScrollableInsetAdjustView show(ScrollableAppsView appsView) {
        Launcher launcher = Launcher.getLauncher(appsView.getContext());
        closeOpenOverlay(launcher);
        ScrollableInsetAdjustView view = (ScrollableInsetAdjustView) LayoutInflater
                .from(launcher)
                .inflate(R.layout.scrollable_inset_adjust, launcher.getDragLayer(), false);
        view.mLauncher = launcher;
        view.mAppsView = appsView;
        view.mInsetFraction = view.readInsetFraction();
        launcher.getDragLayer().addView(view);
        BaseDragLayer.LayoutParams lp =
                (BaseDragLayer.LayoutParams) view.getLayoutParams();
        lp.customPosition = true;
        lp.x = 0;
        lp.y = 0;
        lp.width = launcher.getDragLayer().getWidth();
        lp.height = launcher.getDragLayer().getHeight();
        view.mIsOpen = true;
        view.requestLayout();
        // trebufork: gently fade the overlay in instead of popping it up.
        view.setAlpha(0f);
        view.animate().alpha(1f).setDuration(160L)
                .setInterpolator(new DecelerateInterpolator()).start();
        // trebufork: the buttons must sit below the status bar, like the reorder-mode Done
        // button does (DragLayer applies the system top inset to its XML children; views added
        // at runtime with customPosition do not get that treatment, so apply it here).
        if (view.mButtonBar != null) {
            MarginLayoutParams mlp = (MarginLayoutParams) view.mButtonBar.getLayoutParams();
            mlp.topMargin = launcher.getDragLayer().getInsets().top
                    + Math.round(16 * view.mDensity);
            view.mButtonBar.setLayoutParams(mlp);
        }
        view.invalidate();
        return view;
    }

    /** Closes the inset-adjustment overlay if one is open. */
    public static void closeOpenOverlay(Launcher launcher) {
        for (int i = launcher.getDragLayer().getChildCount() - 1; i >= 0; i--) {
            View child = launcher.getDragLayer().getChildAt(i);
            if (child instanceof ScrollableInsetAdjustView) {
                ((ScrollableInsetAdjustView) child).close(false);
            }
        }
    }

    /** Restores the default 20% inset and re-positions the handle. */
    private void resetToDefault() {
        applyInsetPercent(DEFAULT_INSET_PERCENT);
        invalidate();
    }

    /** Reads the persisted inset (percent) as a 0..1 fraction, clamped to the allowed range. */
    private float readInsetFraction() {
        float percent = LauncherPrefs.getPrefs(getContext()).getFloat(
                LauncherPrefs.SCROLLABLE_TOP_INSET.getSharedPrefKey(), DEFAULT_INSET_PERCENT);
        return Math.max(ScrollableAppsView.MIN_TOP_INSET_FRACTION,
                Math.min(MAX_INSET_FRACTION, percent / 100f));
    }

    /** Current persisted inset percent. */
    private float getCurrentInsetPercent() {
        return LauncherPrefs.getPrefs(getContext()).getFloat(
                LauncherPrefs.SCROLLABLE_TOP_INSET.getSharedPrefKey(), DEFAULT_INSET_PERCENT);
    }

    /**
     * Persists the inset percent (5..50) and re-applies the header sizes so the list
     * reflects the new inset immediately.
     */
    private void applyInsetPercent(float percent) {
        mInsetFraction = Math.max(ScrollableAppsView.MIN_TOP_INSET_FRACTION,
                Math.min(MAX_INSET_FRACTION, percent / 100f));
        SharedPreferences prefs = LauncherPrefs.getPrefs(getContext());
        prefs.edit().putFloat(LauncherPrefs.SCROLLABLE_TOP_INSET.getSharedPrefKey(),
                mInsetFraction * 100f).apply();
        if (mAppsView != null) {
            mAppsView.applyTopInset();
        }
    }

    @Override
    protected void handleClose(boolean animate) {
        if (!animate) {
            removeFromDragLayer();
            return;
        }
        // trebufork: fade the whole overlay out instead of popping it away.
        animate().cancel();
        animate().alpha(0f)
                .setDuration(160L)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(this::removeFromDragLayer)
                .start();
    }

    private void removeFromDragLayer() {
        if (getParent() != null) {
            ((ViewGroup) getParent()).removeView(this);
        }
    }

    @Override
    protected boolean isOfType(@FloatingViewType int type) {
        return (type & TYPE_INSET_ADJUST) != 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // Fill the drag layer so the handle line spans the full screen width.
        if (mLauncher != null && mLauncher.getDragLayer() != null) {
            setMeasuredDimension(mLauncher.getDragLayer().getWidth(),
                    mLauncher.getDragLayer().getHeight());
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        float minY = ScrollableAppsView.MIN_TOP_INSET_FRACTION * getHeight();
        float maxY = MAX_INSET_FRACTION * getHeight();
        float handleY = Math.max(minY, Math.min(maxY, mInsetFraction * getHeight()));

        // trebufork: a short rounded line, inset from the screen edges so the handle does not
        // run off the sides. Rounded caps keep both ends smooth.
        float lineMargin = 24 * mDensity;
        float lineLeft = lineMargin;
        float lineRight = getWidth() - lineMargin;
        canvas.drawLine(lineLeft, handleY, lineRight, handleY, mLinePaint);

        // trebufork: the grab handle itself — a filled pill with fully rounded ends, styled
        // like the reorder-mode Done button (accent fill, rounded corners).
        float handleWidth = 60 * mDensity;
        float handleHeight = 28 * mDensity;
        float left = (getWidth() - handleWidth) / 2f;
        float top = handleY - handleHeight / 2f;
        canvas.drawRoundRect(left, top, left + handleWidth, top + handleHeight,
                handleHeight / 2f, handleHeight / 2f, mHandlePaint);
    }

    @Override
    public boolean onControllerInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() != MotionEvent.ACTION_DOWN) {
            return mDragging;
        }
        // Handle-region tap: anywhere along the width, within touch slop of the line.
        float handleY = getHandleY();
        if (Math.abs(ev.getY() - handleY) <= 2f * mTouchSlop
                || Math.abs(ev.getY() - handleY) <= 24 * mDensity) {
            mDragging = true;
            mDownY = ev.getY();
            mDownFraction = mInsetFraction;
            return true;
        }
        return false;
    }

    @Override
    public boolean onControllerTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                return onControllerInterceptTouchEvent(ev);
            case MotionEvent.ACTION_MOVE:
                if (!mDragging) {
                    return false;
                }
                float fraction = getHeight() <= 0 ? mInsetFraction
                        : Math.max(ScrollableAppsView.MIN_TOP_INSET_FRACTION,
                                Math.min(MAX_INSET_FRACTION, ev.getY() / getHeight()));
                applyInsetPercent(fraction * 100f);
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mDragging = false;
                // Pin the final value.
                applyInsetPercent(mInsetFraction * 100f);
                invalidate();
                return false;
            default:
                return mDragging;
        }
    }

    private float getHandleY() {
        float minY = ScrollableAppsView.MIN_TOP_INSET_FRACTION * getHeight();
        float maxY = MAX_INSET_FRACTION * getHeight();
        return Math.max(minY, Math.min(maxY, mInsetFraction * getHeight()));
    }
}
