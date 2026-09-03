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

import static com.android.launcher3.LauncherConstants.ActivityCodes.REQUEST_RECONFIGURE_APPWIDGET;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Process;
import android.os.SystemClock;
import android.os.UserHandle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.launcher3.allapps.AllAppsStore;
import com.android.launcher3.dragndrop.DragOptions;
import com.android.launcher3.model.data.AppInfo;
import com.android.launcher3.model.data.LauncherAppWidgetInfo;
import com.android.launcher3.model.data.WorkspaceItemInfo;
import com.android.launcher3.popup.PopupContainer;
import com.android.launcher3.popup.PopupContainerWithArrow;
import com.android.launcher3.popup.SystemShortcut;
import com.android.launcher3.shortcuts.DeepShortcutView;
import com.android.launcher3.util.PackageUserKey;
import com.android.launcher3.util.StableViewInfo;
import com.android.launcher3.util.Themes;
import com.android.launcher3.views.ActivityContext;
import com.android.launcher3.views.Snackbar;
import com.android.launcher3.widget.LauncherAppWidgetHostView;
import com.android.launcher3.widget.LauncherAppWidgetProviderInfo;
import com.android.launcher3.widget.PendingAddWidgetInfo;
import com.android.launcher3.widget.WidgetAddFlowHandler;
import com.android.launcher3.widget.WidgetManagerHelper;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Niagara-style home screen: a single column of apps (icon + label) that scrolls
 * vertically. Replaces (not modifies) the paged {@link Workspace} while enabled;
 * the app list comes from {@link AllAppsStore}, the same model Quickstep uses.
 *
 * <p>trebufork: two modes — {@link #MODE_DESKTOP} (a freely ordered list of user-picked
 * apps and widgets, see {@link ScrollableDesktopStore}) and {@link #MODE_APPS} (the
 * alphabetized list of every installed app). The alphabet sidebar switches between them:
 * the star at the top goes to the desktop, any letter opens the apps list scrolled to
 * that section.
 */
