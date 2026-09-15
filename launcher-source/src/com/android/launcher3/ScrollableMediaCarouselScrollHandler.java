/*
 * Copyright (C) 2020 The Android Open Source Project
 * (C) 2026 The trebufork Project (Java port)
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

import android.animation.ValueAnimator;
import android.view.GestureDetector;
import android.view.ViewConfiguration;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.OverScroller;

import androidx.annotation.Nullable;

/**
 * trebufork: verbatim Java port of SystemUI's MediaCarouselScrollHandler — the touch
 * controller of the media controls carousel (lineage-23.2,
 * packages/SystemUI/src/com/android/systemui/media/controls/ui/view/). Responsibilities,
 * identical to the original:
 * <ul>
 * <li>drag: the content scrolls horizontally one player card per media session, with
 * rubber-banding at the ends ({@code RUBBERBAND_FACTOR = 0.2});</li>
 * <li>release without fling: snap to the nearest page with a smooth scroll;</li>
 * <li>fling: jump to the previous/next player, dispatching a touch cancel to the
 * underlying card so a button press never fires mid-fling;</li>
 * <li>page dots: {@code setLocation(visibleIndex + scrollInFraction)} on every partial
 * scroll, exactly like the shade.</li>
 * </ul>
 * SystemUI animates the dismissal translation with PhysicsAnimator; the launcher carousel
 * has no dismiss/settings-gear page, so the springs reduce to a plain ValueAnimator on the
 * rubber-band translation.
 */
class ScrollableMediaCarouselScrollHandler {

    private static final float RUBBERBAND_FACTOR = 0.2f;
    private static final int FLING_SLOP = 1_000_000;

    /** Notified when the user settles on a card (drag or fling committed to a page). */
    interface OnVisibleCardChangedListener {
        void onVisibleCardChanged(int index);
    }

    private final ScrollableMediaScrollView mScrollView;
    private final ScrollableMediaPageIndicator mPageIndicator;
    private final GestureDetector mGestureDetector;
    private final OverScroller mSnapScroller;
    private final ValueAnimator mTranslationAnimator;

    private OnVisibleCardChangedListener mVisibleCardListener;

    /** The currently visible player index (relative, start-to-end). */
    private int visibleMediaIndex = 0;
    /** How much we are scrolled into the current media, in px. */
    private int scrollIntoCurrentMedia = 0;
    /** Width of one player card including its end margin (qs_media_padding). */
    private int playerWidthPlusPadding = 0;
    /** Pending smooth-scroll target posted from a touch callback. */
    private boolean mSnapPending;
    private int mSnapTargetX;
    /** Rubber-band translation beyond the first/last page. */
    private float mEdgeTranslation;
    // trebufork: true only while the CURRENT gesture has actually dragged past the first/
    // last card. A residual mEdgeTranslation from a previous gesture's decaying springback
    // must not make the release/fling paths treat a normal in-range swipe as an edge
    // release (that skips the snap and parks the carousel mid-swipe).
    private boolean mRubberbanded;
    // trebufork: true while a pointer is down on the carousel (a drag/fling gesture is in
    // flight). A session-set change landing mid-gesture must NOT force a scroll re-anchor —
    // it would cancel the user's drag/fling and park the carousel between pages.
    private boolean mGestureActive;
    // trebufork: a session-set/width change (or an external page jump) that arrived during
    // a gesture and is deferred to the gesture end (see onPlayersChanged).
    private boolean mPlayersChangedPending;
    private boolean mPendingWidthChanged;

