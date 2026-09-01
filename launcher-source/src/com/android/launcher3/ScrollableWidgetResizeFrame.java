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
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.launcher3.dragndrop.DragLayer;
import com.android.launcher3.popup.PopupContainer;
import com.android.launcher3.views.BaseDragLayer;

/**
 * trebufork: workspace-style resize frame for widgets on the scrollable home. A floating overlay
 * with four handles drawn around the widget row; dragging a handle scales the widget (width
 * relative to the list width, height relative to the natural aspect-ratio height) live, and on
 * release the new size is persisted in {@link ScrollableDesktopStore}.
 *
 * <p>Modeled after the stock {@link AppWidgetResizeFrame}, but grid-free: the row simply
 * re-measures through {@link ScrollableWidgetRow#setScales(float, float)}.
 */
public class ScrollableWidgetResizeFrame extends AbstractFloatingView {

    private static final float MIN_WIDTH_SCALE = 0.5f;
    private static final float MAX_WIDTH_SCALE = 1f;
    private static final float MIN_HEIGHT_SCALE = 0.5f;
    private static final float MAX_HEIGHT_SCALE = 2f;

    private final Launcher mLauncher;
    private final Rect mTmpRect = new Rect();
    private final Rect mWidgetRect = new Rect();

    private ScrollableAppsView mAppsView;
    private ScrollableWidgetRow mRow;
    private ScrollableDesktopStore.DesktopItem mItem;
    private DragLayer mDragLayer;

    private ImageView mLeftHandle;
    private ImageView mTopHandle;
    private ImageView mRightHandle;
    private ImageView mBottomHandle;

    // Which border the current gesture resizes.
    private boolean mLeftActive;
    private boolean mTopActive;
    private boolean mRightActive;
    private boolean mBottomActive;

    // Scales at gesture start and the row's size at scale 1 (reference deltas against).
    private float mStartWidthScale;
    private float mStartHeightScale;
    private float mBaseWidthPx;
    private float mBaseHeightPx;
    // trebufork: pixel height of the widget when the gesture started. Horizontal resizes keep
    // this constant (see resizeForDelta), so dragging the side handles never changes the height.
    private float mStartHeightPx;

    private float mDownX;
    private float mDownY;

    // Horizontal move state: dragging the widget body (not a handle) repositions it within the
    // free space left of the full list width.
    private boolean mMoveActive;
    private float mStartPositionX;
    private float mMoveDownX;
    private final int mTouchSlop;
    // True while a resize/move drag is in progress. The widget menu hides as soon as a drag
    // starts, but its onCloseCallback must not kill the grid mid-drag — the frame closes
    // itself when the drag commits (see the popup's close callback in ScrollableAppsView).
    private boolean mDragActive;

    public ScrollableWidgetResizeFrame(Context context) {
        this(context, null);
    }

