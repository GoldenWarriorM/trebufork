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

import static com.android.launcher3.ScrollableDesktopStore.TYPE_APP;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.launcher3.dragndrop.DragLayer;
import com.android.launcher3.model.data.AppInfo;
import com.android.launcher3.model.data.ItemInfo;
import com.android.launcher3.model.data.LauncherAppWidgetInfo;
import com.android.launcher3.popup.PopupContainer;
import com.android.launcher3.popup.SystemShortcut;
import com.android.launcher3.shortcuts.DeepShortcutView;
import com.android.launcher3.util.Themes;
import com.android.launcher3.views.ActivityContext;
import com.android.launcher3.views.BaseDragLayer;
import com.android.launcher3.widget.LauncherAppWidgetProviderInfo;

import java.util.List;

/**
 * trebufork: overlay folder for the scrollable-home desktop. Shows a rounded card with the
 * folder's members laid out on a grid; tapping an app member launches it, long-pressing a
 * member moves it back out of the folder onto the desktop list, the title can be renamed, and
 * members can be drag-reordered within the grid.
 *
 * <p>The view fills the drag layer (acting as a dim scrim); taps outside the card dismiss it,
 * exactly like the stock folder. Members are plain {@link ScrollableDesktopStore.DesktopItem}s
 * owned by the desktop store — the folder never touches the paged-workspace model.
 */
public class ScrollableFolderView extends AbstractFloatingView {

    private static final int GRID_COLUMNS = 3;
    private static final float CARD_WIDTH_FRACTION = 0.80f;
    // Fallback vertical position (12% from the top) used only when no anchor row is given.
    private static final float CARD_TOP_FRACTION = 0.12f;
    private static final int SCRIM_ALPHA = 0x66;
    private static final long OPEN_ANIMATION_MS = 150L;
    private static final long CLOSE_ANIMATION_MS = 120L;

    private Launcher mLauncher;
    private ScrollableAppsView mAppsView;
    private ScrollableDesktopStore mStore;
    private ScrollableDesktopStore.DesktopItem mFolder;

    private LinearLayout mCard;
    private TextView mTitle;
    private TextView mEmpty;
    private RecyclerView mGrid;
    private TextView mDoneButton;
    private ImageView mPencilButton;
    private ImageView mToggleButton;
    private EditText mRenameInput;
    // trebufork: while true, long-pressing a member drags it to reorder (otherwise long-press
    // opens the member menu — see MemberViewHolder.onLongClick).
    private boolean mReorderMode;
    // trebufork: true renders members as a scrolling single-column list (like the desktop),
    // false as the 3-column icon grid.
    private boolean mListMode;
    // trebufork: true while inline rename is active (pencil pressed); the card is lifted above
    // the keyboard when the input field would otherwise be covered (see onApplyWindowInsets).
    private boolean mRenaming;
    // trebufork: the card's top padding when the folder opened; restored after an IME lift.
    private int mBaseTop = -1;
    private final MemberAdapter mAdapter = new MemberAdapter();
    private final ItemTouchHelper mItemTouchHelper;

    public ScrollableFolderView(Context context) {
        this(context, null);
    }