public class ScrollableAppsView extends RecyclerView
        implements DropTarget, ScrollableDesktopStore.OnChangeListener {

    /** Desktop mode: user-arranged apps and widgets. */
    public static final int MODE_DESKTOP = 0;
    /** Apps mode: the full alphabetized app list. */
    public static final int MODE_APPS = 1;

    private final List<AppInfo> mApps = new ArrayList<>();
    // Currently active row list. Points at mAppsRows or mDesktopRows (never a copy) so that
    // drag-reordering the desktop list stays in sync with what the adapter renders.
    private List<ListRow> mRows = new ArrayList<>();
    /** Rows of the apps mode (kept even while desktop mode is active, for sidebar dimming). */
    private final List<ListRow> mAppsRows = new ArrayList<>();
    /** Rows of the desktop mode. */
    private final List<ListRow> mDesktopRows = new ArrayList<>();
    private final AppAdapter mAdapter = new AppAdapter();
    private AllAppsStore mAppsStore;
    private ScrollableDesktopStore mDesktopStore;
    // Cache of widget host views keyed by app widget id so widgets survive view recycling.
    private final SparseArray<View> mWidgetViews = new SparseArray<>();

    private int mMode = MODE_DESKTOP;
    private boolean mSuppressStoreRefresh;

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_APP = 1;
    private static final int VIEW_TYPE_SECTION = 2;
    private static final int VIEW_TYPE_DESKTOP_HEADER = 3;
    private static final int VIEW_TYPE_DESKTOP_WIDGET = 4;
    private static final int VIEW_TYPE_FOOTER = 5;
    private static final int VIEW_TYPE_DESKTOP_FOLDER = 6;
    private static final int VIEW_TYPE_DESKTOP_GROUP = 7;
    private static final long ALPHABET_REVEAL_DURATION_MS = 180L;
    // trebufork: boot/appearance fade-in — duration and per-row stagger for the entrance
    // animation played when the scrollable home first becomes visible.
    private static final long ENTRANCE_ANIMATION_DURATION_MS = 260L;
    private static final long ENTRANCE_ANIMATION_STAGGER_MS = 28L;
    private static final float ENTRANCE_RISE_DP = 12f;
    // trebufork: quick, simultaneous fade for the apps-list -> desktop switch. Distinct from the
    // staggered boot entrance animation above: every desktop row fades in together so the switch
    // is fast and unobtrusive.
    private static final long MODE_TRANSITION_DURATION_MS = 150L;
    // trebufork: duration of the search bar's lift/drop animation above the keyboard.
    private static final long SEARCH_BAR_LIFT_ANIMATION_MS = 200L;
    // trebufork: fade duration when hiding the scrollable home under recents/overview.
    private static final long OVERVIEW_FADE_DURATION_MS = 250L;
    // trebufork: horizontal margins of the bottom search bar at rest (centered, slightly narrow)
    // and when lifted by the keyboard (stretched wider). Animated between the two on IME show/hide.
    private static final float SEARCH_BAR_RESTING_MARGIN_DP = 64f;
    private static final float SEARCH_BAR_LIFTED_MARGIN_DP = 12f;
    // trebufork: fraction of the screen reserved as empty space at the bottom of the desktop and
    // apps list, so the last rows stay within one-handed thumb reach.
    private static final float BOTTOM_INSET_FRACTION = 1f / 3f;
    // trebufork: minimum top inset (fraction of the screen). Guards against the user dragging
    // the inset handle to zero, which would put the first row against the screen edge.
    public static final float MIN_TOP_INSET_FRACTION = 0.05f;
    // trebufork: fraction of the screen reserved as an empty strip at the top of the desktop and
    // apps list, so the first row starts comfortably in thumb range. Also used as the scroll
    // offset when jumping to a letter, so the section lands at the same line instead of the
    // screen edge. Default 20%; configurable via pref_scrollable_top_inset (percent).
    // Corner radius of the pastel tile shown in place of an icon when labels are hidden.
    private static final float PASTEL_TILE_RADIUS_DP = 12f;

    private boolean mAlphabetDragging;
    private char mAlphabetDragLetter;
    // Right margin applied to the row tap target so it stops where the alphabet touch strip
    // begins. Computed by the sidebar and pushed here (see setRowEndMarginPx()).
    private int mRowEndMarginPx;
    // When true, app labels are hidden and icons are replaced by a pastel tile (privacy mode).
    private boolean mHideLabels;
    // trebufork: when true, every desktop row shows a drag handle and a floating Done button
    // (in launcher.xml) is shown; dragging reorders the desktop list (see DesktopReorderCallback).
    private boolean mReorderMode;
    // trebufork: true while an ItemTouchHelper drag is in progress, so the widget host view's own
    // long-press (which fires during a handle drag) does not re-start the same drag.
    private boolean mDragInProgress;
    // trebufork: true while the appearance fade-in is playing. Rows bound during this window
    // animate in with a small stagger (see onBindViewHolder).
    private boolean mEntranceAnimating;
    // trebufork: true once the boot entrance animation has been played. It runs exactly once —
    // after boot completes and the launcher window is focused — and never again: returning home,
    // mode switches and the apps list never re-animate.
    private boolean mEntrancePlayed;
    // trebufork: true once the widget host has started listening (refreshWidgetViews was called),
    // so widget rows can be re-created as real host views. The entrance animation waits for this
    // so widgets and icons cascade in together instead of widgets popping in separately.
    private boolean mWidgetsRefreshed;
    // trebufork: polls (one frame at a time) until boot has completed, the launcher window is
    // focused, the widget host is listening, and the apps list has loaded, then plays the
    // entrance animation. See startEntranceWhenReady().
    private final Runnable mEntranceStarter = this::startEntranceWhenReady;
    // trebufork: sibling overlay views animated while reorder mode is toggled. The alphabet
    // sidebar fades/slides out so the drag handles on the right edge are not obstructed; the
    // floating Done button fades in. Wired by Launcher (see setReorderOverlays).
    @Nullable
    private View mSidebar;
    // trebufork: the in-flight alpha animator that restores the sidebar when recents closes,
    // tracked so opening recents again can cancel it (otherwise it keeps running and re-shows
    // the alphabet over overview).
    @Nullable
    private ObjectAnimator mSidebarRecentsFade;
    @Nullable
    private View mDoneButton;
    // Base translationX of the sidebar (applyConfig sets translationX(-marginEnd)); restored
    // when reorder mode ends so the sidebar returns to its configured position.
    private float mSidebarBaseTranslationX;
    // trebufork: true while the sidebar is hidden because the apps-list search is active (the
    // field is focused with the IME open, or a query is present). Kept separate from reorder mode
    // so the two animations don't fight; the two states are mutually exclusive anyway.
    private boolean mSidebarHiddenForSearch;
    // trebufork: bottom search field for the apps list. Filters the list, highlights the first
    // match, and Enter opens it. Only visible while MODE_APPS is active.
    @Nullable
    private View mSearchBar;
    @Nullable
    private ExtendedEditText mSearchEdit;
    @Nullable
    private View mSearchClearButton;
    @Nullable
    private TextView mSearchNoResults;
    private String mSearchQuery = "";
    private final List<ListRow> mSearchRows = new ArrayList<>();
    // Adapter position of the highlighted (first) search result, or -1 when not searching.
    private int mHighlightedPosition = -1;
    // trebufork: true while the bottom search bar is slid off-screen during an alphabet drag;
    // it returns when the finger is released (see setAlphabetDragLetter).
    private boolean mSearchBarHiddenByAlphabet;
    // trebufork: true on the first alphabet letter right after entering the apps list from the
    // desktop; the search bar is then hidden instantly (it was never visible) instead of
    // animating down.
    private boolean mJustEnteredAppsMode;
    // trebufork: true while the recents/overview is open and this view's content (rows, search
    // bar, sidebar) is hidden behind it by setRecentsVisible(false). While hidden, the search
    // bar's alpha must stay 0: cancelSearchBarAnimation() normally pins alpha to 1, but an app
    // launch from recents loses window focus before the launcher state settles, and that focus
    // path (onWindowFocusChanged -> resetSearchBarLift) would resurrect the search bar alone
    // over the launch animation while the rest of the home is still hidden.
    private boolean mRecentsHidden = false;
    // trebufork: state of the in-group member drag (reorder mode). A long-press on a group icon
    // lifts it; horizontal finger movement slides it between the group's slots (see
    // beginGroupMemberDrag / handleGroupMemberDragMove / endGroupMemberDrag).
    private boolean mGroupDragActive;
    private GroupViewHolder mGroupDragHolder;
    private BubbleTextView mGroupDragCell;
    private long mGroupDragGroupId;
    private int mGroupDragOrigIndex;
    private int mGroupDragIndex;
    private int mGroupDragMemberCount;
    private float mGroupDragStartRawX;
    private float mGroupDragStride;

    private final ItemTouchHelper mItemTouchHelper;

    public ScrollableAppsView(Context context) {
        this(context, null);
    }

    public ScrollableAppsView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollableAppsView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayoutManager(new LinearLayoutManager(context));
        setAdapter(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(new DesktopReorderCallback());
        mItemTouchHelper.attachToRecyclerView(this);
        // trebufork: while an in-group member drag is active (reorder mode), consume every
        // subsequent move so the finger drives the reorder instead of scrolling the list.
        setOnTouchListener((v, ev) -> {
            if (mGroupDragActive) {
                handleGroupMemberDragMove(ev);
                return true;
            }
            return false;
        });
    }

    public void setAppsStore(AllAppsStore store) {
        if (mAppsStore != null) {
            mAppsStore.removeUpdateListener(mUpdateListener);
        }
        mAppsStore = store;
        if (mAppsStore != null) {
            mAppsStore.addUpdateListener(mUpdateListener);
            refreshApps();
        }
    }

    /** Attaches the desktop store; the desktop list stays in sync with it. */
    public void setDesktopStore(ScrollableDesktopStore store) {
        if (mDesktopStore != null) {
            mDesktopStore.removeListener(this);
        }
        mDesktopStore = store;
        if (mDesktopStore != null) {
            mDesktopStore.addListener(this);
            rebuildDesktopRows();
        }
    }

    /** Right margin (px) applied to every row's tap target so it ends before the alphabet strip. */
    public void setRowEndMarginPx(int px) {
        if (mRowEndMarginPx != px) {
            mRowEndMarginPx = px;
            // Defer so the adapter re-binds outside the current layout pass.
            post(mAdapter::notifyDataSetChanged);
        }
    }

    /** Toggles the privacy mode that hides app labels and fills icons with a pastel color. */
    public void setHideLabels(boolean hide) {
        if (mHideLabels != hide) {
            mHideLabels = hide;
            mAdapter.notifyDataSetChanged();
        }
    }

    /** Deterministic pastel color derived from the app's package so it stays stable across binds. */
    private static int pastelColorFor(AppInfo info) {
        String key = info.getTargetPackage();
        int hash = key == null ? 0 : key.hashCode();
        float hue = (hash & 0x7FFFFFFF) % 360f;
        float saturation = 0.30f + (Math.abs(hash >>> 8) % 25) / 100f;
        return Color.HSVToColor(new float[] { hue, saturation, 0.90f });
    }

    public void onDestroy() {
        if (mAppsStore != null) {
            mAppsStore.removeUpdateListener(mUpdateListener);
            mAppsStore = null;
        }
        if (mDesktopStore != null) {
            mDesktopStore.removeListener(this);
            mDesktopStore = null;
        }
        mAlphabetDragging = false;
        mAlphabetDragLetter = '\0';
    }

    // ---------------------------------------------------------------------
    // Mode switching
    // ---------------------------------------------------------------------

    /** Switches to the desktop list. */
    public void showDesktop() {
        if (mMode == MODE_DESKTOP) {
            // trebufork: already on the desktop, but still end an active search session — the
            // home gesture (nav-pill swipe, home button) must dismiss desktop search even
            // though no mode switch happens. The early return used to skip endSearch() and
            // left the query/results on screen with no way to close them.
            endSearch();
            return;
        }
        mMode = MODE_DESKTOP;
        // Leaving the apps list hides the search field, clears any active query and dismisses
        // the keyboard.
        if (mSearchBar != null) {
            // trebufork: the search field is normally apps-list-only, but can also be kept on
            // the desktop via pref_scrollable_desktop_search.
            mSearchBar.setVisibility(LauncherPrefs.SCROLLABLE_DESKTOP_SEARCH.get(getContext())
                    ? View.VISIBLE : View.GONE);
        }
        endSearch();
        mHighlightedPosition = -1;
        if (mSearchNoResults != null) {
            mSearchNoResults.setVisibility(View.GONE);
        }
        updateSearchSidebarVisibility();
        applyRows(mDesktopRows);
        scrollToPosition(0);
        // trebufork: quick, simultaneous fade-in of the desktop rows (all at once, unlike the
        // staggered boot entrance animation), so the apps-list -> desktop switch stays subtle.
        animate().cancel();
        setAlpha(0f);
        animate().alpha(1f).setDuration(MODE_TRANSITION_DURATION_MS).start();
    }

    /** Switches to the full apps list. */
    public void showApps() {
        if (mMode == MODE_APPS) {
            return;
        }
        mMode = MODE_APPS;
        mJustEnteredAppsMode = true;
        // Reorder handles only exist on the desktop; leaving desktop mode ends reorder mode and
        // restores the sidebar / hides the floating Done button.
        if (mReorderMode) {
            exitReorderMode();
        }
        if (mSearchBar != null) {
            mSearchBar.setVisibility(View.VISIBLE);
            // trebufork: clear any stale lift left over from a previous search session (the
            // bar is lifted only while the IME is open).
            resetSearchBarLift();
        }
        // Re-apply any active query (normally empty when entering the list).
        applySearchFilter();
        updateSearchSidebarVisibility();
    }

    public boolean isShowingDesktop() {
        return mMode == MODE_DESKTOP;
    }

    /**
     * trebufork: fades the scrollable home (rows/widgets/folders/groups) and the search bar out
     * while the recents/overview is open, and back in when it closes. This hides the desktop
     * elements instead of leaving them dimmed behind the tasks, and covers the search bar which
     * sits above the scrim and would otherwise stay crisp.
     */
    public void setRecentsVisible(boolean visible) {
        mRecentsHidden = !visible;
        if (visible) {
            // Return to home: fade the home content back in.
            animateRecentsAlpha(this, 1f);
            if (mSearchBar != null && mSearchBar.getVisibility() == View.VISIBLE) {
                animateRecentsAlpha(mSearchBar, 1f);
            }
            // trebufork: fade the alphabet sidebar back in together with the home rows. Alpha-only
            // ObjectAnimator (never ViewPropertyAnimator + translationX, never cancelling
            // animateSidebar) so the alphabet's right inset is preserved.
            fadeSidebarRecentsAlpha(1f);
        } else {
            // trebufork: opening recents/overview — hide the home content (rows, search bar,
            // alphabet) INSTANTLY, so it is already gone the moment the overview appears. Fading it
            // during the opening gesture is what made the elements visibly disappear while
            // opening recents; hiding before avoids it.
            //
            // IMPORTANT: force alpha only AFTER cancelling any fade-in still running from a recent
            // return-to-home. animateRecentsAlpha/fadeSidebarRecentsAlpha start duration'd animators
            // on these views; if the user re-opens overview while they're still in flight, the
            // lingering animator keeps driving alpha back to 1 and overrides this setAlpha(0) —
            // which is exactly the "second exit shows all elements" regression.
            this.animate().cancel();
            setAlpha(0f);
            if (mSearchBar != null) {
                mSearchBar.animate().cancel();
                mSearchBar.setAlpha(0f);
            }
            if (mSidebar != null && mSidebar.getVisibility() == View.VISIBLE) {
                if (mSidebarRecentsFade != null) {
                    mSidebarRecentsFade.cancel();
                    mSidebarRecentsFade = null;
                }
                mSidebar.setAlpha(0f);
            }
        }
    }

    private void animateRecentsAlpha(@NonNull View v, float alpha) {
        v.animate().cancel();
        v.animate().alpha(alpha).setDuration(OVERVIEW_FADE_DURATION_MS).start();
    }

    /**
     * trebufork: fades the alphabet sidebar alpha to {@code target} while recents/overview is
     * open (or restores it when closed). Animated with an alpha-only ObjectAnimator so the
     * sidebar's translationX (which carries the alphabet right inset) is never re-asserted and
     * its own animateSidebar animation is never cancelled.
     */
    private void fadeSidebarRecentsAlpha(float target) {
        View sidebar = mSidebar;
        if (sidebar == null || sidebar.getVisibility() != View.VISIBLE) {
            return;
        }
        if (mSidebarRecentsFade != null) {
            mSidebarRecentsFade.cancel();
        }
        mSidebarRecentsFade = ObjectAnimator.ofFloat(sidebar, View.ALPHA, target);
        mSidebarRecentsFade.setDuration(OVERVIEW_FADE_DURATION_MS).start();
    }

    /**
     * trebufork: wires the bottom search field used in apps-list mode. Text changes filter the
     * list, Enter opens the highlighted first match, the clear button resets the query, and the
     * back key closes the field.
     */
    public void setSearchBar(@Nullable View searchBar, @Nullable TextView noResults) {
        mSearchBar = searchBar;
        mSearchNoResults = noResults;
        if (mSearchBar == null) {
            return;
        }
        // trebufork: the launcher runs with SOFT_INPUT_ADJUST_NOTHING, so the bottom field is
        // lifted above the IME manually from the ime window insets (the same approach
        // WorkUtilityView and Folder use). The lift is applied only while the search field
        // itself is focused: when the launcher comes to the front while the keyboard is still
        // animating closed (it was open in the previous app) the launcher briefly receives the
        // IME inset as visible, and without the focus guard the search bar would jump up for a
        // moment even though the user never touched it.
        ViewCompat.setOnApplyWindowInsetsListener(mSearchBar, (v, insets) -> {
            updateSearchBarLift();
            return insets;
        });
        // trebufork: force the pill background to be fully opaque — the dynamic surface color can
        // carry a translucency that lets the scrolling list show through the search bar.
        GradientDrawable surface = new GradientDrawable();
        surface.setShape(GradientDrawable.RECTANGLE);
        int surfaceColor = Themes.getAttrColor(getContext(), R.attr.allappsHeaderProtectionColor);
        surface.setColor(surfaceColor | 0xFF000000);
        surface.setCornerRadius(
                getResources().getDimensionPixelSize(R.dimen.rounded_button_radius));
        mSearchBar.setBackground(surface);
        mSearchEdit = mSearchBar.findViewById(R.id.scroll_search_edit);
        mSearchClearButton = mSearchBar.findViewById(R.id.scroll_search_clear);
        if (mSearchEdit != null) {
            mSearchEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    onSearchQueryChanged(s == null ? "" : s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) { }
            });
            mSearchEdit.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_GO
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || (event != null
                                && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                                && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    return openHighlightedApp();
                }
                return false;
            });
            mSearchEdit.setOnBackKeyListener(this::closeSearch);
            mSearchEdit.addOnFocusChangeListener((v, hasFocus) -> {
                // trebufork: the field can also live on the desktop. With the desktop-search
                // pref on, focusing it keeps the launcher on the desktop and the typed query
                // replaces the desktop content with the search results (a screen separate from
                // the apps list); otherwise it still falls back to the apps list.
                if (hasFocus && mMode == MODE_DESKTOP
                        && !LauncherPrefs.SCROLLABLE_DESKTOP_SEARCH.get(getContext())) {
                    showApps();
                }
                // trebufork: the launcher can regain focus while the previous app's keyboard is
                // still tearing down; the teardown swallows the field's own show-soft-input
                // request, leaving it focused with no keyboard. Retry until the IME is visible.
                if (hasFocus) {
                    // If the IME is already visible at the moment focus is granted, it is the
                    // previous app's keyboard closing — not one we requested. Suppress the bar
                    // lift until that inset goes away, so the bar does not jump up and down
                    // before the retry opens the real keyboard.
                    mInheritedIme = isImeVisible();
                    retryShowSearchKeyboard();
                }
                // trebufork: re-evaluate the IME lift. Covers tapping the field while the IME
                // is already shown (e.g. it lingered from the previous app) — the insets were
                // dispatched before focus was granted, so the lift must be re-applied now.
                updateSearchBarLift();
                updateSearchSidebarVisibility();
            });
        }
        if (mSearchClearButton != null) {
            mSearchClearButton.setOnClickListener(v -> endSearch());
        }
    }

    /**
     * trebufork: shows or hides the search field based on the current mode and whether the
     * scrollable home itself is enabled. Called by Launcher when the scrollable-home setting
     * changes; the field is otherwise driven by {@link #showApps()}/{@link #showDesktop()}.
     */
    public void syncSearchBarVisibility(boolean scrollableEnabled) {
        boolean show = scrollableEnabled && (mMode == MODE_APPS
                || LauncherPrefs.SCROLLABLE_DESKTOP_SEARCH.get(getContext()));
        if (mSearchBar != null) {
            mSearchBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (!show && mSearchNoResults != null) {
            mSearchNoResults.setVisibility(View.GONE);
        }
    }

    /**
     * trebufork: clears the query and dismisses the keyboard. Returns true when the search field
     * was active (had focus or a query), so the back key is consumed instead of propagating.
     */
    public boolean closeSearch() {
        if (mSearchEdit == null) {
            return false;
        }
        boolean active = mSearchEdit.hasFocus() || !mSearchQuery.isEmpty();
        endSearch();
        return active;
    }

    /**
     * trebufork: fully ends a search session — clears the query, drops focus, dismisses the IME
     * and resets the bar to its resting (bottom, centered) position. Dropping focus is what makes
     * the alphabet sidebar reappear; resetting the lift prevents the bar from hanging in a raised
     * position when the IME inset is never re-dispatched (e.g. after a nav-pill swipe).
     */
    private void endSearch() {
        if (mSearchEdit == null) {
            return;
        }
        // trebufork: remember whether a query was present before it is cleared below — only a
        // non-empty query replaced the desktop content with the search results, so only then
        // should ending the search fade the desktop list back in.
        boolean hadQuery = !mSearchQuery.isEmpty();
        mSearchEdit.setText("");
        mSearchEdit.clearFocus();
        mSearchEdit.hideKeyboard();
        resetSearchBarLift();
        // trebufork: on the desktop the search results replaced the desktop content; ending the
        // search restores the desktop list.
        if (mMode == MODE_DESKTOP) {
            mHighlightedPosition = -1;
            applyRows(mDesktopRows);
            if (hadQuery) {
                // trebufork: quick, simultaneous fade-in of the restored desktop rows — the same
                // animation as the apps-list -> desktop switch (see showDesktop) — so leaving the
                // desktop search does not snap the list back without any transition.
                animate().cancel();
                setAlpha(0f);
                animate().alpha(1f).setDuration(MODE_TRANSITION_DURATION_MS).start();
            }
        }
        updateSearchSidebarVisibility();
    }

    /**
     * trebufork: dismisses the search keyboard without clearing the query, so tapping or
     * scrolling the list just hides the IME and keeps the filtered results on screen.
     */
    private void hideSearchKeyboard() {
        if (mSearchEdit != null && mSearchEdit.hasFocus()) {
            mSearchEdit.hideKeyboard();
        }
    }

    /**
     * trebufork: repeatedly requests the IME until it is actually visible. When the user exits
     * an app whose keyboard was open, the launcher regains focus while that keyboard is still
     * tearing down; the teardown swallows the search field's own show-soft-input request, so the
     * field ends up focused with no keyboard and the search appears "stuck" for a few seconds.
     * Retrying while the field keeps focus covers the teardown window; the retry stops as soon
     * as the IME shows or the field loses focus, so it never fights the user.
     */
    private void retryShowSearchKeyboard() {
        if (mSearchEdit == null) {
            return;
        }
        // Poll while the field keeps focus: each tick, if the IME is not actually visible,
        // re-request it. The IME may still be reported visible while the previous app's
        // keyboard is tearing down, so we keep polling until the request sticks — the teardown
        // window is what swallows the field's own show-soft-input request.
        mSearchEdit.postDelayed(new Runnable() {
            private int attempts = 0;
            @Override
            public void run() {
                if (mSearchEdit == null || !mSearchEdit.hasFocus() || attempts >= 10) {
                    return;
                }
                attempts++;
                WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(mSearchBar);
                boolean imeVisible = insets != null
                        && insets.isVisible(WindowInsetsCompat.Type.ime());
                if (!imeVisible) {
                    mSearchEdit.showKeyboard();
                }
                mSearchEdit.postDelayed(this, 150);
            }
        }, 150);
    }

    // trebufork: true while the IME visible at the moment the search field gained focus was the
    // previous app's keyboard tearing down (not one we requested). While set, the search bar must
    // not lift — the inherited inset is about to disappear and would cause a lift-then-drop
    // stutter before the retry opens the real keyboard. Cleared once the inset drops to zero.
    private boolean mInheritedIme;

    private boolean isImeVisible() {
        WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(mSearchBar);
        return insets != null && insets.isVisible(WindowInsetsCompat.Type.ime());
    }

    /**
     * trebufork: applies the search bar lift from the current IME window insets, but only while
     * the search field is focused. See the insets listener in {@link #setSearchBar} for why the
     * focus guard is needed. Also called from the field's focus listener so a tap while the IME
     * is already shown (insets dispatched before focus) still lifts the bar.
     */
    private void updateSearchBarLift() {
        if (mSearchBar == null || mSearchEdit == null) {
            return;
        }
        WindowInsetsCompat rootInsets = ViewCompat.getRootWindowInsets(mSearchBar);
        int imeBottom = (rootInsets != null && rootInsets.isVisible(WindowInsetsCompat.Type.ime()))
                ? rootInsets.getInsets(WindowInsetsCompat.Type.ime()).bottom : 0;
        if (imeBottom == 0) {
            // The inherited IME finished closing; any IME that appears from now on is ours.
            mInheritedIme = false;
        }
        boolean focused = mSearchEdit.hasFocus();
        int translate = 0;
        if (imeBottom > 0 && focused && !mInheritedIme && mSearchBar.getHeight() > 0
                && mSearchBar.getParent() instanceof View parent) {
            int gap = Math.round(12f * getResources().getDisplayMetrics().density);
            translate = Math.max(0, imeBottom - (parent.getHeight() - mSearchBar.getBottom())
                    + gap);
        }
        Log.d("TrebuforkSearch", "updateSearchBarLift: imeBottom=" + imeBottom
                + " focused=" + focused + " translate=" + translate);
        // trebufork: animate the lift/drop. A plain ViewPropertyAnimator is used (instead of
        // the framework's WindowInsetsAnimation callback) so resetSearchBarLift() can cancel
        // it reliably — the framework callback kept firing after an app opened from a result
        // and left the bar stuck mid-transition.
        cancelSearchBarAnimation();
        mSearchBar.animate().translationY(-translate)
                .setDuration(SEARCH_BAR_LIFT_ANIMATION_MS)
                .start();
        // trebufork: stretch the bar wider while the keyboard is open, and shrink it back
        // to the centered resting width when the keyboard closes.
        animateSearchBarStretch(imeBottom > 0 && focused);
    }

    /**
     * trebufork: returns the search bar to its resting (bottom) position. The bar is lifted above
     * the IME only while the keyboard is open; if the launcher loses focus mid-session (an app
     * opens from a result) the IME inset may never be re-dispatched, so the lift must be cleared
     * explicitly to avoid the bar getting stuck above its resting spot.
     */
    private void resetSearchBarLift() {
        if (mSearchBar != null) {
            cancelSearchBarAnimation();
            mSearchBar.setTranslationY(0f);
            setSearchBarMargins(Math.round(SEARCH_BAR_RESTING_MARGIN_DP
                    * getResources().getDisplayMetrics().density));
        }
    }

    /**
     * trebufork: cancels any in-flight animation on the search bar and pins its alpha to 1.
     * The return-from-recents fade-in (setRecentsVisible(true)) drives the bar's alpha from 0
     * to 1 with a duration'd ViewPropertyAnimator. Taking that same animator over with a
     * translationY animation (keyboard lift, alphabet drag) cancels the alpha fade mid-flight,
     * leaving the search bar semi-transparent forever — so every interaction that repositions
     * the bar must first complete the alpha.
     */
    private void cancelSearchBarAnimation() {
        if (mSearchBar != null) {
            mSearchBar.animate().cancel();
            // Pin the alpha to 1 only while the home is actually on screen. While the
            // recents/overview is open the home content is faded out (setRecentsVisible(false))
            // and the search bar must stay hidden; otherwise a focus-loss during an app launch
            // from recents pops the bar back in over the launch animation.
            if (!mRecentsHidden) {
                mSearchBar.setAlpha(1f);
            }
        }
    }

    /**
     * trebufork: animates the horizontal margins of the search bar between its resting (centered,
     * slightly narrow) and lifted (stretched wider) widths, in sync with the IME lift/drop.
     */
    private void animateSearchBarStretch(boolean lifted) {
        if (mSearchBar == null) {
            return;
        }
        ViewGroup.LayoutParams lp = mSearchBar.getLayoutParams();
        if (!(lp instanceof ViewGroup.MarginLayoutParams mlp)) {
            return;
        }
        float density = getResources().getDisplayMetrics().density;
        int target = Math.round((lifted
                ? SEARCH_BAR_LIFTED_MARGIN_DP : SEARCH_BAR_RESTING_MARGIN_DP) * density);
        if (mlp.leftMargin == target && mlp.rightMargin == target) {
            return;
        }
        ValueAnimator anim = ValueAnimator.ofInt(mlp.leftMargin, target);
        anim.setDuration(SEARCH_BAR_LIFT_ANIMATION_MS);
        anim.addUpdateListener(a -> setSearchBarMargins((Integer) a.getAnimatedValue()));
        anim.start();
    }

    /** Sets the search bar's symmetric left/right margins to {@code marginPx}. */
    private void setSearchBarMargins(int marginPx) {
        if (mSearchBar == null) {
            return;
        }
        ViewGroup.LayoutParams lp = mSearchBar.getLayoutParams();
        if (lp instanceof ViewGroup.MarginLayoutParams mlp) {
            mlp.leftMargin = marginPx;
            mlp.rightMargin = marginPx;
            mSearchBar.setLayoutParams(mlp);
        }
    }

    private void onSearchQueryChanged(String query) {
        mSearchQuery = query == null ? "" : query;
        if (mSearchClearButton != null) {
            mSearchClearButton.setVisibility(mSearchQuery.isEmpty() ? View.GONE : View.VISIBLE);
        }
        updateSearchSidebarVisibility();
        // trebufork: on the desktop the typed query replaces the desktop content with the search
        // results (a dedicated search-results screen, separate from the apps list); an empty
        // query restores the desktop rows.
        if (mMode == MODE_DESKTOP) {
            applyDesktopSearch();
            return;
        }
        if (mMode != MODE_APPS) {
            return;
        }
        applySearchFilter();
    }

    /**
     * trebufork: hides the alphabet sidebar while the apps-list search is active (the field is
     * focused with the IME open, or a query is present), and shows it again once the search is
     * cleared or dismissed. Uses the same fade+slide animation as reorder mode.
     */
    private void updateSearchSidebarVisibility() {
        boolean searchActive = (mSearchEdit != null && mSearchEdit.hasFocus())
                || !mSearchQuery.isEmpty();
        if (searchActive == mSidebarHiddenForSearch) {
            return;
        }
        mSidebarHiddenForSearch = searchActive;
        animateSidebar(!searchActive);
    }

    /**
     * trebufork: animates the alphabet sidebar in/out. Shared by reorder mode (desktop) and the
     * search field (apps list) — when the drag handles or the search field/IME are active the
     * sidebar fades and slides out so it does not obstruct the interaction.
     */
    private void animateSidebar(boolean visible) {
        if (mSidebar == null) {
            return;
        }
        mSidebar.animate().cancel();
        // trebufork: the recents fade-in drives the sidebar alpha with a separate ObjectAnimator
        // (fadeSidebarRecentsAlpha); cancel it before starting our own alpha animation so the two
        // never fight over the property and leave the sidebar mid-alpha.
        if (mSidebarRecentsFade != null) {
            mSidebarRecentsFade.cancel();
            mSidebarRecentsFade = null;
        }
        if (visible) {
            mSidebar.setVisibility(View.VISIBLE);
            mSidebar.setAlpha(0f);
            mSidebar.animate().alpha(1f)
                    .translationX(mSidebarBaseTranslationX)
                    .setDuration(ALPHABET_REVEAL_DURATION_MS);
        } else {
            // Remember the sidebar's configured base translation (applyConfig sets
            // translationX(-marginEnd)) so it can be restored when the interaction ends.
            mSidebarBaseTranslationX = mSidebar.getTranslationX();
            mSidebar.setVisibility(View.VISIBLE);
            mSidebar.animate().alpha(0f)
                    .translationX(mSidebarBaseTranslationX + mSidebar.getWidth() * 0.15f)
                    .setDuration(ALPHABET_REVEAL_DURATION_MS)
                    .withEndAction(() -> mSidebar.setVisibility(View.INVISIBLE));
        }
    }

    /**
     * trebufork: rebuilds the apps list for the current query. An empty query restores the full
     * alphabetized list; a non-empty query shows only matching apps with the first match
     * highlighted, or the "no results" hint when nothing matches.
     */
    private void applySearchFilter() {
        String query = mSearchQuery.trim();
        if (query.isEmpty()) {
            mHighlightedPosition = -1;
            if (mSearchNoResults != null) {
                mSearchNoResults.setVisibility(View.GONE);
            }
            applyRows(mAppsRows);
            return;
        }
        buildSearchResults();
        if (mSearchNoResults != null) {
            mSearchNoResults.setVisibility(
                    mSearchRows.size() > 1 ? View.GONE : View.VISIBLE);
        }
        applyRows(mSearchRows);
        scrollToPosition(0);
    }

    /**
     * trebufork: drives the desktop search — the typed query replaces the desktop content with
     * the matching apps, keeping the launcher in desktop mode (a dedicated search-results screen,
     * separate from the apps list). An empty query restores the desktop rows.
     */
    private void applyDesktopSearch() {
        String query = mSearchQuery.trim();
        if (query.isEmpty()) {
            mHighlightedPosition = -1;
            if (mSearchNoResults != null) {
                mSearchNoResults.setVisibility(View.GONE);
            }
            applyRows(mDesktopRows);
            return;
        }
        buildSearchResults();
        if (mSearchNoResults != null) {
            mSearchNoResults.setVisibility(
                    mSearchRows.size() > 1 ? View.GONE : View.VISIBLE);
        }
        applyRows(mSearchRows);
        scrollToPosition(0);
    }

    /**
     * trebufork: builds the shared search-results row list (header, ranked matches, footer) and
     * marks the first match as highlighted. Prefix matches (title starts with the query) rank
     * before arbitrary substring matches, e.g. "you" lists YouTube before "AniHyou".
     */
    private void buildSearchResults() {
        String query = mSearchQuery.trim();
        String needle = query.toLowerCase(Locale.getDefault());
        List<AppInfo> prefixMatches = new ArrayList<>();
        List<AppInfo> substringMatches = new ArrayList<>();
        for (AppInfo app : mApps) {
            String title = app.title == null ? "" : app.title.toString();
            String lower = title.toLowerCase(Locale.getDefault());
            if (lower.contains(needle)) {
                (lower.startsWith(needle) ? prefixMatches : substringMatches).add(app);
            }
        }
        mSearchRows.clear();
        mSearchRows.add(new ListRow(VIEW_TYPE_HEADER, null, '\0', null));
        for (AppInfo app : prefixMatches) {
            mSearchRows.add(new ListRow(VIEW_TYPE_APP, app, '\0', null));
        }
        for (AppInfo app : substringMatches) {
            mSearchRows.add(new ListRow(VIEW_TYPE_APP, app, '\0', null));
        }
        mHighlightedPosition = mSearchRows.size() > 1 ? 1 : -1;
        mSearchRows.add(new ListRow(VIEW_TYPE_FOOTER, null, '\0', null));
    }

    /** trebufork: launches the highlighted (first) search result, if any. */
    private boolean openHighlightedApp() {
        if (mHighlightedPosition < 0 || mHighlightedPosition >= mRows.size()) {
            return false;
        }
        ListRow row = mRows.get(mHighlightedPosition);
        if (row == null || row.app == null || row.app.getIntent() == null) {
            return false;
        }
        if (!(getContext() instanceof Launcher launcher)) {
            return false;
        }
        View target = findIconViewForApp(row.app.getTargetPackage(), row.app.user);
        if (target == null) {
            target = this;
        }
        launcher.startActivitySafely(target, row.app.getIntent(), row.app);
        return true;
    }

    /**
     * trebufork: re-applies the top inset (pref_scrollable_top_inset) to the header rows of the
     * currently shown list. Called by Launcher when the pref changes so the effect is immediate.
     */
    public void applyTopInset() {
        mAdapter.notifyDataSetChanged();
    }

    /**
     * trebufork: attaches the sibling overlay views animated during reorder mode — the alphabet
     * sidebar (hidden so it does not obstruct the drag handles) and the floating Done button.
     */
    public void setReorderOverlays(@Nullable View sidebar, @Nullable View doneButton) {
        mSidebar = sidebar;
        mDoneButton = doneButton;
        if (mDoneButton != null) {
            mDoneButton.setOnClickListener(v -> exitReorderMode());
            // trebufork: the Done button sits on an accent pill; pick a text color with enough
            // contrast (dark text on a light accent, white on a dark accent) instead of the
            // default gray that washed out on light backgrounds.
            int accent = Themes.getAttrColor(getContext(), android.R.attr.colorAccent);
            int textColor = ColorUtils.calculateLuminance(accent) > 0.5f
                    ? Color.BLACK : Color.WHITE;
            ((TextView) mDoneButton).setTextColor(textColor);
        }
    }

    /** trebufork: enters reorder mode — drag handles appear on desktop rows, floating Done shows. */
    public void enterReorderMode() {
        if (mReorderMode) {
            return;
        }
        mReorderMode = true;
        animateReorderOverlays(true);
        mAdapter.notifyDataSetChanged();
    }

    /** trebufork: leaves reorder mode and hides the drag handles / Done button. */
    public void exitReorderMode() {
        if (!mReorderMode) {
            return;
        }
        mReorderMode = false;
        // trebufork: an in-flight group member drag is cancelled so no cell is left elevated or
        // offset when the mode switches.
        endGroupMemberDrag();
        animateReorderOverlays(false);
        mAdapter.notifyDataSetChanged();
    }

    // ---------------------------------------------------------------------
    // In-group member reorder (drag icons within a group row)
    // ---------------------------------------------------------------------

    /**
     * trebufork: starts the in-group member drag: the long-pressed icon is lifted and follows
     * the finger horizontally; crossing slot boundaries moves the member in the store and slides
     * the icon between slots (see {@link #handleGroupMemberDragMove}).
     */
    private void beginGroupMemberDrag(GroupViewHolder holder, int index,
            ScrollableDesktopStore.DesktopItem group, BubbleTextView cell) {
        if (mGroupDragActive || mDesktopStore == null || group == null) {
            return;
        }
        mGroupDragActive = true;
        mDragInProgress = true;
        mGroupDragHolder = holder;
        mGroupDragCell = cell;
        mGroupDragGroupId = group.id;
        mGroupDragOrigIndex = index;
        mGroupDragIndex = index;
        mGroupDragMemberCount = group.members.size();
        float density = getResources().getDisplayMetrics().density;
        float gap = 8f * density;
        mGroupDragStride = cell.getWidth() > 0 ? cell.getWidth() + gap : 52f * density;
        int[] loc = new int[2];
        cell.getLocationOnScreen(loc);
        mGroupDragStartRawX = loc[0] + cell.getWidth() / 2f;
        cell.setElevation(8f * density);
        cell.setAlpha(0.9f);
    }

    /**
     * trebufork: drives the in-group member drag from the finger position. The dragged icon
     * follows the finger; once it crosses a slot boundary the member moves in the store and the
     * row's children are re-slotted so the remaining icons slide into place.
     */
    private void handleGroupMemberDragMove(MotionEvent ev) {
        if (mGroupDragCell == null || mGroupDragHolder == null || mDesktopStore == null) {
            return;
        }
        if (ev.getActionMasked() == MotionEvent.ACTION_UP
                || ev.getActionMasked() == MotionEvent.ACTION_CANCEL) {
            endGroupMemberDrag();
            return;
        }
        if (ev.getActionMasked() != MotionEvent.ACTION_MOVE) {
            return;
        }
        int orig = mGroupDragOrigIndex;
        float stride = mGroupDragStride;
        float dx = ev.getRawX() - mGroupDragStartRawX;
        // Keep the icon inside the row: it cannot leave the first/last slot.
        float minDx = -orig * stride;
        float maxDx = (mGroupDragMemberCount - 1 - orig) * stride;
        dx = Math.max(minDx, Math.min(maxDx, dx));
        int target = orig + Math.round(dx / stride);
        if (target != mGroupDragIndex) {
            mGroupDragHolder.moveCellInRow(mGroupDragIndex, target);
            // trebufork: persist the move but suppress the store listener's full list rebuild
            // while the drag is live — a rebind would destroy the row (and the dragged cell)
            // mid-gesture. The row is updated directly by moveCellInRow.
            mSuppressStoreRefresh = true;
            mDesktopStore.moveInGroup(mGroupDragGroupId, mGroupDragIndex, target);
            mSuppressStoreRefresh = false;
            mGroupDragIndex = target;
        }
        // After a slot jump the icon's new slot base plus this residual keeps it under the
        // finger; at the jump moment the residual is ~0, so the handoff is seamless.
        mGroupDragCell.setTranslationX((orig - mGroupDragIndex) * stride + dx);
    }

    /** trebufork: ends the in-group member drag and restores the dragged icon. */
    private void endGroupMemberDrag() {
        if (!mGroupDragActive) {
            return;
        }
        mGroupDragActive = false;
        mDragInProgress = false;
        if (mGroupDragCell != null) {
            mGroupDragCell.setTranslationX(0f);
            mGroupDragCell.setElevation(0f);
            mGroupDragCell.setAlpha(1f);
            mGroupDragCell = null;
        }
        mGroupDragHolder = null;
        // trebufork: the store may have moved members during the drag while the listener was
        // suppressed; rebind once so every row reflects the persisted order.
        mAdapter.notifyDataSetChanged();
    }

    /**
     * trebufork: animates the reorder overlays in/out. Entering fades the alphabet sidebar out
     * (and slides it right) so the drag handles on the row edges are unobstructed, and fades the
     * floating Done button in; exiting reverses both.
     */
    private void animateReorderOverlays(boolean entering) {
        long duration = ALPHABET_REVEAL_DURATION_MS;
        animateSidebar(!entering);
        if (mDoneButton != null) {
            mDoneButton.animate().cancel();
            if (entering) {
                mDoneButton.setVisibility(View.VISIBLE);
                mDoneButton.setAlpha(0f);
                mDoneButton.animate().alpha(1f).setDuration(duration);
            } else {
                mDoneButton.animate().alpha(0f).setDuration(duration)
                        .withEndAction(() -> mDoneButton.setVisibility(View.GONE));
            }
        }
    }

    private void applyRows(List<ListRow> rows) {
        mRows = rows;
        mAdapter.notifyDataSetChanged();
    }

    /**
     * trebufork: staggered fade+rise entrance for a freshly bound row (boot / feature toggle).
     * The row is hidden (alpha 0, pushed down slightly) and animated in with a small delay based
     * on its adapter position so the list cascades from top to bottom.
     *
     * A re-bind during the entrance window (e.g. the apps store's async load finishing right
     * after the cascade starts, triggering a second notifyDataSetChanged) must NOT restart the
     * animation — the tag marks rows whose entrance already ran (in flight or finished), so
     * the cascade plays exactly once even if every row is rebound mid-window. The tag is
     * cleared on recycle and when the window closes, so freshly-bound rows still cascade in.
     */
    private void playEntranceAnimation(View itemView, int position) {
        if (itemView.getTag(R.id.entrance_animating) != null) {
            return;
        }
        itemView.setTag(R.id.entrance_animating, true);
        itemView.animate().cancel();
        float risePx = ENTRANCE_RISE_DP * getResources().getDisplayMetrics().density;
        itemView.setAlpha(0f);
        itemView.setTranslationY(risePx);
        itemView.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(position * ENTRANCE_ANIMATION_STAGGER_MS)
                .setDuration(ENTRANCE_ANIMATION_DURATION_MS)
                .start();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startEntranceWhenReady();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.d("TrebuforkSearch", "onWindowFocusChanged: hasFocus=" + hasFocus
                + " searchFocused=" + (mSearchEdit != null && mSearchEdit.hasFocus())
                + " mode=" + mMode);
        if (hasFocus) {
            startEntranceWhenReady();
        } else {
            // trebufork: opening an app (e.g. from a search result) can leave the search bar
            // stuck above the keyboard position because the IME inset is never re-dispatched
            // while the launcher is in the background. Reset it and drop the field's focus so
            // the IME does not auto-open when the launcher regains focus.
            resetSearchBarLift();
            if (mSearchEdit != null && mSearchEdit.hasFocus()) {
                Log.d("TrebuforkSearch", "onWindowFocusChanged: clearing search focus");
                mSearchEdit.clearFocus();
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // trebufork: a tap or swipe anywhere on the list dismisses the search keyboard.
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            hideSearchKeyboard();
        }
        return super.onInterceptTouchEvent(ev);
    }

    /**
     * trebufork: plays the boot entrance animation exactly once, and only when it will actually
     * be visible: it waits until {@link Utilities#isBootCompleted()} reports true (the boot
     * animation has finished), this window has focus, the widget host is listening and the apps
     * list has loaded (non-empty) — so every desktop row (apps + widgets) cascades in together
     * instead of widgets appearing first and apps popping in later. Rows bound during the
     * animation window cascade in (see {@link #playEntranceAnimation}); after it closes,
     * returning home, switching modes and the apps list never re-animate.
     */
    private static final String PREF_ENTRANCE = "trebufork_entrance";
    private static final String KEY_ENTRANCE_PLAYED_AT = "entrance_played_at_ms";
    // The package lastUpdateTime recorded when the timestamp above was written. The entrance
    // is skipped only when the package was updated AFTER that moment — i.e. this process start
    // is the module's reinstall restart right after boot (pm install kills the running launcher
    // and the new instance would otherwise replay the cascade). A plain reboot does not change
    // lastUpdateTime, so it always replays the entrance even if the previous cascade was
    // moments ago. Wallclock time is used so the timestamp survives (soft) reboots and kills.
    private static final String KEY_ENTRANCE_PKG_UPDATE = "entrance_pkg_update_ms";
    private static final long ENTRANCE_REPLAY_WINDOW_MS = 60_000;

    private void startEntranceWhenReady() {
        if (mEntrancePlayed || !isAttachedToWindow()) {
            return;
        }
        if (Utilities.isBootCompleted() && hasWindowFocus() && mMode == MODE_DESKTOP
                && mWidgetsRefreshed && !mApps.isEmpty()) {
            mEntrancePlayed = true;
            // Skip the animation only when this process start is a reinstall restart: the
            // module's pm install killed the previous instance right after it cascaded (its
            // pref timestamp is fresh and the package was updated after that cascade). A plain
            // reboot must always replay the entrance, even shortly after the last one — only a
            // package update tells us the restart came from the module's reinstall.
            android.content.SharedPreferences prefs = getContext().getSharedPreferences(
                    PREF_ENTRANCE, Context.MODE_PRIVATE);
            long lastPlayed = prefs.getLong(KEY_ENTRANCE_PLAYED_AT, -1);
            long pkgUpdateAtPlay = prefs.getLong(KEY_ENTRANCE_PKG_UPDATE, -1);
            long sinceLast = System.currentTimeMillis() - lastPlayed;
            long pkgUpdateNow = -1;
            try {
                PackageInfo pi = getContext().getPackageManager()
                        .getPackageInfo(getContext().getPackageName(), 0);
                pkgUpdateNow = pi.lastUpdateTime;
            } catch (android.content.pm.PackageManager.NameNotFoundException e) {
                // Treat as not updated: play the entrance normally.
            }
            boolean reinstallRestart = pkgUpdateNow > 0 && pkgUpdateAtPlay > 0
                    && pkgUpdateNow > pkgUpdateAtPlay;
            boolean skip = sinceLast < ENTRANCE_REPLAY_WINDOW_MS && reinstallRestart;
            Log.d("TrebuforkEntrance", "startEntrance: sinceLast=" + sinceLast
                    + "ms pkgUpdateAtPlay=" + pkgUpdateAtPlay + " pkgUpdateNow=" + pkgUpdateNow
                    + " reinstallRestart=" + reinstallRestart + " skip=" + skip);
            long now = System.currentTimeMillis();
            if (skip) {
                for (int i = 0; i < getChildCount(); i++) {
                    View child = getChildAt(i);
                    child.animate().cancel();
                    child.setTag(R.id.entrance_animating, null);
                    child.setAlpha(1f);
                }
                // Re-create the widget rows as real host views: the previous instance already
                // did so before the reinstall, but this instance's rows were bound as
                // PendingAppWidgetHostView placeholders (see refreshWidgetViews).
                mWidgetViews.clear();
                int first = mRows.isEmpty() ? -1 : 1; // position 0 is the desktop header
                if (first >= 0 && first < mRows.size()) {
                    mAdapter.notifyItemRangeChanged(first, mRows.size() - first);
                } else {
                    mAdapter.notifyDataSetChanged();
                }
                // Record this restart so a subsequent plain reboot (or force-stop) is not
                // mistaken for another reinstall restart.
                prefs.edit().putLong(KEY_ENTRANCE_PLAYED_AT, now)
                        .putLong(KEY_ENTRANCE_PKG_UPDATE, pkgUpdateNow).commit();
                return;
            }
            // commit() (not apply()): the process may be killed by the module's reinstall
            // right after boot, and an async write could be lost before flushing. Record the
            // package update time too: only a later update marks a restart as reinstall.
            prefs.edit().putLong(KEY_ENTRANCE_PLAYED_AT, now)
                    .putLong(KEY_ENTRANCE_PKG_UPDATE, pkgUpdateNow).commit();
            mEntranceAnimating = true;
            // Re-create the widget views as real host views (listening is on) so widgets and
            // icons cascade in together instead of widgets popping in separately.
            mWidgetViews.clear();
            postDelayed(() -> {
                mEntranceAnimating = false;
                // Any row that was hidden before the cascade (alpha 0) and did not get a bind
                // inside the animation window (e.g. recycled view holders) must become visible.
                for (int i = 0; i < getChildCount(); i++) {
                    View child = getChildAt(i);
                    child.animate().cancel();
                    child.setTag(R.id.entrance_animating, null);
                    child.setAlpha(1f);
                }
            }, ENTRANCE_ANIMATION_DURATION_MS + ENTRANCE_ANIMATION_STAGGER_MS * 32);
            mAdapter.notifyDataSetChanged();
        } else {
            // Still booting, not idle, or not on the desktop — check again next frame.
            postOnAnimation(mEntranceStarter);
        }
    }

    @Override
    public void onDesktopChanged() {
        if (mSuppressStoreRefresh) {
            return;
        }
        rebuildDesktopRows();
    }

    // ---------------------------------------------------------------------
    // Alphabet drag / scroll support
    // ---------------------------------------------------------------------

    /**
     * Hides app labels outside the currently selected alphabet section while the sidebar is being
     * dragged. Row heights remain unchanged, so releasing the finger does not disturb the list
     * position and simply restores the labels.
     */
    public void setAlphabetDragLetter(char letter, boolean dragging) {
        boolean changed = mAlphabetDragging != dragging
                || mAlphabetDragLetter != letter;
        boolean releasing = mAlphabetDragging && !dragging;
        mAlphabetDragging = dragging;
        mAlphabetDragLetter = dragging ? letter : '\0';
        // trebufork: slide the bottom search bar away while the alphabet is being dragged so it
        // does not overlap the letters, and bring it back up on release. When the list was just
        // entered from the desktop the bar was never shown, so hide it instantly instead.
        if (dragging && !mSearchBarHiddenByAlphabet) {
            mSearchBarHiddenByAlphabet = true;
            setSearchBarHiddenForAlphabet(true, mJustEnteredAppsMode);
            mJustEnteredAppsMode = false;
        } else if (!dragging && mSearchBarHiddenByAlphabet) {
            mSearchBarHiddenByAlphabet = false;
            setSearchBarHiddenForAlphabet(false, false);
        }
        if (!changed) {
            return;
        }
        if (releasing) {
            // Hidden rows and section headers are still attached with alpha 0. Animate only
            // those so the currently visible section does not blink when the finger is released.
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child.getAlpha() < 1f) {
                    child.animate().cancel();
                    child.animate()
                            .alpha(1f)
                            .setDuration(ALPHABET_REVEAL_DURATION_MS)
                            .setInterpolator(new android.view.animation.DecelerateInterpolator())
                            .start();
                }
            }
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * trebufork: hides the bottom search bar while the alphabet is being dragged and reveals it
     * on release. The reveal is always animated; the hide is animated too, except when
     * {@code instantHide} is set (entering the list from the desktop, where the bar was never
     * visible and must not slide down).
     */
    private void setSearchBarHiddenForAlphabet(boolean hidden, boolean instantHide) {
        if (mSearchBar == null || mMode != MODE_APPS) {
            return;
        }
        float density = getResources().getDisplayMetrics().density;
        // Distance that fully clears the bottom of the screen: the bar's height, its bottom
        // margin and the navigation-bar inset (the window is edge-to-edge, so the area below the
        // drag layer is still visible). getHeight() can still be 0 on the first frame after the
        // bar becomes visible, so fall back to a generous fixed size.
        int navBar = 0;
        WindowInsetsCompat rootInsets = ViewCompat.getRootWindowInsets(this);
        if (rootInsets != null) {
            navBar = rootInsets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
        }
        float hideDistance = Math.max(mSearchBar.getHeight(), 48f * density)
                + 12f * density + navBar + 8f * density;
        cancelSearchBarAnimation();
        if (hidden && instantHide) {
            mSearchBar.setTranslationY(hideDistance);
        } else {
            mSearchBar.animate()
                    .translationY(hidden ? hideDistance : 0f)
                    .setDuration(ALPHABET_REVEAL_DURATION_MS)
                    .start();
        }
    }

    /**
     * trebufork: fraction of the screen reserved as an empty strip above the first content row
     * of the desktop and apps list. Reads the pref_scrollable_top_inset percent value (20 =
     * 20%), clamped to a sane range.
     */
    private static float getTopInsetFraction(Context context) {
        float percent = LauncherPrefs.getPrefs(context).getFloat(
                LauncherPrefs.SCROLLABLE_TOP_INSET.getSharedPrefKey(), 20f);
        return Math.max(MIN_TOP_INSET_FRACTION, Math.min(0.5f, percent / 100f));
    }

    /**
     * trebufork: px of the empty strip above the first content row (see getTopInsetFraction).
     * Falls back to the display height when the view is not laid out yet.
     */
    public int getTopInsetPx() {
        int height = getHeight() > 0 ? getHeight()
                : getResources().getDisplayMetrics().heightPixels;
        return Math.round(height * getTopInsetFraction(getContext()));
    }

    /** trebufork: px of the empty strip below the last content row (1/3 of the screen). */
    private int getBottomInsetPx() {
        int height = getHeight() > 0 ? getHeight()
                : getResources().getDisplayMetrics().heightPixels;
        return Math.round(height * BOTTOM_INSET_FRACTION);
    }

    /**
     * Returns {@code true} when the list is scrolled all the way to the top.
     * Used to decide whether a downward swipe on the home screen may open Quick
     * Settings (only allowed at the top, like the paged workspace).
     */
    public boolean isAtTop() {
        if (getChildCount() == 0) {
            return true;
        }
        LinearLayoutManager lm = (LinearLayoutManager) getLayoutManager();
        if (lm == null) {
            return computeVerticalScrollOffset() == 0;
        }
        return lm.findFirstVisibleItemPosition() <= 0
                && getChildAt(0).getTop() >= getPaddingTop();
    }

    /**
     * trebufork: finds the first row (if any is currently attached to the window) whose bound
     * {@link AppInfo} matches the given package and user. Returns the row's icon view, or null
     * if the app is not currently visible in the list.
     *
     * <p>Used as the target of the close-to-icon animation and the floating icon overlay: the
     * returned view carries an {@link AppInfo} tag (so the icon can be drawn) and its bounds are
     * exactly the 44dp icon, so the closing window springs into the correct spot.
     */
    @Nullable
    public View findIconViewForApp(String packageName, UserHandle user) {
        for (int i = 0; i < getChildCount(); i++) {
            View row = getChildAt(i);
            Object tag = row.getTag();
            if (tag instanceof AppInfo info && info.user.equals(user)
                    && TextUtils.equals(info.getTargetPackage(), packageName)) {
                View icon = row.findViewById(R.id.scroll_app_icon);
                if (icon != null) {
                    return icon;
                }
            }
            // trebufork: inline group rows hold member icons (BubbleTextViews tagged with their
            // AppInfo) inside scroll_group_row_icons. Search them too so the app-close animation
            // lands on the exact member icon instead of falling back to the screen center.
            View groupContent = row.findViewById(R.id.scroll_group_row_icons);
            if (groupContent instanceof ViewGroup vg) {
                for (int j = 0; j < vg.getChildCount(); j++) {
                    View cell = vg.getChildAt(j);
                    Object cellTag = cell.getTag();
                    if (cellTag instanceof AppInfo info && info.user.equals(user)
                            && TextUtils.equals(info.getTargetPackage(), packageName)) {
                        return cell;
                    }
                }
            }
        }
        return null;
    }

    /**
     * trebufork: finds a visible desktop widget host view matching the app-close target. When
     * {@code svi} is non-null it must match the widget's {@link LauncherAppWidgetInfo} exactly
     * (the widget the app was launched from); otherwise any widget from the same package/user is
     * returned. Used by the Quickstep close animation to return into the right widget.
     */
    @Nullable
    public View findWidgetViewForAppClose(@Nullable StableViewInfo svi,
            @Nullable String packageName, @Nullable UserHandle user) {
        for (int i = 0; i < mWidgetViews.size(); i++) {
            View host = mWidgetViews.valueAt(i);
            if (host == null || host.getParent() == null) {
                continue;
            }
            Object tag = host.getTag();
            if (!(tag instanceof LauncherAppWidgetInfo info)) {
                continue;
            }
            if (svi != null) {
                if (svi.matches(info)) {
                    return host;
                }
            } else if (packageName != null && user != null && info.user.equals(user)
                    && TextUtils.equals(info.getTargetPackage(), packageName)) {
                return host;
            }
        }
        return null;
    }

    /**
     * trebufork: computes the fast-scroll section letter for an app row. English A-Z pass through;
     * Cyrillic first letters are transliterated to their Latin equivalent (К→K, Д→D, Ш→S, Ю→Y...);
     * anything else (digits, hieroglyphs, symbols) falls into the {@code '#'} section.
     */
    public static char getSectionLetter(CharSequence title) {
        if (title == null || title.length() == 0) {
            return '#';
        }
        char c = Character.toUpperCase(title.charAt(0));
        if (c >= 'A' && c <= 'Z') {
            return c;
        }
        switch (c) {
            case 'А': return 'A';
            case 'Б': return 'B';
            case 'В': return 'V';
            case 'Г': return 'G';
            case 'Д': return 'D';
            case 'Е': case 'Ё':
            case 'Э': return 'E';
            case 'Ж': return 'Z';
            case 'З': return 'Z';
            case 'И': return 'I';
            case 'Й': return 'I';
            case 'К': return 'K';
            case 'Л': return 'L';
            case 'М': return 'M';
            case 'Н': return 'N';
            case 'О': return 'O';
            case 'П': return 'P';
            case 'Р': return 'R';
            case 'С': return 'S';
            case 'Т': return 'T';
            case 'У': return 'U';
            case 'Ф': return 'F';
            case 'Х': return 'H';
            case 'Ц': return 'C';
            case 'Ч': return 'C';
            case 'Ш': case 'Щ': return 'S';
            case 'Ы': return 'Y';
            case 'Ю': case 'Я': return 'Y';
            default: return '#';
        }
    }

    /**
     * trebufork: true once the apps list has been filled with real data (see {@link #refreshApps}).
     * The sidebar uses it to avoid dimming every letter during early boot, when the apps list is
     * still empty and every section would look absent.
     */
    public boolean hasAppsData() {
        return !mApps.isEmpty();
    }

    /**
     * trebufork: returns the adapter position of the section separator for {@code letter} in the
     * <em>apps</em> list, or {@link RecyclerView#NO_POSITION} if the section is absent. Used by the
     * sidebar to dim letters that have no apps. Works regardless of the currently active mode.
     */
    public int getFirstPositionForLetter(char letter) {
        for (int i = 0; i < mAppsRows.size(); i++) {
            ListRow row = mAppsRows.get(i);
            if (row.type == VIEW_TYPE_SECTION && row.section == letter) {
                return i;
            }
        }
        return RecyclerView.NO_POSITION;
    }

    /**
     * trebufork: scrolls the list so the first app starting with {@code letter} lands at the
     * 30% top inset line (same place the list content starts), keeping the section in thumb
     * range. Only meaningful while the apps list is active.
     */
    public void scrollToLetter(char letter) {
        int pos = getFirstPositionForLetter(letter);
        if (pos != RecyclerView.NO_POSITION) {
            LinearLayoutManager lm = (LinearLayoutManager) getLayoutManager();
            if (lm != null) {
                lm.scrollToPositionWithOffset(pos, getTopInsetPx());
            }
        }
    }

    private final AllAppsStore.OnUpdateListener mUpdateListener = this::refreshApps;

    private void refreshApps() {
        mApps.clear();
        if (mAppsStore != null) {
            AppInfo[] apps = mAppsStore.getApps();
            if (apps != null) {
                Collections.addAll(mApps, apps);
            }
        }

        // Sort by fast-scroll section first ('#' on top for apps starting with digits/symbols/
        // hieroglyphs, then A-Z), and alphabetically within each section.
        Collator collator = Collator.getInstance();
        Collections.sort(mApps, (a, b) -> {
            char la = getSectionLetter(a.title);
            char lb = getSectionLetter(b.title);
            if (la != lb) {
                if (la == '#') {
                    return -1;
                }
                if (lb == '#') {
                    return 1;
                }
                return Character.compare(la, lb);
            }
            return collator.compare(
                    a.title == null ? "" : a.title.toString(),
                    b.title == null ? "" : b.title.toString());
        });
        mAppsRows.clear();
        mAppsRows.add(new ListRow(VIEW_TYPE_HEADER, null, '\0', null));
        char previousSection = '\0';
        for (AppInfo app : mApps) {
            char section = getSectionLetter(app.title);
            if (section != previousSection) {
                mAppsRows.add(new ListRow(VIEW_TYPE_SECTION, null, section, null));
                previousSection = section;
            }
            mAppsRows.add(new ListRow(VIEW_TYPE_APP, app, section, null));
        }
        mAppsRows.add(new ListRow(VIEW_TYPE_FOOTER, null, '\0', null));
        if (mMode == MODE_APPS) {
            // Re-apply through the search filter so an active query is preserved when the apps
            // store refreshes (e.g. finishing its async load right after boot), instead of the
            // full list silently replacing the filtered results.
            applySearchFilter();
        }
        rebuildDesktopRows();
    }

    // ---------------------------------------------------------------------
    // Desktop rows
    // ---------------------------------------------------------------------

    /** Looks up the {@link AppInfo} for a desktop app entry, or null if the app is gone. */
    @Nullable
    AppInfo findApp(ScrollableDesktopStore.DesktopItem item) {
        if (item.packageName == null || mAppsStore == null) {
            return null;
        }
        AppInfo[] apps = mAppsStore.getApps();
        if (apps == null) {
            return null;
        }
        for (AppInfo info : apps) {
            if (item.user != null && !item.user.equals(info.user)) {
                continue;
            }
            if (item.packageName.equals(info.getTargetPackage())) {
                return info;
            }
        }
        return null;
    }

    /** Looks up the widget provider info for a desktop widget entry. */
    @Nullable
    LauncherAppWidgetProviderInfo getWidgetProvider(
            ScrollableDesktopStore.DesktopItem item) {
        if (item.provider == null) {
            return null;
        }
        return new WidgetManagerHelper(getContext()).getLauncherAppWidgetInfo(
                item.appWidgetId, android.content.ComponentName.unflattenFromString(item.provider));
    }

    /**
     * trebufork: builds the {@link LauncherAppWidgetInfo} used to tag a desktop widget host view.
     * It carries a stable identity (id + container + provider component) so the Quickstep
     * open/close animation can set a launch cookie on open and find this exact widget to animate
     * the app back into on close — the same way the paged workspace tags its widget host views.
     */
    LauncherAppWidgetInfo createWidgetInfo(ScrollableDesktopStore.DesktopItem item) {
        android.content.ComponentName provider = item.provider == null
                ? null : android.content.ComponentName.unflattenFromString(item.provider);
        LauncherAppWidgetInfo info = new LauncherAppWidgetInfo(item.appWidgetId, provider);
        if (item.user != null) {
            info.user = item.user;
        }
        info.spanX = Math.max(1, item.spanX);
        info.spanY = Math.max(1, item.spanY);
        info.id = (int) item.id;
        info.container = LauncherSettings.Favorites.CONTAINER_SCROLLABLE_DESKTOP;
        return info;
    }

    /** Removes desktop entries whose app/widget no longer exists, freeing widget ids. */
    private void cleanupInvalidItems() {
        if (mDesktopStore == null) {
            return;
        }
        android.content.pm.PackageManager pm = getContext().getPackageManager();
        mSuppressStoreRefresh = true;
        try {
            for (int i = mDesktopStore.getItems().size() - 1; i >= 0; i--) {
                ScrollableDesktopStore.DesktopItem item = mDesktopStore.getItems().get(i);
                boolean invalid = false;
                if (item.type == ScrollableDesktopStore.TYPE_APP) {
                    if (item.packageName == null) {
                        invalid = true;
                    } else {
                        try {
                            pm.getApplicationInfoAsUser(item.packageName, 0, item.user);
                        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
                            invalid = true;
                        }
                    }
                } else if (item.type == ScrollableDesktopStore.TYPE_WIDGET) {
                    // Only drop entries with corrupt data. A provider that is temporarily
                    // unavailable (e.g. during boot) keeps its row and renders when it resolves;
                    // users can remove genuinely stale widgets manually via the row button.
                    invalid = item.provider == null || item.appWidgetId < 0;
                }
                // trebufork: folders (TYPE_FOLDER) are never cleaned up here — their members
                // are validated lazily when the folder overlay renders.
                if (invalid) {
                    if (item.type == ScrollableDesktopStore.TYPE_WIDGET
                            && item.appWidgetId > LauncherAppWidgetInfo.CUSTOM_WIDGET_ID
                            && getContext() instanceof Launcher launcher) {
                        launcher.getAppWidgetHolder().deleteAppWidgetId(item.appWidgetId);
                    }
                    mWidgetViews.remove(item.appWidgetId);
                    mDesktopStore.remove(item.id);
                }
            }
        } finally {
            mSuppressStoreRefresh = false;
        }
    }

    private void rebuildDesktopRows() {
        if (mDesktopStore == null) {
            return;
        }
        cleanupInvalidItems();
        mDesktopRows.clear();
        mDesktopRows.add(new ListRow(VIEW_TYPE_DESKTOP_HEADER, null, '\0', null));
        for (ScrollableDesktopStore.DesktopItem item : mDesktopStore.getItems()) {
            if (item.type == ScrollableDesktopStore.TYPE_APP) {
                AppInfo info = findApp(item);
                if (info != null) {
                    mDesktopRows.add(new ListRow(VIEW_TYPE_APP, info, '\0', item));
                }
            } else if (item.type == ScrollableDesktopStore.TYPE_FOLDER) {
                mDesktopRows.add(new ListRow(VIEW_TYPE_DESKTOP_FOLDER, null, '\0', item));
            } else if (item.type == ScrollableDesktopStore.TYPE_GROUP) {
                mDesktopRows.add(new ListRow(VIEW_TYPE_DESKTOP_GROUP, null, '\0', item));
            } else {
                mDesktopRows.add(new ListRow(VIEW_TYPE_DESKTOP_WIDGET, null, '\0', item));
            }
        }
        mDesktopRows.add(new ListRow(VIEW_TYPE_FOOTER, null, '\0', null));
        if (mMode == MODE_DESKTOP) {
            applyRows(mDesktopRows);
        }
    }

    /** Adds an app from the apps list to the desktop (dedupes; switches to desktop mode). */
    public void addToDesktop(AppInfo info) {
        if (mDesktopStore != null) {
            mDesktopStore.addApp(info.getTargetPackage(), info.user);
            showDesktop();
        }
    }

    /** Removes a desktop app entry. */
    private void removeDesktopApp(ScrollableDesktopStore.DesktopItem item) {
        if (mDesktopStore != null) {
            mDesktopStore.remove(item.id);
        }
    }

    /**
     * trebufork: persists a widget's resized size (width/height scale) through the desktop
     * store. Called by the resize frame when the user releases a handle.
     */
    public void setWidgetSize(long id, float widthScale, float heightScale) {
        if (mDesktopStore != null) {
            mDesktopStore.setWidgetSize(id, widthScale, heightScale);
        }
    }

    /**
     * trebufork: persists a widget's horizontal position (0..1 of the free space) through the
     * desktop store. Called by the resize frame when the user drags the widget body.
     */
    public void setWidgetPositionX(long id, float positionX) {
        if (mDesktopStore != null) {
            mDesktopStore.setWidgetPositionX(id, positionX);
        }
    }

    /** Removes a desktop widget entry and frees its app widget id. */
    private void removeDesktopWidget(ScrollableDesktopStore.DesktopItem item) {
        if (item.type == ScrollableDesktopStore.TYPE_WIDGET
                && item.appWidgetId > LauncherAppWidgetInfo.CUSTOM_WIDGET_ID
                && getContext() instanceof Launcher launcher) {
            launcher.getAppWidgetHolder().deleteAppWidgetId(item.appWidgetId);
        }
        mWidgetViews.remove(item.appWidgetId);
        if (mDesktopStore != null) {
            mDesktopStore.remove(item.id);
        }
    }

    // ---------------------------------------------------------------------
    // Desktop folders
    // ---------------------------------------------------------------------

    /** trebufork: opens the folder overlay for a desktop folder entry, anchored to its row. */
    void openFolder(ScrollableDesktopStore.DesktopItem item, View anchor) {
        if (mDesktopStore != null) {
            ScrollableFolderView.show(this, mDesktopStore, item, anchor);
        }
    }

    /** trebufork: shows a rename dialog for a folder and persists the new title. */
    void renameFolder(ScrollableDesktopStore.DesktopItem item) {
        if (mDesktopStore == null || !(getContext() instanceof Launcher launcher)) {
            return;
        }
        EditText input = new EditText(getContext());
        input.setText(item.title);
        input.setSelectAllOnFocus(true);
        input.setSingleLine(true);
        int pad = Math.round(16f * getResources().getDisplayMetrics().density);
        input.setPadding(pad, pad, pad, pad);
        new AlertDialog.Builder(launcher)
                .setTitle(R.string.scrollable_folder_rename)
                .setView(input)
                .setPositiveButton(android.R.string.ok, (d, w) -> {
                    String title = input.getText().toString().trim();
                    if (!title.isEmpty()) {
                        mDesktopStore.setFolderTitle(item.id, title);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * trebufork: shows a Material 3 popup that configures an inline group — rename field,
     * alignment selector and a draggable member list for reordering (see
     * scrollable_m3_configure_dialog.xml). Built from scratch (rounded opaque surface, filled
     * text field, accent chips, end-aligned text buttons) since the launcher has no Material
     * Components dependency.
     */
    private void showConfigureGroupDialog(ScrollableDesktopStore.DesktopItem item) {
        if (mDesktopStore == null || item == null
                || !(getContext() instanceof Launcher launcher)) {
            return;
        }
        View content = LayoutInflater.from(launcher)
                .inflate(R.layout.scrollable_m3_configure_dialog, null, false);
        // trebufork: force fully opaque surfaces on the dialog — the dynamic (monet) surface
        // colors can carry a translucency that lets the launcher show through the popup (the
        // search bar gets the same opaque treatment).
        float density = launcher.getResources().getDisplayMetrics().density;
        GradientDrawable dialogBg = new GradientDrawable();
        dialogBg.setShape(GradientDrawable.RECTANGLE);
        // trebufork: resolve the surface colors from the raw resources, not the theme attrs —
        // the launcher overlays attrs like allappsHeaderProtectionColor / allAppsSurfaceHighest
        // with translucent wallpaper tints (e.g. 0x52FFFFFF), and forcing their alpha to FF turns
        // the field pure white. The raw color resources resolve to proper opaque surfaces.
        dialogBg.setColor(launcher.getColor(R.color.materialColorSurfaceContainerHigh));
        dialogBg.setCornerRadius(28f * density);
        content.setBackground(dialogBg);

        EditText input = content.findViewById(R.id.m3_config_input);
        input.setText(item.title == null ? "" : item.title);
        input.setSelection(input.getText().length());
        GradientDrawable fieldBg = new GradientDrawable();
        fieldBg.setShape(GradientDrawable.RECTANGLE);
        float fieldRadius = 4f * density;
        fieldBg.setCornerRadii(new float[] { fieldRadius, fieldRadius, fieldRadius, fieldRadius,
                0f, 0f, 0f, 0f });
        fieldBg.setColor(launcher.getColor(R.color.materialColorSurfaceContainerHighest));
        GradientDrawable underline = new GradientDrawable();
        underline.setShape(GradientDrawable.RECTANGLE);
        underline.setColor(Themes.getAttrColor(launcher, android.R.attr.colorAccent));
        LayerDrawable fieldLayer = new LayerDrawable(new Drawable[] { fieldBg, underline });
        fieldLayer.setLayerGravity(1, Gravity.BOTTOM);
        fieldLayer.setLayerHeight(1, Math.round(2f * density));
        input.setBackground(fieldLayer);

        // Alignment chips: tapping one highlights it (accent pill) and remembers the choice.
        int accent = Themes.getAttrColor(launcher, android.R.attr.colorAccent);
        int onAccent = ColorUtils.calculateLuminance(accent) > 0.5f ? Color.BLACK : Color.WHITE;
        final ImageButton[] alignChips = {
                content.findViewById(R.id.m3_config_align_start),
                content.findViewById(R.id.m3_config_align_center),
                content.findViewById(R.id.m3_config_align_end)
        };
        final int[] selectedAlign = new int[] { item.align };
        Runnable paintAlignChips = () -> {
            for (int i = 0; i < alignChips.length; i++) {
                boolean selected = i == selectedAlign[0];
                if (selected) {
                    GradientDrawable pill = new GradientDrawable();
                    pill.setShape(GradientDrawable.RECTANGLE);
                    pill.setColor(accent);
                    pill.setCornerRadius(20f * density);
                    alignChips[i].setBackground(pill);
                    alignChips[i].setColorFilter(onAccent);
                } else {
                    alignChips[i].setBackground(null);
                    alignChips[i].clearColorFilter();
                }
            }
        };
        paintAlignChips.run();
        for (int i = 0; i < alignChips.length; i++) {
            final int value = i;
            alignChips[i].setOnClickListener(v -> {
                selectedAlign[0] = value;
                paintAlignChips.run();
            });
        }

        // Member list: long-pressing a row's handle drags it to a new slot. The move is
        // persisted to the store immediately (like the in-row drag on the desktop), so the
        // desktop list behind the dialog stays in sync.
        final List<ScrollableDesktopStore.DesktopItem> members =
                new ArrayList<>(item.members);
        RecyclerView list = content.findViewById(R.id.m3_config_list);
        // trebufork: the members are ordered in a single horizontal row of icons; long-pressing
        // one lifts it and dragging left/right moves it between slots.
        list.setLayoutManager(new LinearLayoutManager(
                launcher, LinearLayoutManager.HORIZONTAL, false));
        GroupConfigureAdapter adapter = new GroupConfigureAdapter(members);
        list.setAdapter(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView,
                    @NonNull RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, 0);
            }

            @Override
            public boolean isLongPressDragEnabled() {
                // Dragged from the icon's long-press only.
                return false;
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                    @NonNull RecyclerView.ViewHolder viewHolder,
                    @NonNull RecyclerView.ViewHolder target) {
                int from = viewHolder.getBindingAdapterPosition();
                int to = target.getBindingAdapterPosition();
                if (from < 0 || to < 0 || from == to) {
                    return true;
                }
                ScrollableDesktopStore.DesktopItem member = members.remove(from);
                members.add(to, member);
                adapter.notifyItemMoved(from, to);
                if (mDesktopStore != null) {
                    mDesktopStore.moveInGroup(item.id, from, to);
                }
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) { }
        });
        touchHelper.attachToRecyclerView(list);
        adapter.setTouchHelper(touchHelper);

        // trebufork: the dialog must run under the launcher's (wallpaper-adapted) theme, not the
        // system's default dialog theme — otherwise the ?attr/ colors resolve against a light
        // theme and the field/dialog render white in dark mode.
        Dialog dialog = new Dialog(launcher, Themes.getActivityThemeRes(launcher));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(content);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(Math.round(
                    getResources().getDisplayMetrics().widthPixels * 0.85f),
                    WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setDimAmount(0.32f);
            // trebufork: pop-in / pop-out animation on open and close.
            window.setWindowAnimations(R.style.TrebuforkDialogAnimation);
        }
        content.findViewById(R.id.m3_config_cancel).setOnClickListener(v -> dialog.dismiss());
        content.findViewById(R.id.m3_config_done).setOnClickListener(v -> {
            String title = input.getText().toString().trim();
            if (!title.isEmpty()) {
                mDesktopStore.setGroupTitle(item.id, title);
            }
            if (selectedAlign[0] != item.align) {
                mDesktopStore.setGroupAlign(item.id, selectedAlign[0]);
            }
            dialog.dismiss();
        });
        input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                            && event.getAction() == KeyEvent.ACTION_DOWN)) {
                content.findViewById(R.id.m3_config_done).performClick();
                return true;
            }
            return false;
        });
        dialog.show();
    }

    /** Removes a desktop folder entry, asking what to do with its members first. */
    private void removeDesktopFolder(ScrollableDesktopStore.DesktopItem item) {
        confirmRemoveContainer(item, R.string.scrollable_folder_remove);
    }

    /**
     * trebufork: asks what to do with a container's members before removing it — return them to
     * the desktop top-level list, or remove them along with the container. Empty containers are
     * removed directly without prompting.
     */
    private void confirmRemoveContainer(ScrollableDesktopStore.DesktopItem item, int titleRes) {
        if (mDesktopStore == null || !(getContext() instanceof Launcher launcher)) {
            return;
        }
        if (item.members.isEmpty()) {
            mDesktopStore.remove(item.id);
            return;
        }
        new AlertDialog.Builder(launcher)
                .setTitle(titleRes)
                .setMessage(R.string.scrollable_remove_container_message)
                .setPositiveButton(R.string.scrollable_remove_keep,
                        (d, w) -> keepContainerMembers(item))
                .setNegativeButton(R.string.scrollable_remove_delete,
                        (d, w) -> deleteContainerMembers(item))
                .show();
    }

    /** trebufork: moves every member back onto the desktop (appended) and removes the container. */
    private void keepContainerMembers(ScrollableDesktopStore.DesktopItem item) {
        if (mDesktopStore == null) {
            return;
        }
        boolean isGroup = item.type == ScrollableDesktopStore.TYPE_GROUP;
        int insertAt = mDesktopStore.getItems().size();
        int added = 0;
        while (!item.members.isEmpty()) {
            ScrollableDesktopStore.DesktopItem member = item.members.get(0);
            ScrollableDesktopStore.DesktopItem removed = isGroup
                    ? mDesktopStore.removeItemFromGroup(item.id, member.id)
                    : mDesktopStore.removeItemFromFolder(item.id, member.id);
            if (removed == null) {
                break;
            }
            mDesktopStore.insertItem(insertAt + added, removed);
            added++;
        }
        mDesktopStore.remove(item.id);
    }

    /** trebufork: removes the container together with all its members (freeing widget ids). */
    private void deleteContainerMembers(ScrollableDesktopStore.DesktopItem item) {
        if (mDesktopStore == null) {
            return;
        }
        for (ScrollableDesktopStore.DesktopItem member : item.members) {
            if (member.type == ScrollableDesktopStore.TYPE_WIDGET
                    && member.appWidgetId > LauncherAppWidgetInfo.CUSTOM_WIDGET_ID
                    && getContext() instanceof Launcher launcher) {
                launcher.getAppWidgetHolder().deleteAppWidgetId(member.appWidgetId);
            }
            mWidgetViews.remove(member.appWidgetId);
        }
        mDesktopStore.remove(item.id);
    }

    /** trebufork: creates an empty folder on the desktop and opens it for filling. */
    void createNewFolder() {
        if (mDesktopStore == null) {
            return;
        }
        ScrollableDesktopStore.DesktopItem folder = mDesktopStore.addFolder(null);
        showDesktop();
        // No anchor row yet (the folder was just created) — falls back to the default position.
        openFolder(folder, null);
    }

    /**
     * trebufork: moves an app into a folder. If the app is already on the desktop top-level
     * list it is moved; otherwise a fresh entry is created and moved in.
     */
    private void addAppToFolder(AppInfo info, long folderId) {
        if (mDesktopStore == null) {
            return;
        }
        for (ScrollableDesktopStore.DesktopItem item : mDesktopStore.getItems()) {
            if (item.type == ScrollableDesktopStore.TYPE_APP
                    && info.getTargetPackage().equals(item.packageName)
                    && item.user != null && item.user.equals(info.user)) {
                mDesktopStore.addItemToFolder(folderId, item);
                return;
            }
        }
        ScrollableDesktopStore.DesktopItem fresh =
                mDesktopStore.addApp(info.getTargetPackage(), info.user);
        mDesktopStore.addItemToFolder(folderId, fresh);
    }

    /**
     * trebufork: shows one combined container picker (folders + groups with room + "New folder"
     * + "New group") and adds the app to the chosen container. Groups that are already full are
     * not offered, so the user can never add more apps than fit in a single row.
     */
    private void showAddToContainerPicker(AppInfo info) {
        if (mDesktopStore == null || !(getContext() instanceof Launcher launcher)) {
            return;
        }
        int maxApps = getGroupMaxApps();
        List<ScrollableDesktopStore.DesktopItem> folders = new ArrayList<>();
        List<ScrollableDesktopStore.DesktopItem> groups = new ArrayList<>();
        for (ScrollableDesktopStore.DesktopItem item : mDesktopStore.getItems()) {
            if (item.type == ScrollableDesktopStore.TYPE_FOLDER) {
                folders.add(item);
            } else if (item.type == ScrollableDesktopStore.TYPE_GROUP
                    && item.members.size() < maxApps) {
                groups.add(item);
            }
        }
        int folderCount = folders.size();
        int groupCount = groups.size();
        String[] labels = new String[folderCount + groupCount + 2];
        for (int i = 0; i < folderCount; i++) {
            labels[i] = folderPickerLabel(folders.get(i));
        }
        for (int i = 0; i < groupCount; i++) {
            labels[folderCount + i] = groupPickerLabel(groups.get(i), i);
        }
        labels[folderCount + groupCount] = getContext().getString(R.string.scrollable_new_folder);
        labels[folderCount + groupCount + 1] = getContext().getString(R.string.scrollable_new_group);
        new AlertDialog.Builder(launcher)
                .setTitle(R.string.scrollable_add_to_container)
                .setItems(labels, (dialog, which) -> {
                    if (which < folderCount) {
                        addAppToFolder(info, folders.get(which).id);
                    } else if (which < folderCount + groupCount) {
                        addAppToGroup(info, groups.get(which - folderCount).id);
                    } else if (which == folderCount + groupCount) {
                        ScrollableDesktopStore.DesktopItem folder = mDesktopStore.addFolder(null);
                        addAppToFolder(info, folder.id);
                        showDesktop();
                        openFolder(folder, null);
                    } else {
                        ScrollableDesktopStore.DesktopItem group = mDesktopStore.addGroup();
                        addAppToGroup(info, group.id);
                        showDesktop();
                    }
                })
                .show();
    }

    /** trebufork: picker label for a folder — its name, prefixed when renamed. */
    private String folderPickerLabel(ScrollableDesktopStore.DesktopItem folder) {
        String title = folder.title;
        if (title == null || title.isEmpty()) {
            return getContext().getString(R.string.scrollable_folder_default_title);
        }
        return getContext().getString(R.string.scrollable_folder_label_prefix, title);
    }

    /** trebufork: picker label for a group — "Group N" when unnamed, prefixed when renamed. */
    private String groupPickerLabel(ScrollableDesktopStore.DesktopItem group, int index) {
        String title = group.title;
        if (title == null || title.isEmpty()) {
            return getContext().getString(R.string.scrollable_group_default_title)
                    + " " + (index + 1);
        }
        return getContext().getString(R.string.scrollable_group_label_prefix, title);
    }

    /** trebufork: adds an app to a group, reusing an existing top-level entry when possible. */
    private void addAppToGroup(AppInfo info, long groupId) {
        if (mDesktopStore == null) {
            return;
        }
        ScrollableDesktopStore.DesktopItem group = mDesktopStore.findGroup(groupId);
        if (group == null || group.members.size() >= getGroupMaxApps()) {
            return;
        }
        for (ScrollableDesktopStore.DesktopItem item : mDesktopStore.getItems()) {
            if (item.type == ScrollableDesktopStore.TYPE_APP
                    && info.getTargetPackage().equals(item.packageName)
                    && item.user != null && item.user.equals(info.user)) {
                mDesktopStore.addItemToGroup(groupId, item);
                return;
            }
        }
        ScrollableDesktopStore.DesktopItem fresh =
                mDesktopStore.addApp(info.getTargetPackage(), info.user);
        mDesktopStore.addItemToGroup(groupId, fresh);
    }

    /**
     * trebufork: number of 44dp icons (+8dp gaps) that fit on a single desktop row given the
     * current screen width and the alphabet-strip margin.
     */
    private int getGroupMaxApps() {
        float density = getResources().getDisplayMetrics().density;
        int iconSize = Math.round(44f * density);
        int gap = Math.round(8f * density);
        // The icons live inside scroll_group_row_content, which has 10dp start + 16dp end
        // padding and a right margin that stops at the alphabet strip (mRowEndMarginPx). Use the
        // actual RecyclerView width (not the display width) so the launcher's 40dp left gutter is
        // accounted for; the old formula used the full display width and allowed one icon too many.
        int padding = Math.round(10f * density) + Math.round(16f * density);
        int available = getWidth() - mRowEndMarginPx - padding;
        if (available <= iconSize) {
            return 1;
        }
        return (available + gap) / (iconSize + gap);
    }

    /** trebufork: removes a member from a group and returns it to the desktop top-level list. */
    void removeFromGroup(long groupId, long memberId) {
        if (mDesktopStore == null) {
            return;
        }
        ScrollableDesktopStore.DesktopItem group = mDesktopStore.findGroup(groupId);
        ScrollableDesktopStore.DesktopItem removed =
                mDesktopStore.removeItemFromGroup(groupId, memberId);
        if (removed != null) {
            mDesktopStore.insertItem(mDesktopStore.getItems().size(), removed);
            // trebufork: a group emptied by removing its last member is deleted, so no empty
            // phantom row lingers on the desktop.
            if (group != null && group.members.isEmpty()) {
                mDesktopStore.remove(group.id);
            }
        }
    }

    /** trebufork: removes an inline group from the desktop, asking what to do with its members. */
    private void removeGroup(ScrollableDesktopStore.DesktopItem item) {
        confirmRemoveContainer(item, R.string.scrollable_group_remove);
    }

    /**
     * trebufork: moves a folder member back out onto the desktop top-level list (drag-out
     * equivalent). Called from the folder overlay's member menu.
     */
    void removeFromFolder(long folderId, long memberId) {
        if (mDesktopStore == null) {
            return;
        }
        ScrollableDesktopStore.DesktopItem removed =
                mDesktopStore.removeItemFromFolder(folderId, memberId);
        if (removed != null) {
            mDesktopStore.insertItem(mDesktopStore.getItems().size(), removed);
        }
    }

    // ---------------------------------------------------------------------
    // Widget host views
    // ---------------------------------------------------------------------

    private View getWidgetView(ScrollableDesktopStore.DesktopItem item) {
        View cached = mWidgetViews.get(item.appWidgetId);
        if (cached != null) {
            return cached;
        }
        LauncherAppWidgetProviderInfo provider = getWidgetProvider(item);
        if (provider == null) {
            return null;
        }
        if (getContext() instanceof Launcher launcher) {
            AppWidgetHostView host = launcher.getAppWidgetHolder()
                    .createView(item.appWidgetId, provider);
            mWidgetViews.put(item.appWidgetId, host);
            return host;
        }
        return null;
    }

    /**
     * trebufork: re-creates every desktop widget host view. On boot the desktop rows are bound
     * before {@link com.android.launcher3.widget.LauncherWidgetHolder#startListening()} finishes,
     * so the views are created as {@code PendingAppWidgetHostView} placeholders. Those carry no
     * {@link LauncherAppWidgetInfo} tag, so the holder's {@code reInflate()} path leaves them
     * stuck on the "loading" placeholder. Dropping the cache and rebinding while the host is
     * listening makes {@link #getWidgetView} create real host views that receive updates.
     */
    public void refreshWidgetViews() {
        mWidgetsRefreshed = true;
        if (mMode != MODE_DESKTOP || mDesktopStore == null) {
            return;
        }
        if (!isAttachedToWindow()) {
            // The listening callback can fire before the window is attached (early boot), when
            // the desktop rows have already been bound as PendingAppWidgetHostView placeholders.
            // Defer the refresh until the view is attached so those placeholders get re-created
            // as real host views that receive updates.
            addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {
                    removeOnAttachStateChangeListener(this);
                    refreshWidgetViews();
                }

                @Override
                public void onViewDetachedFromWindow(View v) { }
            });
            return;
        }
        if (mEntrancePlayed) {
            // The boot cascade already ran before listening started; re-create the widget rows
            // now so they become real host views (the entrance animation won't run again).
            mWidgetViews.clear();
            int first = mRows.isEmpty() ? -1 : 1; // position 0 is the desktop header
            if (first >= 0 && first < mRows.size()) {
                mAdapter.notifyItemRangeChanged(first, mRows.size() - first);
            }
        } else {
            // The boot cascade hasn't run yet; it clears the cache and rebinds icons + widgets
            // together once boot completes (see startEntranceWhenReady).
            startEntranceWhenReady();
        }
    }

    /** Natural aspect ratio (height/width) of the widget from its provider min sizes. */
    private float getWidgetAspectRatio(ScrollableDesktopStore.DesktopItem item) {
        LauncherAppWidgetProviderInfo provider = getWidgetProvider(item);
        if (provider == null) {
            return 1f;
        }
        float w = Math.max(provider.minWidth, provider.minResizeWidth);
        float h = Math.max(provider.minHeight, provider.minResizeHeight);
        if (w <= 0f || h <= 0f) {
            return 1f;
        }
        return h / w;
    }

    // ---------------------------------------------------------------------
    // Drop target for widget adds
    // ---------------------------------------------------------------------

    @Override
    public boolean isDropEnabled() {
        // trebufork: only accept drops while the scrollable home is actually shown. The view is
        // registered as a drop target for the whole lifetime of the launcher, and a GONE view
        // keeps its last laid-out bounds (full screen), so without this check its stale hit rect
        // would swallow every drag on the paged workspace once scrollable home is turned off —
        // the remove/uninstall targets would never react and drops would be rejected.
        return getVisibility() == View.VISIBLE;
    }

    @Override
    public boolean acceptDrop(DropTarget.DragObject dragObject) {
        return dragObject.dragInfo instanceof PendingAddWidgetInfo;
    }

    @Override
    public void onDrop(DropTarget.DragObject dragObject, DragOptions options) {
        if (dragObject.dragInfo instanceof PendingAddWidgetInfo info
                && getContext() instanceof Launcher launcher) {
            launcher.addWidgetToScrollableDesktop(info);
        }
        // The workspace animates the drag preview into its cell on a successful drop; this list
        // has no per-item animation, so clear the deferred cleanup or the preview would stay
        // frozen on screen (e.g. over the widget config/bind activity).
        dragObject.deferDragViewCleanupPostAnimation = false;
    }

    @Override
    public void onDragEnter(DropTarget.DragObject dragObject) { }

    @Override
    public void onDragOver(DropTarget.DragObject dragObject) { }

    @Override
    public void onDragExit(DropTarget.DragObject dragObject) { }

    @Override
    public void prepareAccessibilityDrop() { }

    @Override
    public void getHitRectRelativeToDragLayer(Rect outRect) {
        if (getContext() instanceof Launcher launcher) {
            launcher.getDragLayer().getDescendantRectRelativeToSelf(this, outRect);
        }
    }

    // ---------------------------------------------------------------------
    // Adapter
    // ---------------------------------------------------------------------

    private class AppAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_HEADER) {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.scrollable_app_header, parent, false);
                return new HeaderViewHolder(v);
            }
            if (viewType == VIEW_TYPE_SECTION) {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.scrollable_app_section_header, parent, false);
                return new SectionViewHolder(v);
            }
            if (viewType == VIEW_TYPE_DESKTOP_HEADER) {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.scrollable_desktop_header, parent, false);
                return new DesktopHeaderViewHolder(v);
            }
            if (viewType == VIEW_TYPE_DESKTOP_WIDGET) {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.scrollable_widget_row, parent, false);
                return new DesktopWidgetViewHolder(v);
            }
            if (viewType == VIEW_TYPE_DESKTOP_FOLDER) {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.scrollable_folder_row, parent, false);
                return new FolderViewHolder(v);
            }
            if (viewType == VIEW_TYPE_DESKTOP_GROUP) {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.scrollable_group_row, parent, false);
                return new GroupViewHolder(v);
            }
            if (viewType == VIEW_TYPE_FOOTER) {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.scrollable_app_footer, parent, false);
                return new FooterViewHolder(v);
            }
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.scrollable_app_row, parent, false);
            return new AppViewHolder(v);
        }

        @Override
        public int getItemViewType(int position) {
            return mRows.get(position).type;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ListRow row = mRows.get(position);
            if (holder instanceof AppViewHolder && row.app != null) {
                boolean desktopRow = row.desktopItem != null;
                AppViewHolder appHolder = (AppViewHolder) holder;
                appHolder.bind(row.app, row.section, desktopRow, row.desktopItem);
                appHolder.setHighlighted(position == mHighlightedPosition);
            } else if (holder instanceof SectionViewHolder) {
                ((SectionViewHolder) holder).bind(row.section,
                        !mAlphabetDragging || row.section == mAlphabetDragLetter);
            } else if (holder instanceof DesktopHeaderViewHolder) {
                // The header is a 30%-inset long-press target; re-apply its height on rebind so
                // pref_scrollable_top_inset changes take effect without restarting Launcher.
                ((DesktopHeaderViewHolder) holder).applyTopInset();
            } else if (holder instanceof DesktopWidgetViewHolder && row.desktopItem != null) {
                ((DesktopWidgetViewHolder) holder).bind(row.desktopItem);
            } else if (holder instanceof FolderViewHolder && row.desktopItem != null) {
                ((FolderViewHolder) holder).bind(row.desktopItem);
            } else if (holder instanceof GroupViewHolder && row.desktopItem != null) {
                ((GroupViewHolder) holder).bind(row.desktopItem);
            } else if (holder instanceof HeaderViewHolder) {
                ((HeaderViewHolder) holder).applyTopInset();
            } else if (holder instanceof FooterViewHolder) {
                ((FooterViewHolder) holder).applyBottomInset();
            }
            // trebufork: the boot entrance cascade. Before the window opens (early boot: the
            // widget rows are bound while the apps list is still loading), every desktop row is
            // kept invisible so nothing appears ahead of the cascade — widgets and apps are
            // revealed together. Once the window has closed, alpha is forced back to 1: rows
            // hidden earlier are recycled by RecyclerView and their bind() does not reset the
            // alpha, so without this they would stay invisible forever.
            if (mMode == MODE_DESKTOP) {
                if (mEntranceAnimating) {
                    playEntranceAnimation(holder.itemView, position);
                } else if (!mEntrancePlayed) {
                    holder.itemView.setAlpha(0f);
                } else {
                    holder.itemView.setAlpha(1f);
                }
            }
        }

        @Override
        public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
            super.onViewRecycled(holder);
            // A recycled row must not keep its in-flight entrance marker, so a later bind of
            // this (recycled) item view can cascade normally.
            holder.itemView.setTag(R.id.entrance_animating, null);
            if (holder instanceof DesktopWidgetViewHolder widgetHolder) {
                // Detach the host view so it can be re-attached on the next bind.
                widgetHolder.detachWidget();
            }
        }

        @Override
        public int getItemCount() {
            return mRows.size();
        }
    }

    // ---------------------------------------------------------------------
    // Row holders
    // ---------------------------------------------------------------------

    private class AppViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final BubbleTextView mIcon;
        private final TextView mLabel;
        private final View mRowContent;
        private final ImageView mDragHandle;
        private AppInfo mInfo;
        private ScrollableDesktopStore.DesktopItem mDesktopItem;
        private boolean mDesktopRow;

        AppViewHolder(@NonNull View itemView) {
            super(itemView);
            mIcon = itemView.findViewById(R.id.scroll_app_icon);
            mLabel = itemView.findViewById(R.id.scroll_app_label);
            mDragHandle = itemView.findViewById(R.id.scroll_app_drag_handle);
            mDragHandle.setColorFilter(
                    Themes.getAttrColor(getContext(), R.attr.workspaceTextColor));
            // The tap target is the inner rounded container (icon + label), not the full-width
            // row, so the clickable area stays off the alphabet and matches the rounded highlight.
            mRowContent = itemView.findViewById(R.id.scroll_app_row_content);
            mRowContent.setOnClickListener(this);
            mRowContent.setOnLongClickListener(v -> {
                if (mDesktopRow) {
                    if (mReorderMode) {
                        // Reorder mode: long-pressing the row itself also drags it.
                        if (mItemTouchHelper != null && !mDragInProgress) {
                            mItemTouchHelper.startDrag(AppViewHolder.this);
                        }
                    } else {
                        // trebufork: desktop rows open the same long-press menu as the paged
                        // workspace (remove, reorder, app info, ...).
                        return showDesktopAppContextMenu();
                    }
                    return true;
                }
                return showAppContextMenu();
            });
            // trebufork: in reorder mode the drag handle lifts the row; touching it elsewhere
            // (or outside reorder mode) is ignored. Must return false so the RecyclerView keeps
            // receiving the gesture and hands it to the ItemTouchHelper once the drag starts.
            mDragHandle.setOnTouchListener((v, ev) -> {
                if (ev.getActionMasked() == MotionEvent.ACTION_DOWN
                        && mDesktopRow && mReorderMode && !mDragInProgress) {
                    mItemTouchHelper.startDrag(AppViewHolder.this);
                }
                return false;
            });
        }

        void bind(AppInfo info, char section, boolean desktopRow,
                @Nullable ScrollableDesktopStore.DesktopItem desktopItem) {
            mInfo = info;
            mDesktopItem = desktopItem;
            mDesktopRow = desktopRow;
            itemView.setTag(info);
            itemView.animate().cancel();
            boolean showRow = !mAlphabetDragging || section == mAlphabetDragLetter;
            itemView.setAlpha(showRow ? 1f : 0f);
            applyRowEndMargin();
            // trebufork: the row icon is a BubbleTextView so the stock app-icon context menu
            // (PopupContainerWithArrow) can be opened anchored to it, exactly like a workspace icon.
            // applyFromApplicationInfo binds the icon and tags the view with the ItemInfo.
            // The BubbleTextView's own label is cleared — the row shows the label in a separate
            // TextView next to the icon.
            mIcon.applyFromApplicationInfo(info);
            mIcon.setText("");
            mIcon.setContentDescription(null);
            applyHideLabels(info);
            mLabel.setText(info.title);
            // trebufork: the 3-dot reorder handle is intentionally never shown; reordering is
            // started by long-pressing the row in reorder mode.
            mDragHandle.setVisibility(View.GONE);
        }

        private void applyHideLabels(AppInfo info) {
            if (mHideLabels) {
                mLabel.setVisibility(View.GONE);
                mIcon.setIconVisible(false);
                GradientDrawable bg = new GradientDrawable();
                bg.setShape(GradientDrawable.RECTANGLE);
                bg.setColor(pastelColorFor(info));
                bg.setCornerRadius(PASTEL_TILE_RADIUS_DP
                        * mIcon.getResources().getDisplayMetrics().density);
                mIcon.setBackground(bg);
            } else {
                mLabel.setVisibility(View.VISIBLE);
                mIcon.setIconVisible(true);
                mIcon.setBackground(null);
            }
        }

        /** trebufork: highlights the row (the first search result) with a rounded accent fill. */
        void setHighlighted(boolean highlighted) {
            if (highlighted) {
                GradientDrawable bg = new GradientDrawable();
                bg.setShape(GradientDrawable.RECTANGLE);
                int accent = Themes.getAttrColor(getContext(), android.R.attr.colorAccent);
                bg.setColor(ColorUtils.setAlphaComponent(accent, 0x33));
                bg.setCornerRadius(24f * getResources().getDisplayMetrics().density);
                mRowContent.setBackground(bg);
            } else {
                mRowContent.setBackgroundResource(R.drawable.scrollable_app_row_background);
            }
        }

        private void applyRowEndMargin() {
            ViewGroup.MarginLayoutParams lp =
                    (ViewGroup.MarginLayoutParams) mRowContent.getLayoutParams();
            if (lp != null && lp.getMarginEnd() != mRowEndMarginPx) {
                lp.setMarginEnd(mRowEndMarginPx);
                mRowContent.setLayoutParams(lp);
            }
        }

        @Override
        public void onClick(View v) {
            if (mInfo == null || mInfo.getIntent() == null
                    || !(getContext() instanceof Launcher)) {
                return;
            }
            Launcher launcher = (Launcher) getContext();
            // Launch from the icon itself so the clip-reveal / app-open animation scales out of the
            // 44dp icon (source bounds + makeClipRevealAnimation) rather than the whole row.
            launcher.startActivitySafely(mIcon, mInfo.getIntent(), mInfo);
        }

        /**
         * trebufork: shows the stock app-icon context menu (the arrow popup used on the paged
         * workspace and in All Apps) when an app row is long-pressed in the apps list, but keeps
         * the custom menu entries from the original trebufork menu: App info, Add to desktop,
         * Widgets and Uninstall. Mirrors {@code PopupControllerForAppIcon.show()} with a custom
         * {@link SystemShortcut} list.
         */
        private boolean showAppContextMenu() {
            return showAppContextMenu(/* fromDesktop */ false);
        }

        /**
         * trebufork: desktop-row variant of the app menu. Mirrors the paged-workspace menu
         * (App info, Widgets, Uninstall) plus "Remove from desktop" and "Reorder".
         */
        private boolean showDesktopAppContextMenu() {
            return showAppContextMenu(/* fromDesktop */ true);
        }

        private boolean showAppContextMenu(boolean fromDesktop) {
            if (mInfo == null || !(getContext() instanceof Launcher launcher)) {
                return false;
            }
            if (PopupContainer.getOpen(launcher) != null) {
                // There is already an items container open, so don't open this one.
                mIcon.clearFocus();
                return false;
            }

            List<SystemShortcut<ActivityContext>> shortcuts = new ArrayList<>();
            // App info — stock implementation.
            SystemShortcut<ActivityContext> appInfo =
                    SystemShortcut.APP_INFO.getShortcut(launcher, mInfo, mIcon);
            if (appInfo != null) {
                shortcuts.add(appInfo);
            }
            if (fromDesktop) {
                // Remove from desktop — trebufork: removes the app from the desktop list.
                shortcuts.add(new SystemShortcut<ActivityContext>(R.drawable.ic_remove_no_shadow,
                        R.string.scrollable_desktop_remove, launcher, mInfo, mIcon) {
                    @Override
                    public void onClick(View view) {
                        AbstractFloatingView.closeAllOpenViews(mTarget);
                        if (mDesktopItem != null) {
                            removeDesktopApp(mDesktopItem);
                        }
                    }
                });
            } else {
                // Add to desktop — trebufork: adds the app to the scrollable-home desktop list.
                shortcuts.add(new SystemShortcut<ActivityContext>(R.drawable.ic_plus,
                        R.string.trebufork_add_to_desktop, launcher, mInfo, mIcon) {
                    @Override
                    public void onClick(View view) {
                        AbstractFloatingView.closeAllOpenViews(mTarget);
                        addToDesktop(mInfo);
                    }
                });
            }
            // Add to folder or group — trebufork: moves the app into a desktop folder or an
            // inline group (single picker offers both container types).
            shortcuts.add(new SystemShortcut<ActivityContext>(R.drawable.ic_folder,
                    R.string.scrollable_add_to_container, launcher, mInfo, mIcon) {
                @Override
                public void onClick(View view) {
                    AbstractFloatingView.closeAllOpenViews(mTarget);
                    showAddToContainerPicker(mInfo);
                }
            });
            // Widgets — stock implementation, hidden when the package has none.
            SystemShortcut<ActivityContext> widgets =
                    SystemShortcut.WIDGETS.getShortcut(launcher, mInfo, mIcon);
            if (widgets != null) {
                shortcuts.add(widgets);
            }
            // Uninstall — trebufork: only offered from the app list, not from the scrollable
            // desktop (stock implementation, hidden for system apps).
            if (!fromDesktop
                    && SecondaryDropTarget.getUninstallTarget(getContext(), mInfo) != null) {
                shortcuts.add(new SystemShortcut<ActivityContext>(
                        R.drawable.ic_uninstall_no_shadow, R.string.uninstall_drop_target_label,
                        launcher, mInfo, mIcon) {
                    @Override
                    public void onClick(View view) {
                        AbstractFloatingView.closeAllOpenViews(mTarget);
                        SecondaryDropTarget.performUninstall(mTarget.asContext(),
                                SecondaryDropTarget.getUninstallTarget(mTarget.asContext(), mInfo),
                                mInfo);
                    }
                });
            }
            if (fromDesktop) {
                // Reorder — trebufork: enters reorder mode with drag handles on desktop rows.
                shortcuts.add(new SystemShortcut<ActivityContext>(R.drawable.ic_more_vert_dots,
                        R.string.scrollable_desktop_reorder, launcher, mInfo, mIcon) {
                    @Override
                    public void onClick(View view) {
                        AbstractFloatingView.closeAllOpenViews(mTarget);
                        enterReorderMode();
                    }
                });
            }

            PopupContainerWithArrow<Launcher> container =
                    PopupContainerWithArrow.create(
                            /* context */ launcher,
                            /* originalView */ mIcon,
                            /* itemInfo */ mInfo,
                            /* updateIconUi */ true
                    );
            container.configureForLauncher(launcher, mInfo);
            // No deep shortcuts in the scrollable list — show only the system shortcuts.
            container.populateAndShowRows(0, shortcuts);
            launcher.refreshAndBindWidgetsForPackageUser(PackageUserKey.fromItemInfo(mInfo));
            container.requestFocus();
            return true;
        }
    }

    private class FolderViewHolder extends RecyclerView.ViewHolder {

        private final View mRowContent;
        private final TextView mLabel;
        private final BubbleTextView[] mCells = new BubbleTextView[4];
        private final ImageView mDragHandle;
        private ScrollableDesktopStore.DesktopItem mItem;

        FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            mRowContent = itemView.findViewById(R.id.scroll_folder_row_content);
            mLabel = itemView.findViewById(R.id.scroll_folder_label);
            mCells[0] = itemView.findViewById(R.id.scroll_folder_cell0);
            mCells[1] = itemView.findViewById(R.id.scroll_folder_cell1);
            mCells[2] = itemView.findViewById(R.id.scroll_folder_cell2);
            mCells[3] = itemView.findViewById(R.id.scroll_folder_cell3);
            mDragHandle = itemView.findViewById(R.id.scroll_folder_drag_handle);
            mDragHandle.setColorFilter(
                    Themes.getAttrColor(getContext(), R.attr.workspaceTextColor));
            mRowContent.setOnClickListener(v -> openFolder(mItem, v));
            mRowContent.setOnLongClickListener(v -> {
                if (mReorderMode) {
                    if (mItemTouchHelper != null && !mDragInProgress) {
                        mItemTouchHelper.startDrag(FolderViewHolder.this);
                    }
                } else {
                    return showFolderContextMenu();
                }
                return true;
            });
            mDragHandle.setOnTouchListener((v, ev) -> {
                if (ev.getActionMasked() == MotionEvent.ACTION_DOWN
                        && mReorderMode && !mDragInProgress) {
                    mItemTouchHelper.startDrag(FolderViewHolder.this);
                }
                return false;
            });
        }

        void bind(ScrollableDesktopStore.DesktopItem item) {
            mItem = item;
            String title = item.title;
            mLabel.setText(title == null || title.isEmpty()
                    ? getContext().getString(R.string.scrollable_folder_default_title) : title);
            // trebufork: fill the 2x2 preview with up to four app members; widget members and
            // empty cells show a subtle placeholder.
            for (int i = 0; i < mCells.length; i++) {
                BubbleTextView cell = mCells[i];
                AppInfo info = i < item.members.size()
                        ? findApp(item.members.get(i)) : null;
                if (info != null) {
                    cell.applyFromApplicationInfo(info);
                    cell.setText("");
                    cell.setIconVisible(true);
                    cell.setBackground(null);
                } else {
                    cell.setIconVisible(false);
                    cell.setText("");
                    GradientDrawable bg = new GradientDrawable();
                    bg.setShape(GradientDrawable.RECTANGLE);
                    bg.setColor(ColorUtils.setAlphaComponent(
                            Themes.getAttrColor(getContext(), R.attr.workspaceTextColor), 0x18));
                    // trebufork: adaptive app icons render ~1dp smaller than the 26dp cell, so
                    // inset the placeholder by the same amount (and match the icon corner
                    // radius) — otherwise empty cells look bigger than cells with an icon.
                    float density = cell.getResources().getDisplayMetrics().density;
                    bg.setCornerRadius(4f * density);
                    int inset = Math.round(density);
                    cell.setBackground(new InsetDrawable(bg, inset, inset, inset, inset));
                }
            }
            ViewGroup.MarginLayoutParams lp =
                    (ViewGroup.MarginLayoutParams) mRowContent.getLayoutParams();
            if (lp != null && lp.getMarginEnd() != mRowEndMarginPx) {
                lp.setMarginEnd(mRowEndMarginPx);
                mRowContent.setLayoutParams(lp);
            }
            // trebufork: the 3-dot reorder handle is intentionally never shown; reordering is
            // started by long-pressing the row in reorder mode.
            mDragHandle.setVisibility(View.GONE);
        }

        /** trebufork: folder row context menu (rename, remove, reorder). */
        private boolean showFolderContextMenu() {
            if (mItem == null || !(getContext() instanceof Launcher launcher)) {
                return false;
            }
            if (PopupContainer.getOpen(launcher) != null) {
                return false;
            }
            // A folder is not an ItemInfo, so synthesize a minimal WorkspaceItemInfo to anchor
            // the popup (the plain PopupContainer only uses it for identity/positioning).
            WorkspaceItemInfo folderInfo = new WorkspaceItemInfo();
            folderInfo.itemType = LauncherSettings.Favorites.ITEM_TYPE_FOLDER;
            folderInfo.container = LauncherSettings.Favorites.CONTAINER_SCROLLABLE_DESKTOP;
            folderInfo.id = (int) mItem.id;
            folderInfo.title = mItem.title;
            folderInfo.user = Process.myUserHandle();

            PopupContainer<Launcher> container =
                    PopupContainer.create(launcher, mRowContent, folderInfo);
            container.setSystemShortcutContainer(
                    container.inflateAndAdd(R.layout.system_shortcut_rows_container, container));
            addWidgetPopupRow(container, R.drawable.ic_folder,
                    R.string.scrollable_folder_rename, () -> renameFolder(mItem));
            addWidgetPopupRow(container, R.drawable.ic_remove_no_shadow,
                    R.string.scrollable_folder_remove, () -> removeDesktopFolder(mItem));
            addWidgetPopupRow(container, R.drawable.ic_more_vert_dots,
                    R.string.scrollable_desktop_reorder, ScrollableAppsView.this::enterReorderMode);
            container.show();
            return true;
        }
    }

    private class GroupViewHolder extends RecyclerView.ViewHolder {

        private final View mRowContent;
        private final TextView mTitle;
        private final LinearLayout mIcons;
        private ScrollableDesktopStore.DesktopItem mItem;
        private final List<BubbleTextView> mCells = new ArrayList<>();

        GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            mRowContent = itemView.findViewById(R.id.scroll_group_row_content);
            mTitle = itemView.findViewById(R.id.scroll_group_title);
            mIcons = itemView.findViewById(R.id.scroll_group_row_icons);
            // trebufork: long-pressing the row (its empty area, or a renamed group's title)
            // opens the group menu / starts the row drag; long-pressing an icon itself handles
            // the member menu / in-group drag, so the icon's press never reaches this.
            mRowContent.setOnLongClickListener(v -> {
                if (mReorderMode) {
                    if (mItemTouchHelper != null && !mDragInProgress) {
                        mItemTouchHelper.startDrag(GroupViewHolder.this);
                    }
                } else {
                    return showGroupContextMenu();
                }
                return true;
            });
        }

        void bind(ScrollableDesktopStore.DesktopItem item) {
            mItem = item;
            // trebufork: renamed groups show a small title above the icons; unnamed groups stay
            // compact (the label is hidden).
            String title = item.title;
            if (title == null || title.isEmpty()) {
                mTitle.setText("");
                mTitle.setVisibility(View.GONE);
            } else {
                mTitle.setText(title);
                mTitle.setVisibility(View.VISIBLE);
            }
            for (BubbleTextView cell : mCells) {
                mIcons.removeView(cell);
            }
            mCells.clear();
            float density = getResources().getDisplayMetrics().density;
            int gap = Math.round(8f * density);
            for (ScrollableDesktopStore.DesktopItem member : item.members) {
                AppInfo info = findApp(member);
                BubbleTextView cell = (BubbleTextView) LayoutInflater.from(getContext())
                        .inflate(R.layout.scrollable_group_icon, mIcons, false);
                if (info != null) {
                    cell.applyFromApplicationInfo(info);
                    cell.setText("");
                    cell.setContentDescription(null);
                    cell.setOnClickListener(v -> launchGroupApp(cell, info));
                    cell.setOnLongClickListener(v -> {
                        // trebufork: in reorder mode a long-press on an icon drags it to a new
                        // slot inside the group; otherwise it opens the member menu.
                        if (mReorderMode && mDesktopStore != null) {
                            int index = mCells.indexOf(cell);
                            if (index >= 0) {
                                beginGroupMemberDrag(GroupViewHolder.this, index, mItem, cell);
                            }
                            return true;
                        }
                        return showGroupMemberMenu(member);
                    });
                } else {
                    cell.setVisibility(View.GONE);
                }
                mIcons.addView(cell);
                mCells.add(cell);
            }
            // Small gap between icons (none before the first icon).
            for (int i = 1; i < mCells.size(); i++) {
                ViewGroup.MarginLayoutParams mlp =
                        (ViewGroup.MarginLayoutParams) mCells.get(i).getLayoutParams();
                mlp.setMarginStart(gap);
                mCells.get(i).setLayoutParams(mlp);
            }
            // trebufork: the icons are pinned to the left, centered, or pinned to the right of
            // the row per the group's alignment setting (ALIGN_START / ALIGN_CENTER / ALIGN_END).
            int alignGravity;
            if (item.align == ScrollableDesktopStore.ALIGN_CENTER) {
                alignGravity = Gravity.CENTER;
            } else if (item.align == ScrollableDesktopStore.ALIGN_END) {
                alignGravity = Gravity.END;
            } else {
                alignGravity = Gravity.START;
            }
            mIcons.setGravity(alignGravity | Gravity.CENTER_VERTICAL);
            ViewGroup.MarginLayoutParams lp =
                    (ViewGroup.MarginLayoutParams) mRowContent.getLayoutParams();
            if (lp != null && lp.getMarginEnd() != mRowEndMarginPx) {
                lp.setMarginEnd(mRowEndMarginPx);
                mRowContent.setLayoutParams(lp);
            }
        }

        /**
         * trebufork: moves the icon at slot {@code from} to slot {@code to} within the row and
         * keeps {@link #mCells} in sync with the new visual order. Used by the in-group member
         * drag (see {@link #beginGroupMemberDrag}).
         */
        void moveCellInRow(int from, int to) {
            if (from < 0 || from >= mIcons.getChildCount() || to < 0
                    || to >= mIcons.getChildCount() || from == to) {
                return;
            }
            View cell = mIcons.getChildAt(from);
            mIcons.removeViewAt(from);
            mIcons.addView(cell, to);
            mCells.remove(from);
            mCells.add(to, (BubbleTextView) cell);
            // Re-apply the slot margins: slot 0 has none, every other slot gets the gap.
            int gap = Math.round(8f * getResources().getDisplayMetrics().density);
            for (int i = 0; i < mIcons.getChildCount(); i++) {
                ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams)
                        mIcons.getChildAt(i).getLayoutParams();
                mlp.setMarginStart(i == 0 ? 0 : gap);
                mIcons.getChildAt(i).setLayoutParams(mlp);
            }
        }

        private void launchGroupApp(View view, AppInfo info) {
            if (info != null && info.getIntent() != null
                    && getContext() instanceof Launcher launcher) {
                // Launch from the icon itself so the clip-reveal / app-open animation scales out
                // of the 44dp icon, matching the desktop app rows.
                launcher.startActivitySafely(view, info.getIntent(), info);
            }
        }

        /** trebufork: group row context menu (rename, alignment, remove, reorder). */
        private boolean showGroupContextMenu() {
            if (mItem == null || !(getContext() instanceof Launcher launcher)) {
                return false;
            }
            if (PopupContainer.getOpen(launcher) != null) {
                return false;
            }
            WorkspaceItemInfo groupInfo = new WorkspaceItemInfo();
            groupInfo.itemType = LauncherSettings.Favorites.ITEM_TYPE_FOLDER;
            groupInfo.container = LauncherSettings.Favorites.CONTAINER_SCROLLABLE_DESKTOP;
            groupInfo.id = (int) mItem.id;
            groupInfo.user = Process.myUserHandle();

            PopupContainer<Launcher> container =
                    PopupContainer.create(launcher, mRowContent, groupInfo);
            container.setSystemShortcutContainer(
                    container.inflateAndAdd(R.layout.system_shortcut_rows_container, container));
            // trebufork: rename, alignment and member reorder all live in one Configure popup.
            addWidgetPopupRow(container, R.drawable.ic_edit_24,
                    R.string.scrollable_group_configure, () -> showConfigureGroupDialog(mItem));
            addWidgetPopupRow(container, R.drawable.ic_remove_no_shadow,
                    R.string.scrollable_group_remove, () -> removeGroup(mItem));
            addWidgetPopupRow(container, R.drawable.ic_more_vert_dots,
                    R.string.scrollable_desktop_reorder, ScrollableAppsView.this::enterReorderMode);
            container.show();
            return true;
        }

        /** trebufork: member context menu (remove the app back onto the desktop). */
        private boolean showGroupMemberMenu(ScrollableDesktopStore.DesktopItem member) {
            if (mItem == null || member == null
                    || !(getContext() instanceof Launcher launcher)) {
                return false;
            }
            if (PopupContainer.getOpen(launcher) != null) {
                return false;
            }
            AppInfo info = findApp(member);
            if (info == null) {
                return false;
            }
            PopupContainer<Launcher> container = PopupContainer.create(launcher, mIcons, info);
            container.setSystemShortcutContainer(
                    container.inflateAndAdd(R.layout.system_shortcut_rows_container, container));
            DeepShortcutView removeView = container.inflateAndAdd(
                    R.layout.system_shortcut, container.getSystemShortcutContainer());
            removeView.getIconView().setBackgroundResource(R.drawable.ic_remove_no_shadow);
            removeView.getBubbleText().setText(R.string.scrollable_group_remove_app);
            removeView.setOnClickListener(v -> {
                AbstractFloatingView.closeAllOpenViews(launcher);
                removeFromGroup(mItem.id, member.id);
            });
            container.show();
            return true;
        }
    }

    /**
     * trebufork: adapter for the member reorder strip inside the group Configure dialog — a
     * single horizontal row of the group's icons (see {@link #showConfigureGroupDialog}).
     * Long-pressing an icon lifts it and dragging left/right moves it to a new slot; the move is
     * persisted to the store immediately, like the in-row drag on the desktop.
     */
    private class GroupConfigureAdapter
            extends RecyclerView.Adapter<GroupConfigureAdapter.Holder> {

        private final List<ScrollableDesktopStore.DesktopItem> mMembers;
        private ItemTouchHelper mTouchHelper;

        GroupConfigureAdapter(List<ScrollableDesktopStore.DesktopItem> members) {
            mMembers = members;
        }

        void setTouchHelper(ItemTouchHelper touchHelper) {
            mTouchHelper = touchHelper;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(LayoutInflater.from(getContext())
                    .inflate(R.layout.scrollable_group_icon, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            holder.bind(mMembers.get(position), position);
        }

        @Override
        public int getItemCount() {
            return mMembers.size();
        }

        class Holder extends RecyclerView.ViewHolder {

            private final BubbleTextView mIcon;

            Holder(@NonNull View itemView) {
                super(itemView);
                mIcon = (BubbleTextView) itemView;
                mIcon.setOnLongClickListener(v -> {
                    if (mTouchHelper != null) {
                        mTouchHelper.startDrag(this);
                        return true;
                    }
                    return false;
                });
            }

            void bind(ScrollableDesktopStore.DesktopItem member, int position) {
                AppInfo info = findApp(member);
                if (info != null) {
                    mIcon.applyFromApplicationInfo(info);
                    mIcon.setText("");
                    mIcon.setContentDescription(null);
                }
                // Even gap between the icons (none before the first).
                int gap = Math.round(8f * getResources().getDisplayMetrics().density);
                ViewGroup.MarginLayoutParams mlp =
                        (ViewGroup.MarginLayoutParams) mIcon.getLayoutParams();
                mlp.setMarginStart(position == 0 ? 0 : gap);
                mIcon.setLayoutParams(mlp);
            }
        }
    }

    private class DesktopWidgetViewHolder extends RecyclerView.ViewHolder {

        private final ScrollableWidgetRow mRow;
        private final ImageView mDragHandle;
        private ScrollableDesktopStore.DesktopItem mItem;

        DesktopWidgetViewHolder(@NonNull View itemView) {
            super(itemView);
            mRow = (ScrollableWidgetRow) itemView;
            mDragHandle = itemView.findViewById(R.id.scroll_widget_drag_handle);
            mDragHandle.setColorFilter(
                    Themes.getAttrColor(getContext(), R.attr.workspaceTextColor));
            // trebufork: in reorder mode the drag handle lifts the row; touching it elsewhere
            // (or outside reorder mode) is ignored. Must return false so the RecyclerView keeps
            // receiving the gesture and hands it to the ItemTouchHelper once the drag starts.
            mDragHandle.setOnTouchListener((v, ev) -> {
                if (ev.getActionMasked() == MotionEvent.ACTION_DOWN
                        && mReorderMode && !mDragInProgress) {
                    mItemTouchHelper.startDrag(this);
                }
                return false;
            });
            // The row-level long-press listener below never fires for widget-area touches because
            // the widget host view consumes the whole gesture; the actual long-press handling
            // happens on the host view (see bind()). It is kept as a harmless fallback.
            itemView.setOnLongClickListener(v -> {
                if (mReorderMode) {
                    if (mItemTouchHelper != null && !mDragInProgress) {
                        mItemTouchHelper.startDrag(this);
                    }
                } else {
                    return showWidgetContextMenu();
                }
                return true;
            });
        }

        void bind(ScrollableDesktopStore.DesktopItem item) {
            mItem = item;
            View host = getWidgetView(item);
            // Detach whatever widget view this row currently holds. Host views are cached in
            // mWidgetViews and can outlive the row they were attached to: a full rebind (e.g. the
            // entrance animation's notifyDataSetChanged, or a mode switch) may re-parent the same
            // cached host into a different row while the previous holder sits in the scrap without
            // onViewRecycled() running, so the cached host can still be a child of that row.
            detachWidget();
            if (host != null) {
                // Re-parent safety: the cached host may still be attached to another row (see
                // above). Remove it from there before adding it here, otherwise addView throws
                // "The specified child already has a parent".
                if (host.getParent() instanceof ViewGroup parent && parent != mRow) {
                    parent.removeView(host);
                }
                if (host.getParent() == null) {
                    mRow.addView(host, 0);
                }
                host.setVisibility(View.VISIBLE);
                // trebufork: tag the host view like the paged workspace does, so the Quickstep
                // widget open/close animation gets a proper ItemInfo (launch cookie + return
                // target) instead of treating the widget as an anonymous view.
                host.setTag(createWidgetInfo(item));
                // The widget host view consumes every touch and runs its own long-press
                // detection (CheckLongPressHelper -> LauncherAppWidgetHostView.onLongClick ->
                // performLongClick on itself), so the menu must be wired directly onto the host
                // view — exactly like the paged workspace does for its widget views.
                host.setOnLongClickListener(v -> {
                    if (mReorderMode) {
                        if (mItemTouchHelper != null && !mDragInProgress) {
                            mItemTouchHelper.startDrag(DesktopWidgetViewHolder.this);
                        }
                    } else if (showWidgetContextMenu()) {
                        // On the paged workspace the drag started by the long-press consumes the
                        // whole gesture, so the widget never sees the ACTION_UP and its clickable
                        // content does not fire a click. There is no drag here, so cancel the
                        // widget's current touch — the pressed button then ignores the release.
                        cancelWidgetTouch();
                    }
                    return true;
                });
            }
            mRow.setAspectRatio(getWidgetAspectRatio(item));
            // trebufork: per-widget size (width/height scale) and horizontal position persisted
            // by the desktop store; adjusted with the resize frame (see
            // ScrollableWidgetResizeFrame).
            mRow.setScales(item.widthScale, item.heightScale);
            mRow.setPositionX(item.positionX);
            // trebufork: the 3-dot reorder handle is intentionally never shown; reordering is
            // started by long-pressing the widget in reorder mode.
            mDragHandle.setVisibility(View.GONE);
        }

        void detachWidget() {
            View host = mRow.getWidgetView();
            if (host != null) {
                mRow.removeView(host);
            }
        }

        /**
         * trebufork: sends a synthetic ACTION_CANCEL into the widget host view, clearing the
         * pressed state of any clickable content the user is currently holding. The menu opened by
         * the long-press must consume the whole gesture; without this, releasing the finger over a
         * button inside the widget would trigger its click (the paged workspace avoids this by
         * starting a drag, which intercepts the remaining events).
         */
        private void cancelWidgetTouch() {
            View host = mRow.getChildCount() > 0 && mRow.getChildAt(0) != mDragHandle
                    ? mRow.getChildAt(0) : null;
            if (host == null) {
                return;
            }
            long now = SystemClock.uptimeMillis();
            MotionEvent cancel = MotionEvent.obtain(
                    now, now, MotionEvent.ACTION_CANCEL, 0f, 0f, /* metaState */ 0);
            host.dispatchTouchEvent(cancel);
            cancel.recycle();
        }

        /**
         * trebufork: shows the widget context menu (the base popup used on the paged workspace
         * for widgets) with "Remove from desktop", "Widget settings" (when the provider supports
         * it) and "Reorder".
         */
        private boolean showWidgetContextMenu() {
            if (mItem == null || !(getContext() instanceof Launcher launcher)) {
                return false;
            }
            if (PopupContainer.getOpen(launcher) != null) {
                // There is already an items container open, so don't open this one.
                return false;
            }
            LauncherAppWidgetInfo widgetInfo = createWidgetInfo(mItem);
            // Anchor the popup to the widget itself (the host view), falling back to the row.
            View anchor = mRow;
            if (mRow.getChildCount() > 0 && mRow.getChildAt(0) != mDragHandle) {
                anchor = mRow.getChildAt(0);
            }
            PopupContainer<Launcher> container =
                    PopupContainer.create(launcher, anchor, widgetInfo);
            container.setSystemShortcutContainer(
                    container.inflateAndAdd(R.layout.system_shortcut_rows_container, container));
            // Remove from desktop.
            addWidgetPopupRow(container, R.drawable.ic_remove_no_shadow,
                    R.string.scrollable_desktop_remove, () -> removeDesktopWidget(mItem));
            // Widget settings, when the provider supports reconfiguration (as on the workspace).
            LauncherAppWidgetProviderInfo providerInfo = getWidgetProvider(mItem);
            if (providerInfo != null && providerInfo.isReconfigurable()) {
                addWidgetPopupRow(container, R.drawable.ic_setting, R.string.widget_settings,
                        () -> launcher.getAppWidgetHolder().startConfigActivity(
                                launcher, mItem.appWidgetId, REQUEST_RECONFIGURE_APPWIDGET));
            }
            // Reorder — enters reorder mode with drag handles on every desktop row.
            addWidgetPopupRow(container, R.drawable.ic_more_vert_dots,
                    R.string.scrollable_desktop_reorder,
                    ScrollableAppsView.this::enterReorderMode);
            container.show();
            // trebufork: show the workspace-style resize frame around the widget together with
            // the menu, so dragging a handle resizes it (like the paged workspace). The frame
            // closes when the menu closes.
            ScrollableWidgetResizeFrame.show(ScrollableAppsView.this, mRow, mItem);
            container.addOnCloseCallback(() -> {
                // The frame closes itself when its drag commits; while a resize/move drag is
                // in progress the menu hides but the grid must stay on screen.
                AbstractFloatingView frame = AbstractFloatingView.getOpenView(
                        launcher, AbstractFloatingView.TYPE_WIDGET_RESIZE_FRAME);
                if (frame == null || !(frame instanceof ScrollableWidgetResizeFrame)
                        || !((ScrollableWidgetResizeFrame) frame).isDragActive()) {
                    ScrollableWidgetResizeFrame.closeOpenFrame(launcher);
                }
            });
            return true;
        }
    }

    /**
     * trebufork: adds one icon + label row to a widget popup container and runs {@code action}
     * (after closing the popup) when the row is tapped.
     */
    private void addWidgetPopupRow(PopupContainer<Launcher> container, int iconRes, int labelRes,
            Runnable action) {
        DeepShortcutView view = container.inflateAndAdd(
                R.layout.system_shortcut, container.getSystemShortcutContainer());
        view.getIconView().setBackgroundResource(iconRes);
        view.getBubbleText().setText(labelRes);
        view.setOnClickListener(v -> {
            AbstractFloatingView.closeAllOpenViews((Launcher) getContext());
            action.run();
        });
    }

    private class DesktopHeaderViewHolder extends RecyclerView.ViewHolder {

        // trebufork: the header row is kept only as a long-press target that opens the launcher
        // options (add widgets etc.); the Done button is a floating overlay in launcher.xml.
        private final float[] mPress = {-1f, -1f};

        /** trebufork: re-sizes the header to the configured top inset. */
        void applyTopInset() {
            ViewGroup.LayoutParams lp = itemView.getLayoutParams();
            if (lp != null) {
                lp.height = getTopInsetPx();
                itemView.setLayoutParams(lp);
            }
        }

        DesktopHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            // trebufork: the top inset of the desktop (pref_scrollable_top_inset percent of the
            // screen), matching the apps list so the two views line up.
            applyTopInset();
            Launcher launcher = itemView.getContext() instanceof Launcher
                    ? (Launcher) itemView.getContext() : null;
            if (launcher == null) {
                return;
            }
            itemView.setOnTouchListener((v, ev) -> {
                if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                    int[] loc = new int[2];
                    v.getLocationOnScreen(loc);
                    int[] dlLoc = new int[2];
                    launcher.getDragLayer().getLocationOnScreen(dlLoc);
                    mPress[0] = loc[0] + ev.getX() - dlLoc[0];
                    mPress[1] = loc[1] + ev.getY() - dlLoc[1];
                }
                return false;
            });
            itemView.setOnLongClickListener(v -> {
                launcher.showScrollableOptions(mPress[0], mPress[1]);
                return true;
            });
        }
    }

    private static class ListRow {

        final int type;
        @Nullable
        final AppInfo app;
        final char section;
        @Nullable
        final ScrollableDesktopStore.DesktopItem desktopItem;

        ListRow(int type, @Nullable AppInfo app, char section,
                @Nullable ScrollableDesktopStore.DesktopItem desktopItem) {
            this.type = type;
            this.app = app;
            this.section = section;
            this.desktopItem = desktopItem;
        }
    }

    private static class FooterViewHolder extends RecyclerView.ViewHolder {

        FooterViewHolder(@NonNull View itemView) {
            super(itemView);
            applyBottomInset();
        }

        /** trebufork: sizes the footer to the 1/3-screen bottom inset. */
        void applyBottomInset() {
            ViewGroup.LayoutParams lp = itemView.getLayoutParams();
            if (lp != null) {
                lp.height = Math.round(
                        itemView.getResources().getDisplayMetrics().heightPixels
                                * BOTTOM_INSET_FRACTION);
                itemView.setLayoutParams(lp);
            }
        }
    }

    private static class SectionViewHolder extends RecyclerView.ViewHolder {

        private final TextView mLabel;

        SectionViewHolder(@NonNull View itemView) {
            super(itemView);
            mLabel = (TextView) itemView;
        }

        void bind(char section, boolean show) {
            mLabel.setText(String.valueOf(section));
            itemView.animate().cancel();
            itemView.setAlpha(show ? 1f : 0f);
        }
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final float[] mPress = {-1f, -1f};

        /** trebufork: re-sizes the header to the configured top inset (status-bar only when
         * searching). */
        void applyTopInset() {
            ViewGroup.LayoutParams lp = itemView.getLayoutParams();
            if (lp != null) {
                int height;
                if (mSearchQuery.isEmpty()) {
                    height = Math.round(itemView.getResources().getDisplayMetrics().heightPixels
                            * getTopInsetFraction(itemView.getContext()));
                } else {
                    // trebufork: while searching, keep a small inset so the first result clears
                    // the status bar instead of sitting right under it. Use the drag layer's
                    // normalized insets (the same value the reorder/resize overlays use), because
                    // the raw window insets aren't always dispatched to this row yet at bind time.
                    int statusBarTop = 0;
                    if (itemView.getContext() instanceof Launcher launcher) {
                        statusBarTop = launcher.getDragLayer().getInsets().top;
                    }
                    if (statusBarTop <= 0) {
                        WindowInsetsCompat rootInsets = ViewCompat.getRootWindowInsets(itemView);
                        statusBarTop = rootInsets != null
                                ? rootInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top : 0;
                    }
                    height = statusBarTop * 2;
                }
                lp.height = height;
                itemView.setLayoutParams(lp);
            }
        }

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            // trebufork: the top inset of the apps list (pref_scrollable_top_inset percent of
            // the screen), so the first app row starts in thumb range.
            applyTopInset();
            Launcher launcher = itemView.getContext() instanceof Launcher
                    ? (Launcher) itemView.getContext() : null;
            if (launcher == null) {
                return;
            }
            itemView.setOnTouchListener((v, ev) -> {
                if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                    int[] loc = new int[2];
                    v.getLocationOnScreen(loc);
                    int[] dlLoc = new int[2];
                    launcher.getDragLayer().getLocationOnScreen(dlLoc);
                    mPress[0] = loc[0] + ev.getX() - dlLoc[0];
                    mPress[1] = loc[1] + ev.getY() - dlLoc[1];
                }
                return false;
            });
            itemView.setOnLongClickListener(v -> {
                launcher.showScrollableOptions(mPress[0], mPress[1]);
                return true;
            });
        }
    }

    /**
     * trebufork: drag-and-drop reordering of desktop rows (apps and widgets). Long-pressing a
     * desktop row lifts it; moving up/down reorders the desktop list and persists the order.
     */
    private class DesktopReorderCallback extends ItemTouchHelper.Callback {

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView,
                @NonNull RecyclerView.ViewHolder viewHolder) {
            return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
        }

        @Override
        public boolean isLongPressDragEnabled() {
            // Drags are started manually on long-press of desktop rows.
            return false;
        }

        @Override
        public boolean canDropOver(@NonNull RecyclerView recyclerView,
                @NonNull RecyclerView.ViewHolder current, @NonNull RecyclerView.ViewHolder target) {
            // Never drop on the desktop header row (position 0) or the bottom-inset footer
            // (last position).
            int pos = target.getBindingAdapterPosition();
            return pos > 0 && pos < mDesktopRows.size() - 1;
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,
                @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int from = viewHolder.getBindingAdapterPosition();
            int to = target.getBindingAdapterPosition();
            if (from <= 0 || to <= 0 || mMode != MODE_DESKTOP) {
                return false;
            }
            // Exclude the footer (last position) from valid move destinations.
            if (to >= mDesktopRows.size() - 1) {
                to = mDesktopRows.size() - 2;
            }
            if (from == to) {
                return true;
            }
            ListRow row = mDesktopRows.remove(from);
            mDesktopRows.add(to, row);
            mSuppressStoreRefresh = true;
            if (mDesktopStore != null) {
                mDesktopStore.move(from - 1, to - 1);
            }
            mSuppressStoreRefresh = false;
            mAdapter.notifyItemMoved(from, to);
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) { }

        @Override
        public void clearView(@NonNull RecyclerView recyclerView,
                @NonNull RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            mDragInProgress = false;
            viewHolder.itemView.setElevation(0f);
            viewHolder.itemView.animate().cancel();
            viewHolder.itemView.setAlpha(1f);
        }

        @Override
        public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
            super.onSelectedChanged(viewHolder, actionState);
            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && viewHolder != null) {
                mDragInProgress = true;
                viewHolder.itemView.setElevation(8f * getResources().getDisplayMetrics().density);
                viewHolder.itemView.setAlpha(0.85f);
            }
        }
    }
}