    public ScrollableWidgetResizeFrame(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollableWidgetResizeFrame(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mLauncher = Launcher.getLauncher(context);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mLeftHandle = findViewById(R.id.scroll_widget_resize_left_handle);
        mTopHandle = findViewById(R.id.scroll_widget_resize_top_handle);
        mRightHandle = findViewById(R.id.scroll_widget_resize_right_handle);
        mBottomHandle = findViewById(R.id.scroll_widget_resize_bottom_handle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // The frame draws on top of the context menu; taps on menu rows that overlap the frame
        // must reach the menu, so pass them through instead of consuming them.
        PopupContainer popup = PopupContainer.getOpen(mLauncher);
        if (popup != null && mLauncher.getDragLayer().isEventOverView(popup, ev)) {
            return false;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    protected void handleClose(boolean animate) {
        mDragActive = false;
        if (mAppsView != null) {
            mAppsView.removeOnScrollListener(mScrollListener);
        }
        if (mRow != null) {
            mRow.removeOnLayoutChangeListener(mRowLayoutListener);
        }
        if (getParent() != null) {
            ((ViewGroup) getParent()).removeView(this);
        }
    }

    /** True while a resize/move drag is in progress (see {@link #mDragActive}). */
    public boolean isDragActive() {
        return mDragActive;
    }

    @Override
    protected boolean isOfType(@FloatingViewType int type) {
        return (type & TYPE_WIDGET_RESIZE_FRAME) != 0;
    }

    private final View.OnLayoutChangeListener mRowLayoutListener =
            (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> snapToWidget();

    /**
     * trebufork: when the list scrolls the widget moves out from under the frame, so the grid
     * (and its menu) closes instead of hanging at a stale position.
     */
    private final RecyclerView.OnScrollListener mScrollListener =
            new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    if (dx == 0 && dy == 0) {
                        return;
                    }
                    PopupContainer popup = PopupContainer.getOpen(mLauncher);
                    if (popup != null) {
                        popup.close(true);
                    }
                    close(false);
                }
            };

    /**
     * Shows the resize frame around the given widget row. Closes any previously open frame.
     */
    public static ScrollableWidgetResizeFrame show(ScrollableAppsView appsView,
            ScrollableWidgetRow row, ScrollableDesktopStore.DesktopItem item) {
        Launcher launcher = Launcher.getLauncher(row.getContext());
        closeOpenFrame(launcher);
        DragLayer dragLayer = launcher.getDragLayer();
        ScrollableWidgetResizeFrame frame = (ScrollableWidgetResizeFrame) LayoutInflater
                .from(launcher)
                .inflate(R.layout.scrollable_widget_resize_frame, dragLayer, false);
        frame.mAppsView = appsView;
        frame.mRow = row;
        frame.mItem = item;
        frame.mDragLayer = dragLayer;
        dragLayer.addView(frame);
        BaseDragLayer.LayoutParams lp = (BaseDragLayer.LayoutParams) frame.getLayoutParams();
        lp.customPosition = true;
        frame.mIsOpen = true;
        frame.snapToWidget();
        row.addOnLayoutChangeListener(frame.mRowLayoutListener);
        appsView.addOnScrollListener(frame.mScrollListener);
        return frame;
    }

    /** Closes the resize frame if one is open (used when the widget menu closes). */
    public static void closeOpenFrame(Launcher launcher) {
        for (int i = launcher.getDragLayer().getChildCount() - 1; i >= 0; i--) {
            View child = launcher.getDragLayer().getChildAt(i);
            if (child instanceof ScrollableWidgetResizeFrame) {
                ((ScrollableWidgetResizeFrame) child).close(false);
            }
        }
    }

    private void snapToWidget() {
        if (mRow == null || mDragLayer == null) {
            return;
        }
        View widget = mRow.getWidgetView();
        if (widget == null || widget.getWidth() == 0 || widget.getHeight() == 0) {
            return;
        }
        mDragLayer.getDescendantRectRelativeToSelf(widget, mWidgetRect);
        int padding = getResources().getDimensionPixelSize(R.dimen.resize_frame_background_padding);
        int newWidth = mWidgetRect.width() + 2 * padding;
        int newHeight = mWidgetRect.height() + 2 * padding;
        int newX = mWidgetRect.left - padding;
        int newY = mWidgetRect.top - padding;
        BaseDragLayer.LayoutParams lp = (BaseDragLayer.LayoutParams) getLayoutParams();
        lp.width = newWidth;
        lp.height = newHeight;
        lp.x = newX;
        lp.y = newY;
        requestLayout();
    }

    @Override
    public boolean onControllerInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            return beginResizeIfPointInRegion(ev);
        }
        // trebufork: take over a gesture that is already in progress (for example the
        // long-press that opened the widget menu) as soon as it is over the widget, so vertical
        // swipes on the widget do not scroll the list and horizontal moves work from the same
        // press instead of needing a second one. The gesture state is re-seeded at the current
        // position, so nothing jumps.
        if (!mDragActive && ev.getAction() == MotionEvent.ACTION_MOVE
                && isEventOverFrame(ev) && !isEventOverPopup(ev)) {
            beginResizeIfPointInRegion(ev);
            mDragActive = true;
            // trebufork: this is the long-press that opened the menu still in progress, so it
            // must never arm a resize handle — otherwise a long-press near a border would start
            // resizing that edge immediately. Only the free horizontal move of the widget body
            // is available from the same press; the resize handles need a fresh, deliberate tap.
            mMoveActive = true;
            PopupContainer popup = PopupContainer.getOpen(mLauncher);
            if (popup != null) {
                popup.close(true);
            }
            return true;
        }
        return false;
    }

    /** True when the event point (DragLayer coordinates) is inside the frame's own bounds. */
    private boolean isEventOverFrame(MotionEvent ev) {
        return ev.getX() >= getLeft() && ev.getX() <= getRight()
                && ev.getY() >= getTop() && ev.getY() <= getBottom();
    }

    /** True when the event point is over the open widget menu (its rows must keep working). */
    private boolean isEventOverPopup(MotionEvent ev) {
        PopupContainer popup = PopupContainer.getOpen(mLauncher);
        return popup != null && mLauncher.getDragLayer().isEventOverView(popup, ev);
    }

    @Override
    public boolean onControllerTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                return beginResizeIfPointInRegion(ev);
            case MotionEvent.ACTION_MOVE:
                // Resize handles and the horizontal move activate only once the finger really
                // moves past the touch slop; a plain tap must never enter those paths (it would
                // commit a no-op resize and close only the frame, leaving the menu open).
                if (!isResizing() && !mMoveActive && hasExceededTouchSlop(ev)) {
                    activateGesture(ev);
                    // The menu hides once a drag starts, but the grid must stay: the popup's
                    // onCloseCallback skips closing the frame while isDragActive() is true.
                    PopupContainer popup = PopupContainer.getOpen(mLauncher);
                    if (popup != null) {
                        popup.close(true);
                    }
                }
                if (isResizing()) {
                    resizeForDelta(ev.getX() - mDownX, ev.getY() - mDownY);
                } else if (mMoveActive) {
                    updateHorizontalMove(ev);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (isResizing()) {
                    resizeForDelta(ev.getX() - mDownX, ev.getY() - mDownY);
                    commitResize();
                    // trebufork: keep the grid open after releasing a handle — it must dismiss
                    // only on an empty-space tap, not on release (the menu already hid when the
                    // drag started).
                    finishDragKeepOpen();
                } else if (mMoveActive) {
                    commitMove();
                    finishDragKeepOpen();
                } else {
                    // A plain tap without movement dismisses the widget menu and the resize
                    // frame together — one tap, not two.
                    dismissTap();
                }
                break;
            default:
                break;
        }
        return true;
    }

