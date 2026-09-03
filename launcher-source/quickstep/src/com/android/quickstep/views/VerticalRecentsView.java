/*
 * Copyright (C) 2026 The Trebufork Project
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
package com.android.quickstep.views;

import static com.android.launcher3.Flags.enableRefactorTaskThumbnail;
import static com.android.launcher3.LauncherState.OVERVIEW;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.OverScroller;

import androidx.annotation.Nullable;

import com.android.launcher3.DeviceProfile;
import com.android.launcher3.LauncherPrefs;
import com.android.launcher3.LauncherState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Fork of the tablet-grid recents design ("Vertical recents grid" pref, phones only): the cards
 * keep the stock grid rendering (phone-fitted grid cell sizes, real thumbnails via the same
 * visible-task reporting the stock pipeline uses) but are arranged into two vertical columns that
 * scroll smoothly up/down with drag + fling. The most recent task sits in the top-left cell.
 *
 * <p>The vertical mode only engages when the pref is enabled <b>and</b> the device is a phone
 * <b>and</b> the overview is showing; in every other case (tablets, pref off, other states) this
 * class behaves exactly like {@link LauncherRecentsView} — the stock tablet grid design is never
 * modified.
 */
public class VerticalRecentsView extends LauncherRecentsView {

    private static final int VERTICAL_COLUMNS = 2;
    private static final float MARGIN_SIDE_DP = 22f;
    private static final float MARGIN_TOP_DP = 26f;
    private static final float MARGIN_BOTTOM_DP = 52f;
    private static final float COL_GAP_DP = 12f;
    private static final float ROW_GAP_DP = 14f;

    // Whether the vertical mode may run for the current session (phone + pref).
    private boolean mVerticalEnabled = false;
    // Whether the vertical layout is currently engaged (overview settled).
    private boolean mVerticalActive = false;
    // Vertical scroll offset of the sheet, in pixels (>= 0).
    private float mScrollY = 0f;
    // Touch state of the vertical scroll engine.
    private float mTouchDownY;
    private boolean mTouchScrolling;
    @Nullable
    private TaskView mTouchCandidate;
    @Nullable
    private VelocityTracker mVelocityTracker;
    // Smooth fling for the list-like scroll.
    @Nullable
    private OverScroller mScroller;
    // True right after engaging, until the first arrange has played the slide-up entrance.
    private boolean mEntrancePending = false;
    // True while the entrance animators are still running; while true, layout passes must not
    // touch translationY or they would snap the rise animation.
    private boolean mEntranceActive = false;
    // Number of entrance animators still running.
    private int mEntranceRunning = 0;
    // Cached cell size; -1 forces a full re-measure when the grid engages or the view resizes.
    private int mCachedCellW = -1;
    private int mCachedCardH = -1;

    public VerticalRecentsView(Context context) {
        this(context, null);
    }

    public VerticalRecentsView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerticalRecentsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onStateTransitionStart(LauncherState toState) {
        super.onStateTransitionStart(toState);
        DeviceProfile dp = mContainer.getDeviceProfile();
        mVerticalEnabled = LauncherPrefs.RECENTS_VERTICAL_GRID.get(getContext())
                && !dp.getDeviceProperties().isTablet();
    }

    @Override
    public void onStateTransitionComplete(LauncherState finalState) {
        super.onStateTransitionComplete(finalState);
        // Engage only in the plain overview; modal/split-select and the entry/exit transitions
        // keep the stock (grid) geometry so all stock animations keep working.
        setVerticalActive(mVerticalEnabled && finalState == OVERVIEW);
    }

