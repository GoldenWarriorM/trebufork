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
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import java.util.ArrayList;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.android.launcher3.notification.NotificationListener;
import com.android.launcher3.Utilities;
import com.android.launcher3.util.ActivityOptionsWrapper;

/**
 * trebufork: one player card of the media carousel — the verbatim port of the SystemUI
 * shade/lockscreen media player (media_session_view.xml + MediaControlPanel) extracted from
 * {@link ScrollableMediaRowView} so that the carousel (the MediaScrollView +
 * MediaCarouselScrollHandler port) can host ONE REAL CARD PER MEDIA SESSION, exactly like
 * the shade: each page is a full player that slides in from the edge.
 *
 * <p>Full-bleed album art with a Monet-tinted radial scrim, app icon, title/artist, a
 * squiggly seek bar (see {@link SquigglyProgress}) and prev / play-pause / next actions,
 * colored at runtime from the album artwork through {@link MonetColorExtractor} exactly
 * like MediaColorSchemes.kt does.
 */
public class ScrollableMediaCardView extends FrameLayout
        implements com.android.launcher3.views.UpdateDeferrableView {

    private static final long PROGRESS_TICK_MS = 500L;
    // Lineage 23.2 MediaControlPanel.MEDIA_PLAYER_SCRIM_START/END_ALPHA is 0.65/0.75 for
    // the shade player; trebufork lightens it for the home-screen widget so the artwork
    // stays clearly visible (text remains readable over the radial gradient).
    private static final float SCRIM_START_ALPHA = 0.45f;
    private static final float SCRIM_END_ALPHA = 0.60f;
    // Desaturation filter for the app icon (MediaControlViewBinder.getGrayscaleFilter).
    private static final android.graphics.ColorMatrixColorFilter GRAYSCALE_FILTER =
            createGrayscaleFilter();

    private static android.graphics.ColorMatrixColorFilter createGrayscaleFilter() {
        android.graphics.ColorMatrix matrix = new android.graphics.ColorMatrix();
        matrix.setSaturation(0f);
        return new android.graphics.ColorMatrixColorFilter(matrix);
    }

    private final android.widget.ImageView mAlbumArt;
    private final TextView mTitle;
    private final TextView mArtist;
    private final SeekBar mSeekBar;
    private final ImageButton mPrev;
    private final ImageButton mPlayPause;
    private final ImageButton mNext;
    private final ImageButton[] mCustomActions;
    private final android.widget.ImageView mAppIcon;
    private final SquigglyProgress mSquiggly;
    private final ScrollableMediaRippleView mRippleView;

    @Nullable
    private MediaController mController;
    @Nullable
    private ScrollableMediaController mSource;
    // ElapsedRealtime anchor of the last playback state + extrapolated position, so
    // the seek bar advances smoothly between state updates.
    private long mStateElapsedRealtime;
    private long mStatePosition;
    private float mPlaybackSpeed;
    private boolean mIsPlaying;
    private long mDuration;

    // Monet colors extracted from the current artwork.
    @Nullable
    private MonetColorExtractor mColorScheme;
    // Guards against applying a stale background-extracted scheme after a newer bind.
    private int mSchemeRequestToken;
    // The scheme's accentPrimary (ColorSchemeTransition.accentPrimary = accent1.s100): the
    // play/pause container tint, the ripple color and the media small icon color filter
    // all use it. White until the first scheme arrives.
    private int mAccentPrimary = 0xFFFFFFFF;
    // Which app-icon branch is active (accentPrimary tint for the media small icon,
    // grayscale for the launcher-icon fallback), so scheme updates re-apply the right
    // filter.
    private boolean mAppIconUsesSmallIcon;

    // False until the play/pause icon is shown once, so the first bind doesn't run the
    // morph animation.
    private boolean mPlayPauseShown;

    // Metadata transition (MediaControlPanel + MetadataAnimationHandler port): the
    // title/artist pair slides out and in when the song changes, and the album art
    // crossfades via a TransitionDrawable (addGradientToPlayerAlbum path).
    @Nullable
    private AnimatorSet mMetadataEnter;
    @Nullable
    private AnimatorSet mMetadataExit;
    @Nullable
    private Runnable mPostExitUpdate;
    @Nullable
    private Runnable mPostEnterUpdate;
    // Identity of the currently shown (title, artist) pair; a change triggers the
    // exit -> update -> enter motion.
    @Nullable
    private CharSequence mShownTitle;
    @Nullable
    private CharSequence mShownArtist;
    // Previous artwork drawable for the crossfade (mPrevArtwork).
    @Nullable
    private Drawable mPrevArtwork;

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

    public ScrollableMediaCardView(Context context) {
        this(context, null);
    }

    public ScrollableMediaCardView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollableMediaCardView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // Inflate the verbatim port of SystemUI media_session_view.xml.
        LayoutInflater.from(context).inflate(R.layout.scrollable_media_player_card, this, true);

        mAlbumArt = findViewById(R.id.card_media_album_art);
        mAppIcon = findViewById(R.id.card_media_icon);
        mTitle = findViewById(R.id.card_media_header_title);
        mArtist = findViewById(R.id.card_media_header_artist);
        mSeekBar = findViewById(R.id.card_media_progress_bar);
        mPrev = findViewById(R.id.card_media_actionPrev);
        mPlayPause = findViewById(R.id.card_media_actionPlayPause);
        mNext = findViewById(R.id.card_media_actionNext);
        mCustomActions = new ImageButton[] {
                findViewById(R.id.card_media_action0),
                findViewById(R.id.card_media_action1),
        };

        mRippleView = findViewById(R.id.card_media_touch_ripple);

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
        // Media playback is in the direction of tape, not time, so it stays LTR
        // (mirrors MediaViewHolder.create).
        mSeekBar.setLayoutDirection(LAYOUT_DIRECTION_LTR);

        // Tap on the card body opens the player, like the shade player's clickIntent
        // (MediaControlPanel: player.setOnClickListener -> clickIntent.send). Button and
        // seek bar taps are consumed by their own handlers and never reach this.
        setOnClickListener(v -> openPlayerApp());

        mPrev.setImageResource(R.drawable.scrollable_media_ic_prev);
        mNext.setImageResource(R.drawable.scrollable_media_ic_next);

        loadMetadataAnimators();

        mBackgroundColor = new AnimatingColorTransition(0xFF1c1b1f, color ->
                mAlbumArt.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(color)));
        mPrimaryColor = new AnimatingColorTransition(0xFFFFFFFF, color -> {
            mAccentPrimary = color;
            tintPlayPauseBackground();
            mRippleView.updateColor(color);
            if (mAppIcon.getVisibility() == VISIBLE && mAppIconUsesSmallIcon) {
                mAppIcon.setColorFilter(color);
            }
        });
        mOnPrimaryColor = new AnimatingColorTransition(0xFF1c1b1f, color ->
                mPlayPause.setImageTintList(
                        android.content.res.ColorStateList.valueOf(color)));

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
            // start their morph right on the tap, before the session state updates. The
            // rebind that lands the new resting pose is deferred until the morph ends.
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
    }

    private float dimen(int resId) {
        return getResources().getDimension(resId);
    }

    /**
     * Loads the enter/exit animator sets (MediaControlPanel.attachPlayer: loadAnimator
     * with media_metadata_enter/exit): one outer AnimatorSet per animation that plays
     * a per-view set (title, artist) together — exactly like loadAnimator builds
     * result.playTogether(per-target sets loaded from the same XML). SystemUI's private
     * EMPHASIZED interpolators are approximated with the closest public framework
     * curves, set on the translationX child (childAnimations.get(0)) like loadAnimator.
     */
    private void loadMetadataAnimators() {
        mMetadataEnter = loadMetadataAnimator(R.anim.scrollable_media_metadata_enter,
                android.R.interpolator.decelerate_quad);
        mMetadataExit = loadMetadataAnimator(R.anim.scrollable_media_metadata_exit,
                android.R.interpolator.accelerate_quad);
        mMetadataEnter.addListener(mMetadataAnimatorListener);
        mMetadataExit.addListener(mMetadataAnimatorListener);
    }

    /** One outer set playing the XML animation for title and artist together. */
    private AnimatorSet loadMetadataAnimator(int animRes, int interpolatorRes) {
        android.view.animation.Interpolator interpolator =
                android.view.animation.AnimationUtils.loadInterpolator(getContext(),
                        interpolatorRes);
        ArrayList<Animator> perView = new ArrayList<>();
        for (View target : new View[] {mTitle, mArtist}) {
            AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(),
                    animRes);
            set.getChildAnimations().get(0).setInterpolator(interpolator);
            set.setTarget(target);
            perView.add(set);
        }
        AnimatorSet result = new AnimatorSet();
        result.playTogether(perView);
        return result;
    }

    // MetadataAnimationHandler.onAnimationEnd: exit end runs the queued data update and
    // starts the enter; enter end flushes a newer queued update (restarts the exit).
    private final AnimatorListenerAdapter mMetadataAnimatorListener =
            new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator anim) {
                    if (anim == mMetadataExit) {
                        Runnable update = mPostExitUpdate;
                        mPostExitUpdate = null;
                        if (update != null) {
                            update.run();
                        }
                        if (mMetadataEnter != null) {
                            mMetadataEnter.start();
                        }
                    } else if (anim == mMetadataEnter) {
                        if (mPostExitUpdate != null) {
                            // A new song arrived while entering: restart the exit.
                            if (mMetadataExit != null) {
                                mMetadataExit.start();
                            }
                        } else {
                            Runnable update = mPostEnterUpdate;
                            mPostEnterUpdate = null;
                            if (update != null) {
                                update.run();
                            }
                        }
                    }
                }
            };

    /**
     * MetadataAnimationHandler.setNext port: when the (title, artist) pair changes,
     * queue the data update and run the exit; the enter follows in the exit's end
     * callback. Returns true when the transition was triggered.
     */
    private boolean transitionMetadata(CharSequence title, CharSequence artist,
            Runnable update) {
        boolean firstBind = mShownTitle == null && mShownArtist == null;
        boolean changed = mShownTitle == null || !mShownTitle.equals(title)
                || mShownArtist == null || !mShownArtist.equals(artist);
        if (!changed) {
            return false;
        }
        mShownTitle = title;
        mShownArtist = artist;
        mPostExitUpdate = update;
        // The FIRST bind (including a card freshly rebound to a swapped session of the
        // same package) applies the text directly: the exit/enter motion is a TRACK
        // CHANGE effect, and playing it on a brand-new card animates from blank in
        // front of the user (MetadataAnimationHandler is only fed on updates).
        if (firstBind) {
            return false;
        }
        if (!isMetadataAnimating() && mMetadataExit != null) {
            mMetadataExit.start();
        }
        return true;
    }

    private boolean isMetadataAnimating() {
        return (mMetadataExit != null && mMetadataExit.isRunning())
                || (mMetadataEnter != null && mMetadataEnter.isRunning());
    }

    /** The media session bound to this card, or null. */
    @Nullable
    public MediaController getBoundController() {
        return mController;
    }

    /**
     * Binds this card to a media session. The source (the shared session monitor) supplies
     * the app icon; pass null to unbind.
     */
    public void setController(@Nullable ScrollableMediaController source,
            @Nullable MediaController controller) {
        if (mController != null && mController != controller) {
            try {
                mController.unregisterCallback(mCallback);
            } catch (IllegalStateException ignored) {
            }
        }
        mSource = source;
        mController = controller;
        if (mController != null) {
            try {
                mController.registerCallback(mCallback);
            } catch (IllegalStateException ignored) {
            }
            bindContent();
            applyPlaybackState(mController.getPlaybackState());
        }
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
        // Song metadata with the SystemUI transition (bindSongMetadata +
        // MetadataAnimationHandler.setNext): when the (title, artist) pair changed, the
        // old text exits (slide + fade) and the new text enters only after the exit
        // ends; otherwise the text is applied directly.
        final CharSequence newTitle = title == null ? "" : title;
        final CharSequence newArtist = artist == null ? "" : artist;
        final Runnable applyText = () -> {
            mTitle.setText(newTitle);
            mArtist.setText(newArtist);
        };
        if (!transitionMetadata(newTitle, newArtist, applyText)) {
            applyText.run();
        }

        bindAppIcon();

        if (artworkBitmap != null) {
            bindArtworkWithCrossfade(artworkBitmap);
        }
        // Rebuild the Monet scheme from the artwork and retint the whole player,
        // exactly like ColorSchemeTransition.updateColorScheme in SystemUI.
        updateColorScheme(artworkBitmap);
    }

    /**
     * Album art binding with the MediaControlPanel.bindArtworkAndColors crossfade: the
     * previous artwork crossfades into the new one via a TransitionDrawable (333ms,
     * the same duration SystemUI uses for artwork-to-artwork transitions), so a track
     * change glides between covers instead of popping. The processed bitmap is only
     * rebuilt when the raw artwork actually changed.
     */
    private void bindArtworkWithCrossfade(Bitmap rawArtwork) {
        if (rawArtwork == mShownRawArtwork) {
            return; // same artwork object: nothing to update
        }
        mShownRawArtwork = rawArtwork;
        Bitmap processed = trimBlackBars(cropToFill(rawArtwork));
        mProcessedForAspect = mAlbumArt.getWidth() == 0 || mAlbumArt.getHeight() == 0
                ? -1f : (float) mAlbumArt.getWidth() / mAlbumArt.getHeight();
        Bitmap prevProcessed = mShownProcessedArtwork;
        mShownProcessedArtwork = processed;
        if (prevProcessed != null && prevProcessed.sameAs(processed)) {
            // Identical art (play/pause re-posts metadata with a fresh bitmap object):
            // keep the current drawable — a crossfade here flashed the white album
            // placeholder through and read as a brightening of the whole card.
            return;
        }
        Drawable newArt = new android.graphics.drawable.BitmapDrawable(
                getResources(), processed);
        Drawable prev = mPrevArtwork;
        if (prev != null && mAlbumArt.getDrawable() != null) {
            TransitionDrawable transition = new TransitionDrawable(
                    new Drawable[] {prev, newArt});
            transition.setCrossFadeEnabled(true);
            // Port of SystemUI scaleTransitionDrawableLayer + setLayerGravity(CENTER):
            // TransitionDrawable does NOT propagate the view bounds to its layers, so
            // without this the layers draw at intrinsic size from (0,0) and the white
            // album background shines through wherever they don't cover. EACH layer is
            // scaled to COVER the art view by its OWN bitmap size — sizing both from the
            // new bitmap left the old layer (different aspect, e.g. the wide frame a
            // video app posted before the song change) short of the edges, and the white
            // placeholder flashed through the crossfade.
            int viewW = mAlbumArt.getWidth();
            int viewH = mAlbumArt.getHeight();
            if (viewW > 0 && viewH > 0) {
                Drawable[] layers = {prev, newArt};
                for (int i = 0; i < layers.length; i++) {
                    int artW = layers[i].getIntrinsicWidth();
                    int artH = layers[i].getIntrinsicHeight();
                    if (artW <= 0 || artH <= 0) {
                        continue;
                    }
                    float scale = Math.max((float) viewW / artW, (float) viewH / artH);
                    transition.setLayerSize(i,
                            Math.round(artW * scale), Math.round(artH * scale));
                    transition.setLayerGravity(i, android.view.Gravity.CENTER);
                }
            }
            mAlbumArt.setImageDrawable(transition);
            transition.startTransition(333);
        } else {
            mAlbumArt.setImageDrawable(newArt);
        }
        mPrevArtwork = newArt;
    }

    @Nullable
    private Bitmap mShownRawArtwork;
    @Nullable
    private Bitmap mShownProcessedArtwork;
    // Aspect ratio the CURRENT processed artwork was cropped for. When the art view is
    // measured later (first bind can run before layout) or re-measured (size change),
    // onSizeChanged reprocesses the raw artwork so the zoom-crop is always correct —
    // the user never sees the wide frame shrink-to-fit and then zoom in.
    private float mProcessedForAspect = -1f;

    /**
     * trebufork: some video apps (YouTube on non-16:9 content) deliver artwork with
     * BLACK BARS BAKED INTO THE BITMAP — letterboxing done by the sending app. No view
     * scaling or aspect crop can remove those (they scale along with the picture). So
     * detect the uniform near-black rows at the bitmap's top/bottom edges and trim them
     * before the fill-crop. Detection is conservative: a row counts as a bar only when
     * the WHOLE row is nearly black (max channel < 24), and at most 25% of the height
     * per edge is ever trimmed so real dark artwork is never cut.
     */
    private static Bitmap trimBlackBars(Bitmap art) {
        if (art == null || art.isRecycled() || art.getHeight() < 16) {
            return art;
        }
        int w = art.getWidth();
        int h = art.getHeight();
        int maxTrim = h / 4;
        int[] row = new int[w];
        int top = 0;
        while (top < maxTrim && isBlackRow(art, row, top, w)) {
            top++;
        }
        int bottom = 0;
        while (bottom < maxTrim && isBlackRow(art, row, h - 1 - bottom, w)) {
            bottom++;
        }
        if (top == 0 && bottom == 0) {
            return art;
        }
        int newH = h - top - bottom;
        if (newH < 16) {
            return art; // would leave nothing: not artwork bars, bail out
        }
        try {
            return Bitmap.createBitmap(art, 0, top, w, newH);
        } catch (IllegalArgumentException e) {
            return art;
        }
    }

    private static boolean isBlackRow(Bitmap art, int[] row, int y, int w) {
        art.getPixels(row, 0, w, 0, y, w, 1);
        for (int x = 0; x < w; x++) {
            int c = row[x];
            int r = (c >> 16) & 0xFF;
            int g = (c >> 8) & 0xFF;
            int b = c & 0xFF;
            int max = Math.max(r, Math.max(g, b));
            if (max > 24) {
                return false;
            }
        }
        return true;
    }

    /**
     * trebufork: source-side center-crop of the artwork to the card's aspect ratio.
     * Video-streaming sessions (YouTube etc.) deliver a WIDE 16:9 frame as their art,
     * while the card is much taller than wide; the shade letterboxes such art in a
     * (dark) placeholder background — the black bars the user reported. Instead of
     * letterboxing we ZOOM: the bitmap is cropped around its center so its aspect ratio
     * matches the art view, and the view's centerCrop then scales it to cover exactly.
     * The extracted Monet colors keep using the full, uncropped artwork.
     */
    private Bitmap cropToFill(Bitmap art) {
        int viewW = mAlbumArt.getWidth();
        int viewH = mAlbumArt.getHeight();
        if (art == null || art.isRecycled() || viewW == 0 || viewH == 0) {
            return art;
        }
        int artW = art.getWidth();
        int artH = art.getHeight();
        float viewAspect = (float) viewW / viewH;
        float artAspect = (float) artW / artH;
        if (Math.abs(viewAspect - artAspect) < 0.02f) {
            return art; // already matching: nothing to crop
        }
        int cropW;
        int cropH;
        if (artAspect > viewAspect) {
            // Art is wider than the view: crop the sides.
            cropH = artH;
            cropW = Math.round(artH * viewAspect);
        } else {
            // Art is taller than the view: crop top/bottom.
            cropW = artW;
            cropH = Math.round(artW / viewAspect);
        }
        cropW = Math.min(cropW, artW);
        cropH = Math.min(cropH, artH);
        int cropX = (artW - cropW) / 2;
        int cropY = (artH - cropH) / 2;
        try {
            return Bitmap.createBitmap(art, cropX, cropY, cropW, cropH);
        } catch (IllegalArgumentException e) {
            return art; // defensive: pathological dimensions
        }
    }

    /**
     * The app icon slot mirrors the SystemUI shade player: the card's OWN app icon comes
     * from its media notification small icon (MediaControlViewBinder normal path — a
     * monochrome drawable tinted with the scheme accent), otherwise the launcher icon of
     * the card's own package shaped through the LauncherIcons factory with the shade
     * player's grayscale filter (resume-player path). The shared controller's active-app
     * icon is NOT used here: every card in the carousel must show its own session's app.
     * Called from every bind (metadata and playback state) so the icon picks up as soon as
     * the notification listener delivers it.
     */
    private void bindAppIcon() {
        Drawable smallIcon = mController == null ? null
                : NotificationListener.getMediaSmallIcon(mController.getPackageName());
        android.util.Log.d("TrebuforkMedia", "bindAppIcon pkg="
                + (mController == null ? "null" : mController.getPackageName())
                + " smallIcon=" + smallIcon);
        if (smallIcon != null) {
            mAppIcon.setImageDrawable(smallIcon);
            // SystemUI normal path: the small icon is tinted with the scheme's
            // accentPrimary (MediaControlViewBinder.bindArtworkAndColor).
            mAppIcon.setColorFilter(mAccentPrimary);
            mAppIcon.setVisibility(VISIBLE);
            mAppIconUsesSmallIcon = true;
        } else if (mController != null) {
            // Fallback: the launcher icon of THIS session's package (never the shared
            // active app's icon, which painted every carousel card with one icon).
            Drawable appIcon = loadLauncherIcon(mController.getPackageName());
            if (appIcon != null) {
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
        } else {
            mAppIcon.setVisibility(GONE);
        }
    }

    /** The launcher icon of the given package, or null. */
    @Nullable
    private Drawable loadLauncherIcon(String packageName) {
        try {
            return getContext().getPackageManager().getApplicationIcon(packageName);
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    /**
     * Applies the monet scheme derived from the artwork to every colored surface. Faithful
     * port of Lineage 23.2 ColorSchemeTransition + MediaColorSchemes: scrim =
     * onSurface (neutral1 t10, 0.65/0.75 alpha); title, artist, seekbar wave/thumb,
     * prev/next and custom action icons = plain white (media_on_background, no runtime
     * tint); play/pause background = primaryFixed (accent1 tone tracking the seed) with
     * an onPrimaryFixed icon (accent1 t10, near-black); tap ripple + app icon filter =
     * primaryFixed.
     */
    private void updateColorScheme(@Nullable Bitmap artwork) {
        // The scheme extraction (WallpaperColors quantization, seed scoring and four HCT
        // tonal palettes — dozens of CAM16 solver runs) is expensive; SystemUI runs exactly
        // this on mBackgroundExecutor in MediaControlPanel and only applies the result on
        // the main thread. Running it synchronously in bind() janks the launcher right
        // when the player appears during a scroll.
        //
        // Note: WallpaperColors.fromBitmap only reads the bitmap, and the scaled-down copy
        // it creates is local to the extraction, so a background pass is safe. The bitmap
        // itself is also being shown via setImageBitmap — it is only read here.
        final Bitmap art = (artwork != null && !artwork.isRecycled())
                ? artwork : null;
        final int requestToken = ++mSchemeRequestToken;
        if (art == null && mColorScheme != null) {
            // No artwork: keep the previous tinting rather than recomputing the fallback.
            return;
        }
        Executors.SINGLE.execute(() -> {
            final MonetColorExtractor scheme = MonetColorExtractor.fromArtwork(art, false);
            post(() -> {
                if (requestToken != mSchemeRequestToken || !isAttachedToWindow()) {
                    return; // a newer bind superseded this one
                }
                applyColorScheme(scheme);
            });
        });
    }

    // ---------------------------------------------------------------------
    // AnimatingColorTransition — Java port of SystemUI's class of the same name
    // (media/controls/ui/animation/ColorSchemeTransition.kt): ArgbEvaluator between the
    // CURRENT color and the new target over 333 ms, so scheme changes animate.
    // ---------------------------------------------------------------------
    private static final class AnimatingColorTransition
            implements android.animation.ValueAnimator.AnimatorUpdateListener {
        private final android.animation.ArgbEvaluator mArgbEvaluator =
                new android.animation.ArgbEvaluator();
        private final android.animation.ValueAnimator mAnimator;
        private final java.util.function.IntConsumer mApplyColor;
        private int mCurrentColor;
        private int mSourceColor;
        private int mTargetColor;

        AnimatingColorTransition(int defaultColor, java.util.function.IntConsumer applyColor) {
            mApplyColor = applyColor;
            mCurrentColor = defaultColor;
            mSourceColor = defaultColor;
            mTargetColor = defaultColor;
            mAnimator = android.animation.ValueAnimator.ofFloat(0f, 1f);
            mAnimator.setDuration(333); // ColorSchemeTransition.buildAnimator duration
            mAnimator.addUpdateListener(this);
        }

        @Override
        public void onAnimationUpdate(android.animation.ValueAnimator animation) {
            mCurrentColor = (int) mArgbEvaluator.evaluate(
                    animation.getAnimatedFraction(), mSourceColor, mTargetColor);
            mApplyColor.accept(mCurrentColor);
        }

        void update(int newTargetColor) {
            if (newTargetColor == mTargetColor) {
                return;
            }
            mSourceColor = mCurrentColor;
            mTargetColor = newTargetColor;
            mAnimator.cancel();
            mAnimator.start();
        }
    }

    // The three color slots of ColorSchemeTransition that our player uses:
    // background = album art letterbox background (onSurface),
    // primary = play/pause container (primaryFixed),
    // onPrimary = play/pause icon (onPrimaryFixed).
    // Created in the constructor AFTER the views are found (the apply lambdas capture
    // them).
    private final AnimatingColorTransition mBackgroundColor;
    private final AnimatingColorTransition mPrimaryColor;
    private final AnimatingColorTransition mOnPrimaryColor;

    /** Main-thread half of {@link #updateColorScheme}: tints every surface. */
    private void applyColorScheme(MonetColorExtractor scheme) {
        mColorScheme = scheme;

        int scrimColor = scheme.getScrim();
        int accent = scheme.getPillBackground();
        mAccentPrimary = accent;
        int pillIcon = scheme.getPillIcon();

        // Port of SystemUI ColorSchemeTransition: every slot is an AnimatingColorTransition
        // that ArgbEvaluator-interpolates from the CURRENT color to the new scheme color
        // over 333 ms — a track change glides between palettes instead of snapping.
        mBackgroundColor.update(scheme.getScrim());
        mPrimaryColor.update(accent);
        mOnPrimaryColor.update(pillIcon);

        // Radial scrim over the album art + album background tint (ColorSchemeTransition
        // backgroundColor: albumView.backgroundTintList = onSurface). The scrim gradient
        // itself is tinted synchronously; the background behind letterboxed art animates.
        applyScrim(scrimColor);

        // Title and artist are plain white (media_on_background) — the shade applies no
        // runtime text tint at all (Lineage removed the textPrimary/textSecondary
        // transitions; the layouts hardcode @color/media_on_background).
        mTitle.setTextColor(white());
        mArtist.setTextColor(white());

        // The app icon slot follows the branch chosen in bindAppIcon: accentPrimary tint
        // for the media small icon (animated via mPrimaryColor), grayscale for the
        // launcher-icon fallback.
        if (mAppIcon.getVisibility() == VISIBLE && !mAppIconUsesSmallIcon) {
            mAppIcon.setColorFilter(GRAYSCALE_FILTER);
        }

        // Small action icons: media_player_action_color = plain white (the shade never
        // retints the transparent buttons from the scheme).
        int white = white();
        mPrev.setImageTintList(android.content.res.ColorStateList.valueOf(white));
        mNext.setImageTintList(android.content.res.ColorStateList.valueOf(white));
        for (ImageButton button : mCustomActions) {
            button.setImageTintList(android.content.res.ColorStateList.valueOf(white));
        }

        // Play/pause + ripple colors are animated by the transitions above (update()
        // already applied the target colors this frame — the animator only glides
        // intermediate frames); no duplicated application here.

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
        android.util.Log.d("TrebuforkMedia", "playRipple btn=" + button.getId()
                + " attached=" + mRippleView.isAttachedToWindow()
                + " size=" + mRippleView.getWidth() + "x" + mRippleView.getHeight());
        mRippleView.playRipple(
                button.getX() + button.getWidth() / 2f,
                button.getY() + button.getHeight() / 2f);
    }

    private int white() {
        return getResources().getColor(R.color.scrollable_media_on_background);
    }

    /**
     * Radial scrim over the album art (Lineage 23.2 addGradientToPlayerAlbum: qs_media_scrim
     * with the onSurface color at trebufork's lightened 0.45 / 0.60 alphas).
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
        // The small icon may arrive after the first bind (the notification listener
        // connects asynchronously), so re-check it on every playback state update too.
        bindAppIcon();
        // The squiggly wave animates while playing and flattens when paused,
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

    // Trebufork port of SystemUI's AnimationBindHandler for the play/pause morphs: while
    // a tap-triggered AVD morph is running, session-state rebinds are QUEUED and only
    // applied when the morph ends — otherwise setImageResource would cut the animation
    // mid-flight (bindButtonCommon starts the AVDs only on click, never on rebind).

    @Nullable
    private Runnable mQueuedPlayPauseRebind;
    private final Animatable2.AnimationCallback mMorphEndCallback =
            new Animatable2.AnimationCallback() {
                @Override
                public void onAnimationEnd(Drawable drawable) {
                    Runnable rebind = mQueuedPlayPauseRebind;
                    if (rebind != null && !isPlayPauseMorphRunning()) {
                        mQueuedPlayPauseRebind = null;
                        rebind.run();
                    }
                }
            };

    /** True while the play/pause icon or container AVD morph is running. */
    private boolean isPlayPauseMorphRunning() {
        return isRunning(mPlayPause.getDrawable()) || isRunning(mPlayPause.getBackground());
    }

    private static boolean isRunning(@Nullable Drawable drawable) {
        return drawable instanceof Animatable && ((Animatable) drawable).isRunning();
    }

    private void registerMorphEndCallback(@Nullable Drawable drawable) {
        if (drawable instanceof Animatable2) {
            // registerAnimationCallback adds — clear first to avoid stacking duplicate
            // callbacks across rebinds.
            ((Animatable2) drawable).clearAnimationCallbacks();
            ((Animatable2) drawable).registerAnimationCallback(mMorphEndCallback);
        }
    }

    /**
     * Shows the play/pause icon and container with the Lineage 23.2 (Android 16) morphs,
     * exactly like MediaActions.getStandardAction + bindButtonCommon + AnimationBindHandler:
     * <ul>
     * <li>playing → pause icon AVD + pause button container (pill blob), resting pose;</li>
     * <li>paused → play icon AVD + play button container (rounded rectangle), resting pose;</li>
     * <li>the AVDs are started ONLY by the click listener (the tap plays the 333ms morph
     * toward the next state); state-driven rebinds never start them;</li>
     * <li>rebinds arriving while a morph is running are delayed until it ends, so the tap
     * animation is never cut off mid-flight.</li>
     * </ul>
     */
    private void applyPlayPauseIcon(boolean wasPlaying) {
        final int iconRes = mIsPlaying
                ? R.drawable.scrollable_media_ic_pause_button
                : R.drawable.scrollable_media_ic_play_button;
        final int bgRes = mIsPlaying
                ? R.drawable.scrollable_media_ic_pause_button_container
                : R.drawable.scrollable_media_ic_play_button_container;
        Runnable rebind = () -> {
            mPlayPause.setImageResource(iconRes);
            mPlayPause.setBackgroundResource(bgRes);
            tintPlayPauseBackground();
            // Track the morph end so a queued rebind fires exactly when the AVD finishes
            // (AnimationBindHandler registers itself as an Animatable2 callback).
            registerMorphEndCallback(mPlayPause.getDrawable());
            registerMorphEndCallback(mPlayPause.getBackground());
        };
        if (mPlayPauseShown && isPlayPauseMorphRunning()) {
            // A tap morph is in flight: defer the bind until it completes, exactly like
            // AnimationBindHandler.tryExecute.
            mQueuedPlayPauseRebind = rebind;
            return;
        }
        mQueuedPlayPauseRebind = null;
        // First bind and state changes land on the static resting poses; the morph only
        // plays on tap (bindButtonCommon never starts the AVDs during a rebind).
        rebind.run();
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
                // The framework CustomAction.getIcon() returns a resource id in
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
        // Port of SystemUI MediaCarouselController.updateSeekbarListening(visibleToUser):
        // the progress only ticks when the card is attached to the window AND visible to
        // the user (the carousel page the user is actually looking at). Hidden cards stop
        // extrapolating position — no wasted work and no drift from stale updates.
        if (mIsPlaying && isAttachedToWindow() && mVisibleToUser) {
            postDelayed(mProgressTick, PROGRESS_TICK_MS);
        }
    }

    /** Whether this card is the carousel page the user is looking at. */
    private boolean mVisibleToUser = true;

    /**
     * Port of MediaCarouselController.updateSeekbarListening: toggles the progress
     * extrapolation on the visible card. Called by the row when the page set changes.
     */
    void setVisibleToUser(boolean visible) {
        if (mVisibleToUser == visible) {
            return;
        }
        mVisibleToUser = visible;
        if (visible) {
            updateProgressUi(); // resync from the last state + extrapolation anchor
        }
        scheduleProgressTick();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w == 0 || h == 0) {
            return;
        }
        // The art view got its real size (first layout or a resize). If the shown artwork
        // was processed with a different aspect (or not processed at all — bound before
        // layout), reprocess NOW so the zoom-crop is applied before this frame is drawn:
        // the wide video frame never appears letterboxed and "zooming in" during a swipe.
        float aspect = (float) w / h;
        if (mShownRawArtwork != null && aspect != mProcessedForAspect) {
            reprocessShownArtwork();
        }
    }

    /** Re-crops the current raw artwork for the current view size, without crossfade. */
    private void reprocessShownArtwork() {
        Bitmap raw = mShownRawArtwork;
        if (raw == null || raw.isRecycled()) {
            return;
        }
        Bitmap processed = trimBlackBars(cropToFill(raw));
        if (mShownProcessedArtwork != null && mShownProcessedArtwork.sameAs(processed)) {
            mProcessedForAspect = (float) mAlbumArt.getWidth() / mAlbumArt.getHeight();
            return; // same pixels: keep the drawable, avoid a needless swap
        }
        mShownProcessedArtwork = processed;
        mProcessedForAspect = (float) mAlbumArt.getWidth() / mAlbumArt.getHeight();
        // Silent swap: no crossfade — this is a geometry fix-up, not a track change.
        mAlbumArt.setImageDrawable(
                new android.graphics.drawable.BitmapDrawable(getResources(), processed));
        mPrevArtwork = mAlbumArt.getDrawable();
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
        // The media small icon may arrive after the first bind (the notification listener
        // connects asynchronously) — re-check the icon slot whenever the icon map changes.
        NotificationListener.addMediaSmallIconListener(mSmallIconListener);
    }

    private final NotificationListener.MediaSmallIconListener mSmallIconListener =
            () -> post(() -> {
                if (mController != null) {
                    bindAppIcon();
                }
            });

    @Override
    protected void onDetachedFromWindow() {
        NotificationListener.removeMediaSmallIconListener(mSmallIconListener);
        setVisibleToUser(true); // rearm so a re-attach resumes ticking
        super.onDetachedFromWindow();
        removeCallbacks(mProgressTick);
        if (mController != null) {
            try {
                mController.unregisterCallback(mCallback);
            } catch (IllegalStateException ignored) {
            }
        }
    }

    /**
     * Opens the player app for this card with the widget-style RemoteAnimation: the
     * card implements UpdateDeferrableView, which QuickstepTransitionManager treats as a
     * widget launch source, so tapping plays the same stretch-from-view transition the
     * system widget plays (FloatingWidgetView ghosts the card and morphs it into the
     * opening app window). The launch options also carry the sender-side
     * background-activity-start allowance Android 12+ requires when the PendingIntent's
     * creator is a background media app. Falls back to the app's launch intent when the
     * notification carries none.
     *
     * trebufork: the PendingIntent.send path bypasses startActivitySafely (and its defer
     * guard), so a tap landing while the previous close-to-icon spring's recents transition
     * is still finishing gets merged into it by WM Shell and the open animation is skipped
     * (the "relaunch interrupt" bug). Route the send through the launcher's
     * run-when-transition-idle wrapper: it force-finishes the running transition first and
     * re-issues the send, exactly like the deferred icon-launch path.
     */
    private void openPlayerApp() {
        if (mController == null) {
            return;
        }
        String pkg = mController.getPackageName();
        android.app.PendingIntent clickIntent =
                NotificationListener.getMediaClickIntent(pkg);
        Context context = getContext();
        if (clickIntent != null && context instanceof Launcher) {
            Launcher launcher = (Launcher) context;
            ActivityOptionsWrapper options = launcher.getActivityLaunchOptions(this, null);
            options.options.setPendingIntentLaunchFlags(
                    android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            launcher.runWhenLaunchTransitionIdle(() -> {
                try {
                    clickIntent.send(context, 0, null, null, null, null, options.toBundle());
                } catch (android.app.PendingIntent.CanceledException e) {
                    // fall through to the package launch intent
                }
            });
            return;
        }
        android.content.Intent launch =
                context.getPackageManager().getLaunchIntentForPackage(pkg);
        if (launch != null && context instanceof Launcher) {
            ((Launcher) context).startActivitySafely(this, launch, null);
        } else if (launch != null) {
            context.startActivity(launch);
        }
    }

    // UpdateDeferrableView: the widget-style launch animation (FloatingWidgetView) defers
    // view updates while the card is ghosted into the floating overlay. The card has no
    // remote-updates pipeline, so this is a no-op stub.
    @Override
    public void setUpdatesDeferred(boolean isDeferred) {
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