    private boolean hasExceededTouchSlop(MotionEvent ev) {
        return Math.abs(ev.getX() - mDownX) > mTouchSlop
                || Math.abs(ev.getY() - mDownY) > mTouchSlop;
    }

    /**
     * Decides, from the current finger position, whether the gesture is a handle resize or a
     * free horizontal move of the widget body, and arms the corresponding state.
     */
    private void activateGesture(MotionEvent ev) {
        mDragActive = true;
        int x = Math.round(ev.getX() - getLeft());
        int y = Math.round(ev.getY() - getTop());
        int touchTarget = 2 * getResources().getDimensionPixelSize(
                R.dimen.resize_frame_background_padding);
        mLeftActive = x < touchTarget;
        mRightActive = x > getWidth() - touchTarget;
        mTopActive = y < touchTarget;
        mBottomActive = y > getHeight() - touchTarget;
        if (!isResizing()) {
            // Free drag of the widget body: horizontal move within the row's free space.
            mMoveActive = true;
        }
    }

    private boolean isResizing() {
        return mLeftActive || mRightActive || mTopActive || mBottomActive;
    }

    private boolean beginResizeIfPointInRegion(MotionEvent ev) {
        // Taps on the context menu rows must keep working, so pass those through.
        if (isEventOverPopup(ev)) {
            return false;
        }
        // Record the down point and the gesture-start geometry. The handle regions are not
        // armed yet: they activate on the first MOVE past the touch slop (see activateGesture),
        // so a tap anywhere just dismisses menu + frame on release.
        mDownX = ev.getX();
        mDownY = ev.getY();
        mStartWidthScale = mItem.widthScale;
        mStartHeightScale = mItem.heightScale;
        // The widget is full-width at scale 1; use its measured size as reference.
        View widget = mRow.getWidgetView();
        mBaseWidthPx = widget != null
                ? widget.getWidth() / mStartWidthScale : mRow.getWidth();
        mBaseHeightPx = widget != null
                ? widget.getHeight() / mStartHeightScale : mRow.getHeight();
        mStartHeightPx = widget != null ? widget.getHeight() : mRow.getHeight();
        mStartPositionX = mItem.positionX;
        mMoveDownX = ev.getX();
        mMoveActive = false;
        mLeftActive = false;
        mRightActive = false;
        mTopActive = false;
        mBottomActive = false;
        return true;
    }

