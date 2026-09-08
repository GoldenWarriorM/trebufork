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
 * RemoteViews: the seek bar is interactive and the wave animates while playing. The row is
 * always laid out at its natural height — it does NOT hide when the media session goes
 * away (the earlier hide/show height animation caused relayout storms and rows stuck in a
 * collapsed "thin strip" state). It supports the same user resizing as widget rows (see
 * {@link ScrollableWidgetResizeFrame}) through width/height scales and a horizontal
 * position persisted in the desktop store.
 */
public class ScrollableMediaRowView extends FrameLayout implements ScrollableResizableRow {

    private static final long PROGRESS_TICK_MS = 500L;
    // Lineage 23.2 MediaControlPanel.MEDIA_PLAYER_SCRIM_START/END_ALPHA: the scrim is a
    // radial gradient of the neutral onSurface color from 65% in the center to 75% at
    // the edges (NOT 0.25/1.0 — that renders as a colored vignette).
    private static final float SCRIM_START_ALPHA = 0.65f;
    private static final float SCRIM_END_ALPHA = 0.75f;
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
    private final ScrollableMediaRippleView mRippleView;

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
    // trebufork: the scheme's accentPrimary (ColorSchemeTransition.accentPrimary =
    // accent1.s100): the play/pause container tint, the ripple color and the media
    // small icon color filter all use it. White until the first scheme arrives.
    private int mAccentPrimary = 0xFFFFFFFF;
    // trebufork: which app-icon branch is active (accentPrimary tint for the media small
    // icon, grayscale for the launcher-icon fallback), so scheme updates re-apply the
    // right filter.
    private boolean mAppIconUsesSmallIcon;

    // trebufork: user-configurable size, persisted in ScrollableDesktopStore (same fields as
    // widget rows): width relative to the list width, height relative to the natural height.
    // The width is capped below 1.0 so the card never slides under the alphabet index
    // strip on the right edge of the desktop.
    public static final float MAX_WIDTH_SCALE = 0.9f;
    private float mWidthScale = 1f;
    private float mHeightScale = 1f;
    private float mPositionX = 0f;

    // trebufork: false until the play/pause icon is shown once, so the first bind doesn't run
    // the morph animation.
    private boolean mPlayPauseShown;

    // trebufork: the row no longer hides when there is no active media session — the
    // show/hide height-animation machinery caused layout thrash (stuck "thin strip"
    // states, list relayout storms) and the empty card is kept visible instead.

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

        mRippleView = findViewById(R.id.scrollable_media_touch_ripple);

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

        mPrev.setOnClickListener(v -> {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            playButtonRipple(v);
            MediaController.TransportControls controls =
                    mController == null ? null : mController.getTransportControls();
            if (controls != null) {
                controls.skipToPrevious();
            }
        });
        mPlayPause.setOnClickListener(v -> {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            playButtonRipple(v);
            // Same as bindButtonCommon: the icon AVD and the current container AVD both
            // start their morph right on the tap, before the session state updates.
            Drawable icon = mPlayPause.getDrawable();
            if (icon instanceof android.graphics.drawable.Animatable) {
                ((android.graphics.drawable.Animatable) icon).start();
            }
            Drawable background = mPlayPause.getBackground();
            if (background instanceof android.graphics.drawable.Animatable) {
                ((android.graphics.drawable.Animatable) background).start();
            }
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
            playButtonRipple(v);
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

        // trebufork: fonts — exactly what the shade's layouts request:
        // @*android:string/config_headlineFontFamily(Medium), resolved through the
        // framework's own config path (the ROM's Google Sans Flex overlay). No code-side
        // Typeface override: Typeface.create("google-sans") silently falls back to Roboto
        // when the family is not in the app's font map, which is exactly what made the
        // text look different from the shade.
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
        }
    }

