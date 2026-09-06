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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.android.launcher3.util.Themes;

/**
 * trebufork: the built-in media player row of the scrollable home. Renders the currently
 * active {@link android.media.session.MediaSession} — artwork, title/artist, a live seek bar
 * and transport controls — in the style of the SystemUI shade / lock screen media controls.
 *
 * <p>Unlike an AppWidget this is a plain in-process view, so nothing is constrained by
 * RemoteViews: the seek bar is interactive, playback state animates, and the row hides itself
 * whenever the session goes away (the desktop keeps the row's position reserved).
 *
 * <p>The row has no card background: it sits directly on the wallpaper and its texts carry the
 * same drop shadow as the app labels. It supports the same user resizing as widget rows
 * (see {@link ScrollableWidgetResizeFrame}) through width/height scales and a horizontal
 * position persisted in the desktop store. When the media session ends the row animates its
 * height down to zero (so the list closes the gap smoothly instead of leaving a blank space),
 * and grows back when playback resumes.
 */
public class ScrollableMediaRowView extends FrameLayout implements ScrollableResizableRow {

    private static final long PROGRESS_TICK_MS = 500L;
    private static final long COLLAPSE_ANIM_MS = 220L;
    // trebufork: same shadow as the app row labels (see scrollable_app_row.xml / sidebar paint).
    private static final int LABEL_SHADOW_COLOR = 0x66000000;
    private static final float LABEL_SHADOW_RADIUS = 3f;
    private static final float LABEL_SHADOW_DY = 1f;

    private final LinearLayout mContent;
    private final ImageView mArtwork;
    private final TextView mTitle;
    private final TextView mArtist;
    private final TextView mAppName;
    private final SeekBar mSeekBar;
    private final TextView mPositionText;
    private final TextView mDurationText;
    private final ImageView mPrev;
    private final ImageView mPlayPause;
    private final ImageView mNext;
    private final ImageView mOwnerIcon;

    @Nullable
    private MediaController mController;
    @Nullable
    private ScrollableMediaController mSource;
    // Trebufork: elapsedRealtime anchor of the last playback state + extrapolated position, so
    // the seek bar advances smoothly between state updates.
    private long mStateElapsedRealtime;
    private long mStatePosition;
    private float mPlaybackSpeed;
    private boolean mIsPlaying;
    private long mDuration;

    // trebufork: user-configurable size, persisted in ScrollableDesktopStore (same fields as
    // widget rows): width relative to the list width, height relative to the natural height.
    private float mWidthScale = 1f;
    private float mHeightScale = 1f;
    private float mPositionX = 0f;

    // trebufork: hidden state (no active session). The row collapses to zero height so the
    // RecyclerView reclaims the gap; while animating, mHeightAnimator drives lp.height.
    private boolean mHidden = true;
    private boolean mEverShown;
    @Nullable
    private ValueAnimator mHeightAnimator;

    private final Runnable mProgressTick = new Runnable() {
        @Override
        public void run() {
            if (mIsPlaying) {
                updateProgressUi();
                postDelayed(this, PROGRESS_TICK_MS);
            }
        }
    };

    private final MediaController.Callback mCallback = new MediaController.Callback() {
        @Override
        public void onMetadataChanged(@Nullable MediaMetadata metadata) {
            post(() -> bindContent());
        }

        @Override
        public void onPlaybackStateChanged(@Nullable PlaybackState state) {
            post(() -> applyPlaybackState(state));
        }
    };

    public ScrollableMediaRowView(Context context) {
        this(context, null);
    }

    public ScrollableMediaRowView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollableMediaRowView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        int dp = Math.round(getResources().getDisplayMetrics().density);

        // trebufork: no card background — the row sits on the wallpaper and relies on the same
        // drop shadow as the app labels for legibility.
        int contentColor = Themes.getAttrColor(getContext(), android.R.attr.textColorPrimary);
        int subtextColor = Themes.getAttrColor(getContext(), android.R.attr.textColorSecondary);