    private void setVerticalActive(boolean active) {
        if (mVerticalActive == active) {
            return;
        }
        mVerticalActive = active;
        mScrollY = 0f;
        mTouchScrolling = false;
        mTouchCandidate = null;
        mEntrancePending = active;
        mCachedCellW = -1;
        mCachedCardH = -1;
        if (mScroller != null) {
            mScroller.abortAnimation();
        }
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
        requestLayout();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mVerticalActive) {
            verticalArrange();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // The vertical grid owns every touch; the stock pager must never intercept (it would
        // page horizontally or steal the vertical drag).
        if (mVerticalActive) {
            return false;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mVerticalActive) {
            return handleVerticalTouch(ev);
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public void computeScroll() {
        if (mVerticalActive) {
            if (mScroller != null && !mScroller.isFinished() && mScroller.computeScrollOffset()) {
                mScrollY = mScroller.getCurrY();
                verticalArrange();
                invalidate();
            }
            return;
        }
        super.computeScroll();
    }

    // ------------------------------------------------------------------
    // Vertical two-column layout
    // ------------------------------------------------------------------

    /**
     * Places every task card in a two-column vertical flow (most recent child first, top-left)
     * and scrolls the sheet by {@link #mScrollY}. Card dimensions keep the aspect of the stock
     * phone grid cell and each card is resized through the stock {@link TaskView#updateTaskSize}
     * helper, so the thumbnail content scales to the new box exactly like in the grid design.
     */
    private void verticalArrange() {
        if (!mVerticalActive) {
            return;
        }
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        if (width <= 0 || height <= 0) {
            return;
        }
        float density = getResources().getDisplayMetrics().density;
        int marginSide = Math.round(MARGIN_SIDE_DP * density);
        int marginTop = Math.round(MARGIN_TOP_DP * density);
        int marginBottom = Math.round(MARGIN_BOTTOM_DP * density);
        int gapCol = Math.round(COL_GAP_DP * density);
        int gapRow = Math.round(ROW_GAP_DP * density);

        List<TaskView> tasks = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof TaskView) {
                tasks.add((TaskView) child);
            }
        }
        int taskCount = tasks.size();
        if (taskCount == 0) {
            return;
        }

        // Keep the card proportions of the approved stock phone grid cell.
        Rect gridCell = getLastComputedGridTaskSize();
        float cellRatio = gridCell.width() > 0
                ? gridCell.height() / (float) gridCell.width() : 1.55f;
        int cellW = (width - 2 * marginSide - gapCol * (VERTICAL_COLUMNS - 1)) / VERTICAL_COLUMNS;
        if (cellW <= 0) {
            return;
        }
        int thumbnailPad = mContainer.getDeviceProfile().getOverviewProfile()
                .getTaskThumbnailTopMarginPx();
        int cellH = Math.round(cellW * cellRatio);
        // Total view height of a card: thumbnail area + icon/label strip below it.
        int cardH = cellH + thumbnailPad;
        if (cellH <= 0) {
            return;
        }

        int rows = (taskCount + VERTICAL_COLUMNS - 1) / VERTICAL_COLUMNS;
        int sheetHeight = marginTop + rows * cardH + (rows - 1) * gapRow;
        int maxScroll = Math.max(0, sheetHeight + marginBottom - height);
        mScrollY = Math.max(0f, Math.min(maxScroll, mScrollY));
        int scrollY = Math.round(mScrollY);

        // A stale horizontal scroll from the stock pager would shift the whole sheet; the grid
        // always owns the scroll position.
        if (getScrollX() != 0) {
            setScrollX(0);
        }

        // Only resize the cards when the cell size actually changed: updateTaskSize goes through
        // updateLayoutParams -> requestLayout, so calling it on every layout pass would re-trigger
        // onLayout forever and starve the entrance animation (cards stuck invisible).
        boolean cellChanged = mCachedCellW != cellW || mCachedCardH != cardH;
        if (cellChanged) {
            mCachedCellW = cellW;
            mCachedCardH = cardH;
        }
        Rect ourCell = new Rect(0, 0, cellW, cellH);
        Rect lastTaskSize = getLastComputedTaskSize();
        // Children are added newest -> oldest, so iterating forward puts the most recent task in
        // the top-left cell.
        for (int i = 0; i < taskCount; i++) {
            TaskView taskView = tasks.get(i);
            if (cellChanged) {
                // Reuse the stock grid sizing helper so the thumbnail scales to our cell.
                taskView.updateTaskSize(lastTaskSize, ourCell);
                taskView.forceLayout();
            }
            int col = i % VERTICAL_COLUMNS;
            int row = i / VERTICAL_COLUMNS;
            int left = marginSide + col * (cellW + gapCol);
            int top = marginTop + row * (cardH + gapRow) - scrollY;
            taskView.measure(MeasureSpec.makeMeasureSpec(cellW, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(cardH, MeasureSpec.EXACTLY));
            taskView.layout(left, top, left + cellW, top + cardH);
            // Clear leftover translations from the stock two-row band arrangement. translationY
            // must keep whatever the entrance animator is doing, so only reset it after the
            // entrance has finished.
            taskView.setGridTranslationY(0f);
            taskView.setNonGridTranslationX(0f);
            taskView.setTranslationX(0f);
            if (!mEntranceActive) {
                taskView.setTranslationY(0f);
            }
        }

        reportAllTasksVisible(tasks);
        if (mEntrancePending) {
            mEntrancePending = false;
            playEntranceAnimation(tasks);
        }
    }

    /**
     * The stock pipeline loads and keeps high-res snapshots only for the tasks it considers
     * visible on the pager. In the vertical grid every card is on the sheet, so report all cards
     * as visible — otherwise off-window cards stay as blank/solid placeholders.
     */
    private void reportAllTasksVisible(List<TaskView> tasks) {
        if (enableRefactorTaskThumbnail()) {
            List<Integer> visibleTaskIds = new ArrayList<>();
            Set<Integer> fullyVisibleTaskIds = new HashSet<>();
            for (TaskView taskView : tasks) {
                int[] ids = taskView.getTaskIds();
                if (ids != null) {
                    for (int id : ids) {
                        visibleTaskIds.add(id);
                        fullyVisibleTaskIds.add(id);
                    }
                }
            }
            mRecentsViewModel.updateVisibleTasks(visibleTaskIds);
            mRecentsViewModel.updateTasksFullyVisible(fullyVisibleTaskIds);
        } else {
            for (TaskView taskView : tasks) {
                taskView.setOverlayEnabled(true);
            }
        }
    }

    /**
     * Plays the vertical-grid entrance once the cards have been arranged: each card rises from
     * below its resting spot while fading in. Cards lower in the list start further down and
     * later, and the motion decelerates (fast start, slow finish) for a "pushed from below" feel.
     */
    private void playEntranceAnimation(List<TaskView> tasks) {
        float density = getResources().getDisplayMetrics().density;
        mEntranceActive = true;
        mEntranceRunning = tasks.size();
        int seq = 0;
        for (TaskView taskView : tasks) {
            float rise = density * (90f + Math.min(320f, seq * 70f));
            taskView.animate().cancel();
            taskView.setAlpha(0f);
            taskView.setTranslationY(rise);
            taskView.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(430)
                    .setStartDelay(seq * 45)
                    .setInterpolator(new DecelerateInterpolator(2.2f))
                    .withEndAction(() -> {
                        mEntranceRunning--;
                        if (mEntranceRunning <= 0) {
                            mEntranceActive = false;
                            // Restore a clean state in case a later layout pass touches the card.
                            for (TaskView tv : tasks) {
                                tv.setTranslationY(0f);
                            }
                        }
                    })
                    .start();
            seq++;
        }
    }

    // ------------------------------------------------------------------
    // Vertical scroll + tap-to-launch
    // ------------------------------------------------------------------

    /** Returns the topmost task card under (x, y) in this view's coordinates, if any. */
    @Nullable
    private TaskView findChildAt(float x, float y) {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (child instanceof TaskView && x >= child.getLeft() && x <= child.getRight()
                    && y >= child.getTop() && y <= child.getBottom()) {
                return (TaskView) child;
            }
        }
        return null;
    }