    // ---------------------------------------------------------------------
    // (Show/hide with an animated height collapse removed: the row is always
    // laid out at its natural height, empty or not — see class javadoc.)
    // ---------------------------------------------------------------------

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
        // Never let the card cover the alphabet index on the right.
        widthScale = Math.min(widthScale, MAX_WIDTH_SCALE);
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
        if (width == 0) {
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
            // SystemUI normal path: the small icon is tinted with the scheme's
            // accentPrimary (MediaControlViewBinder.bindArtworkAndColor).
            mAppIcon.setColorFilter(mAccentPrimary);
            mAppIcon.setVisibility(VISIBLE);
            mAppIconUsesSmallIcon = true;
        } else if (appIcon != null) {
            Drawable shaped = shapeAppIcon(appIcon);
            mAppIcon.setImageDrawable(shaped);
            // Resume-player path: the launcher icon carries only the grayscale filter,
            // no accent tint (MediaControlViewBinder.useGrayColorFilter).
            mAppIcon.setColorFilter(GRAYSCALE_FILTER);
            mAppIcon.setVisibility(VISIBLE);
            mAppIconUsesSmallIcon = false;
        } else {
            mAppIcon.setVisibility(GONE);
        }
    }

    /**
     * Applies the monet scheme derived from the artwork to every colored surface. Faithful
     * port of Lineage 23.2 ColorSchemeTransition + MediaColorSchemes: scrim =
     * onSurface (neutral1 t10, 0.65/0.75 alpha); title, artist, seekbar wave/thumb,
     * prev/next and custom action icons = plain white (media_on_background, no runtime
     * tint); play/pause background = primaryFixed (accent1 t90) with an onPrimaryFixed
     * icon (accent1 t10); tap ripple + app icon filter = primaryFixed.
     */
    private void updateColorScheme(@Nullable Bitmap artwork) {
        // The shade player uses the LIGHT scheme (darkTheme=false) with TONAL_SPOT palettes.
        MonetColorExtractor scheme = MonetColorExtractor.fromArtwork(artwork, false);
        mColorScheme = scheme;

        int scrimColor = scheme.getScrim();
        int accent = scheme.getPillBackground();
        mAccentPrimary = accent;
        int pillIcon = scheme.getPillIcon();

        // Radial scrim over the album art (addGradientToPlayerAlbum: qs_media_scrim with
        // MEDIA_PLAYER_SCRIM_START/END_ALPHA = 0.25 / 1.0).
        applyScrim(scrimColor);

        // Title and artist are plain white (media_on_background) — the shade applies no
        // runtime text tint at all (Lineage removed the textPrimary/textSecondary
        // transitions; the layouts hardcode @color/media_on_background).
        mTitle.setTextColor(white());
        mArtist.setTextColor(white());

        // The app icon slot follows the branch chosen in bindAppIcon: accentPrimary tint
        // for the media small icon, grayscale for the launcher-icon fallback.
        if (mAppIcon.getVisibility() == VISIBLE) {
            if (mAppIconUsesSmallIcon) {
                mAppIcon.setColorFilter(mAccentPrimary);
            } else {
                mAppIcon.setColorFilter(GRAYSCALE_FILTER);
            }
        }

        // Small action icons: media_player_action_color = plain white (the shade never
        // retints the transparent buttons from the scheme).
        int white = white();
        mPrev.setImageTintList(android.content.res.ColorStateList.valueOf(white));
        mNext.setImageTintList(android.content.res.ColorStateList.valueOf(white));
        for (ImageButton button : mCustomActions) {
            button.setImageTintList(android.content.res.ColorStateList.valueOf(white));
        }

        // Play/pause: backgroundTint = primaryFixed (accentPrimary); imageTint =
        // onPrimaryFixed (a dark tone of the same hue, ColorSchemeTransition.onPrimary).
        tintPlayPauseBackground();
        mPlayPause.setImageTintList(android.content.res.ColorStateList.valueOf(pillIcon));

        // Tap ripple color = accentPrimary (ColorSchemeTransition feeds the
        // MultiRippleController the same color).
        mRippleView.updateColor(accent);

        // Seekbar: wave, thumb and rest-of-bar follow the style's media_on_background
        // (white); Lineage's MediaPlayer.ProgressBar hardcodes the color, no scheme tint.
        if (mSquiggly != null) {
            mSquiggly.setTintList(android.content.res.ColorStateList.valueOf(white));
        }
        mSeekBar.setThumbTintList(android.content.res.ColorStateList.valueOf(white));
        mSeekBar.setProgressBackgroundTintList(
                android.content.res.ColorStateList.valueOf(white));
    }

    /** Tints the current play/pause container background with the scheme accent. */
    private void tintPlayPauseBackground() {
        Drawable background = mPlayPause.getBackground();
        if (background != null) {
            background.mutate().setTint(mAccentPrimary);
        }
    }

    /**
     * Plays the tap ripple centered on an action button, like
     * MediaControlViewBinder.createTouchRippleAnimation: the circle grows from the button
     * center to cover the whole player. The button's parent (the player card) is the same
     * view the ripple overlay is constrained to, so card coordinates match directly.
     */
    private void playButtonRipple(View button) {
        mRippleView.playRipple(
                button.getX() + button.getWidth() / 2f,
                button.getY() + button.getHeight() / 2f);
    }

    private int white() {
        return getResources().getColor(R.color.scrollable_media_on_background);
    }

    /**
     * Radial scrim over the album art (Lineage 23.2 addGradientToPlayerAlbum: qs_media_scrim
     * with the onSurface color at MEDIA_PLAYER_SCRIM_START/END_ALPHA = 0.65 / 0.75).
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
     * Shows the play/pause icon and container with the Lineage 23.2 (Android 16) morphs,
     * exactly like MediaActions.getStandardAction + bindButtonCommon:
     * <ul>
     * <li>playing → pause icon AVD + pause button container (pill blob);</li>
     * <li>paused → play icon AVD + play button container (rounded rectangle).</li>
     * </ul>
     * Both the 24dp icon AVD (333ms path/translate morph) and the 88x56dp container AVD
     * (two-phase 167ms scale + 333ms path morph) are {@code AnimatedVectorDrawable}s that
     * run their forward morph when started after the swap; the first bind shows the
     * static resting states. On tap the click listener also starts the background AVD so
     * the morph begins immediately, before the session state updates.
     */
    private void applyPlayPauseIcon(boolean wasPlaying) {
        if (mPlayPauseShown && wasPlaying != mIsPlaying) {
            // State change: swap in the animated variants and run the morph forward.
            mPlayPause.setImageResource(mIsPlaying
                    ? R.drawable.scrollable_media_ic_pause_button
                    : R.drawable.scrollable_media_ic_play_button);
            startIfAnimatable(mPlayPause.getDrawable());
            mPlayPause.setBackgroundResource(mIsPlaying
                    ? R.drawable.scrollable_media_ic_pause_button_container
                    : R.drawable.scrollable_media_ic_play_button_container);
            startIfAnimatable(mPlayPause.getBackground());
            tintPlayPauseBackground();
        } else {
            // First bind (or no state change): the static resting states.
            mPlayPause.setImageResource(mIsPlaying
                    ? R.drawable.scrollable_media_ic_pause_button
                    : R.drawable.scrollable_media_ic_play_button);
            mPlayPause.setBackgroundResource(mIsPlaying
                    ? R.drawable.scrollable_media_ic_pause_button_container
                    : R.drawable.scrollable_media_ic_play_button_container);
            tintPlayPauseBackground();
        }
        mPlayPauseShown = true;
    }

    private static void startIfAnimatable(Drawable drawable) {
        if (drawable instanceof android.graphics.drawable.Animatable) {
            ((android.graphics.drawable.Animatable) drawable).start();
        }
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
                    button.setImageTintList(
                            android.content.res.ColorStateList.valueOf(
                                    mColorScheme == null
                                            ? white() : mColorScheme.getTextPrimary()));
                    button.setVisibility(VISIBLE);
                    button.setContentDescription(action.getName());
                    button.setOnClickListener(v -> {
                        performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                        playButtonRipple(v);
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
