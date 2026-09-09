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
        int action = motionEvent.getActionMasked();
        boolean isUp = action == MotionEvent.ACTION_UP;
        if (mGestureDetector.onTouchEvent(motionEvent)) {
            if (isUp) {
                // If this is an up and we're flinging, we don't want to have this touch
                // reach the view, otherwise that would scroll, while we are trying to snap
                // to the new page. Let's dispatch a cancel instead.
                mScrollView.cancelCurrentScroll();
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
            if (mEdgeTranslation != 0f) {
                // We started a swipe past the edge: spring the rubber-band back.
                animateEdgeTranslationTo(0f);
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
                    // posted snap and the carousel parks between pages mid-swipe.
                    mScrollView.cancelCurrentScroll();
                    mScrollView.post(this::runSnap);
                    return true;
                }
            }
        }
        // Always pass touches to the scrollView.
        return false;
    }

    private boolean onInterceptTouch(MotionEvent motionEvent) {
        return mGestureDetector.onTouchEvent(motionEvent);
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
        float totalX = lastMotion.getX() - down.getX();
        boolean canScrollTowardDrag = mScrollView.canScrollHorizontally((int) -totalX);
        if (canScrollTowardDrag) {
            // Regular in-range drag: let the HorizontalScrollView handle the scroll
            // natively (the shade behavior — the cards slide as real views). Cancel any
            // edge rubber-band as soon as the drag returns into the valid range.
            if (mEdgeTranslation != 0f) {
                animateEdgeTranslationTo(0f);
            }
            return false;
        }
        // Dragging beyond the first/last card: rubber-band the edge (MediaCarouselScroll
        // Handler.onScroll's rubberband branch). The translation is applied to the
        // content and springs back on release.
        float newTranslation = mEdgeTranslation - distanceX * RUBBERBAND_FACTOR;
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
        if (mEdgeTranslation != 0f) {
            // Flung while rubber-banded: always spring back to the current page.
            animateEdgeTranslationTo(0f);
            return true;
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
        mScrollView.setRelativeScrollX(visibleMediaIndex * playerWidthPlusPadding);
        if (widthChanged) {
            // Keep the page dots settled after a size change (no fractional position).
            mPageIndicator.setLocation(visibleMediaIndex);
        }
    }

    /** Forces the carousel to a page (session pinning from outside). */
    void setVisibleMediaIndex(int index) {
        visibleMediaIndex = Math.max(0, index);
        mScrollView.setRelativeScrollX(visibleMediaIndex * playerWidthPlusPadding);
        mPageIndicator.setLocation(visibleMediaIndex);
        mScrollView.updateClipToOutline(false);
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