    public ScrollableFolderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollableFolderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mItemTouchHelper = new ItemTouchHelper(new MemberReorderCallback());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mCard = findViewById(R.id.scroll_folder_card);
        mTitle = findViewById(R.id.scroll_folder_title);
        mEmpty = findViewById(R.id.scroll_folder_empty);
        mGrid = findViewById(R.id.scroll_folder_grid);
        mDoneButton = findViewById(R.id.scroll_folder_done);
        mPencilButton = findViewById(R.id.scroll_folder_pencil);
        mToggleButton = findViewById(R.id.scroll_folder_toggle);
        mRenameInput = findViewById(R.id.scroll_folder_rename_input);
        findViewById(R.id.scroll_folder_close).setOnClickListener(v -> close(true));
        mPencilButton.setOnClickListener(v -> startRename());
        mToggleButton.setOnClickListener(v -> toggleLayout());
        mDoneButton.setOnClickListener(v -> exitReorderMode());
        // trebufork: match the desktop reorder Done button — dark text on a light accent, white
        // on a dark accent (the default gray text washes out on the accent pill).
        int accent = Themes.getAttrColor(getContext(), android.R.attr.colorAccent);
        mDoneButton.setTextColor(ColorUtils.calculateLuminance(accent) > 0.5f
                ? Color.BLACK : Color.WHITE);
        // trebufork: the pencil, layout-toggle and close buttons share one color (the title's)
        // so the header looks consistent instead of three different shades.
        int headerColor = Themes.getAttrColor(getContext(), R.attr.workspaceTextColor);
        mPencilButton.setColorFilter(headerColor);
        mToggleButton.setColorFilter(headerColor);
        ((ImageView) findViewById(R.id.scroll_folder_close)).setColorFilter(headerColor);
        mRenameInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                commitRename();
                return true;
            }
            return false;
        });
        mGrid.setAdapter(mAdapter);
        mItemTouchHelper.attachToRecyclerView(mGrid);
    }

    /**
     * Opens the folder overlay for {@code folder}, replacing any already-open folder. The card
     * is vertically centered on the tapped folder row (clamped so it stays fully on screen).
     */
    public static ScrollableFolderView show(ScrollableAppsView appsView,
            ScrollableDesktopStore store, ScrollableDesktopStore.DesktopItem folder, View anchor) {
        Launcher launcher = Launcher.getLauncher(appsView.getContext());
        closeOpenContainer(launcher, TYPE_FOLDER);
        DragLayer dragLayer = launcher.getDragLayer();
        ScrollableFolderView view = (ScrollableFolderView) LayoutInflater.from(launcher)
                .inflate(R.layout.scrollable_folder_view, dragLayer, false);
        view.mLauncher = launcher;
        view.mAppsView = appsView;
        view.mStore = store;
        view.mFolder = folder;
        // trebufork: grid vs scrolling-list layout for the folder members (user pref).
        view.mListMode = LauncherPrefs.SCROLLABLE_FOLDER_LIST.get(launcher);
        view.mGrid.setLayoutManager(view.mListMode
                ? new LinearLayoutManager(launcher)
                : new GridLayoutManager(launcher, GRID_COLUMNS));
        view.updateToggleIcon();
        dragLayer.addView(view);
        BaseDragLayer.LayoutParams lp = (BaseDragLayer.LayoutParams) view.getLayoutParams();
        lp.customPosition = true;
        lp.width = dragLayer.getWidth();
        lp.height = dragLayer.getHeight();
        lp.x = 0;
        lp.y = 0;
        view.setLayoutParams(lp);
        view.mIsOpen = true;
        view.refresh();
        // The card is 80% of the screen width, centered horizontally; vertically it opens over
        // the tapped folder row. Measure it first so the top edge can be clamped without a
        // visible reflow when the folder row is near the screen edges.
        float density = view.getResources().getDisplayMetrics().density;
        int padX = Math.round(dragLayer.getWidth() * (1f - CARD_WIDTH_FRACTION) / 2f);
        int widthSpec = MeasureSpec.makeMeasureSpec(
                dragLayer.getWidth() - 2 * padX, MeasureSpec.EXACTLY);
        view.applyListHeightCap(widthSpec - Math.round(32f * density), dragLayer.getHeight() / 2);
        view.measure(widthSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        int cardHeight = view.getMeasuredHeight();
        int rowCenter;
        if (anchor != null) {
            int[] rowLoc = new int[2];
            anchor.getLocationInWindow(rowLoc);
            rowCenter = rowLoc[1] + anchor.getHeight() / 2;
        } else {
            rowCenter = Math.round(dragLayer.getHeight() * CARD_TOP_FRACTION)
                    + cardHeight / 2;
        }
        WindowInsets insets = dragLayer.getRootWindowInsets();
        int statusBar = insets == null ? 0 : insets.getInsets(WindowInsets.Type.statusBars()).top;
        int minTop = statusBar + Math.round(12f * density);
        int maxTop = dragLayer.getHeight() - cardHeight - Math.round(12f * density);
        int top = Math.max(minTop, Math.min(rowCenter - cardHeight / 2, maxTop));
        view.setPadding(padX, top, padX, 0);
        view.mBaseTop = top;
        view.setBackgroundColor(Color.argb(SCRIM_ALPHA, 0, 0, 0));
        view.animate().cancel();
        view.setAlpha(0f);
        view.animate().alpha(1f).setDuration(OPEN_ANIMATION_MS).start();
        return view;
    }

    @Override
    protected void handleClose(boolean animate) {
        // trebufork: closing the folder while inline rename is active must dismiss the IME,
        // otherwise the keyboard lingers over the home screen after the overlay is gone.
        if (mRenaming && mRenameInput != null) {
            InputMethodManager imm = (InputMethodManager) getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mRenameInput.getWindowToken(), 0);
            mRenameInput.clearFocus();
            mRenaming = false;
        }
        ViewGroup parent = getParent() instanceof ViewGroup ? (ViewGroup) getParent() : null;
        if (parent == null) {
            return;
        }
        if (animate && ValueAnimator.areAnimatorsEnabled()) {
            animate().cancel();
            animate().alpha(0f).setDuration(CLOSE_ANIMATION_MS).withEndAction(() -> {
                if (getParent() instanceof ViewGroup p) {
                    p.removeView(this);
                }
            }).start();
        } else {
            parent.removeView(this);
        }
    }

    @Override
    protected boolean isOfType(@FloatingViewType int type) {
        return (type & TYPE_FOLDER) != 0;
    }

    /** Dims/consumes the scrim; a tap outside the card dismisses the folder. */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN && !isEventOverCard(ev)) {
            close(true);
        }
        return true;
    }

    @Override
    public boolean onControllerInterceptTouchEvent(MotionEvent ev) {
        // The folder overlay does not participate in the drag controller; the scrim handles
        // all touches itself (see onTouchEvent).
        return false;
    }

    /**
     * trebufork: while inline rename is active, lifts the card above the keyboard if the input
     * field would otherwise be covered. Mirrors the stock folder behavior, but translates only
     * the card so the dim scrim stays in place.
     */
    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        if (mCard == null) {
            return insets;
        }
        applyImeShift(insets.isVisible(WindowInsets.Type.ime())
                ? insets.getInsets(WindowInsets.Type.ime()).bottom : 0);
        return insets;
    }

    /**
     * trebufork: while inline rename is active, lifts the card above the keyboard if the input
     * field would otherwise be covered. The card is repositioned by changing the view's top
     * padding (a real layout change, so the content always renders at the new position) rather
     * than a translation transform that the scrim/LinearLayout would clip.
     */
    private void applyImeShift(int keyboardBottom) {
        if (!mRenaming || keyboardBottom <= 0 || mRenameInput == null || mBaseTop < 0) {
            if (mBaseTop >= 0 && getPaddingTop() != mBaseTop) {
                setPadding(getPaddingLeft(), mBaseTop, getPaddingRight(), getPaddingBottom());
                requestLayout();
            }
            return;
        }
        View header = (View) mRenameInput.getParent();
        int inputBottom = mBaseTop + header.getBottom();
        int keyboardTop = getHeight() - keyboardBottom;
        int newTop = mBaseTop;
        if (inputBottom > keyboardTop) {
            int gap = Math.round(12f * getResources().getDisplayMetrics().density);
            newTop = Math.max(0, mBaseTop - (inputBottom - keyboardTop + gap));
        }
        if (getPaddingTop() != newTop) {
            setPadding(getPaddingLeft(), newTop, getPaddingRight(), getPaddingBottom());
            requestLayout();
        }
    }

    private boolean isEventOverCard(MotionEvent ev) {
        if (mCard == null) {
            return false;
        }
        float x = ev.getX();
        float y = ev.getY();
        return x >= mCard.getLeft() && x <= mCard.getRight()
                && y >= mCard.getTop() && y <= mCard.getBottom();
    }

    /** Re-reads the folder title and members from the store and refreshes the grid. */
    private void refresh() {
        if (mFolder == null) {
            return;
        }
        String title = mFolder.title;
        mTitle.setText(title == null || title.isEmpty()
                ? getContext().getString(R.string.scrollable_folder_default_title) : title);
        boolean empty = mFolder.members.isEmpty();
        mEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        mGrid.setVisibility(empty ? View.GONE : View.VISIBLE);
        mAdapter.notifyDataSetChanged();
    }

    /** trebufork: enters reorder mode — long-press now drags members, Done finishes. */
    private void enterReorderMode() {
        mReorderMode = true;
        if (mDoneButton != null) {
            mDoneButton.setVisibility(View.VISIBLE);
        }
        mAdapter.notifyDataSetChanged();
    }

    /** trebufork: leaves reorder mode and hides the Done button. */
    private void exitReorderMode() {
        mReorderMode = false;
        if (mDoneButton != null) {
            mDoneButton.setVisibility(View.GONE);
        }
        mAdapter.notifyDataSetChanged();
    }

    /** trebufork: switches the folder into inline rename mode (pencil button). */
    private void startRename() {
        if (mFolder == null) {
            return;
        }
        mRenaming = true;
        mTitle.setVisibility(View.GONE);
        mPencilButton.setVisibility(View.GONE);
        mRenameInput.setVisibility(View.VISIBLE);
        mRenameInput.setText(mFolder.title);
        mRenameInput.setSelection(mRenameInput.length());
        mRenameInput.requestFocus();
        InputMethodManager imm = (InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mRenameInput, InputMethodManager.SHOW_IMPLICIT);
    }

    /** trebufork: commits the inline rename and restores the title/pencil header. */
    private void commitRename() {
        String newTitle = mRenameInput.getText().toString().trim();
        if (!newTitle.isEmpty() && mStore != null && mFolder != null) {
            mStore.setFolderTitle(mFolder.id, newTitle);
        }
        mRenameInput.setVisibility(View.GONE);
        mTitle.setVisibility(View.VISIBLE);
        mPencilButton.setVisibility(View.VISIBLE);
        InputMethodManager imm = (InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mRenameInput.getWindowToken(), 0);
        mRenameInput.clearFocus();
        mRenaming = false;
        if (mBaseTop >= 0) {
            setPadding(getPaddingLeft(), mBaseTop, getPaddingRight(), getPaddingBottom());
        }
        refresh();
    }

    /** trebufork: flips the folder between the icon grid and the scrolling list (user pref). */
    private void toggleLayout() {
        mListMode = !mListMode;
        LauncherPrefs.get(getContext()).put(LauncherPrefs.SCROLLABLE_FOLDER_LIST, mListMode);
        updateToggleIcon();
        mGrid.setLayoutManager(mListMode
                ? new LinearLayoutManager(getContext())
                : new GridLayoutManager(getContext(), GRID_COLUMNS));
        mAdapter.notifyDataSetChanged();
        int gridWidth = mGrid.getWidth();
        if (gridWidth <= 0) {
            gridWidth = mCard.getWidth()
                    - Math.round(32f * getResources().getDisplayMetrics().density);
        }
        applyListHeightCap(gridWidth, getHeight() / 2);
    }

    /** Shows the icon matching the current layout mode (list or grid). */
    private void updateToggleIcon() {
        if (mToggleButton != null) {
            mToggleButton.setImageResource(
                    mListMode ? R.drawable.ic_folder_list : R.drawable.ic_folder_grid);
        }
    }

    /**
     * In list mode the grid is capped to {@code maxHeightPx} so a large folder scrolls inside
     * the card instead of overflowing the screen; in grid mode it returns to wrap_content.
     */
    private void applyListHeightCap(int gridWidthPx, int maxHeightPx) {
        ViewGroup.LayoutParams gridLp = mGrid.getLayoutParams();
        if (!mListMode) {
            gridLp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        } else {
            mGrid.measure(MeasureSpec.makeMeasureSpec(gridWidthPx, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            gridLp.height = Math.min(mGrid.getMeasuredHeight(), maxHeightPx);
        }
        mGrid.setLayoutParams(gridLp);
    }

    /** Moves a member back out of the folder onto the desktop list (drag-out equivalent). */
    private void removeMemberFromFolder(ScrollableDesktopStore.DesktopItem member) {
        if (mStore == null || member == null) {
            return;
        }
        ScrollableDesktopStore.DesktopItem removed =
                mStore.removeItemFromFolder(mFolder.id, member.id);
        if (removed != null) {
            mStore.insertItem(mStore.getItems().size(), removed);
        }
        refresh();
    }

    private ItemInfo itemInfoFor(ScrollableDesktopStore.DesktopItem member) {
        if (member.type == TYPE_APP) {
            return mAppsView.findApp(member);
        }
        return mAppsView.createWidgetInfo(member);
    }

    private void showMemberMenu(ScrollableDesktopStore.DesktopItem member, View anchor) {
        ItemInfo itemInfo = itemInfoFor(member);
        if (itemInfo == null) {
            return;
        }
        PopupContainer<Launcher> container = PopupContainer.create(mLauncher, anchor, itemInfo);
        container.setSystemShortcutContainer(
                container.inflateAndAdd(R.layout.system_shortcut_rows_container, container));
        // App info (apps only) — stock implementation, shown at the very top.
        if (member.type == TYPE_APP) {
            SystemShortcut<ActivityContext> appInfo =
                    SystemShortcut.APP_INFO.getShortcut(mLauncher, itemInfo, anchor);
            if (appInfo != null) {
                DeepShortcutView infoView = container.inflateAndAdd(
                        R.layout.system_shortcut, container.getSystemShortcutContainer());
                appInfo.setIconAndLabelFor(infoView.getIconView(), infoView.getBubbleText());
                infoView.setOnClickListener(appInfo);
            }
        }
        DeepShortcutView removeView = container.inflateAndAdd(
                R.layout.system_shortcut, container.getSystemShortcutContainer());
        removeView.getIconView().setBackgroundResource(R.drawable.ic_remove_no_shadow);
        removeView.getBubbleText().setText(R.string.scrollable_desktop_remove);
        removeView.setOnClickListener(v -> {
            closePopupOnly();
            removeMemberFromFolder(member);
        });
        DeepShortcutView reorderView = container.inflateAndAdd(
                R.layout.system_shortcut, container.getSystemShortcutContainer());
        reorderView.getIconView().setBackgroundResource(R.drawable.ic_more_vert_dots);
        reorderView.getBubbleText().setText(R.string.scrollable_desktop_reorder);
        reorderView.setOnClickListener(v -> {
            closePopupOnly();
            enterReorderMode();
        });
        container.show();
    }

    /** Closes only the open action popup, leaving the folder overlay itself on screen. */
    private void closePopupOnly() {
        closeOpenContainer(mLauncher, TYPE_ACTION_POPUP);
    }

    // ---------------------------------------------------------------------
    // Member grid
    // ---------------------------------------------------------------------

    private class MemberAdapter extends RecyclerView.Adapter<MemberViewHolder> {

        private static final int VIEW_TYPE_LIST = 0;
        private static final int VIEW_TYPE_GRID = 1;

        @Override
        public int getItemViewType(int position) {
            // Distinct view types force RecyclerView to rebuild holders when the folder is
            // toggled between grid and list (otherwise recycled grid holders are rebound into
            // list rows and the folder layout looks broken).
            return mListMode ? VIEW_TYPE_LIST : VIEW_TYPE_GRID;
        }

        @NonNull
        @Override
        public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(viewType == VIEW_TYPE_LIST
                            ? R.layout.scrollable_folder_list_item
                            : R.layout.scrollable_folder_item, parent, false);
            return new MemberViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
            holder.bind(mFolder.members.get(position));
        }

        @Override
        public int getItemCount() {
            return mFolder == null ? 0 : mFolder.members.size();
        }
    }

    private class MemberViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        private final ImageView mIcon;
        private final TextView mLabel;
        private ScrollableDesktopStore.DesktopItem mMember;

        MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            mIcon = itemView.findViewById(R.id.folder_item_icon);
            mLabel = itemView.findViewById(R.id.folder_item_label);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        void bind(ScrollableDesktopStore.DesktopItem member) {
            mMember = member;
            if (member.type == TYPE_APP) {
                AppInfo info = mAppsView.findApp(member);
                if (info != null) {
                    mIcon.setImageDrawable(info.newIcon(getContext()));
                    mIcon.setColorFilter(null);
                    mLabel.setText(info.title);
                    itemView.setAlpha(1f);
                } else {
                    // App is no longer installed: show a dim placeholder.
                    mIcon.setImageDrawable(null);
                    mLabel.setText(member.packageName);
                    itemView.setAlpha(0.5f);
                }
            } else {
                LauncherAppWidgetProviderInfo provider = mAppsView.getWidgetProvider(member);
                mIcon.setImageResource(R.drawable.ic_widget);
                mIcon.setColorFilter(
                        Themes.getAttrColor(getContext(), R.attr.workspaceTextColor));
                mLabel.setText(provider == null || provider.label == null
                        ? "" : provider.label);
                itemView.setAlpha(1f);
            }
        }

        @Override
        public void onClick(View v) {
            if (mMember != null && mMember.type == TYPE_APP) {
                AppInfo info = mAppsView.findApp(mMember);
                if (info != null && info.getIntent() != null) {
                    close(true);
                    mLauncher.startActivitySafely(mIcon, info.getIntent(), info);
                }
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (mMember != null) {
                if (mReorderMode) {
                    // Reorder mode: long-press drags the member, like the desktop list.
                    mItemTouchHelper.startDrag(this);
                } else {
                    showMemberMenu(mMember, itemView);
                }
                return true;
            }
            return false;
        }
    }

    /** trebufork: drag-and-drop reordering of members within the folder grid. */
    private class MemberReorderCallback extends ItemTouchHelper.Callback {

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView,
                @NonNull RecyclerView.ViewHolder viewHolder) {
            int dragFlags = mListMode
                    ? (ItemTouchHelper.UP | ItemTouchHelper.DOWN)
                    : (ItemTouchHelper.UP | ItemTouchHelper.DOWN
                            | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            return makeMovementFlags(dragFlags, 0);
        }

        @Override
        public boolean isLongPressDragEnabled() {
            // Drags are started manually on long-press in reorder mode (see
            // MemberViewHolder.onLongClick), so long-press stays available for the member menu
            // outside reorder mode.
            return false;
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,
                @NonNull RecyclerView.ViewHolder viewHolder,
                @NonNull RecyclerView.ViewHolder target) {
            int from = viewHolder.getBindingAdapterPosition();
            int to = target.getBindingAdapterPosition();
            if (from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION) {
                return false;
            }
            List<ScrollableDesktopStore.DesktopItem> members = mFolder.members;
            if (from < 0 || from >= members.size() || to < 0 || to >= members.size()) {
                return false;
            }
            mStore.moveInFolder(mFolder.id, from, to);
            mAdapter.notifyItemMoved(from, to);
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) { }

        @Override
        public void clearView(@NonNull RecyclerView recyclerView,
                @NonNull RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            viewHolder.itemView.setAlpha(1f);
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            super.onSelectedChanged(viewHolder, actionState);
            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && viewHolder != null) {
                viewHolder.itemView.setAlpha(0.8f);
            }
        }
    }
}