    /** trebufork: live horizontal move of the widget inside the free space of its row. */
    private void updateHorizontalMove(MotionEvent ev) {
        float deltaX = ev.getX() - mMoveDownX;
        if (!mMoveActive) {
            if (Math.abs(deltaX) < mTouchSlop) {
                return;
            }
            mMoveActive = true;
        }
        View widget = mRow.getWidgetView();
        if (widget == null) {
            return;
        }
        float freeSpace = mRow.getWidth() - widget.getWidth();
        if (freeSpace <= 0f) {
            return;
        }
        float positionX = Math.max(0f, Math.min(1f, mStartPositionX + deltaX / freeSpace));
        mItem.positionX = positionX;
        mRow.setPositionX(positionX);
    }

    private void commitMove() {
        if (mItem != null && mAppsView != null) {
            mAppsView.setWidgetPositionX(mItem.id, mItem.positionX);
        }
    }

    /**
     * trebufork: ends a resize/move drag but keeps the grid on screen. The menu has already
     * hidden when the drag started; the frame now stays around the (possibly resized) widget
     * until the user taps empty space (see {@link #dismissTap()}).
     */
    private void finishDragKeepOpen() {
        mDragActive = false;
        mLeftActive = false;
        mRightActive = false;
        mTopActive = false;
        mBottomActive = false;
        mMoveActive = false;
        // Re-align the frame and its handles around the widget after the resize/move.
        snapToWidget();
    }

    private void dismissTap() {
        PopupContainer popup = PopupContainer.getOpen(mLauncher);
        if (popup != null) {
            popup.close(true);
        }
        close(false);
    }

    private void resizeForDelta(float deltaX, float deltaY) {
        float widthScale = mStartWidthScale;
        float heightScale = mStartHeightScale;
        if (mLeftActive) {
            widthScale = mStartWidthScale - deltaX / mBaseWidthPx;
        } else if (mRightActive) {
            widthScale = mStartWidthScale + deltaX / mBaseWidthPx;
        }
        if (mTopActive) {
            heightScale = mStartHeightScale - deltaY / mBaseHeightPx;
        } else if (mBottomActive) {
            heightScale = mStartHeightScale + deltaY / mBaseHeightPx;
        }
        widthScale = Math.max(MIN_WIDTH_SCALE, Math.min(MAX_WIDTH_SCALE, widthScale));
        // trebufork: no aspect-ratio coupling — dragging the left/right handles must not change
        // the widget's vertical size. height = width * aspect * heightScale, so to keep the
        // gesture-start pixel height constant across a width change, the height scale is
        // re-derived as startHeightScale * startWidthScale / widthScale.
        if ((mLeftActive || mRightActive) && !mTopActive && !mBottomActive) {
            heightScale = mStartHeightScale * mStartWidthScale / widthScale;
        }
        heightScale = Math.max(MIN_HEIGHT_SCALE, Math.min(MAX_HEIGHT_SCALE, heightScale));
        // Live update the item and the row so the widget visibly grows/shrinks while dragging.
        mItem.widthScale = widthScale;
        mItem.heightScale = heightScale;
        mRow.setScales(widthScale, heightScale);
    }

    private void commitResize() {
        if (mItem != null && mAppsView != null) {
            mAppsView.setWidgetSize(mItem.id, mItem.widthScale, mItem.heightScale);
        }
    }
}