    private boolean handleVerticalTouch(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                mTouchDownY = ev.getY();
                mTouchScrolling = false;
                mTouchCandidate = findChildAt(ev.getX(), ev.getY());
                if (mVelocityTracker == null) {
                    mVelocityTracker = VelocityTracker.obtain();
                } else {
                    mVelocityTracker.clear();
                }
                mVelocityTracker.addMovement(ev);
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                float dy = ev.getY() - mTouchDownY;
                if (!mTouchScrolling && Math.abs(dy) > ViewConfiguration
                        .get(getContext()).getScaledTouchSlop()) {
                    mTouchScrolling = true;
                    mTouchCandidate = null;
                }
                if (mTouchScrolling) {
                    mScrollY -= dy;
                    mTouchDownY = ev.getY();
                    if (mVelocityTracker != null) {
                        mVelocityTracker.addMovement(ev);
                    }
                    verticalArrange();
                    invalidate();
                }
                return true;
            }
            case MotionEvent.ACTION_UP: {
                TaskView candidate = mTouchCandidate;
                boolean wasScrolling = mTouchScrolling;
                float velocityY = 0f;
                if (mVelocityTracker != null) {
                    mVelocityTracker.addMovement(ev);
                    mVelocityTracker.computeCurrentVelocity(1000);
                    velocityY = mVelocityTracker.getYVelocity();
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                mTouchCandidate = null;
                mTouchScrolling = false;
                if (!wasScrolling && candidate != null) {
                    // Stock launch path: TaskView's own click handler launches with animation.
                    candidate.performClick();
                } else if (wasScrolling) {
                    fling(velocityY);
                }
                return true;
            }
            case MotionEvent.ACTION_CANCEL: {
                mTouchCandidate = null;
                mTouchScrolling = false;
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                return true;
            }
            default:
                return true;
        }
    }

    private void fling(float velocityY) {
        if (mScroller == null) {
            mScroller = new OverScroller(getContext());
        } else {
            mScroller.abortAnimation();
        }
        int maxScroll = computeVerticalMaxScroll();
        mScroller.fling(0, Math.round(mScrollY), 0, Math.round(-velocityY),
                0, 0, 0, maxScroll, 0, 0);
        if (!mScroller.isFinished()) {
            invalidate();
        }
    }

    private int computeVerticalMaxScroll() {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        if (width <= 0 || height <= 0) {
            return 0;
        }
        float density = getResources().getDisplayMetrics().density;
        int marginSide = Math.round(MARGIN_SIDE_DP * density);
        int marginTop = Math.round(MARGIN_TOP_DP * density);
        int marginBottom = Math.round(MARGIN_BOTTOM_DP * density);
        int gapCol = Math.round(COL_GAP_DP * density);
        int gapRow = Math.round(ROW_GAP_DP * density);

        int taskCount = 0;
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i) instanceof TaskView) {
                taskCount++;
            }
        }
        if (taskCount == 0) {
            return 0;
        }
        Rect gridCell = getLastComputedGridTaskSize();
        float cellRatio = gridCell.width() > 0
                ? gridCell.height() / (float) gridCell.width() : 1.55f;
        int cellW = (width - 2 * marginSide - gapCol * (VERTICAL_COLUMNS - 1)) / VERTICAL_COLUMNS;
        int thumbnailPad = mContainer.getDeviceProfile().getOverviewProfile()
                .getTaskThumbnailTopMarginPx();
        int cardH = Math.round(cellW * cellRatio) + thumbnailPad;
        int rows = (taskCount + VERTICAL_COLUMNS - 1) / VERTICAL_COLUMNS;
        int sheetHeight = marginTop + rows * cardH + (rows - 1) * gapRow;
        return Math.max(0, sheetHeight + marginBottom - height);
    }
}