    ScrollableMediaCarouselScrollHandler(
            ScrollableMediaScrollView scrollView,
            ScrollableMediaPageIndicator pageIndicator) {
        mScrollView = scrollView;
        mPageIndicator = pageIndicator;
        mGestureDetector = new GestureDetector(
                scrollView.getContext(), new GestureListener());
        mSnapScroller = new OverScroller(scrollView.getContext());
        mTranslationAnimator = ValueAnimator.ofFloat(0f, 0f);
        mTranslationAnimator.addUpdateListener(animation -> {
            mEdgeTranslation = (float) animation.getAnimatedValue();
            mScrollView.getContentContainer().setTranslationX(mEdgeTranslation);
        });
        scrollView.setRelativeScrollX(0);
        scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (playerWidthPlusPadding == 0) {
                return;
            }
            int relativeScrollX = mScrollView.getRelativeScrollX();
            onMediaScrollingChanged(
                    relativeScrollX / playerWidthPlusPadding,
                    relativeScrollX % playerWidthPlusPadding);
        });
        scrollView.setTouchListener(new ScrollableMediaScrollView.TouchListener() {
            @Override
            public boolean onTouchEvent(MotionEvent ev) {
                return onTouch(ev);
            }

            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                return onInterceptTouch(ev);
            }
        });
    }

    void setVisibleCardChangedListener(@Nullable OnVisibleCardChangedListener listener) {
        mVisibleCardChangedListener = listener;
    }

    /**
     * trebufork: the action fired when the user presses and holds still on the carousel.
     * HorizontalScrollView overrides onTouchEvent entirely, so the framework long-press
     * detection (View.onTouchEvent's CheckForLongPress) NEVER runs for it — a long-click
     * listener set on the scroll view is unreachable from a real touch. The shade solves
     * this in its own touch pipeline; here the stationary-press detection runs in
     * {@link #onInterceptTouch}, which sees every event of the gesture.
     */
    void setOnLongPressAction(@Nullable Runnable action) {
        mLongPressAction = action;
    }

    @Nullable
    private Runnable mLongPressAction;
    private final Runnable mLongPressRunnable = new Runnable() {
        @Override
        public void run() {
            if (mLongPressAction == null) {
                return;
            }
            // A long-press means a menu/resize, not a scroll: kill the touch stream the
            // children (and the scroll view) are tracking, then fire the action.
            mScrollView.cancelCurrentScroll();
            mLongPressAction.run();
            mLongPressArmed = false;
        }
    };
    private boolean mLongPressArmed;
    private float mLongPressDownX;
    private float mLongPressDownY;

    private OnVisibleCardChangedListener mVisibleCardChangedListener;

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent eStart, MotionEvent eCurrent, float vX, float vY) {
            return ScrollableMediaCarouselScrollHandler.this.onFling(vX, vY);
        }

        @Override
        public boolean onScroll(MotionEvent down, MotionEvent lastMotion,
                float distanceX, float distanceY) {
            return ScrollableMediaCarouselScrollHandler.this.onScroll(down, lastMotion, distanceX);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }
    }

    private boolean onTouch(MotionEvent motionEvent) {
        // trebufork: track here as well as in onInterceptTouch — touches that land on the
        // scroll view itself (not a child) never go through onInterceptTouchEvent, and a
        // gesture stolen by an ancestor (vertical desktop scroll) delivers its ACTION_CANCEL
        // to onTouchEvent, which MUST disarm the pending long-press or the timer fires
        // mid-swipe and opens the menu.
        trackGesture(motionEvent);
        trackLongPress(motionEvent);
        int action = motionEvent.getActionMasked();
        boolean isUp = action == MotionEvent.ACTION_UP;
        if (mGestureDetector.onTouchEvent(motionEvent)) {
            if (isUp) {
                // If this is an up and we're flinging, we don't want to have this touch
                // reach the view, otherwise that would scroll, while we are trying to snap
                // to the new page. Let's dispatch a cancel instead. Resolve a deferred
                // players change FIRST (it re-anchors scrollX), then post the snap to the
                // new page: runSnap reads mSnapTargetX, which was computed from the
                // pre-anchor scroll position — rebase it after the re-anchor so the fling
                // lands on the right page and never parks between pages.
                mScrollView.cancelCurrentScroll();
                int oldScrollX = mScrollView.getScrollX();
                finishGesture(action);
                if (mSnapPending && oldScrollX != mScrollView.getScrollX()) {
                    mSnapTargetX += mScrollView.getScrollX() - oldScrollX;
                }
                return true;
            }
            // Pass touches to the scrollView.
            return false;
        }
        if (action == MotionEvent.ACTION_MOVE) {
            // Cancel any going snap animation if there is any.
            mSnapScroller.forceFinished(true);
            mSnapPending = false;
        } else if (isUp || action == MotionEvent.ACTION_CANCEL) {
            // trebufork: a rubber-band springback is often still decaying here (150 ms).
            // A normal in-range swipe that merely STARTED near the edge must still snap to
            // the nearest page: only a gesture that actually dragged past the edge
            // (mRubberbanded) takes the edge-release path below. Kill the decay and, for a
            // non-rubber-banded gesture, zero any residual translation instantly so the
            // snap doesn't slide from a visually offset position.
            mTranslationAnimator.cancel();
            // trebufork: the edge-release path is ONLY valid while the scroll still sits
            // on the boundary page where the rubber-band started. When the gesture already
            // reversed and scrolled back into range (a short swipe toward the player:
            // relX moved off the boundary while edgeT was still decaying), a residual
            // edgeT here must NOT divert the release away from the snap-to-page — that
            // exact diversion parked the carousel mid-swipe (edgeT=60, relX=402 at UP in
            // the trace). Decay the stale offset SMOOTHLY (it can still be ~90px — zeroing
            // it in one frame teleports the card and reads as jank) while the normal snap
            // below runs; the two animations compose into one fluid motion.
            boolean offBoundary = playerWidthPlusPadding > 0
                    && mScrollView.getRelativeScrollX() % playerWidthPlusPadding != 0;
            if (mEdgeTranslation != 0f && offBoundary) {
                animateEdgeTranslationTo(0f);
                // Zero the VARIABLE so the routing checks below take the snap path; the
                // running animator restores the decaying values frame by frame and ends
                // at exactly 0.
                mEdgeTranslation = 0f;
            }
            if (!mRubberbanded && mEdgeTranslation != 0f) {
                mEdgeTranslation = 0f;
                mScrollView.getContentContainer().setTranslationX(0f);
            }
            if (mEdgeTranslation != 0f) {
                // We started a swipe past the edge: spring the rubber-band back.
                animateEdgeTranslationTo(0f);
                finishGesture(action);
                return false;
            }
            // It's an up and the fling didn't take it above: snap to the nearest page.
            if (action == MotionEvent.ACTION_UP && playerWidthPlusPadding > 0) {
                int relativePos = mScrollView.getRelativeScrollX() % playerWidthPlusPadding;
                int scrollXAmount;
                if (relativePos > playerWidthPlusPadding / 2) {
                    scrollXAmount = playerWidthPlusPadding - relativePos;
                } else {
                    scrollXAmount = -relativePos;
                }
                if (scrollXAmount != 0) {
                    // Delay the scrolling since scrollView calls springback which cancels
                    // the animation again (original mainExecutor.execute).
                    mSnapTargetX = mScrollView.getScrollX() + scrollXAmount;
                    mSnapPending = true;
                    // trebufork: cancel the native scroll before snapping. Without this,
                    // HorizontalScrollView's own springback/fling on this UP races the
                    // posted snap and the carousel parks between pages mid-swipe. Resolve a
                    // deferred players change FIRST and rebase the snap target on the
                    // re-anchored scroll position (same as the fling path above).
                    mScrollView.cancelCurrentScroll();
                    int oldScrollX = mScrollView.getScrollX();
                    finishGesture(action);
                    if (mSnapPending && oldScrollX != mScrollView.getScrollX()) {
                        mSnapTargetX += mScrollView.getScrollX() - oldScrollX;
                    }
                    mScrollView.post(this::runSnap);
                    return true;
                }
            }
        }
        finishGesture(action);
        // Always pass touches to the scrollView.
        return false;
    }

    private boolean onInterceptTouch(MotionEvent motionEvent) {
        trackGesture(motionEvent);
        trackLongPress(motionEvent);
        int action = motionEvent.getActionMasked();
        if (action == MotionEvent.ACTION_CANCEL) {
            // A cancel can terminate the gesture before onTouchEvent ever sees it (the
            // touch target swallowed every event): resolve a pending deferred change here.
            finishGesture(action);
        }
        return mGestureDetector.onTouchEvent(motionEvent);
    }

    /**
     * trebufork: tracks whether a pointer is down on the carousel. While it is, any
     * session-set change defers its scroll re-anchor (see onPlayersChanged) instead of
     * cancelling the user's in-flight drag/fling.
     */
    private void trackGesture(MotionEvent motionEvent) {
        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mGestureActive = true;
                // trebufork: a swipe toward an empty edge ends in a rubber-band whose
                // 150 ms decay can still be running when the NEXT swipe starts (toward a
                // player this time). The residual translation visually offsets the cards
                // and makes every exit path treat the gesture as an edge release instead
                // of a normal snap/fling — the exact "stuck mid-swipe" report. This is
                // the ONLY per-gesture reset: trackGesture runs on ACTION_DOWN from BOTH
                // touch paths (onInterceptTouch always, onTouchEvent only when no card
                // child consumed the DOWN), so the state is fresh no matter where the
                // finger lands. Idempotent when both paths see the same DOWN.
                if (mEdgeTranslation != 0f) {
                    mTranslationAnimator.cancel();
                    mEdgeTranslation = 0f;
                    mScrollView.getContentContainer().setTranslationX(0f);
                }
                mRubberbanded = false;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mGestureActive = false;
                break;
            default:
                break;
        }
    }

    /**
     * trebufork: the gesture is over (UP/CANCEL) — apply a session-set change that landed
     * while it ran. The change was deferred (see onPlayersChanged) so the pages moved
     * beneath the finger without a forced re-anchor; now that the gesture is over, settle
     * the scroll onto the still-visible page. Doing this on BOTH UP and CANCEL keeps the
     * state consistent: on UP the re-anchor runs before the posted nearest-page snap (and
     * re-targets it via the scroll-position delta), so a deferred set change can no longer
     * leave the carousel parked between pages.
     */
    private void finishGesture(int action) {
        if (action != MotionEvent.ACTION_UP && action != MotionEvent.ACTION_CANCEL) {
            return;
        }
        if (!mPlayersChangedPending) {
            return;
        }
        boolean widthChanged = mPendingWidthChanged;
        mPendingWidthChanged = false;
        applyPlayersChanged(widthChanged);
    }

    /**
     * trebufork: stationary-press detection. Down arms the check; moving beyond the touch
     * slop (a scroll starting) or lifting the finger disarms it. Holding still for the
     * system long-press timeout fires {@link #mLongPressRunnable}.
     */
    private void trackLongPress(MotionEvent motionEvent) {
        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (mLongPressArmed) {
                    // Already armed by the intercept path for this gesture — never schedule
                    // the runnable twice.
                    break;
                }
                mLongPressDownX = motionEvent.getX();
                mLongPressDownY = motionEvent.getY();
                mLongPressArmed = mLongPressAction != null;
                if (mLongPressArmed) {
                    mScrollView.postDelayed(mLongPressRunnable,
                            ViewConfiguration.getLongPressTimeout());
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mLongPressArmed
                        && (Math.abs(motionEvent.getX() - mLongPressDownX)
                                > ViewConfiguration.get(mScrollView.getContext())
                                        .getScaledTouchSlop()
                                || Math.abs(motionEvent.getY() - mLongPressDownY)
                                        > ViewConfiguration.get(mScrollView.getContext())
                                                .getScaledTouchSlop())) {
                    mLongPressArmed = false;
                    mScrollView.removeCallbacks(mLongPressRunnable);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mLongPressArmed = false;
                mScrollView.removeCallbacks(mLongPressRunnable);
                break;
            default:
                break;
        }
    }

    private void runSnap() {
        if (!mSnapPending) {
            return;
        }
        mSnapPending = false;
        mScrollView.smoothScrollTo(mSnapTargetX, mScrollView.getScrollY());
    }

    private boolean onScroll(MotionEvent down, MotionEvent lastMotion, float distanceX) {
        if (playerWidthPlusPadding == 0) {
            return false;
        }
        // trebufork: decide the branch by the CURRENT movement direction, not by the
        // cumulative offset from ACTION_DOWN (GestureDetector's distanceX > 0 means the
        // finger moved left, toward the end of the carousel). The cumulative check kept
        // the rubber-band branch alive for the whole reversal — until the finger crossed
        // the original down point — and let the offset cross zero to the OPPOSITE side;
        // the flip only became visible when native scrolling resumed, so the pull offset
        // appeared to "apply" exactly at the direction change.
        boolean draggingTowardEnd = distanceX > 0;
        boolean draggingTowardStart = distanceX < 0;
        int relativeScrollX = mScrollView.getRelativeScrollX();
        boolean atStart = relativeScrollX == 0;
        boolean atEnd = relativeScrollX
                >= mScrollView.getContentContainer().getWidth() - mScrollView.getWidth();
        boolean canScrollTowardDrag = !(atStart && draggingTowardStart)
                && !(atEnd && draggingTowardEnd);
        if (canScrollTowardDrag && mEdgeTranslation == 0f) {
            // Regular in-range drag: let the HorizontalScrollView handle the scroll
            // natively (the shade behavior — the cards slide as real views).
            mRubberbanded = false;
            return false;
        }
        // Either dragging beyond the first/last card (rubber-band the edge) or still
        // unwinding a rubber-band offset after a direction change. The offset must never
        // cross zero to the opposite side: as soon as the reversal consumes it, hand the
        // gesture back to the native scroll at EXACTLY zero — a seamless 1:1 takeover
        // with no visible flip and no double motion (the HorizontalScrollView has been
        // tracking every move event all along, so its next delta continues from here).
        mTranslationAnimator.cancel();
        float newTranslation = mEdgeTranslation - distanceX * RUBBERBAND_FACTOR;
        boolean unwound = mEdgeTranslation != 0f
                && (mEdgeTranslation > 0f ? newTranslation <= 0f : newTranslation >= 0f);
        if (unwound) {
            mRubberbanded = false;
            mEdgeTranslation = 0f;
            mScrollView.getContentContainer().setTranslationX(0f);
            // Fall through to the native scroll for this event.
            return false;
        }
        // The decay must be CANCELLED here — a running springback restarted on every
        // move event never finishes, and its residual translation then corrupts the
        // release handling below.
        mRubberbanded = true;
        mEdgeTranslation = newTranslation;
        mScrollView.getContentContainer().setTranslationX(newTranslation);
        return true;
    }

    private boolean onFling(float vX, float vY) {
        if (vX * vX < 0.5 * vY * vY) {
            return false;
        }
        if (vX * vX < FLING_SLOP) {
            return false;
        }
        // trebufork: only swallow the fling while an ACTUAL edge offset is live AND the
        // scroll is still parked on the rubber-band's boundary page. After a reversal the
        // scroll is mid-page — the fling must page normally, not "spring back" to a page
        // it has already left (the other half of the mid-swipe parking in the trace).
        boolean flingOffBoundary = playerWidthPlusPadding > 0
                && mScrollView.getRelativeScrollX() % playerWidthPlusPadding != 0;
        if (mRubberbanded && mEdgeTranslation != 0f && !flingOffBoundary) {
            // Flung while rubber-banded: always spring back to the current page.
            animateEdgeTranslationTo(0f);
            return true;
        }
        if (mEdgeTranslation != 0f) {
            // trebufork: residual translation while the scroll is already mid-page (a
            // reversed short swipe): decay it smoothly alongside the page fling instead of
            // teleporting the card to the un-translated position first.
            animateEdgeTranslationTo(0f);
            mEdgeTranslation = 0f;
            mScrollView.getContentContainer().setTranslationX(
                    mTranslationAnimator.getAnimatedValue() instanceof Float f ? f : 0f);
        }
        // We're flinging the player! Go to either the previous or the next player
        // (MediaCarouselScrollHandler.onFling).
        int pos = mScrollView.getRelativeScrollX();
        int currentIndex = playerWidthPlusPadding > 0 ? pos / playerWidthPlusPadding : 0;
        boolean flungTowardEnd = vX < 0;
        int destIndex = flungTowardEnd ? currentIndex + 1 : currentIndex;
        destIndex = Math.max(0, destIndex);
        ViewGroup content = mScrollView.getContentContainer();
        destIndex = Math.min(content.getChildCount() - 1, destIndex);
        View view = content.getChildAt(destIndex);
        if (view == null) {
            return true;
        }
        // We need to post this since we're dispatching a touch to the underlying view to
        // cancel but canceling will actually abort the animation.
        mSnapTargetX = view.getLeft();
        mSnapPending = true;
        mScrollView.post(this::runSnap);
        return true;
    }

    private void animateEdgeTranslationTo(float target) {
        mTranslationAnimator.cancel();
        mTranslationAnimator.setFloatValues(mEdgeTranslation, target);
        mTranslationAnimator.setDuration(150);
        mTranslationAnimator.start();
    }

    private void onMediaScrollingChanged(int newIndex, int scrollInAmount) {
        boolean wasScrolledIn = scrollIntoCurrentMedia != 0;
        scrollIntoCurrentMedia = scrollInAmount;
        boolean nowScrolledIn = scrollIntoCurrentMedia != 0;
        if (newIndex != visibleMediaIndex || wasScrolledIn != nowScrolledIn) {
            visibleMediaIndex = newIndex;
            if (!nowScrolledIn && mVisibleCardChangedListener != null) {
                // The card is fully visible: the page settled on it (drag commit).
                mVisibleCardChangedListener.onVisibleCardChanged(visibleMediaIndex);
            }
        }
        float relativeLocation = visibleMediaIndex
                + (playerWidthPlusPadding > 0
                ? (float) scrollInAmount / playerWidthPlusPadding : 0f);
        mPageIndicator.setLocation(relativeLocation);
        mScrollView.updateClipToOutline(scrollIntoCurrentMedia != 0);
    }

    /** Called when the page set or the page width changed; re-anchors the scroll. */
    void onPlayersChanged(int newPlayerWidthPlusPadding) {
        boolean widthChanged = newPlayerWidthPlusPadding != playerWidthPlusPadding;
        playerWidthPlusPadding = newPlayerWidthPlusPadding;
        int childCount = mScrollView.getContentContainer().getChildCount();
        if (visibleMediaIndex > childCount - 1) {
            visibleMediaIndex = Math.max(0, childCount - 1);
        }
        if (mGestureActive) {
            // trebufork: forcing scrollX right now would cancel the user's in-flight
            // drag/fling and park the carousel between pages (a rebuild fired by a playback
            // change or a media-notification update lands exactly mid-swipe). Defer the
            // re-anchor until the gesture ends.
            mPlayersChangedPending = true;
            mPendingWidthChanged = widthChanged;
            return;
        }
        applyPlayersChanged(widthChanged);
    }

    private void applyPlayersChanged(boolean widthChanged) {
        mPlayersChangedPending = false;
        mScrollView.setRelativeScrollX(visibleMediaIndex * playerWidthPlusPadding);
        if (widthChanged) {
            // Keep the page dots settled after a size change (no fractional position).
            mPageIndicator.setLocation(visibleMediaIndex);
        }
    }

    /** Forces the carousel to a page (session pinning from outside). */
    void setVisibleMediaIndex(int index) {
        visibleMediaIndex = Math.max(0, index);
        mPageIndicator.setLocation(visibleMediaIndex);
        mScrollView.updateClipToOutline(false);
        if (mGestureActive) {
            // trebufork: a mid-gesture jump (the active session changed under the user's
            // finger) would cancel the drag/fling — apply it when the gesture ends.
            mPlayersChangedPending = true;
            return;
        }
        mScrollView.setRelativeScrollX(visibleMediaIndex * playerWidthPlusPadding);
    }

    /** Resets to the first page. */
    void scrollToStart() {
        setVisibleMediaIndex(0);
    }

    /** The scroll handler must be detached from the scroll view's touch listener. */
    void detach() {
        mScrollView.setTouchListener(null);
    }
}
