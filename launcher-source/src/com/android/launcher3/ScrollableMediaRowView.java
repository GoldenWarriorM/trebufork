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
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.android.launcher3.notification.NotificationListener;

/**
 * trebufork: the built-in media player row of the scrollable home. A verbatim port of the
 * SystemUI shade/lockscreen media player (media_session_view.xml + MediaControlPanel):
 * full-bleed album art with a Monet-tinted radial scrim, app icon, title/artist, a squiggly
 * seek bar (see {@link SquigglyProgress}) and prev / play-pause / next actions, colored at
 * runtime from the album artwork through {@link MonetColorExtractor} exactly like
 * MediaColorSchemes.kt does.
 *
 * <p>Unlike an AppWidget this is a plain in-process view, so nothing is constrained by
 * RemoteViews: the seek bar is interactive, the wave animates while playing, and the row
 * hides itself whenever the session goes away (the desktop keeps the row's position
 * reserved). It supports the same user resizing as widget rows (see
 * {@link ScrollableWidgetResizeFrame}) through width/height scales and a horizontal
 * position persisted in the desktop store. When the media session ends the row animates its
 * height down to zero, and grows back when playback resumes.
 */
public class ScrollableMediaRowView extends FrameLayout implements ScrollableResizableRow {

    private static final long PROGRESS_TICK_MS = 500L;
    private static final long COLLAPSE_ANIM_MS = 220L;
    // trebufork: scrim alphas from MediaControlPanel.MEDIA_PLAYER_SCRIM_*.
    private static final float SCRIM_START_ALPHA = 0.65f;
    private static final float SCRIM_END_ALPHA = 0.75f;
    // trebufork: same shadow as the app row labels (see scrollable_app_row.xml / sidebar paint).
    private static final int LABEL_SHADOW_COLOR = 0x66000000;
    private static final float LABEL_SHADOW_RADIUS = 3f;
    private static final float LABEL_SHADOW_DY = 1f;
    // Desaturation filter for the app icon (MediaControlViewBinder.getGrayscaleFilter).
    private static final android.graphics.ColorMatrixColorFilter GRAYSCALE_FILTER =
            createGrayscaleFilter();

    private static android.graphics.ColorMatrixColorFilter createGrayscaleFilter() {
        android.graphics.ColorMatrix matrix = new android.graphics.ColorMatrix();
        matrix.setSaturation(0f);
        return new android.graphics.ColorMatrixColorFilter(matrix);
    }

    private final ImageView mAlbumArt;
    private final TextView mTitle;
    private final TextView mArtist;
    private final SeekBar mSeekBar;
    private final ImageButton mPrev;
    private final ImageButton mPlayPause;
    private final ImageButton mNext;
    private final ImageButton[] mCustomActions;
    private final ImageView mAppIcon;
    private final SquigglyProgress mSquiggly;

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

    // trebufork: Monet colors extracted from the current artwork.
    @Nullable
    private MonetColorExtractor mColorScheme;

    // trebufork: user-configurable size, persisted in ScrollableDesktopStore (same fields as
    // widget rows): width relative to the list width, height relative to the natural height.
    private float mWidthScale = 1f;
    private float mHeightScale = 1f;
    private float mPositionX = 0f;

    // trebufork: false until the play/pause icon is shown once, so the first bind doesn't run
    // the morph animation.
    private boolean mPlayPauseShown;
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
        // trebufork: inflate the verbatim port of SystemUI media_session_view.xml.
        LayoutInflater.from(context).inflate(R.layout.scrollable_media_player_view, this, true);

        mAlbumArt = findViewById(R.id.scrollable_media_album_art);
        mAppIcon = findViewById(R.id.scrollable_media_icon);
        mTitle = findViewById(R.id.scrollable_media_header_title);
        mArtist = findViewById(R.id.scrollable_media_header_artist);
        mSeekBar = findViewById(R.id.scrollable_media_progress_bar);
        mPrev = findViewById(R.id.scrollable_media_actionPrev);
        mPlayPause = findViewById(R.id.scrollable_media_actionPlayPause);
        mNext = findViewById(R.id.scrollable_media_actionNext);
        mCustomActions = new ImageButton[] {
                findViewById(R.id.scrollable_media_action0),
                findViewById(R.id.scrollable_media_action1),
        };

