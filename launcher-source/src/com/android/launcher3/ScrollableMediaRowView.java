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
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import java.util.List;

/**
 * trebufork: the built-in media player row of the scrollable home — the carousel of the
 * SystemUI shade/lockscreen media controls (MediaScrollView + MediaCarouselScrollHandler,
 * lineage-23.2) hosting ONE REAL PLAYER CARD PER MEDIA SESSION
 * ({@link ScrollableMediaCardView}, the media_session_view.xml + MediaControlPanel port):
 * the cards slide as real views when the user swipes, with the shade's fling-to-page,
 * snap-to-nearest, rubber-band at the ends, rounded-corner clipping mid-swipe and the
 * animated page dots (the PageIndicator port) tracking the fractional position.
 */
public class ScrollableMediaRowView extends FrameLayout implements ScrollableResizableRow {

    // The card carousel (a HorizontalScrollView; the touch/fling/snap logic lives in
    // ScrollableMediaCarouselScrollHandler — the MediaCarouselScrollHandler port).
    private final ScrollableMediaScrollView mScrollView;
    private final LinearLayout mCardContent;
    private final ScrollableMediaPageIndicator mPageIndicator;
    private final ScrollableMediaCarouselScrollHandler mScrollHandler;

    @Nullable
    private ScrollableMediaController mSource;
    // Cards currently in the carousel, parallel to the carousel sessions.
    private final java.util.List<ScrollableMediaCardView> mCards = new java.util.ArrayList<>();
    // The sessions backing the cards (by token), so reorders keep pages anchored.
    private final java.util.List<MediaSession.Token> mCardTokens = new java.util.ArrayList<>();
    // Index of the card currently shown (kept in sync with the scroll handler).
    private int mCarouselIndex;
    // True while a rebuild is in progress, so listener callbacks don't recurse.
    private boolean mRebuilding;

    // User-configurable size, persisted in ScrollableDesktopStore (same fields as widget
    // rows): width relative to the list width, height relative to the natural height.
    // The width is capped below 1.0 so the card never slides under the alphabet index
    // strip on the right edge of the desktop.
    public static final float MAX_WIDTH_SCALE = 0.9f;
    private float mWidthScale = 1f;
    private float mHeightScale = 1f;
    private float mPositionX = 0f;
    // Last measured card width (contentWidth), for the explicit card sizing above.
    private int mLastContentWidth;

    public ScrollableMediaRowView(Context context) {
        this(context, null);
    }

