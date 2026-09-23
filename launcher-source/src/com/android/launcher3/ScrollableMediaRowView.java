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
import android.animation.ValueAnimator;
import android.view.animation.AnimationUtils;
import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;

import androidx.annotation.Nullable;

import com.android.launcher3.statemanager.StateManager;

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
    // The session token of the card the user is actually looking at (settled page):
    // rebuilds re-anchor the carousel to THIS card, never to an index, so a reorder can
    // never swap a different player in front of the user.
    @Nullable
    private MediaSession.Token mVisibleCardToken;
    // True while a rebuild is in progress, so listener callbacks don't recurse.
    private boolean mRebuilding;
    // True while a rebuild is deferred until the launcher leaves its transition.
    private boolean mDeferredRebuild;
    // trebufork: the row height measured at the last stable (focused, idle home) moment.
    // Kept across unfocused measurements so a session appearing/disappearing mid-launch
    // cannot change the row size and shift the desktop or the animating app window.
    // -1 until the first stable measurement.
    private int mLastStableHeight = -1;
    // trebufork: in-flight height animation (expand/collapse at idle home). While running,
    // onMeasure reports the animated value instead of jumping to the new height in one frame.
    @Nullable
    private ValueAnimator mHeightAnimator;

    // trebufork: global count of in-flight height animations across all rows (rows are
    // created/destroyed dynamically, so a plain static int guarded by start/end/cancel
    // listeners is not enough). Launches consult it (Launcher.startActivitySafely) and wait
    // for the animation to finish instead of capturing icon geometry mid-relayout — the row
    // animation itself is untouched, other animations simply start after it settles.
    private static final java.util.Set<Object> sRunningAnimations =
            java.util.Collections.newSetFromMap(new java.util.WeakHashMap<>());
    // Registered per running animator; removed on end/cancel.
    @Nullable
    private Object mAnimationToken;

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
        // trebufork: re-measure after every completed launcher transition — covers paths
        // where window focus never left the launcher window (e.g. closing a player from
        // recents), so the row can grow/collapse once back at idle home.
        registerStableHeightTrigger();
        // trebufork: HorizontalScrollView runs its own onTouchEvent and never reaches the
        // framework long-press detection, so a long-click listener on it is unreachable from
        // a real touch. The scroll handler detects a stationary press in its touch pipeline
        // (which sees every event) and fires this action — a press held still on the card
        // body opens the remove/reorder menu, exactly like long-pressing a widget row.
        mScrollHandler.setOnLongPressAction(() -> {
            android.util.Log.d("TrebuforkMedia", "carousel long-press -> row menu");
            performLongClick();
        });
        // A page the user settled on (drag or fling) becomes the active session — the
        // SystemUI "swiped-to player stays visible" behavior (pinSession).
        mScrollHandler.setVisibleCardChangedListener(index -> {
            if (index != mCarouselIndex && index < mCardTokens.size() && mSource != null) {
                mCarouselIndex = index;
                mVisibleCardToken = mCardTokens.get(index);
                updateSeekbarListening();
                List<MediaController> sessions = mSource.getSessionList();
                for (MediaController session : sessions) {
                    if (session.getSessionToken().equals(mCardTokens.get(index))) {
                        // fromSwipe=true: the activation came from the user's own gesture,
                        // so the row must not reorder the pages (no exit/enter transition
                        // replayed over the swipe the user just made).
                        mSource.pinSession(session, true);
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
                    post(ScrollableMediaRowView.this::rebuildCarouselWhenIdle);
                }

                @Override
                public void onSessionListChanged() {
                    // trebufork: the SESSION SET changed (a player appeared/died) even though
                    // the active one may not have — like the shade's carousel, which rebuilds
                    // on every media-data update. Rebuild (deferred while a transition runs,
                    // same as above).
                    post(ScrollableMediaRowView.this::rebuildCarouselWhenIdle);
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
     * trebufork: rebuilding the carousel (a new session's card appearing) changes the row's
     * measured height, which relayouts the whole desktop. When that happens while an app
     * launch animation is in flight (the app that just started a session), the relayout
     * shifts the animating window. So the rebuild is deferred until the launcher is back
     * in its NORMAL state, exactly what the shade does by not running the media transition
     * during an app launch.
     */
    private void rebuildCarouselWhenIdle() {
        rebuildCarouselWhenIdle(false);
    }

    private void rebuildCarouselWhenIdle(boolean reorderOnHomeReturn) {
        Launcher launcher;
        try {
            launcher = Launcher.getLauncher(getContext());
        } catch (ClassCastException | IllegalStateException e) {
            rebuildCarousel(reorderOnHomeReturn);
            return;
        }
        if (launcher.getStateManager().isInTransition()
                || launcher.getStateManager().getState() != LauncherState.NORMAL) {
            // Defer: run the rebuild when the launch animation is done.
            if (!mDeferredRebuild) {
                mDeferredRebuild = true;
                launcher.getStateManager().addStateListener(new StateManager.StateListener<LauncherState>() {
                    @Override
                    public void onStateTransitionComplete(LauncherState finalState) {
                        launcher.getStateManager().removeStateListener(this);
                        mDeferredRebuild = false;
                        post(() -> rebuildCarousel(reorderOnHomeReturn));
                    }
                });
            }
            return;
        }
        rebuildCarousel(reorderOnHomeReturn);
    }

    /** No-reorder rebuild (session events, playback changes, play/pause taps). */
    private void rebuildCarousel() {
        rebuildCarousel(false);
    }

    /**
     * Re-anchors the carousel to the card the user is looking at (by session token), or
     * to the active (playing) player on the home-return reorder. Never an index: an index
     * anchor after a reorder would show a DIFFERENT player than the user had on screen.
     */
    private void anchorCarousel(boolean toActivePlayer) {
        MediaController target = null;
        if (toActivePlayer) {
            target = mSource == null ? null : mSource.getController();
        }
        if (target == null) {
            ScrollableMediaCardView visible = findCard(mVisibleCardToken);
            if (visible != null && visible.getBoundController() != null) {
                target = visible.getBoundController();
            }
        }
        if (target != null) {
            int index = mCardTokens.indexOf(target.getSessionToken());
            if (index >= 0) {
                if (index != mCarouselIndex) {
                    mCarouselIndex = index;
                    mVisibleCardToken = target.getSessionToken();
                    mScrollHandler.setVisibleMediaIndex(index);
                }
                return;
            }
        }
        if (mVisibleCardToken != null && !mCardTokens.contains(mVisibleCardToken)) {
            mVisibleCardToken = null;
        }
    }

    /**
     * Rebuilds the carousel page list from the shared monitor. Cards are keyed by session
     * token: sessions that stay keep their card (and the scroll position), removed ones
     * drop out, new ones get a card appended — exactly how the shade's
     * MediaCarouselController diffs the player set.
     */
    private void rebuildCarousel(boolean reorderOnHomeReturn) {
        if (mRebuilding) {
            return;
        }
        mRebuilding = true;
        try {
            List<MediaController> sessions = mSource == null
                    ? java.util.Collections.emptyList() : mSource.getSessionList();
            // trebufork: a rebuild with an UNCHANGED card set must not touch the carousel
            // at all: removeAllViews + re-add resets the scroll position, and the following
            // onPlayersChanged force-anchors scrollX — together they cancel a swipe that is
            // in flight (any playback-state change or media-notification update re-runs
            // this). Bail out early when the session set is identical.
            // trebufork: the carousel order follows the ACTIVE session first, the rest in
            // system order (getCarouselSessions / the shade's re-ranked player list): when
            // another app starts playing, its player moves to the FRONT — even while the
            // user is looking at the desktop. A pure reorder (same sessions, new order)
            // plays the metadata exit/enter transition instead of snapping (see below).
            java.util.List<ScrollableMediaCardView> newCards = new java.util.ArrayList<>();
            java.util.List<MediaSession.Token> newTokens = new java.util.ArrayList<>();
            MediaController activeSession = mSource == null ? null : mSource.getController();
            // Pass 1a: the active session's card first — ONLY when a reorder was armed by
            // returning to the desktop (passed as reorderOnHomeReturn). Otherwise the current
            // order is kept: swipes and play/pause taps never shuffle the pages.
            boolean reorder = reorderOnHomeReturn;
            if (activeSession != null && reorder) {
                ScrollableMediaCardView card = findCard(activeSession.getSessionToken());
                if (card != null) {
                    newCards.add(card);
                    newTokens.add(activeSession.getSessionToken());
                }
            }
            // Pass 1b: surviving cards KEEP THEIR CURRENT VISIBLE ORDER (not the system
            // order): a rebuild triggered by a play/pause tap or a notification update
            // must never silently reshuffle pages in front of the user. Only the
            // active-first pass (1a, armed by returning to the desktop) may reorder.
            // trebufork (like SystemUI's MediaCarouselController, which diffs the player
            // list against the authoritative mediaEntries map): a card may only survive
            // if its session is STILL in the controller's session list — otherwise a
            // duplicate that the controller already dropped (e.g. two sessions of one
            // app) would keep its card forever.
            java.util.Set<MediaSession.Token> liveTokens = new java.util.HashSet<>();
            for (MediaController session : sessions) {
                liveTokens.add(session.getSessionToken());
            }
            for (ScrollableMediaCardView existing : mCards) {
                MediaController bound = existing.getBoundController();
                MediaSession.Token token = bound == null ? null : bound.getSessionToken();
                if (token == null || !liveTokens.contains(token) || newTokens.contains(token)
                        || (activeSession != null && reorder
                                && token.equals(activeSession.getSessionToken()))) {
                    continue;
                }
                newCards.add(existing);
                newTokens.add(token);
            }
            // Pass 2: brand-new sessions, appended.
            // trebufork: when a package's representative session SWAPPED (the dedupe
            // picked its playing session — e.g. pressing play in an mpv-based player),
            // the orphaned card is REBOUND to the new session instead of destroying and
            // recreating it: a fresh card would flash the white album placeholder (its
            // Monet scheme is extracted asynchronously) and replay the title enter
            // animation from blank, right in front of the user.
            java.util.List<ScrollableMediaCardView> orphans = new java.util.ArrayList<>();
            for (ScrollableMediaCardView card : mCards) {
                if (!newCards.contains(card)) {
                    orphans.add(card);
                }
            }
            for (MediaController session : sessions) {
                if (findCard(session.getSessionToken()) == null
                        && !newTokens.contains(session.getSessionToken())) {
                    ScrollableMediaCardView card = null;
                    for (java.util.Iterator<ScrollableMediaCardView> it =
                            orphans.iterator(); it.hasNext(); ) {
                        ScrollableMediaCardView orphan = it.next();
                        MediaController bound = orphan.getBoundController();
                        if (bound != null && bound.getPackageName()
                                .equals(session.getPackageName())) {
                            card = orphan;
                            it.remove();
                            break;
                        }
                    }
                    if (card == null) {
                        card = new ScrollableMediaCardView(getContext());
                    }
                    card.setController(mSource, session);
                    newCards.add(card);
                    newTokens.add(session.getSessionToken());
                }
            }
            // Unregister cards that dropped out for real (their controller callbacks are
            // cleared inside the card when the binding changes).
            for (ScrollableMediaCardView card : orphans) {
                card.setController(null, null);
            }
            // trebufork: cheap no-set-change check BEFORE mutating anything: if the
            // surviving+appended token list equals the current one (same order, same
            // tokens), the child views are already correct — skip removeAllViews/re-add and
            // onPlayersChanged entirely so an in-flight swipe is never disturbed by a no-op
            // rebuild (any playback-state change or media-notification update re-runs this
            // through onActiveControllerChanged).
            boolean sameSet = newTokens.size() == mCardTokens.size();
            if (sameSet) {
                for (int i = 0; i < newTokens.size(); i++) {
                    if (!newTokens.get(i).equals(mCardTokens.get(i))) {
                        sameSet = false;
                        break;
                    }
                }
            }
            if (sameSet) {
                return;
            }
            // trebufork: a PURE REORDER (identical session set, different order) applies
            // silently: pages never shuffle while the user interacts — reordering only
            // happens on RETURNING TO THE DESKTOP (reorderOnHomeReturn, passed by window
            // focus / a completed launcher transition), and even then it is a quiet swap
            // without any exit/enter animation (the user asked for no slide animation).
            boolean pureReorder = newCards.size() == mCards.size()
                    && newCards.containsAll(mCards);
            int padding = getResources().getDimensionPixelSize(
                    R.dimen.scrollable_media_padding);
            if (pureReorder) {
                applyNewOrder(newCards, newTokens, padding);
                anchorCarousel(reorder);
                return;
            }
            mCards.clear();
            mCards.addAll(newCards);
            mCardTokens.clear();
            mCardTokens.addAll(newTokens);

            mCardContent.removeAllViews();
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
            // Port of MediaCarouselController.updateSeekbarListening: only the page the
            // user is looking at extrapolates its progress; hidden cards stop ticking.
            updateSeekbarListening();
            // Keep the pinned/visible session's page on screen when the set changes: the
            // page is located by SESSION TOKEN (the visible card the user is looking at),
            // never by index — an index anchor after a reorder would show a DIFFERENT
            // player than the one the user had on screen. On the home-return reorder the
            // ACTIVE (playing) player is shown instead — see anchorCarousel.
            anchorCarousel(reorder);
        } finally {
            mRebuilding = false;
        }
    }

    /**
     * trebufork: applies a new card order instantly (used while a swipe gesture is active
     * — the touch pipeline must keep its page — and as the hidden middle step of the
     * animated reorder).
     */
    private void applyNewOrder(java.util.List<ScrollableMediaCardView> newCards,
            java.util.List<MediaSession.Token> newTokens, int padding) {
        mCards.clear();
        mCards.addAll(newCards);
        mCardTokens.clear();
        mCardTokens.addAll(newTokens);
        relayoutCards(padding);
    }

    /** Re-adds the cards in current mCards order with correct page widths/margins. */
    private void relayoutCards(int padding) {
        mCardContent.removeAllViews();
        for (int i = 0; i < mCards.size(); i++) {
            ScrollableMediaCardView card = mCards.get(i);
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
    }

    //
    // trebufork: reorder gating. The carousel reorders ONLY on the "user returned to the
    // desktop" rebuilds: the flag is passed as a parameter (reorderOnHomeReturn) from
    // exactly two call sites — window focus regained and a launcher transition completing
    // into NORMAL. Every other rebuild (session events, playback changes, play/pause
    // taps) passes false and keeps the current card order verbatim. A shared boolean
    // field was deliberately NOT used: a stale armed flag consumed by a later unrelated
    // rebuild reordered pages in front of the user while they were interacting.

    /**
     * Port of MediaCarouselController.updateSeekbarListening(visibleToUser): feeds each
     * card its visibility so only the visible one runs its progress extrapolation tick.
     */
    private void updateSeekbarListening() {
        for (int i = 0; i < mCards.size(); i++) {
            mCards.get(i).setVisibleToUser(i == mCarouselIndex);
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
        // trebufork: a session appearing (or disappearing) while an app-launch animation is in
        // flight would change this row's height and shift the whole desktop — including the
        // animating window. So while the launcher is NOT in a stable, focused home state
        // (launch animation running, another app in the foreground, a state transition active)
        // the row KEEPS ITS LAST STABLE HEIGHT — no growth, no collapse mid-animation. The new
        // natural height is adopted only once the user is back at an idle home screen, where a
        // size change cannot corrupt any animation.
        int height = Math.round(naturalHeight * mHeightScale);
        boolean launcherStable = false;
        try {
            Launcher launcher = Launcher.getLauncher(getContext());
            launcherStable = (launcher.getActivityFlags()
                    & BaseActivity.ACTIVITY_STATE_WINDOW_FOCUSED) != 0
                    && !launcher.getStateManager().isInTransition()
                    && launcher.getStateManager().getState() == LauncherState.NORMAL;
        } catch (ClassCastException | IllegalStateException ignored) {
            // No launcher context (e.g. preview): behave normally.
            launcherStable = true;
        }
        if (launcherStable) {
            if (mLastStableHeight >= 0 && height != mLastStableHeight
                    && mHeightAnimator == null) {
                // trebufork: the row grew (player appeared) or collapsed (sessions gone) at
                // idle home — animate the change instead of snapping, so the desktop rows
                // below glide instead of jumping in a single frame.
                startHeightAnimation(mLastStableHeight, height);
                height = mLastStableHeight;
            } else if (mHeightAnimator != null) {
                // Report the animated intermediate height while the transition runs.
                height = (int) mHeightAnimator.getAnimatedValue();
            } else {
                mLastStableHeight = height;
            }
        } else if (mLastStableHeight >= 0) {
            if (mHeightAnimator != null) {
                // trebufork: a collapse/expand that started in the same frame as an app
                // launch (e.g. the media session dropped because the launched app paused)
                // must not keep mutating the layout while the open animation is in flight:
                // its onAnimationEnd sets mLastStableHeight to the target height, which
                // collapses/grows the row MID-LAUNCH and shifts the whole desktop while the
                // icon geometry was captured with the old height ("teleport by the widget
                // height" before the real surface appears). Cancel the animator WITHOUT
                // adopting its target: the row stays frozen at mLastStableHeight (the height
                // the launch geometry was captured with), and the change is re-evaluated —
                // and animated as usual — once home is stable again. The animation logic
                // itself is untouched at idle home.
                android.util.Log.d("TrebuforkMedia", "heightAnim cancelled (launcher unstable)"
                        + " uptime=" + android.os.SystemClock.uptimeMillis());
                mHeightAnimator.cancel();
                mHeightAnimator = null;
            }
            if (height != mLastStableHeight) {
                android.util.Log.d("TrebuforkMedia", "row pinned: natural=" + height
                        + " lastStable=" + mLastStableHeight + " cards=" + mCards.size());
            }
            height = mLastStableHeight;
        }
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
            height = MeasureSpec.getSize(heightMeasureSpec);
        }
        setMeasuredDimension(width, height);
    }

    /**
     * trebufork: animates the row between its previous stable height and the new natural
     * height (expand when a player appears, collapse when the last session ends). The
     * animator drives onMeasure through mHeightAnimator until it completes; the pinning
     * machinery is untouched, since the animation only ever runs at idle home.
     */
    private void startHeightAnimation(int from, int to) {
        if (mHeightAnimator != null) {
            mHeightAnimator.cancel();
            mHeightAnimator = null;
        }
        android.util.Log.d("TrebuforkMedia", "heightAnim start " + from + "->" + to
                + " uptime=" + android.os.SystemClock.uptimeMillis());
        ValueAnimator anim = ValueAnimator.ofInt(from, to);
        anim.setDuration(220);
        anim.setInterpolator(AnimationUtils.loadInterpolator(getContext(),
                android.R.interpolator.fast_out_slow_in));
        anim.addUpdateListener(a -> {
            requestLayout();
            // The parent RecyclerView positions rows from its own layout pass; keep the
            // desktop consistent during the height change.
            if (getParent() instanceof View parent) {
                parent.requestLayout();
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {
            private boolean mCancelled;

            @Override
            public void onAnimationCancel(Animator animation) {
                mCancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mHeightAnimator = null;
                android.util.Log.d("TrebuforkMedia", "heightAnim end (cancelled=" + mCancelled
                        + ") uptime=" + android.os.SystemClock.uptimeMillis());
                if (mAnimationToken != null) {
                    sRunningAnimations.remove(mAnimationToken);
                    mAnimationToken = null;
                }
                notifyHeightAnimationEnd();
                if (!mCancelled) {
                    mLastStableHeight = to;
                }
                requestLayout();
            }
        });
        mHeightAnimator = anim;
        // trebufork: register this animator in the global in-flight set BEFORE start (the
        // end listener may fire synchronously on cancel) so a concurrent launch deferral
        // always sees a consistent count. The token just needs identity; the animator is
        // retained only while it runs.
        Object token = new Object();
        sRunningAnimations.add(token);
        mAnimationToken = token;
        anim.start();
    }

    // trebufork: callbacks waiting for ALL in-flight height animations to finish (see
    // hasHeightAnimationsRunning / runWhenHeightAnimationsEnd).
    private static final java.util.List<Runnable> sHeightAnimEndCallbacks =
            new java.util.ArrayList<>();

    private static void notifyHeightAnimationEnd() {
        android.util.Log.d("TrebuforkMedia", "notifyHeightAnimationEnd: running="
                + sRunningAnimations.size() + " callbacks=" + sHeightAnimEndCallbacks.size());
        if (sRunningAnimations.isEmpty() && !sHeightAnimEndCallbacks.isEmpty()) {
            // trebufork: POST, don't run inline. onAnimationEnd fires inside the animation
            // callback phase of the current frame, but the requestLayout() issued there has
            // NOT been processed yet — the row still measures at the intermediate height.
            // Running the launch inline captured icon geometry from the STALE layout and the
            // open window teleported once the layout pass shrank/grew the row (the "jump up
            // by the widget height" before the real surface appeared). A posted message runs
            // after this frame's traversal, i.e. after the final layout settles. If a new
            // animation started meanwhile, the callbacks stay queued until it ends too.
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                if (sRunningAnimations.isEmpty() && !sHeightAnimEndCallbacks.isEmpty()) {
                    java.util.ArrayList<Runnable> toRun =
                            new java.util.ArrayList<>(sHeightAnimEndCallbacks);
                    sHeightAnimEndCallbacks.clear();
                    for (int i = 0; i < toRun.size(); i++) {
                        toRun.get(i).run();
                    }
                }
            });
        }
    }

    /**
     * trebufork: whether any row's expand/collapse height animation is in flight.
     * Launcher defers launches while this is true so icon geometry is captured from a
     * stable layout (the row animation itself keeps running untouched).
     */
    public static boolean hasHeightAnimationsRunning() {
        return !sRunningAnimations.isEmpty();
    }

    /**
     * trebufork: runs {@param action} once every in-flight height animation has ended
     * (immediately when none is running). Must be called on the main thread.
     */
    public static void runWhenHeightAnimationsEnd(Runnable action) {
        if (sRunningAnimations.isEmpty()) {
            action.run();
        } else {
            sHeightAnimEndCallbacks.add(action);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        // trebufork: while the launcher was unfocused (app launch/foreground) the row kept its
        // last stable measured height, so sessions appearing/disappearing in that window did
        // NOT change the row size and could not shift the launch animation. But a pinned height
        // is only a snapshot: once home becomes interactive again the row must re-measure to
        // adopt the new natural height — otherwise a player registered during the launch never
        // shows up (row still pinned at 0) and closed players leave a permanent blank slot
        // (row still pinned at full height).
        // POST the re-measure: the view tree receives window-focus change BEFORE
        // BaseActivity.onWindowFocusChanged updates ACTIVITY_STATE_WINDOW_FOCUSED, so a
        // synchronous measure here would still read the stale flag and re-pin forever.
        android.util.Log.d("TrebuforkMedia", "row focus changed=" + hasWindowFocus
                + " lastStable=" + mLastStableHeight);
        if (!hasWindowFocus) {
            // trebufork: while the launcher is in the background the carousel behaves like
            // the shade — the most recently playing session wins. Drop the user's swipe pin
            // so a session that starts playing in another app becomes the active player;
            // when home comes back, the visible page follows the (possibly changed) active
            // session via the rebuild below.
            if (mSource != null) {
                mSource.clearPin();
            }
        } else {
            // trebufork: the user is back at the desktop — run the rebuild WITH the
            // home-return reorder (active player first). This is one of exactly two call
            // sites that may reorder; all other rebuilds keep the visible order verbatim.
            post(this::requestLayout);
            post(() -> rebuildCarouselWhenIdle(true));
        }
    }

    /** trebufork: registers once; every completed launcher transition re-checks the height. */
    private void registerStableHeightTrigger() {
        Launcher launcher;
        try {
            launcher = Launcher.getLauncher(getContext());
        } catch (ClassCastException | IllegalStateException e) {
            return;
        }
        launcher.getStateManager().addStateListener(
                new StateManager.StateListener<LauncherState>() {
                    @Override
                    public void onStateTransitionComplete(LauncherState finalState) {
                        // Covers paths where window focus never changed (e.g. a player killed
                        // from the launcher's own recents): when the launcher settles back into
                        // NORMAL, the row must re-measure to grow (player registered mid-launch)
                        // or collapse (all sessions gone) — at idle home this cannot disturb
                        // any animation.
                        if (finalState == LauncherState.NORMAL) {
                            // A completed transition back to home is the second "exited to
                            // the desktop" moment: rebuild WITH the home-return reorder
                            // (active player first).
                            post(ScrollableMediaRowView.this::requestLayout);
                            post(() -> rebuildCarouselWhenIdle(true));
                        }
                    }
                });
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