        mSquiggly = mSeekBar.getProgressDrawable() instanceof SquigglyProgress
                ? (SquigglyProgress) mSeekBar.getProgressDrawable()
                : null;
        if (mSquiggly != null) {
            mSquiggly.waveLength = dimen(R.dimen.scrollable_media_seekbar_progress_wavelength);
            mSquiggly.lineAmplitude =
                    dimen(R.dimen.scrollable_media_seekbar_progress_amplitude);
            mSquiggly.phaseSpeed = dimen(R.dimen.scrollable_media_seekbar_progress_phase);
            mSquiggly.setStrokeWidth(
                    dimen(R.dimen.scrollable_media_seekbar_progress_stroke_width));
        }
        // trebufork: media playback is in the direction of tape, not time, so it stays LTR
        // (mirrors MediaViewHolder.create).
        mSeekBar.setLayoutDirection(LAYOUT_DIRECTION_LTR);

        mPrev.setImageResource(R.drawable.scrollable_media_ic_prev);
        mNext.setImageResource(R.drawable.scrollable_media_ic_next);
        mPlayPause.setImageResource(R.drawable.scrollable_media_ic_pause_vector);

        mPrev.setOnClickListener(v -> {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            MediaController.TransportControls controls =
                    mController == null ? null : mController.getTransportControls();
            if (controls != null) {
                controls.skipToPrevious();
            }
        });
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
        mNext.setOnClickListener(v -> {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            MediaController.TransportControls controls =
                    mController == null ? null : mController.getTransportControls();
            if (controls != null) {
                controls.skipToNext();
            }
        });

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
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