    public ScrollableMediaRowView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollableMediaRowView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        inflate(getContext(), R.layout.scrollable_media_carousel_row, this);
        mScrollView = findViewById(R.id.scrollable_media_carousel_scroll);
        mCardContent = findViewById(R.id.scrollable_media_carousel_content);
        mPageIndicator = findViewById(R.id.scrollable_media_page_indicator);
        mScrollHandler = new ScrollableMediaCarouselScrollHandler(mScrollView, mPageIndicator);
        // A page the user settled on (drag or fling) becomes the active session — the
        // SystemUI "swiped-to player stays visible" behavior (pinSession).
        mScrollHandler.setVisibleCardChangedListener(index -> {
            if (index != mCarouselIndex && index < mCardTokens.size() && mSource != null) {
                mCarouselIndex = index;
                List<MediaController> sessions = mSource.getSessionList();
                for (MediaController session : sessions) {
                    if (session.getSessionToken().equals(mCardTokens.get(index))) {
                        mSource.pinSession(session);
                        break;
                    }
                }
            }
        });
    }

    /** Binds the media source (the shared session monitor). Safe to call repeatedly. */
    public void setSource(@Nullable ScrollableMediaController source) {
        if (mSource == source) {
            return;
        }
        if (mSource != null) {
            mSource.removeListener(mSourceListener);
        }
        mSource = source;
        if (mSource != null) {
            mSource.addListener(mSourceListener);
        }
        rebuildCarousel();
    }

    private final ScrollableMediaController.Listener mSourceListener =
            new ScrollableMediaController.Listener() {
                @Override
                public void onActiveControllerChanged() {
                    post(ScrollableMediaRowView.this::rebuildCarousel);
                }

                @Override
                public void onMediaChanged() {
                    post(() -> {
                        // Metadata/playback changes do not alter the page set: rebind the
                        // card of the changed session only (a full rebuild would reset the
                        // scroll position mid-gesture).
                        MediaController active = mSource == null
                                ? null : mSource.getController();
                        for (ScrollableMediaCardView card : mCards) {
                            MediaController bound = card.getBoundController();
                            if (bound != null && bound.equals(active)) {
                                card.setController(mSource, active);
                            }
                        }
                    });
                }
            };

    /**
     * Rebuilds the carousel page list from the shared monitor. Cards are keyed by session
     * token: sessions that stay keep their card (and the scroll position), removed ones
     * drop out, new ones get a card appended — exactly how the shade's
     * MediaCarouselController diffs the player set.
     */
    private void rebuildCarousel() {
        if (mRebuilding) {
            return;
        }
        mRebuilding = true;
        try {
            List<MediaController> sessions = mSource == null
                    ? java.util.Collections.emptyList() : mSource.getSessionList();
            java.util.List<ScrollableMediaCardView> newCards = new java.util.ArrayList<>();
            java.util.List<MediaSession.Token> newTokens = new java.util.ArrayList<>();
            for (MediaController session : sessions) {
                ScrollableMediaCardView existing = findCard(session.getSessionToken());
                if (existing == null) {
                    existing = new ScrollableMediaCardView(getContext());
                    existing.setController(mSource, session);
                }
                newCards.add(existing);
                newTokens.add(session.getSessionToken());
            }
            // Unregister cards that dropped out (their controller callbacks are cleared
            // inside the card when the binding changes).
            for (ScrollableMediaCardView card : mCards) {
                if (!newCards.contains(card)) {
                    MediaController bound = card.getBoundController();
                    card.setController(null, null);
                }
            }
            mCards.clear();
            mCards.addAll(newCards);
            mCardTokens.clear();
            mCardTokens.addAll(newTokens);

            mCardContent.removeAllViews();
            int padding = getResources().getDimensionPixelSize(
                    R.dimen.scrollable_media_padding);
            for (int i = 0; i < mCards.size(); i++) {
                ScrollableMediaCardView card = mCards.get(i);
                // updateMediaPaddings: every card except the last carries the end margin
                // (qs_media_padding) that makes one page exactly one card width. The width
                // is set explicitly during onMeasure (below) — like the shade, where the
                // carousel measures each player at exactly its own width.
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        mLastContentWidth > 0 ? mLastContentWidth
                                : ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.rightMargin = i == mCards.size() - 1 ? 0 : padding;
                mCardContent.addView(card, lp);
            }

            mPageIndicator.setNumPages(mCards.size());
            if (mScrollHandler != null) {
                mScrollHandler.onPlayersChanged(
                        mScrollView.getWidth() > 0 ? mScrollView.getWidth() + padding : 0);
            }
            // Keep the pinned/active session's page on screen when the set changes.
            MediaController active = mSource == null ? null : mSource.getController();
            if (active != null) {
                int index = mCardTokens.indexOf(active.getSessionToken());
                if (index >= 0 && index != mCarouselIndex) {
                    mCarouselIndex = index;
                    mScrollHandler.setVisibleMediaIndex(index);
                }
            }
        } finally {
            mRebuilding = false;
        }
    }

    @Nullable
    private ScrollableMediaCardView findCard(MediaSession.Token token) {
        for (int i = 0; i < mCardTokens.size(); i++) {
            if (mCardTokens.get(i).equals(token)) {
                return mCards.get(i);
            }
        }
        return null;
    }

    // ----------------------------------------------------------------------
    // Scaled row sizing (see ScrollableResizableRow / ScrollableWidgetResizeFrame)
    // ----------------------------------------------------------------------

    @Override
    public View getWidgetView() {
        return this;
    }

    @Override
    public void setScales(float widthScale, float heightScale) {
        if (widthScale <= 0f) {
            widthScale = 1f;
        }
        // Never let the card cover the alphabet index on the right.
        widthScale = Math.min(widthScale, MAX_WIDTH_SCALE);
        if (heightScale <= 0f) {
            heightScale = 1f;
        }
        if (mWidthScale != widthScale || mHeightScale != heightScale) {
            mWidthScale = widthScale;
            mHeightScale = heightScale;
            mScrollView.setHeightScale(heightScale);
            requestLayout();
        }
    }

    @Override
    public void setPositionX(float positionX) {
        positionX = Math.max(0f, Math.min(1f, positionX));
        if (mPositionX != positionX) {
            mPositionX = positionX;
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        if (width == 0) {
            setMeasuredDimension(width, 0);
            return;
        }
        // The carousel is the single child; measure it at the scaled width so the
        // ConstraintLayout inside each card resolves all its constraints at the final
        // size. Each card is measured at EXACTLY this width (the shade measures every
        // player at the carousel width too — MediaCarouselController.setCarouselBounds +
        // the media_player width WRAP_CONTENT on the TransitionLayout).
        int contentWidth = Math.round(width * mWidthScale);
        int padding = getResources().getDimensionPixelSize(R.dimen.scrollable_media_padding);
        if (contentWidth != mLastContentWidth) {
            mLastContentWidth = contentWidth;
            for (int i = 0; i < mCards.size(); i++) {
                LinearLayout.LayoutParams lp =
                        (LinearLayout.LayoutParams) mCards.get(i).getLayoutParams();
                lp.width = contentWidth;
                lp.rightMargin = i == mCards.size() - 1 ? 0 : padding;
            }
            if (mScrollHandler != null && contentWidth > 0) {
                // One page = one card width + its end margin.
                mScrollHandler.onPlayersChanged(contentWidth + padding);
            }
        }
        // Measure the scroll view exactly at the card width: this is the carousel
        // viewport. Height is UNSPECIFIED so it takes the card's natural height; the
        // user height scale is applied inside the scroll view itself.
        mScrollView.measure(
                MeasureSpec.makeMeasureSpec(contentWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        // The page indicator overlays the card's bottom edge; measure it too (it is
        // positioned manually in onLayout).
        mPageIndicator.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        int naturalHeight = mScrollView.getMeasuredHeight();
        int height = Math.round(naturalHeight * mHeightScale);
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
            height = MeasureSpec.getSize(heightMeasureSpec);
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int rowWidth = r - l;
        int rowHeight = b - t;
        int contentWidth = Math.round(rowWidth * mWidthScale);
        int freeSpace = Math.max(0, rowWidth - contentWidth);
        int offsetX = Math.round(freeSpace * mPositionX);
        if (mScrollView.getVisibility() != GONE) {
            int childWidth = Math.min(contentWidth, mScrollView.getMeasuredWidth());
            int childHeight = Math.min(rowHeight, mScrollView.getMeasuredHeight());
            mScrollView.layout(offsetX, 0, offsetX + childWidth, childHeight);
        }
        // The page indicator overlays the carousel's bottom edge, inside the row bounds.
        int indicatorWidth = mPageIndicator.getMeasuredWidth();
        int indicatorHeight = mPageIndicator.getMeasuredHeight();
        int indicatorX = offsetX + (contentWidth - indicatorWidth) / 2;
        mPageIndicator.layout(indicatorX, rowHeight - indicatorHeight - 4,
                indicatorX + indicatorWidth, rowHeight - 4);
    }

    /** Single background thread for the Monet scheme extraction (like mBackgroundExecutor). */
    private static final class Executors {
        static final java.util.concurrent.ExecutorService SINGLE =
                java.util.concurrent.Executors.newSingleThreadExecutor(
                        r -> {
                            Thread t = new Thread(r, "trebufork-monet");
                            t.setPriority(Thread.NORM_PRIORITY - 1);
                            return t;
                        });
    }
}
