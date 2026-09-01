/*
 * Copyright (C) 2019 The Android Open Source Project
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
package com.android.launcher3.views;

import static android.view.Gravity.LEFT;

import static com.android.app.animation.Interpolators.LINEAR;
import static com.android.launcher3.Utilities.getFullDrawable;
import static com.android.launcher3.Utilities.mapToRange;
import static com.android.launcher3.graphics.PreloadIconDelegate.newPendingIcon;
import static com.android.launcher3.icons.BitmapInfo.FLAG_CUSTOM_SHAPE;
import static com.android.launcher3.icons.BitmapInfo.FLAG_FULL_BLEED;
import static com.android.launcher3.util.Executors.MODEL_EXECUTOR;
import static com.android.launcher3.views.FloatingIconViewCompanion.setPropertiesVisible;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.recyclerview.widget.RecyclerView;

import com.android.launcher3.BubbleTextView;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.InsettableFrameLayout;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.ScrollableAppsView;
import com.android.launcher3.Utilities;
import com.android.launcher3.dragndrop.DragLayer;
import com.android.launcher3.folder.FolderIcon;
import com.android.launcher3.graphics.PreloadIconDelegate;
import com.android.launcher3.icons.FastBitmapDrawable;
import com.android.launcher3.icons.IconNormalizer;
import com.android.launcher3.icons.IconShape;
import com.android.launcher3.model.data.ItemInfo;
import com.android.launcher3.model.data.ItemInfoWithIcon;
import com.android.launcher3.popup.SystemShortcut;
import com.android.launcher3.shortcuts.DeepShortcutView;
import com.android.launcher3.util.AsyncView;

import java.util.function.Supplier;

/**
 * A view that is created to look like another view with the purpose of creating fluid animations.
 */