        // trebufork: wallpaper-surface legibility — same shadow as the app labels.
        applyLabelShadow(mTitle);
        applyLabelShadow(mArtist);
        applySystemFont(mTitle, /* medium= */ true);
        applySystemFont(mArtist, /* medium= */ false);
    }

    private float dimen(int resId) {
        return getResources().getDimension(resId);
    }

    /**
     * Shapes a raw app icon (an AdaptiveIconDrawable renders as a solid blob when drawn
     * small and tinted) into the launcher's masked bitmap, like home-screen icons.
     */
    private Drawable shapeAppIcon(Drawable rawIcon) {
        try (com.android.launcher3.icons.LauncherIcons li =
                com.android.launcher3.icons.LauncherIcons.obtain(getContext())) {
            android.graphics.Bitmap bitmap = li.createIconBitmap(rawIcon, 1f);
            return new com.android.launcher3.icons.FastBitmapDrawable(bitmap);
        }
    }

    private static void applyLabelShadow(TextView view) {
        view.setShadowLayer(LABEL_SHADOW_RADIUS, 0f, LABEL_SHADOW_DY, LABEL_SHADOW_COLOR);
    }

    /**
     * Applies the SystemUI headline font (the ROM's Google Sans Flex through the framework
     * config resource, the exact strings the shade player's layout uses). Done in code so
     * the family string is resolved at runtime even if the XML attribute is overridden by a
     * launcher theme text appearance. Falls back to the sans-serif-medium / sans-serif
     * system families when the config resource is unavailable.
     */
    private void applySystemFont(TextView view, boolean medium) {
        int resId = getResources().getIdentifier(
                medium ? "config_headlineFontFamilyMedium" : "config_headlineFontFamily",
                "string", "android");
        String family = null;
        if (resId != 0) {
            try {
                family = getResources().getString(resId);
            } catch (Exception ignored) {
                family = null;
            }
        }
        if (family == null || family.isEmpty()) {
            family = medium ? "sans-serif-medium" : "sans-serif";
        }
        view.setTypeface(android.graphics.Typeface.create(family, android.graphics.Typeface.NORMAL));
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
     * Shows or hides the row. When hiding, the height animates to 0 so the list closes the
     * gap smoothly. When showing, the height animates from 0 back to the natural content
     * height. Nothing overlaps: only lp.height changes, so the rows below follow the
     * shrinking/growing row frame by frame, and the content fades with the same fraction.
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
                    if (lp != null && lp.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
                        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
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

    /** Sets the row's layout height (0 collapses the row; WRAP_CONTENT restores natural). */
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
        measure(
                MeasureSpec.makeMeasureSpec(contentWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        return Math.round(getMeasuredHeight() * mHeightScale);
    }

    // ---------------------------------------------------------------------
    // Scaled row sizing (see ScrollableResizableRow / ScrollableWidgetResizeFrame)
    // ---------------------------------------------------------------------

    @Override
    public View getWidgetView() {
        return this;
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
        // The player card is the single child; measure it at the scaled width so the
        // ConstraintLayout inside resolves all its constraints at the final size.
        int contentWidth = Math.round(width * mWidthScale);
        super.onMeasure(
                MeasureSpec.makeMeasureSpec(contentWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        int naturalHeight = getMeasuredHeight();
        int height = Math.round(naturalHeight * mHeightScale);
        // During the show/hide animation the layout params carry the exact animated height.
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
            height = MeasureSpec.getSize(heightMeasureSpec);
        }
        // Report the full row width: the card itself is laid out offset below.
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int rowWidth = r - l;
        int rowHeight = b - t;
        int contentWidth = Math.round(rowWidth * mWidthScale);
        int freeSpace = Math.max(0, rowWidth - contentWidth);
        int offsetX = Math.round(freeSpace * mPositionX);
        if (getChildCount() > 0) {
            View child = getChildAt(0);
            // Lay the card out at the size it was measured with, positioned by mPositionX;
            // ConstraintLayout positions its own children within this frame.
            int childWidth = Math.min(contentWidth, child.getMeasuredWidth());
            int childHeight = Math.min(rowHeight, child.getMeasuredHeight());
            child.layout(offsetX, 0, offsetX + childWidth, childHeight);
        }
    }

    // ---------------------------------------------------------------------
    // Media content binding (the MediaControlPanel.bindPlayer port)
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

        bindAppIcon();

        if (artworkBitmap != null) {
            mAlbumArt.setImageBitmap(artworkBitmap);
        }
        // trebufork: rebuild the Monet scheme from the artwork and retint the whole player,
        // exactly like ColorSchemeTransition.updateColorScheme in SystemUI.
        updateColorScheme(artworkBitmap);
    }

    /**
     * trebufork: the app icon slot mirrors the SystemUI shade player: the app's media
     * notification small icon when available (MediaControlViewBinder normal path), otherwise
     * the launcher icon shaped through the LauncherIcons factory with the shade player's
     * grayscale filter (resume-player path). Called from every bind (metadata and playback
     * state) so the icon picks up as soon as the notification listener delivers it, without
     * waiting for a metadata change.
     */
    private void bindAppIcon() {
        Drawable appIcon = mSource == null ? null : mSource.getAppIcon();
        Drawable smallIcon = mController == null ? null
                : NotificationListener.getMediaSmallIcon(mController.getPackageName());
        if (smallIcon != null) {
            mAppIcon.setImageDrawable(smallIcon);
            mAppIcon.clearColorFilter();
            mAppIcon.setVisibility(VISIBLE);
        } else if (appIcon != null) {
            Drawable shaped = shapeAppIcon(appIcon);
            mAppIcon.setImageDrawable(shaped);
            mAppIcon.clearColorFilter();
            mAppIcon.setColorFilter(GRAYSCALE_FILTER);
            mAppIcon.setVisibility(VISIBLE);
        } else {
            mAppIcon.setVisibility(GONE);
        }
    }

    /**
     * Applies the monet scheme derived from the artwork to every colored surface. Faithful
     * port of SystemUI ColorSchemeTransition + MediaColorSchemes: title/seekbar =
     * neutral1.s50, artist = neutral2.s200, scrim = accent2.s800 (0.65-0.75 alpha),
     * play/pause pill = accent1.s100 with a neutral1.s900 icon, seekbar rest =
     * neutral2.s400, press glow = accent1.s100, app icon = accent1.s100.
     */
    private void updateColorScheme(@Nullable Bitmap artwork) {
        // The shade player uses the LIGHT scheme (darkTheme=false) with TONAL_SPOT palettes.
        MonetColorExtractor scheme = MonetColorExtractor.fromArtwork(artwork, false);
        mColorScheme = scheme;

        int scrimColor = scheme.getScrim();
        int accent = scheme.getPillBackground();
        int pillIcon = scheme.getPillIcon();
        int textPrimary = scheme.getTextPrimary();
        int textSecondary = scheme.getTextSecondary();
        int seekbarRest = scheme.getSeekbarRest();

        // Radial scrim over the album art (MediaControlPanel.addGradientToPlayerAlbum).
        applyScrim(scrimColor);

        // Title = neutral1.s50 (ColorSchemeTransition.textPrimary); artist = neutral2.s200
        // (textSecondary). Keep the label shadow for wallpaper legibility.
        mTitle.setTextColor(textPrimary);
        mArtist.setTextColor(textSecondary);

        // The app icon slot: the media small icon is tinted with the scheme accent (the
        // SystemUI normal path); the launcher-icon fallback carries its own grayscale filter
        // set in bindContent.

        // Small action icons are always media_on_background (white).
        mPrev.setImageTintList(android.content.res.ColorStateList.valueOf(white()));
        mNext.setImageTintList(android.content.res.ColorStateList.valueOf(white()));

        // Play/pause pill: backgroundTint = media_player_solid_button_bg (accent1.s100),
        // imageTint = textPrimaryInverse (neutral1.s900, a dark icon on the pastel pill).
        Drawable pill = mPlayPause.getBackground();
        if (pill != null) {
            pill.mutate().setTint(accent);
        }
        mPlayPause.setImageTintList(android.content.res.ColorStateList.valueOf(pillIcon));

        // LightSourceDrawable glow on every action button (the shade player's tap effect):
        // ColorSchemeTransition feeds it accentPrimary (accent1.s100).
        setHighlightColorOnButtons(accent);

        // Seekbar: wave and thumb = neutral1.s50 (textPrimary); the un-played rest of the
        // bar = neutral2.s400 (textTertiary).
        if (mSquiggly != null) {
            mSquiggly.setTintList(
                    android.content.res.ColorStateList.valueOf(textPrimary));
        }
        mSeekBar.setThumbTintList(android.content.res.ColorStateList.valueOf(textPrimary));
        mSeekBar.setProgressBackgroundTintList(
                android.content.res.ColorStateList.valueOf(seekbarRest));
    }

    /** Sets the scheme highlight color on every LightSourceDrawable button background. */
    private void setHighlightColorOnButtons(int highlightColor) {
        ImageButton[] buttons = new ImageButton[mCustomActions.length + 3];
        buttons[0] = mPrev;
        buttons[1] = mPlayPause;
        buttons[2] = mNext;
        System.arraycopy(mCustomActions, 0, buttons, 3, mCustomActions.length);
        for (ImageButton button : buttons) {
            Drawable bg = button.getBackground();
            if (bg instanceof ScrollableMediaLightSourceDrawable) {
                ((ScrollableMediaLightSourceDrawable) bg).setHighlightColor(highlightColor);
            }
        }
    }

    private int white() {
        return getResources().getColor(R.color.scrollable_media_on_background);
    }

    /**
     * Overlays the radial scrim on the album art, tinted with the scheme color at the
     * MediaControlPanel alphas (0.65 center -> 0.75 edges).
     */
    private void applyScrim(int scrimColor) {
        Drawable current = mAlbumArt.getForeground();
        if (current instanceof LayerDrawable) {
            current.mutate();
            GradientDrawable gradient = (GradientDrawable)
                    ((LayerDrawable) current).getDrawable(0);
            if (gradient != null) {
                gradient.setColors(new int[] {
                        com.android.internal.graphics.ColorUtils.setAlphaComponent(
                                scrimColor, (int) (SCRIM_START_ALPHA * 255f)),
                        com.android.internal.graphics.ColorUtils.setAlphaComponent(
                                scrimColor, (int) (SCRIM_END_ALPHA * 255f)),
                });
            }
            return;
        }
        Drawable scrim = getResources().getDrawable(
                R.drawable.scrollable_media_scrim, getContext().getTheme()).mutate();
        GradientDrawable gradient = (GradientDrawable) scrim;
        gradient.setColors(new int[] {
                com.android.internal.graphics.ColorUtils.setAlphaComponent(
                        scrimColor, (int) (SCRIM_START_ALPHA * 255f)),
                com.android.internal.graphics.ColorUtils.setAlphaComponent(
                        scrimColor, (int) (SCRIM_END_ALPHA * 255f)),
        });
        mAlbumArt.setForeground(new LayerDrawable(new Drawable[] {gradient}));
    }

    private void applyPlaybackState(@Nullable PlaybackState state) {
        boolean wasPlaying = mIsPlaying;
        if (state == null) {
            mIsPlaying = false;
            mPlaybackSpeed = 0f;
        } else {
            mIsPlaying = state.getState() == PlaybackState.STATE_PLAYING;
            mPlaybackSpeed = state.getPlaybackSpeed();
            mStatePosition = state.getPosition();
            mStateElapsedRealtime = SystemClock.elapsedRealtime();
        }
        applyPlayPauseIcon(wasPlaying);
        // trebufork: the small icon may arrive after the first bind (the notification listener
        // connects asynchronously), so re-check it on every playback state update too.
        bindAppIcon();
        // trebufork: the squiggly wave animates while playing and flattens when paused,
        // exactly like SeekBarViewModel drives SquigglyProgress.animate.
        if (mSquiggly != null) {
            mSquiggly.setAnimate(mIsPlaying);
        }
        boolean seekable = state != null
                && (state.getActions() & PlaybackState.ACTION_SEEK_TO) != 0;
        mSeekBar.setEnabled(seekable && mDuration > 0);
        updateProgressUi();
        scheduleProgressTick();
        bindCustomActions(state);
    }

    /**
     * Shows the play/pause icon with the SystemUI AVD morph: when the state changes the
     * animated-vector runs its 333ms path/translate morph (ic_media_play_button.xml /
     * ic_media_pause_button.xml); the first bind just shows the static resting state.
     */
    private void applyPlayPauseIcon(boolean wasPlaying) {
        if (mPlayPauseShown && wasPlaying != mIsPlaying) {
            mPlayPause.setImageResource(mIsPlaying
                    ? R.drawable.scrollable_media_ic_play_morph
                    : R.drawable.scrollable_media_ic_pause_morph);
            Drawable morph = mPlayPause.getDrawable();
            if (morph instanceof android.graphics.drawable.AnimatedVectorDrawable) {
                ((android.graphics.drawable.AnimatedVectorDrawable) morph).start();
            }
        } else {
            mPlayPause.setImageResource(mIsPlaying
                    ? R.drawable.scrollable_media_ic_pause_vector
                    : R.drawable.scrollable_media_ic_play_vector);
        }
        mPlayPauseShown = true;
    }

    /** Binds PlaybackState custom actions (heart, shuffle, ...) like bindActionButtons. */
    private void bindCustomActions(@Nullable PlaybackState state) {
        java.util.List<PlaybackState.CustomAction> actions = state == null
                ? java.util.Collections.emptyList() : state.getCustomActions();
        for (int i = 0; i < mCustomActions.length; i++) {
            ImageButton button = mCustomActions[i];
            if (i < actions.size()) {
                PlaybackState.CustomAction action = actions.get(i);
                // trebufork: the framework CustomAction.getIcon() returns a resource id in
                // the *media app's* package — resolve it through that app's resources,
                // exactly like SystemUI's MediaAction loading does.
                int iconRes = action.getIcon();
                Drawable icon = null;
                if (iconRes != 0) {
                    try {
                        String pkg = mController.getPackageName();
                        android.content.Context appContext = getContext()
                                .createPackageContext(pkg, 0);
                        icon = appContext.getResources().getDrawable(iconRes,
                                appContext.getTheme());
                    } catch (Exception ignored) {
                        // Package not found or resource missing: hide the button.
                    }
                }
                if (icon != null) {
                    button.setImageDrawable(icon);
                    // Same as SystemUI: all action icons are tinted media_on_background.
                    button.setImageTintList(android.content.res.ColorStateList.valueOf(white()));
                    button.setVisibility(VISIBLE);
                    button.setContentDescription(action.getName());
                    button.setOnClickListener(v -> {
                        performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                        MediaController.TransportControls controls =
                                mController == null ? null : mController.getTransportControls();
                        if (controls != null) {
                            controls.sendCustomAction(action.getAction(), action.getExtras());
                        }
                    });
                } else {
                    button.setVisibility(GONE);
                    button.setOnClickListener(null);
                }
            } else {
                button.setVisibility(GONE);
                button.setOnClickListener(null);
            }
        }
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
    }

    private void scheduleProgressTick() {
        removeCallbacks(mProgressTick);
        if (mIsPlaying) {
            postDelayed(mProgressTick, PROGRESS_TICK_MS);
        }
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