        mContent = new LinearLayout(getContext());
        mContent.setOrientation(LinearLayout.HORIZONTAL);
        mContent.setGravity(Gravity.CENTER_VERTICAL);
        int rootPadding = 16 * dp;
        mContent.setPadding(rootPadding, rootPadding, rootPadding, rootPadding);
        addView(mContent, new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        mArtwork = new ImageView(getContext());
        mArtwork.setScaleType(ImageView.ScaleType.CENTER_CROP);
        GradientDrawable artPlaceholder = new GradientDrawable();
        artPlaceholder.setShape(GradientDrawable.RECTANGLE);
        artPlaceholder.setCornerRadius(16 * dp);
        artPlaceholder.setColor(subtextColor);
        mArtwork.setImageDrawable(artPlaceholder);
        mArtwork.setClipToOutline(true);
        LinearLayout.LayoutParams artLp = new LinearLayout.LayoutParams(72 * dp, 72 * dp);
        artLp.setMarginEnd(16 * dp);
        mContent.addView(mArtwork, artLp);

        LinearLayout textColumn = new LinearLayout(getContext());
        textColumn.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(
                0, LayoutParams.WRAP_CONTENT, 1f);
        mContent.addView(textColumn, textLp);

        LinearLayout titleRow = new LinearLayout(getContext());
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        textColumn.addView(titleRow, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        mTitle = new TextView(getContext());
        mTitle.setTextColor(contentColor);
        mTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        mTitle.setSingleLine(true);
        mTitle.setEllipsize(android.text.TextUtils.TruncateAt.END);
        titleRow.addView(mTitle, new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        mOwnerIcon = new ImageView(getContext());
        LinearLayout.LayoutParams ownerLp = new LinearLayout.LayoutParams(14 * dp, 14 * dp);
        ownerLp.setMarginStart(6 * dp);
        titleRow.addView(mOwnerIcon, ownerLp);

        mArtist = new TextView(getContext());
        mArtist.setTextColor(subtextColor);
        mArtist.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        mArtist.setSingleLine(true);
        mArtist.setEllipsize(android.text.TextUtils.TruncateAt.END);
        textColumn.addView(mArtist, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        mAppName = new TextView(getContext());
        mAppName.setTextColor(subtextColor);
        mAppName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        mAppName.setSingleLine(true);
        mAppName.setEllipsize(android.text.TextUtils.TruncateAt.END);
        textColumn.addView(mAppName, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout progressRow = new LinearLayout(getContext());
        progressRow.setOrientation(LinearLayout.HORIZONTAL);
        progressRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams progressLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        progressLp.topMargin = 6 * dp;
        textColumn.addView(progressRow, progressLp);

        mPositionText = new TextView(getContext());
        mPositionText.setTextColor(subtextColor);
        mPositionText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        progressRow.addView(mPositionText, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        mSeekBar = new SeekBar(getContext(), null, android.R.attr.progressBarStyleHorizontal);
        mSeekBar.getProgressDrawable().setColorFilter(contentColor, PorterDuff.Mode.SRC_IN);
        if (mSeekBar.getThumb() != null) {
            mSeekBar.getThumb().setColorFilter(contentColor, PorterDuff.Mode.SRC_IN);
        }
        LinearLayout.LayoutParams seekLp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        seekLp.setMargins(8 * dp, 0, 8 * dp, 0);
        progressRow.addView(mSeekBar, seekLp);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mPositionText.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                removeCallbacks(mProgressTick);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                MediaController.TransportControls controls =
                        mController == null ? null : mController.getTransportControls();
                if (controls != null) {
                    controls.seekTo(seekBar.getProgress());
                }
                updateProgressUi();
                scheduleProgressTick();
            }
        });

        mDurationText = new TextView(getContext());
        mDurationText.setTextColor(subtextColor);
        mDurationText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        progressRow.addView(mDurationText, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout controlsRow = new LinearLayout(getContext());
        controlsRow.setOrientation(LinearLayout.HORIZONTAL);
        controlsRow.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams controlsLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        controlsLp.topMargin = 4 * dp;
        textColumn.addView(controlsRow, controlsLp);

        mPrev = makeControlButton(controlsRow, contentColor, 36 * dp);
        mPrev.setImageResource(android.R.drawable.ic_media_previous);
        mPrev.setOnClickListener(v -> {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            MediaController.TransportControls controls =
                    mController == null ? null : mController.getTransportControls();
            if (controls != null) {
                controls.skipToPrevious();
            }
        });
        mPlayPause = makeControlButton(controlsRow, contentColor, 52 * dp);
        mPlayPause.setImageResource(android.R.drawable.ic_media_pause);
        LinearLayout.LayoutParams ppLp = (LinearLayout.LayoutParams) mPlayPause.getLayoutParams();
        ppLp.setMargins(12 * dp, 0, 12 * dp, 0);
        mPlayPause.setOnClickListener(v -> {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            MediaController.TransportControls controls =
                    mController == null ? null : mController.getTransportControls();
            if (controls != null) {
                if (mIsPlaying) {
                    controls.pause();
                } else {
                    controls.play();
                }
            }
        });
        mNext = makeControlButton(controlsRow, contentColor, 36 * dp);
        mNext.setImageResource(android.R.drawable.ic_media_next);
        mNext.setOnClickListener(v -> {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            MediaController.TransportControls controls =
                    mController == null ? null : mController.getTransportControls();
            if (controls != null) {
                controls.skipToNext();
            }
        });

        // trebufork: wallpaper-surface legibility — same shadow as the app labels.
        applyLabelShadow(mTitle);
        applyLabelShadow(mArtist);
        applyLabelShadow(mAppName);
        applyLabelShadow(mPositionText);
        applyLabelShadow(mDurationText);
    }

    private ImageView makeControlButton(LinearLayout parent, int iconColor, int sizeDp) {
        ImageView button = new ImageView(getContext());
        button.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
        GradientDrawable rippleBg = new GradientDrawable();
        rippleBg.setShape(GradientDrawable.OVAL);
        button.setBackground(new RippleDrawable(
                ColorStateList.valueOf(Themes.getAttrColor(getContext(),
                        android.R.attr.colorControlHighlight)),
                null, rippleBg));
        button.setClickable(true);
        button.setFocusable(true);
        parent.addView(button, new LinearLayout.LayoutParams(sizeDp, sizeDp));
        return button;
    }

    private static void applyLabelShadow(TextView view) {
        view.setShadowLayer(LABEL_SHADOW_RADIUS, 0f, LABEL_SHADOW_DY, LABEL_SHADOW_COLOR);
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
        refresh();
    }

    private final ScrollableMediaController.Listener mSourceListener =
            new ScrollableMediaController.Listener() {
                @Override
                public void onActiveControllerChanged() {
                    post(ScrollableMediaRowView.this::refresh);
                }

                @Override
                public void onMediaChanged() {
                    post(() -> {
                        bindContent();
                        applyPlaybackState(
                                mController == null ? null : mController.getPlaybackState());
                    });
                }
            };

    /** Re-resolves the active controller and rebinds everything (with show/hide animation). */
    private void refresh() {
        MediaController next = mSource == null ? null : mSource.getController();
        if (mController != null && mController != next) {
            try {
                mController.unregisterCallback(mCallback);
            } catch (IllegalStateException ignored) {
            }
        }
        mController = next;
        if (mController != null) {
            try {
                mController.registerCallback(mCallback);
            } catch (IllegalStateException ignored) {
            }
            bindContent();
            applyPlaybackState(mController.getPlaybackState());
            setHidden(false);
        } else {
            setHidden(true);
        }
    }

    // ---------------------------------------------------------------------
    // Show / hide with an animated height collapse
    // ---------------------------------------------------------------------

    /** True while the show/hide height animation is running (used to guard list rebinds). */
    public boolean isHeightAnimating() {
        return mHeightAnimator != null;
    }

    /**
     * Shows or hides the row. When hiding, the height animates to 0 so the list closes the gap
     * smoothly (a GONE child of a RecyclerView still occupies its measured size, and an
     * instant jump leaves the rows below snapping up). When showing, the height animates from
     * 0 back to the natural content height. Nothing overlaps: only lp.height changes, so the
     * rows below follow the shrinking/growing row frame by frame, and the content fades with
     * the same fraction.
     */
    private void setHidden(boolean hidden) {
        if (mHidden == hidden) {
            return;
        }
        mHidden = hidden;
        if (mHeightAnimator != null) {
            mHeightAnimator.cancel();
            mHeightAnimator = null;
        }
        if (hidden) {
            // First bind before anything was ever laid out: collapse instantly, no animation.
            if (!mEverShown) {
                setVisibility(GONE);
                setRowHeight(0);
                setAlpha(1f);
                return;
            }
            setVisibility(VISIBLE);
            animateRowHeight(getHeight(), 0, /* expand= */ false);
        } else {
            int target = computeContentHeight();
            setVisibility(VISIBLE);
            mEverShown = true;
            // Row not laid out yet (fresh bind at boot): snap, no animation.
            if (getWidth() == 0 || target <= 0) {
                setRowHeight(0);
                setAlpha(1f);
                requestLayout();
                return;
            }
            animateRowHeight(0, target, /* expand= */ true);
        }
    }

    private void animateRowHeight(int from, int to, boolean expand) {
        ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
        anim.setDuration(COLLAPSE_ANIM_MS);
        anim.addUpdateListener(animation -> {
            float fraction = animation.getAnimatedFraction();
            int height = Math.round(from + (to - from) * fraction);
            setRowHeight(height);
            // Content fades out while collapsing and in while expanding; the row frame moves
            // the rows below/above via requestLayout, so nothing overlaps at any point.
            setAlpha(expand ? fraction : 1f - fraction);
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
                if (mCancelled) {
                    return;
                }
                if (expand) {
                    setAlpha(1f);
                    // Back to the natural wrap-content sizing once fully shown.
                    ViewGroup.LayoutParams lp = getLayoutParams();
                    if (lp != null && lp.height != LayoutParams.WRAP_CONTENT) {
                        lp.height = LayoutParams.WRAP_CONTENT;
                        setLayoutParams(lp);
                    }
                } else {
                    setVisibility(GONE);
                }
            }
        });
        mHeightAnimator = anim;
        anim.start();
    }

    /** Sets the row's layout height (0 collapses the row; -1 / WRAP_CONTENT restores natural). */
    private void setRowHeight(int height) {
        ViewGroup.LayoutParams lp = getLayoutParams();
        if (lp == null) {
            return;
        }
        lp.height = height <= 0 ? 0 : height;
        setLayoutParams(lp);
    }

    /** Natural (scaled) content height computed by measuring the content at the row width. */
    private int computeContentHeight() {
        int width = getWidth();
        if (width <= 0) {
            width = getResources().getDisplayMetrics().widthPixels;
        }
        int contentWidth = Math.round(width * mWidthScale);
        mContent.measure(
                MeasureSpec.makeMeasureSpec(contentWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        return Math.round(mContent.getMeasuredHeight() * mHeightScale);
    }

    // ---------------------------------------------------------------------
    // Scaled row sizing (see ScrollableResizableRow / ScrollableWidgetResizeFrame)
    // ---------------------------------------------------------------------

    @Override
    public View getWidgetView() {
        return mContent;
    }

    @Override
    public void setScales(float widthScale, float heightScale) {
        if (widthScale <= 0f) {
            widthScale = 1f;
        }
        if (heightScale <= 0f) {
            heightScale = 1f;
        }
        if (mWidthScale != widthScale || mHeightScale != heightScale) {
            mWidthScale = widthScale;
            mHeightScale = heightScale;
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
        // Collapsed (hidden) rows report zero height so the list reclaims the gap.
        if (mHidden || width == 0) {
            setMeasuredDimension(width, 0);
            return;
        }
        int contentWidth = Math.round(width * mWidthScale);
        mContent.measure(
                MeasureSpec.makeMeasureSpec(contentWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        int naturalHeight = mContent.getMeasuredHeight();
        int height = Math.round(naturalHeight * mHeightScale);
        // During the show/hide animation the layout params carry the exact animated height.
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
        mContent.layout(offsetX, 0, offsetX + contentWidth, rowHeight);
    }

    // ---------------------------------------------------------------------
    // Media content binding
    // ---------------------------------------------------------------------

    private void bindContent() {
        if (mController == null) {
            return;
        }
        MediaMetadata metadata = mController.getMetadata();
        CharSequence title = null;
        CharSequence artist = null;
        mDuration = 0L;
        Bitmap artworkBitmap = null;
        if (metadata != null) {
            title = metadata.getText(MediaMetadata.METADATA_KEY_TITLE);
            artist = metadata.getText(MediaMetadata.METADATA_KEY_ARTIST);
            if (artist == null || artist.length() == 0) {
                artist = metadata.getText(MediaMetadata.METADATA_KEY_ALBUM_ARTIST);
            }
            mDuration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION);
            artworkBitmap = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);
            if (artworkBitmap == null) {
                artworkBitmap = metadata.getBitmap(MediaMetadata.METADATA_KEY_ART);
            }
        }
        mTitle.setText(title == null ? "" : title);
        mArtist.setText(artist == null ? "" : artist);
        String appName = mSource == null ? null : mSource.getAppName();
        mAppName.setText(appName == null ? "" : appName);
        Drawable appIcon = mSource == null ? null : mSource.getAppIcon();
        if (appIcon != null) {
            mOwnerIcon.setImageDrawable(appIcon);
            mOwnerIcon.setVisibility(VISIBLE);
        } else {
            mOwnerIcon.setVisibility(GONE);
        }
        if (artworkBitmap != null) {
            mArtwork.setImageBitmap(artworkBitmap);
        }
    }

    private void applyPlaybackState(@Nullable PlaybackState state) {
        if (state == null) {
            mIsPlaying = false;
            mPlaybackSpeed = 0f;
        } else {
            mIsPlaying = state.getState() == PlaybackState.STATE_PLAYING;
            mPlaybackSpeed = state.getPlaybackSpeed();
            mStatePosition = state.getPosition();
            mStateElapsedRealtime = SystemClock.elapsedRealtime();
        }
        mPlayPause.setImageResource(mIsPlaying
                ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
        boolean seekable = state != null
                && (state.getActions() & PlaybackState.ACTION_SEEK_TO) != 0;
        mSeekBar.setEnabled(seekable && mDuration > 0);
        updateProgressUi();
        scheduleProgressTick();
    }

    private void updateProgressUi() {
        if (mController == null) {
            return;
        }
        long position = mStatePosition;
        if (mIsPlaying && mPlaybackSpeed != 0f) {
            position += (long) ((SystemClock.elapsedRealtime() - mStateElapsedRealtime)
                    * mPlaybackSpeed);
        }
        if (mDuration > 0 && position > mDuration) {
            position = mDuration;
        }
        mSeekBar.setMax(mDuration > 0 ? (int) mDuration : 0);
        if (!mSeekBar.isPressed()) {
            mSeekBar.setProgress((int) position);
        }
        mPositionText.setText(formatTime(position));
        mDurationText.setText(formatTime(mDuration));
    }

    private void scheduleProgressTick() {
        removeCallbacks(mProgressTick);
        if (mIsPlaying) {
            postDelayed(mProgressTick, PROGRESS_TICK_MS);
        }
    }

    private static String formatTime(long ms) {
        if (ms < 0) {
            ms = 0;
        }
        long totalSeconds = ms / 1000L;
        long seconds = totalSeconds % 60L;
        long minutes = totalSeconds / 60L;
        if (minutes >= 60L) {
            return String.format("%d:%02d:%02d", minutes / 60L, minutes % 60L, seconds);
        }
        return String.format("%d:%02d", minutes, seconds);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(mProgressTick);
        if (mHeightAnimator != null) {
            mHeightAnimator.cancel();
            mHeightAnimator = null;
        }
        if (mController != null) {
            try {
                mController.unregisterCallback(mCallback);
            } catch (IllegalStateException ignored) {
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mController != null) {
            try {
                mController.registerCallback(mCallback);
            } catch (IllegalStateException ignored) {
            }
        }
        refresh();
    }
}