public class FloatingIconView extends FrameLayout implements
        Animator.AnimatorListener, OnGlobalLayoutListener, FloatingView {

    private static final String TAG = "FloatingIconView";

    // trebufork: shared tag for animation lifecycle diagnostics (stuck floating icon).
    // Filter: adb logcat -s TrebuforkAnim
    private static final String TREBUFORK_TAG = "TrebuforkAnim";

    // Manages loading the icon on a worker thread
    private static @Nullable IconLoadResult sIconLoadResult;
    private static long sFetchIconId = 0;
    private static long sRecycledFetchIconId = sFetchIconId;

    public static final float SHAPE_PROGRESS_DURATION = 0.10f;
    private static final RectF sTmpRectF = new RectF();

    private Runnable mEndRunnable;
    private CancellationSignal mLoadIconSignal;

    private final Launcher mLauncher;
    private final boolean mIsRtl;

    private boolean mIsOpening;

    private IconLoadResult mIconLoadResult;

    private View mBtvDrawable;

    private ClipIconView mClipIconView;
    private @Nullable Drawable mBadge;

    // A view whose visibility should update in sync with mOriginalIcon.
    private @Nullable AsyncView mMatchVisibilityView;

    // A view that will fade out as the animation progresses.
    private @Nullable AsyncView mFadeOutView;

    private View mOriginalIcon;
    private RectF mPositionOut;
    private Runnable mOnTargetChangeRunnable;

    private final Rect mFinalDrawableBounds = new Rect();

    private ListenerView mListenerView;
    private Runnable mFastFinishRunnable;

    private float mIconOffsetY;

    // trebufork: per-instance id so create/finish/cancel logs can be paired exactly.
    private static int sInstanceCounter;
    private final int mInstanceId = ++sInstanceCounter;

    // trebufork: failsafe cleanup. A floating icon must be removed within this many ms of its
    // last re-target no matter what drives its animation. Close icons are driven by
    // RectFSpringAnim (which has its own watchdog), but open icons are driven by a plain
    // ValueAnimator whose end/cancel can be lost when the launch transition is abandoned
    // mid-flight (rapid open/close), leaking the icon on the workspace.
    private static final long CLEANUP_WATCHDOG_DELAY_MS = 3000L;
    private final Handler mCleanupHandler = new Handler(Looper.getMainLooper());
    private Runnable mCleanupWatchdog;

    private void armCleanupWatchdog() {
        mCleanupHandler.removeCallbacks(mCleanupWatchdog);
        mCleanupWatchdog = () -> {
            if (getParent() == null) {
                // Already removed.
                return;
            }
            Log.d(TREBUFORK_TAG, "FloatingIconView.watchdog: force cleanup id=" + mInstanceId
                    + " endRunnable=" + (mEndRunnable != null));
            if (mEndRunnable != null) {
                onAnimationEnd(null);
            } else {
                finish(mLauncher.getDragLayer());
            }
        };
        mCleanupHandler.postDelayed(mCleanupWatchdog, CLEANUP_WATCHDOG_DELAY_MS);
    }

    // trebufork: when the tracked icon lives inside the scrollable home list, this is that list.
    // Used to follow the icon while the list scrolls (RecyclerView scroll does not fire a global
    // layout) and to keep the close animation from being cut short by a scroll gesture.
    private @Nullable ScrollableAppsView mScrollableAppsView;

    // trebufork: once true, the tracked icon left the visible list area (scrolled out, or the
    // user jumped via the alphabet) so it can never reach its final position. Instead of chasing
    // an unreachable icon we collapse the closing window to a near-zero point where it currently
    // is and fade it out - a graceful shrink instead of an abrupt pop.
    private boolean mShrinkToZero;
    // Visible bounds of the scrollable list, in drag-layer coordinates. The list view itself does
    // not translate while its contents scroll, so this is computed once and reused.
    private @Nullable RectF mListViewport;
    private final RecyclerView.OnScrollListener mScrollListener =
            new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    updatePosition();
                }
            };

    public FloatingIconView(Context context) {
        this(context, null);
    }

    public FloatingIconView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatingIconView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mLauncher = Launcher.getLauncher(context);
        mIsRtl = Utilities.isRtl(getResources());
        mListenerView = new ListenerView(context, attrs);
        mClipIconView = new ClipIconView(context, attrs);
        mBtvDrawable = new ImageView(context, attrs);
        addView(mBtvDrawable);
        addView(mClipIconView);
        setWillNotDraw(false);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!mIsOpening) {
            getViewTreeObserver().addOnGlobalLayoutListener(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
        super.onDetachedFromWindow();
    }

    /**
     * Positions this view to match the size and location of {@code rect}.
     */
    public void update(float alpha, RectF rect, float progress, float shapeProgressStart,
            float cornerRadius, boolean isOpening) {
        update(alpha, rect, progress, shapeProgressStart, cornerRadius, isOpening, 0);
    }

    /**
     * Positions this view to match the size and location of {@code rect}.
     * <p>
     * @param alpha The alpha[0, 1] of the entire floating view.
     * @param progress A value from [0, 1] that represents the animation progress.
     * @param shapeProgressStart The progress value at which to start the shape reveal.
     * @param cornerRadius The corner radius of {@code rect}.
     * @param isOpening True if view is used for app open animation, false for app close animation.
     * @param taskViewDrawAlpha the drawn {@link com.android.quickstep.views.TaskView} alpha
     */
    public void update(float alpha, RectF rect, float progress, float shapeProgressStart,
            float cornerRadius, boolean isOpening, int taskViewDrawAlpha) {
        // trebufork: shrink-to-zero mode - the target icon is gone, so the window collapses to a
        // point. Fade the icon out over the final stretch so it dissolves to nothing instead of
        // popping once the unreachable icon is removed.
        if (mShrinkToZero) {
            alpha *= Math.max(0f, 1f - (progress - 0.7f) / 0.3f);
        }
        // The non-running task home animation has some very funky first few frames because this
        // FIV hasn't fully laid out. During those frames, hide this FIV and continue drawing the
        // TaskView directly while transforming it in the place of this FIV. However, if we fade
        // the TaskView at all, we need to display this FIV regardless.
        setAlpha(isLaidOut() || taskViewDrawAlpha < 255 ? alpha : 0f);
        mClipIconView.update(rect, progress, shapeProgressStart, cornerRadius, isOpening, this,
                mLauncher.getDeviceProfile(), taskViewDrawAlpha);

        // The alpha goes from 1 to 0 when progress is 0 and 0.15 respectively.
        // This value minimizes view display time while still allowing the view to fade out.
        if (mFadeOutView != null) {
            mFadeOutView.postAlpha(
                    1 - Math.min(1f, mapToRange(progress, 0, 0.15f, 0, 1, LINEAR)));
        }
    }

    /**
     * Sets a {@link com.android.quickstep.views.TaskView} that will draw a
     * {@link com.android.quickstep.views.TaskView} within the {@code mClipIconView} clip bounds
     */
    public void setOverlayArtist(ClipIconView.TaskViewArtist taskViewArtist) {
        mClipIconView.setTaskViewArtist(taskViewArtist);
    }

    @Override
    public void onAnimationEnd(Animator animator) {
        Log.d(TREBUFORK_TAG, "FloatingIconView.onAnimationEnd: id=" + mInstanceId
                + " mEndRunnable=" + (mEndRunnable != null));
        if (mLoadIconSignal != null) {
            mLoadIconSignal.cancel();
        }
        if (mEndRunnable != null) {
            mEndRunnable.run();
        } else {
            // End runnable also ends the reveal animator, so we manually handle it here.
            mClipIconView.endReveal();
        }
    }

    /**
     * Sets the size and position of this view to match {@code v}.
     * <p>
     * @param v The view to copy
     * @param positionOut Rect that will hold the size and position of v.
     */
    private void matchPositionOf(Launcher launcher, View v, boolean isOpening, RectF positionOut) {
        getLocationBoundsForView(launcher, v, isOpening, positionOut);
        final InsettableFrameLayout.LayoutParams lp = new InsettableFrameLayout.LayoutParams(
                Math.round(positionOut.width()),
                Math.round(positionOut.height()));
        updatePosition(positionOut, lp);
        setLayoutParams(lp);

        // For code simplicity, we always layout the child views using Gravity.LEFT
        // and manually handle RTL for FloatingIconView when positioning it on the screen.
        mClipIconView.setLayoutParams(new FrameLayout.LayoutParams(lp.width, lp.height, LEFT));
        mBtvDrawable.setLayoutParams(new FrameLayout.LayoutParams(lp.width, lp.height, LEFT));
    }

    private void updatePosition(RectF pos, InsettableFrameLayout.LayoutParams lp) {
        mPositionOut.set(pos);
        lp.ignoreInsets = true;
        // Position the floating view exactly on top of the original
        lp.topMargin = Math.round(pos.top);
        if (mIsRtl) {
            lp.setMarginStart(Math.round(mLauncher.getDeviceProfile().getDeviceProperties().getWidthPx() - pos.right));
        } else {
            lp.setMarginStart(Math.round(pos.left));
        }
        // Set the properties here already to make sure they are available when running the first
        // animation frame.
        int left = mIsRtl
                ? mLauncher.getDeviceProfile().getDeviceProperties().getWidthPx() - lp.getMarginStart() - lp.width
                : lp.leftMargin;
        layout(left, lp.topMargin, left + lp.width, lp.topMargin + lp.height);
        // trebufork: this re-layout can run while the close spring is mid-flight (scrollable-home
        // scroll re-targeting). Reset the per-frame spring translation here, otherwise the
        // container sits one frame at layout + stale translation (a visible jump, e.g. to the
        // right) until the spring's next frame recomputes it against the new layout position.
        setTranslationX(0f);
        setTranslationY(0f);
    }

    private static void getLocationBoundsForView(Launcher launcher, View v, boolean isOpening,
            RectF outRect) {
        getLocationBoundsForView(launcher, v, isOpening, outRect, new Rect());
    }

    /**
     * Gets the location bounds of a view and returns the overall rotation.
     * - For DeepShortcutView, we return the bounds of the icon view.
     * - For BubbleTextView, we return the icon bounds.
     */
    public static void getLocationBoundsForView(Launcher launcher, View v, boolean isOpening,
            RectF outRect, Rect outViewBounds) {
        boolean ignoreTransform = !isOpening;
        if (v instanceof DeepShortcutView dsv) {
            v = dsv.getIconView();
            ignoreTransform = false;
        } else if (v.getParent() instanceof DeepShortcutView dsv) {
            v = dsv.getIconView();
            ignoreTransform = false;
        } else if (v instanceof BubbleTextHolder bth) {
            v = bth.getBubbleText();
            ignoreTransform = false;
        }
        if (v == null) {
            return;
        }

        if (v instanceof BubbleTextView) {
            ((BubbleTextView) v).getIconBounds(outViewBounds);
        } else if (v instanceof FolderIcon) {
            ((FolderIcon) v).getPreviewBounds(outViewBounds);
        } else {
            outViewBounds.set(0, 0, v.getWidth(), v.getHeight());
        }

        Utilities.getBoundsForViewInDragLayer(launcher.getDragLayer(), v, outViewBounds,
                ignoreTransform, null /** recycle */, outRect);
    }

    /**
     * Loads the icon and saves the results to {@link #sIconLoadResult}.
     * <p>
     * Runs onIconLoaded callback (if any), which signifies that the FloatingIconView is
     * ready to display the icon. Otherwise, the FloatingIconView will grab the results when its
     * initialized.
     * <p>
     * @param originalView The View that the FloatingIconView will replace.
     * @param info ItemInfo of the originalView
     * @param pos The position of the view.
     * @param btvIcon The drawable of the BubbleTextView. May be null if original view is not a BTV
     * @param outIconLoadResult We store the icon results into this object.
     */
    @WorkerThread
    @SuppressWarnings("WrongThread")
    private static void getIconResult(Launcher l, View originalView, ItemInfo info, RectF pos,
            @Nullable Drawable btvIcon, IconLoadResult outIconLoadResult) {
        Drawable drawable;
        boolean supportsAdaptiveIcons = !info.isDisabled(); // Use original icon for disabled icons.

        Drawable badge = null;
        if (info instanceof SystemShortcut) {
            if (originalView instanceof ImageView iv) {
                drawable = iv.getDrawable();
            } else if (originalView instanceof DeepShortcutView dsv) {
                drawable = dsv.getIconView().getBackground();
            } else {
                drawable = originalView.getBackground();
            }
        } else if (btvIcon instanceof FastBitmapDrawable fbd
                && fbd.getDelegate() instanceof PreloadIconDelegate) {
            // Force the progress bar to display.
            drawable = btvIcon;
        } else if (originalView instanceof ImageView) {
            drawable = ((ImageView) originalView).getDrawable();
        } else {
            int width = (int) pos.width();
            int height = (int) pos.height();
            Pair<AdaptiveIconDrawable, Drawable> fullIcon = null;
            if (supportsAdaptiveIcons) {
                boolean shouldThemeIcon = (btvIcon instanceof FastBitmapDrawable fbd)
                        && fbd.isCreatedForTheme();
                fullIcon = getFullDrawable(l, info, width, height, shouldThemeIcon);
            } else if (!(originalView instanceof BubbleTextView)) {
                fullIcon = getFullDrawable(l, info, width, height, true /* shouldThemeIcon */);
            }

            if (fullIcon != null) {
                drawable = fullIcon.first;
                badge = fullIcon.second;
            } else {
                drawable = btvIcon;
            }
        }

        drawable = drawable == null ? null : drawable.getConstantState().newDrawable();
        int iconOffset = getOffsetForIconBounds(l, drawable, pos);
        // Clone right away as we are on the background thread instead of blocking the
        // main thread later
        Drawable btvClone = btvIcon == null ? null : btvIcon.getConstantState().newDrawable();
        synchronized (outIconLoadResult) {
            outIconLoadResult.btvDrawable = () -> btvClone;
            outIconLoadResult.drawable = drawable;
            outIconLoadResult.badge = badge;
            outIconLoadResult.iconOffset = iconOffset;
            if (outIconLoadResult.onIconLoaded != null) {
                l.getMainExecutor().execute(outIconLoadResult.onIconLoaded);
                outIconLoadResult.onIconLoaded = null;
            }
            outIconLoadResult.isIconLoaded = true;
        }
    }

    /**
     * Sets the drawables of the {@code originalView} onto this view.
     * <p>
     * @param drawable The drawable of the original view.
     * @param badge The badge of the original view.
     * @param iconOffset The amount of offset needed to match this view with the original view.
     */
    @UiThread
    private void setIcon(@Nullable Drawable drawable, @Nullable Drawable badge,
            @Nullable Supplier<Drawable> btvIcon, int iconOffset, boolean usingCustomShape) {
        final DeviceProfile dp = mLauncher.getDeviceProfile();
        final InsettableFrameLayout.LayoutParams lp =
                (InsettableFrameLayout.LayoutParams) getLayoutParams();
        mBadge = badge;
        mClipIconView.setIcon(drawable, iconOffset, lp, mIsOpening, usingCustomShape, dp);
        if (drawable instanceof AdaptiveIconDrawable) {
            final int originalHeight = lp.height;
            final int originalWidth = lp.width;

            mFinalDrawableBounds.set(0, 0, originalWidth, originalHeight);

            float aspectRatio = mLauncher.getDeviceProfile().getDeviceProperties().getAspectRatio();
            if (dp.getDeviceProperties().isLandscape()) {
                lp.width = (int) Math.max(lp.width, lp.height * aspectRatio);
            } else {
                lp.height = (int) Math.max(lp.height, lp.width * aspectRatio);
            }
            setLayoutParams(lp);

            final LayoutParams clipViewLp = (LayoutParams) mClipIconView.getLayoutParams();
            if (mBadge != null) {
                Rect badgeBounds = new Rect(0, 0, clipViewLp.width, clipViewLp.height);
                FastBitmapDrawable.setBadgeBounds(mBadge, badgeBounds);
            }
            clipViewLp.width = lp.width;
            clipViewLp.height = lp.height;
            mClipIconView.setLayoutParams(clipViewLp);
        }

        setOriginalDrawableBackground(btvIcon);
        invalidate();
    }

    /**
     * Draws the drawable of the BubbleTextView behind ClipIconView
     * <p>
     * This is used to:
     * - Have icon displayed while Adaptive Icon is loading
     * - Displays the built in shadow to ensure a clean handoff
     * <p>
     * Allows nullable as this may be cleared when drawing is deferred to ClipIconView.
     */
    private void setOriginalDrawableBackground(@Nullable Supplier<Drawable> btvIcon) {
        if (!mIsOpening) {
            mBtvDrawable.setBackground(btvIcon == null ? null : btvIcon.get());
        }
    }

    /**
     * Returns true if the icon is different from main app icon
     */
    public boolean isDifferentFromAppIcon() {
        return mIconLoadResult == null ? false : mIconLoadResult.isThemed;
    }

    /**
     * Checks if the icon result is loaded. If true, we set the icon immediately. Else, we add a
     * callback to set the icon once the icon result is loaded.
     */
    private void checkIconResult() {
        CancellationSignal cancellationSignal = new CancellationSignal();

        if (mIconLoadResult == null) {
            Log.w(TAG, "No icon load result found in checkIconResult");
            return;
        }

        synchronized (mIconLoadResult) {
            if (mIconLoadResult.isIconLoaded) {
                setIcon(mIconLoadResult.drawable, mIconLoadResult.badge,
                        mIconLoadResult.btvDrawable, mIconLoadResult.iconOffset,
                        mIconLoadResult.usingCustomShape);
                setVisibility(VISIBLE);
                hideOriginalIconNextFrame();
            } else {
                mIconLoadResult.onIconLoaded = () -> {
                    if (cancellationSignal.isCanceled()) {
                        return;
                    }

                    setIcon(mIconLoadResult.drawable, mIconLoadResult.badge,
                            mIconLoadResult.btvDrawable, mIconLoadResult.iconOffset,
                            mIconLoadResult.usingCustomShape);

                    setVisibility(VISIBLE);
                    hideOriginalIconNextFrame();
                };
                mLoadIconSignal = cancellationSignal;
            }
        }
    }

    /**
     * trebufork: hides the original icon one frame later instead of synchronously. When the icon
     * finishes loading (often right at animation composition) the floating view may not be laid
     * out / drawn yet - hiding the real icon immediately then leaves a one-frame gap where the
     * icon is gone but the floating view isn't shown yet (visible on the paged workspace; the
     * scrollable home's row icon is not affected the same way). Deferring the hide keeps the real
     * icon visible until the floating view is actually drawn over it.
     */
    private void hideOriginalIconNextFrame() {
        post(() -> updateViewsVisibility(false /* isVisible */));
    }

    @WorkerThread
    @SuppressWarnings("WrongThread")
    private static int getOffsetForIconBounds(Launcher l, Drawable drawable, RectF position) {
        if (!(drawable instanceof AdaptiveIconDrawable)) {
            return 0;
        }
        int blurSizeOutline =
                l.getResources().getDimensionPixelSize(R.dimen.blur_size_medium_outline);

        Rect bounds = new Rect(0, 0, (int) position.width() + blurSizeOutline,
                (int) position.height() + blurSizeOutline);
        bounds.inset(blurSizeOutline / 2, blurSizeOutline / 2);
        Utilities.scaleRectAboutCenter(bounds, IconNormalizer.ICON_VISIBLE_AREA_FACTOR);

        bounds.inset(
                (int) (-bounds.width() * AdaptiveIconDrawable.getExtraInsetFraction()),
                (int) (-bounds.height() * AdaptiveIconDrawable.getExtraInsetFraction())
        );

        return bounds.left;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mBadge != null) {
            mBadge.draw(canvas);
        }
    }

    /**
     * Sets a runnable that is called after a call to {@link #fastFinish()}.
     */
    public void setFastFinishRunnable(Runnable runnable) {
        mFastFinishRunnable = runnable;
    }

    @Override
    public void fastFinish() {
        Log.d(TREBUFORK_TAG, "FloatingIconView.fastFinish: mEndRunnable="
                + (mEndRunnable != null) + " mFastFinishRunnable=" + (mFastFinishRunnable != null));
        if (mFastFinishRunnable != null) {
            mFastFinishRunnable.run();
            mFastFinishRunnable = null;
        }
        if (mLoadIconSignal != null) {
            mLoadIconSignal.cancel();
            mLoadIconSignal = null;
        }
        if (mEndRunnable != null) {
            mEndRunnable.run();
            mEndRunnable = null;
        }
    }

    @Override
    public void onAnimationStart(Animator animator) {
        if ((mIconLoadResult != null && mIconLoadResult.isIconLoaded)
                || (!mIsOpening && mBtvDrawable.getBackground() != null)) {
            // No need to wait for icon load since we can display the BubbleTextView drawable.
            setVisibility(View.VISIBLE);
        }
        if (!mIsOpening) {
            // When closing an app, we want the item on the workspace to be invisible immediately
            updateViewsVisibility(false  /* isVisible */);
        }
        if (mFadeOutView != null) {
            mFadeOutView.postForceHideDotRingAsFloatingIconViewCompanion(true);
        }
    }

    @Override
    public void onAnimationCancel(Animator animator) {
        // trebufork: the opening animation (or its spring) can be cancelled mid-flight when the
        // user quickly closes the just-opened app. Previously this was a no-op, so the floating
        // icon was never removed and stayed stuck on the workspace (multiple copies stacked on
        // top of each other). Run the same cleanup as onAnimationEnd.
        Log.d(TREBUFORK_TAG, "FloatingIconView.onAnimationCancel: cleaning up id=" + mInstanceId
                + " (mEndRunnable=" + (mEndRunnable != null) + ")");
        onAnimationEnd(animator);
    }

    @Override
    public void onAnimationRepeat(Animator animator) {}

    @Override
    public void setPositionOffsetY(float y) {
        mIconOffsetY = y;
        onGlobalLayout();
    }

    @Override
    public void onGlobalLayout() {
        updatePosition();
    }

    /**
     * trebufork: recomputes the tracked icon position and re-targets the spring if it moved.
     * Also driven directly by the scroll listener registered when the icon lives inside the
     * scrollable home list, because RecyclerView scrolling does not fire a global layout.
     */
    private void updatePosition() {
        if (mOriginalIcon == null || mPositionOut == null) {
            return;
        }
        if (mOriginalIcon.isAttachedToWindow()) {
            getLocationBoundsForView(mLauncher, mOriginalIcon, mIsOpening, sTmpRectF);
            sTmpRectF.offset(0, mIconOffsetY);
            // trebufork: if the icon scrolled out of the visible list area (or the user jumped
            // via the alphabet), it can no longer reach its final position - collapse the closing
            // window to a point and fade it out instead of chasing the unreachable icon.
            if (shrinkToZeroIfTargetLost(sTmpRectF)) {
                return;
            }
            if (!sTmpRectF.equals(mPositionOut)) {
                // trebufork: only re-target the spring. Do NOT re-layout the container or mutate
                // its margins mid-flight: the spring translates the container to the live icon
                // position (ClipIconView aligns it to the spring rect), and a layout pass racing
                // the translation makes the flying icon jump left/right for a few frames.
                mPositionOut.set(sTmpRectF);
                if (mOnTargetChangeRunnable != null) {
                    mOnTargetChangeRunnable.run();
                }
                // trebufork: the tracked icon is actively moving (list scroll), so extend the
                // cleanup grace period.
                armCleanupWatchdog();
            }
        } else if (mScrollableAppsView != null && !mShrinkToZero) {
            // The recycler detached the tracked icon (it scrolled out of the viewport) while the
            // close was still in flight - the icon can no longer be reached. Collapse the window
            // to a point rather than freezing the flight at an on-screen position.
            shrinkToZeroIfTargetLost(null);
        }
    }

    /**
     * trebufork: if {@code target} has left the visible region of the scrollable home list (so
     * the closing window can no longer reach the icon), collapse the window to a near-zero point
     * at its current center and fade it out. The scale spring morphs the window's size down as it
     * finishes, so the transition is a smooth shrink rather than an abrupt disappear. Pass
     * {@code null} target when the icon was already detached (recycled), forcing the collapse.
     * Returns true when the shrink is (or was already) engaged.
     */
    private boolean shrinkToZeroIfTargetLost(RectF target) {
        if (mScrollableAppsView == null || !mScrollableAppsView.isAttachedToWindow()) {
            return false;
        }
        // null target means the icon was already detached - it is unreachable by definition.
        boolean offViewport = target == null;
        if (target != null) {
            if (mListViewport == null) {
                mListViewport = new RectF();
                Utilities.getBoundsForViewInDragLayer(mLauncher.getDragLayer(),
                        mScrollableAppsView,
                        new Rect(0, 0, mScrollableAppsView.getWidth(),
                                mScrollableAppsView.getHeight()),
                        true, null, mListViewport);
            }
            final float tolerance = 1f;
            offViewport = target.bottom <= mListViewport.top + tolerance
                    || target.top >= mListViewport.bottom - tolerance;
        }
        if (!offViewport) {
            return false;
        }
        if (mShrinkToZero) {
            return true;
        }
        mShrinkToZero = true;
        // trebufork: we no longer fly into this icon, so stop hiding it - the real icon must be
        // visible again (restores setIconVisible/dot/ring even on an already-recycled view, so
        // the user never sees an empty slot when scrolling back).
        updateViewsVisibility(true);

        // Collapse to a near-zero rect centered where the window currently is; the scale spring
        // morphs width/height toward this tiny target as it finishes.
        final float cx = mPositionOut.centerX();
        final float cy = mPositionOut.centerY();
        final float e = 1f;
        mPositionOut.set(cx - e / 2f, cy - e / 2f, cx + e / 2f, cy + e / 2f);
        Log.d(TREBUFORK_TAG, "FloatingIconView.shrinkToZero: target icon unreachable"
                + " - collapsing window to a point, target=" + mPositionOut);
        if (mOnTargetChangeRunnable != null) {
            mOnTargetChangeRunnable.run();
        }
        return true;
    }

    public void setOnTargetChangeListener(Runnable onTargetChangeListener) {
        mOnTargetChangeRunnable = onTargetChangeListener;
    }

    /** trebufork: finds the nearest {@link ScrollableAppsView} ancestor of {@code v}, if any. */
    private static @Nullable ScrollableAppsView findScrollableAppsView(View v) {
        for (ViewParent parent = v.getParent(); parent instanceof View;
                parent = ((View) parent).getParent()) {
            if (parent instanceof ScrollableAppsView scrollableAppsView) {
                return scrollableAppsView;
            }
        }
        return null;
    }

    /**
     * Loads the icon drawable on a worker thread to reduce latency between swapping views.
     */
    @UiThread
    public static IconLoadResult fetchIcon(Launcher l, View v, ItemInfo info, boolean isOpening) {
        RectF position = new RectF();
        getLocationBoundsForView(l, v, isOpening, position);

        final FastBitmapDrawable btvIcon;
        final Supplier<Drawable> btvDrawableSupplier;
        if (v instanceof BubbleTextView btv) {
            if (info instanceof ItemInfoWithIcon iiwi && iiwi.shouldShowPendingIcon()) {
                btvIcon = newPendingIcon(iiwi, l, btv.getIconCreationFlagsForInfo(iiwi));
                btvDrawableSupplier = () -> btvIcon;
            } else {
                btvIcon = btv.getIcon();
                // Clone when needed
                btvDrawableSupplier = () -> btvIcon.getConstantState().newDrawable();
            }
        } else {
            btvIcon = null;
            btvDrawableSupplier = null;
        }

        boolean isThemed = false;
        boolean usingCustomShape = false;
        if (btvIcon != null) {
            isThemed = btvIcon.isThemed();
            usingCustomShape = (btvIcon.creationFlags & FLAG_CUSTOM_SHAPE) != 0;
        }

        IconLoadResult result = new IconLoadResult(info, isThemed, usingCustomShape);
        result.btvDrawable = btvDrawableSupplier;

        final long fetchIconId = sFetchIconId++;
        MODEL_EXECUTOR.getHandler().postAtFrontOfQueue(() -> {
            if (fetchIconId < sRecycledFetchIconId) {
                return;
            }
            getIconResult(l, v, info, position, btvIcon, result);
        });

        sIconLoadResult = result;
        return result;
    }

    /**
     * Resets the static icon load result used for preloading the icon for a launching app.
     */
    public static void resetIconLoadResult() {
        sIconLoadResult = null;
    }

    /**
     * Creates a floating icon view for {@code originalView}.
     * <p>
     * @param originalView The view to copy
     * @param visibilitySyncView A view whose visibility should update in sync with originalView.
     * @param fadeOutView A view that will fade out as the animation progresses.
     * @param hideOriginal If true, it will hide {@code originalView} while this view is visible.
     *                     Else, we will not draw anything in this view.
     * @param positionOut Rect that will hold the size and position of v.
     * @param isOpening True if this view replaces the icon for app open animation.
     */
    public static FloatingIconView getFloatingIconView(Launcher launcher, View originalView,
            @Nullable AsyncView visibilitySyncView, @Nullable AsyncView fadeOutView,
            boolean hideOriginal, RectF positionOut, boolean isOpening) {
        final DragLayer dragLayer = launcher.getDragLayer();
        ViewGroup parent = (ViewGroup) dragLayer.getParent();
        // trebufork: during close-to-home the floating icon must render BELOW the recents
        // overview when the user opens recents mid-animation. The overview panel lives inside the
        // DragLayer, while the icon is normally added to the root (above everything, incl. the
        // overview) - so for closing icons we add it to the DragLayer itself, right before the
        // overview child: above the workspace/scrim, under recents.
        // trebufork: during close-to-home the floating icon must render BELOW the recents
        // overview AND below the accent scrim that dims the home behind the overview. Both live
        // inside the DragLayer, while the icon is normally added to the root (above everything,
        // incl. the overview and its scrim) - so for closing icons we add it to the DragLayer
        // itself, right before the scrim child (which precedes the overview in the layout):
        // above the workspace, under the accent dim and under recents.
        final View scrimView = !isOpening ? launcher.findViewById(R.id.scrim_view) : null;
        if (scrimView != null && scrimView.getParent() == dragLayer) {
            parent = dragLayer;
        }
        FloatingIconView view = launcher.getViewCache().getView(R.layout.floating_icon_view,
                launcher, parent);
        view.recycle();

        // Init properties before getting the drawable.
        view.mIsOpening = isOpening;
        view.mOriginalIcon = originalView;
        view.mMatchVisibilityView = visibilitySyncView;
        view.mFadeOutView = fadeOutView;
        view.mPositionOut = positionOut;

        // trebufork: when the close animation targets an icon inside the scrollable home list,
        // track its position as the list scrolls (RecyclerView scroll does not fire a global
        // layout) and do not fast-finish on touch, so scrolling does not cut the animation short.
        view.mScrollableAppsView = findScrollableAppsView(originalView);
        if (view.mScrollableAppsView != null) {
            view.mScrollableAppsView.addOnScrollListener(view.mScrollListener);
        }

        // Get the drawable on the background thread
        boolean shouldLoadIcon = originalView.getTag() instanceof ItemInfo && hideOriginal;
        if (shouldLoadIcon) {
            if (sIconLoadResult != null && sIconLoadResult.itemInfo == originalView.getTag()) {
                view.mIconLoadResult = sIconLoadResult;
            } else {
                view.mIconLoadResult = fetchIcon(launcher, originalView,
                        (ItemInfo) originalView.getTag(), isOpening);
            }
            view.setOriginalDrawableBackground(view.mIconLoadResult.btvDrawable);
        }
        resetIconLoadResult();

        // Match the position of the original view.
        view.matchPositionOf(launcher, originalView, isOpening, positionOut);

        // We need to add it to the overlay, but keep it invisible until animation starts..
        view.setVisibility(View.INVISIBLE);

        if (parent == dragLayer && scrimView != null) {
            int index = dragLayer.indexOfChild(scrimView);
            if (index < 0) {
                index = dragLayer.getChildCount();
            }
            dragLayer.addView(view, Math.min(index, dragLayer.getChildCount()));
        } else {
            parent.addView(view);
        }
        dragLayer.addView(view.mListenerView);
        // trebufork: for icons inside the scrollable home list, do not fast-finish on touch —
        // a scroll gesture must not cut the close animation short. The ListenerView stays open
        // (so it is still cleaned up by finish()) but its close listener becomes a no-op.
        view.mListenerView.setListener(
                view.mScrollableAppsView != null ? () -> { } : view::fastFinish);

        Log.d(TREBUFORK_TAG, "FloatingIconView.animateAsync: created id=" + view.mInstanceId
                + " isOpening=" + isOpening + " hideOriginal=" + hideOriginal
                + " scrollableHomeTracking=" + (view.mScrollableAppsView != null)
                + " tag=" + (originalView.getTag() != null
                        ? originalView.getTag().getClass().getSimpleName() : "null"));

        // trebufork: arm the cleanup watchdog; re-armed on each position change, removed in
        // finish(). Covers open icons whose ValueAnimator never fires end/cancel when the
        // launch transition is abandoned.
        view.armCleanupWatchdog();

        view.mEndRunnable = () -> {
            view.mEndRunnable = null;

            if (view.mFadeOutView != null) {
                view.mFadeOutView.postAlpha(1f);
                view.mFadeOutView.postForceHideDotRingAsFloatingIconViewCompanion(false);
            }

            if (hideOriginal) {
                view.updateViewsVisibility(true /* isVisible */);
                view.finish(dragLayer);
            } else {
                view.finish(dragLayer);
            }
        };

        // Must be called after matchPositionOf so that we know what size to load.
        // Must be called after the fastFinish listener and end runnable is created so that
        // the icon is not left in a hidden state.
        if (shouldLoadIcon) {
            view.checkIconResult();
        }

        return view;
    }

    private void updateViewsVisibility(boolean isVisible) {
        if (mOriginalIcon != null) {
            setPropertiesVisible(mOriginalIcon, isVisible);
        }
        if (mMatchVisibilityView != null) {
            mMatchVisibilityView.postVisibilityAsFloatingIconViewCompanion(isVisible);
        }
    }

    private void finish(DragLayer dragLayer) {
        ViewParent parent = getParent();
        Log.d(TREBUFORK_TAG, "FloatingIconView.finish: removing floating view id=" + mInstanceId
                + " parent=" + (parent == null ? "null" : parent.getClass().getSimpleName())
                + " dragLayerParent=" + (dragLayer == null || dragLayer.getParent() == null
                        ? "null" : dragLayer.getParent().getClass().getSimpleName()));
        // Remove from the actual parent the view is attached to (it may differ from
        // dragLayer.getParent() if the drag layer was re-parented mid-animation).
        if (parent instanceof ViewGroup viewGroup) {
            viewGroup.removeView(this);
        } else if (dragLayer != null && dragLayer.getParent() != null) {
            ((ViewGroup) dragLayer.getParent()).removeView(this);
        }
        if (dragLayer != null) {
            dragLayer.removeView(mListenerView);
        }
        recycle();
        mLauncher.getViewCache().recycleView(R.layout.floating_icon_view, this);
    }

    private void recycle() {
        mCleanupHandler.removeCallbacks(mCleanupWatchdog);
        mCleanupWatchdog = null;
        setTranslationX(0);
        setTranslationY(0);
        setScaleX(1);
        setScaleY(1);
        setAlpha(1);
        if (mLoadIconSignal != null) {
            mLoadIconSignal.cancel();
        }
        mLoadIconSignal = null;
        mEndRunnable = null;
        mFinalDrawableBounds.setEmpty();
        mIsOpening = false;
        mPositionOut = null;
        mListenerView.setListener(null);
        if (mScrollableAppsView != null) {
            mScrollableAppsView.removeOnScrollListener(mScrollListener);
            mScrollableAppsView = null;
        }
        mOriginalIcon = null;
        mOnTargetChangeRunnable = null;
        mShrinkToZero = false;
        mListViewport = null;
        mBadge = null;
        sRecycledFetchIconId = sFetchIconId;
        mIconLoadResult = null;
        mClipIconView.recycle();
        mBtvDrawable.setBackground(null);
        mFastFinishRunnable = null;
        mIconOffsetY = 0;
        mMatchVisibilityView = null;
        mFadeOutView = null;
    }

    private static class IconLoadResult {
        final ItemInfo itemInfo;
        final boolean isThemed;
        final boolean usingCustomShape;
        Supplier<Drawable> btvDrawable;
        Drawable drawable;
        Drawable badge;
        int iconOffset;
        Runnable onIconLoaded;
        boolean isIconLoaded;

        IconLoadResult(ItemInfo itemInfo, boolean isThemed, boolean usingCustomShape) {
            this.itemInfo = itemInfo;
            this.isThemed = isThemed;
            this.usingCustomShape = usingCustomShape;
        }
    }
}
