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

import static android.view.HapticFeedbackConstants.CLOCK_TICK;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.android.launcher3.util.Themes;

/**
 * Index rendered along the right edge of the {@link ScrollableAppsView}. The star at the very
 * top exits to the desktop; tapping or dragging over a letter opens the apps list and scrolls it
 * to the first app starting with that letter. Cyrillic first letters are transliterated to Latin
 * equivalents (see {@link ScrollableAppsView#getSectionLetter}).
 */
public class ScrollableAppsSidebar extends View {

    // The star comes first and exits to the desktop; then '#' (digits/symbols/hieroglyphs at the
    // top of the apps list), then the Latin alphabet.
    public static final String LETTERS = "★#ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    // Index of the star (exit to desktop) at the very top of the sidebar.
    public static final int STAR_INDEX = 0;

    // The wave spans the complete alphabet; only the touch target remains limited to the edge.
    // Right-alphabet touch strip width in dp, user-configurable via pref_sidebar_touch_width.
    // Reduced by a third from the original 64dp.
    private static final float DEFAULT_TOUCH_TARGET_WIDTH_DP = 64f * 2f / 3f;
    // These values mirror the right-aligned branch of the referenced Flutter implementation.
    private static final float ARTICLE_FIXED_OFFSET_DP = 60f;
    private static final float ARTICLE_MAX_OFFSET_FRACTION = 0.30f;
    private static final float ARTICLE_OFFSET_CLAMP_DP = 100f;
    private static final float ARTICLE_DIVISOR_BASE_DP = 12f;
    private static final float ARTICLE_OFFSET_CLAMP_LIMIT_DP = 85f;
    private static final long LETTER_SELECTION_DURATION_MS = 90L;
    private static final long WAVE_RELEASE_DURATION_MS = 180L;
    // Formation uses the same duration as the release collapse so the semicircle grows in
    // with the same smooth motion it uses when the finger is lifted.
    private static final long WAVE_FORMATION_DURATION_MS = 180L;
    // Smallest allowed block height fraction so the alphabet never collapses to a single point.
    private static final float MIN_BLOCK_HEIGHT_FRACTION = 0.05f;
    // trebufork: the letter block never starts above this fraction of the screen, so the top
    // letters stay in thumb range and line up with the list content (which starts at the same
    // inset). Default 20%; configurable via pref_scrollable_top_inset (percent).
    private static final float TOP_INSET_FRACTION_DEFAULT = 0.20f;

    private final Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mDimTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect mTmpRect = new Rect();

    @Nullable
    private ScrollableAppsView mAppsView;

    // trebufork: user-configurable layout, see applyConfig().
    // Vertical position as a fraction of the travel range [0..1] (0.5 = centered),
    // top/bottom inset in px and right-edge margin in px.
    private float mPositionFraction = 0.5f;
    private float mInsetPx;
    private float mMarginEndPx;
    private float mLeftInsetPx;
    private float mTouchTargetWidthPx;
    private float mHeightFraction = 1f;
    // trebufork: minimum fraction of the screen the letter block must stay below at the top,
    // matching the list content inset (pref_scrollable_top_inset).
    private float mTopInsetFraction = TOP_INSET_FRACTION_DEFAULT;
    // True while the current gesture started in the left-hand swipe strip (left of
    // mLeftInsetPx). The wave then bulges at the alphabet instead of following the finger.
    private boolean mLeftHandTouch;
    // Current and initial horizontal drag positions, matching _dragPosition and
    // _startDragPosition in the Flutter implementation.
    private float mStartTouchX;

    private int mPressedIndex = -1;
    private float mAnimatedPressedIndex = -1f;
    private float mTouchX;
    private float mTouchY;
    private float mWaveProgress;
    private ValueAnimator mSelectionAnimator;
    private ValueAnimator mReleaseAnimator;
    private ValueAnimator mFormationAnimator;

    public ScrollableAppsSidebar(Context context) {
        this(context, null);
    }

    public ScrollableAppsSidebar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollableAppsSidebar(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        float density = getResources().getDisplayMetrics().density;
        int textColor = Themes.getAttrColor(context, android.R.attr.textColorPrimary);

        mTextPaint.setColor(textColor);
        mTextPaint.setTextSize(14 * density);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        // trebufork: light drop shadow so the alphabet letters stay readable over wallpaper.
        mTextPaint.setShadowLayer(3f, 0f, 1f, 0x66000000);

        mDimTextPaint.setColor(textColor);
        mDimTextPaint.setAlpha(70);
        mDimTextPaint.setTextSize(14 * density);
        mDimTextPaint.setTextAlign(Paint.Align.CENTER);
        mDimTextPaint.setShadowLayer(3f, 0f, 1f, 0x66000000);

        mHighlightPaint.setColor(Themes.getAttrColor(context, android.R.attr.colorAccent));
        mHighlightPaint.setAlpha(40);

        applyConfig();
    }

    public void setAppsView(ScrollableAppsView appsView) {
        mAppsView = appsView;
        applyLeftInset();
    }

    /**
     * trebufork: shifts the app list right by the left inset so the empty strip on the left
     * remains free for left-hand alphabet swipes (see SIDEBAR_LEFT_INSET).
     */
    private void applyLeftInset() {
        if (mAppsView == null) {
            return;
        }
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mAppsView.getLayoutParams();
        if (lp != null) {
            lp.leftMargin = Math.round(mLeftInsetPx);
            mAppsView.setLayoutParams(lp);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateAppsRowEndMargin();
    }

    /**
     * trebufork: pushes the row tap-target right margin to the app list so it stops exactly
     * where the alphabet touch strip begins. Both views share the DragLayer as parent, so their
     * {@code getX()} values are in a common coordinate space.
     */
    private void updateAppsRowEndMargin() {
        if (mAppsView == null || mAppsView.getWidth() <= 0) {
            return;
        }
        float stripStart = getBaseCenterX() - mTouchTargetWidthPx + getX();
        float appRight = mAppsView.getX() + mAppsView.getWidth();
        mAppsView.setRowEndMarginPx(Math.max(0, Math.round(appRight - stripStart)));
    }

    /**
     * trebufork: read the user preferences (position, edge inset, right margin) and
     * re-apply the layout. Invoked from the constructor and whenever any of the three
     * scrollable-home sidebar settings change (see Launcher.mScrollableHomePrefListener).
     */
    public void applyConfig() {
        float density = getResources().getDisplayMetrics().density;
        Context context = getContext();
        mPositionFraction = Math.max(0f, Math.min(1f,
                readFloatPref(context, LauncherPrefs.SIDEBAR_POSITION.getSharedPrefKey(), 100f)
                        / 100f));
        mInsetPx = readFloatPref(context,
                LauncherPrefs.SIDEBAR_EDGE_INSET.getSharedPrefKey(), 50f) * density;
        mMarginEndPx = readFloatPref(context,
                LauncherPrefs.SIDEBAR_MARGIN_END.getSharedPrefKey(), 10f) * density;
        mLeftInsetPx = readFloatPref(context,
                LauncherPrefs.SIDEBAR_LEFT_INSET.getSharedPrefKey(), 32f) * density;
        mTouchTargetWidthPx = readFloatPref(context,
                LauncherPrefs.SIDEBAR_TOUCH_WIDTH.getSharedPrefKey(), DEFAULT_TOUCH_TARGET_WIDTH_DP)
                * density;
        mHeightFraction = Math.max(MIN_BLOCK_HEIGHT_FRACTION, Math.min(1f,
                readFloatPref(context, LauncherPrefs.SIDEBAR_HEIGHT.getSharedPrefKey(), 75f)
                        / 100f));
        mTopInsetFraction = Math.max(ScrollableAppsView.MIN_TOP_INSET_FRACTION,
                Math.min(0.5f,
                        readFloatPref(context,
                                LauncherPrefs.SCROLLABLE_TOP_INSET.getSharedPrefKey(), 20f)
                                / 100f));
        // The full-width view is anchored to the end of the DragLayer. Keeping the view wide lets
        // the wave arc move left of the edge without being clipped by the old wrap_content bounds.
        setTranslationX(-mMarginEndPx);
        applyLeftInset();
        invalidate();
    }

    /**
     * Reads a float sidebar preference, migrating a legacy integer value (stored by the old
     * SeekBarPreference) to a float in place so the launcher does not crash on upgrade.
     */
    private static float readFloatPref(Context context, String key, float defaultValue) {
        SharedPreferences prefs = LauncherPrefs.getPrefs(context);
        Object value = prefs.getAll().get(key);
        if (value instanceof Number) {
            float result = ((Number) value).floatValue();
            if (!(value instanceof Float)) {
                prefs.edit().putFloat(key, result).apply();
            }
            return result;
        }
        return defaultValue;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float density = getResources().getDisplayMetrics().density;
        float textWidth = mTextPaint.measureText("A");
        int desiredWidth = Math.round(textWidth + getPaddingLeft() + getPaddingRight()
                + 8 * density);
        int width = resolveSize(desiredWidth, widthMeasureSpec);
        int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        int count = LETTERS.length();
        float top = getBlockTop();
        float bottom = getBlockBottom();
        float slot = (bottom - top) / count;
        Paint.FontMetrics fm = mTextPaint.getFontMetrics();
        float centerOffset = (fm.descent - fm.ascent) / 2f + fm.ascent;

        // trebufork: the selection highlight follows the animated letter index (so it glides
        // smoothly between letters while dragging) and its alpha is tied to the wave progress, so
        // it fades in with the formation animation and fades out smoothly on release instead of
        // snapping away.
        float animatedIndex = mAnimatedPressedIndex >= 0f ? mAnimatedPressedIndex : mPressedIndex;
        boolean drawHighlight = mPressedIndex >= 0 && mWaveProgress > 0f;
        float highlightHalfWidth = Math.max(
                12 * getResources().getDisplayMetrics().density,
                mTextPaint.measureText("A"));

        for (int i = 0; i < count; i++) {
            float centerY = top + i * slot + slot / 2f;
            float centerX = getLetterCenterX(i, slot);

            char letter = LETTERS.charAt(i);
            // While the apps list has not loaded yet (early boot), every letter is drawn in the
            // normal color; dimming only applies once the apps are known.
            boolean exists = i == STAR_INDEX
                    || mAppsView == null
                    || !mAppsView.hasAppsData()
                    || mAppsView.getFirstPositionForLetter(letter)
                            != RecyclerView.NO_POSITION;
            Paint paint = exists ? mTextPaint : mDimTextPaint;
            canvas.drawText(String.valueOf(letter), centerX, centerY - centerOffset, paint);
        }

        // Drawn last so the highlight sits on top of the letters.
        if (drawHighlight) {
            int slotIndex = Math.round(animatedIndex);
            if (slotIndex < 0) {
                slotIndex = 0;
            } else if (slotIndex >= count) {
                slotIndex = count - 1;
            }
            float letterCenterX = getLetterCenterX(slotIndex, slot);
            float widgetCenterY = top + animatedIndex * slot + slot / 2f;
            mTmpRect.set(Math.round(letterCenterX - highlightHalfWidth),
                    Math.round(widgetCenterY - slot / 2f),
                    Math.round(letterCenterX + highlightHalfWidth),
                    Math.round(widgetCenterY + slot / 2f));
            mHighlightPaint.setAlpha(Math.round(40 * mWaveProgress));
            canvas.drawRoundRect(mTmpRect.left, mTmpRect.top, mTmpRect.right, mTmpRect.bottom,
                    slot / 2f, slot / 2f, mHighlightPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // The sidebar is full-width so its left-facing arc can be drawn without clipping,
                // but only the right edge strip and the configured left strip are activation
                // targets. Touches elsewhere on the app list pass through to RecyclerView.
                boolean inLeftStrip = mLeftInsetPx > 0f && event.getX() < mLeftInsetPx;
                boolean inRightStrip = event.getX() >= getBaseCenterX() - mTouchTargetWidthPx;
                if (!inLeftStrip && !inRightStrip) {
                    return false;
                }
                mLeftHandTouch = inLeftStrip;
                // trebufork: the alphabet strip owns its gesture. Prevent the DragLayer's
                // StatusBarTouchController from stealing a downward swipe (which would open the
                // notification shade / Quick Settings when the list is at the top) so the swipe
                // scrolls the alphabet instead.
                getParent().requestDisallowInterceptTouchEvent(true);
                cancelReleaseAnimation();
                cancelSelectionAnimation();
                mAnimatedPressedIndex = -1f;
                startFormationAnimation();
                mStartTouchX = event.getX();
                mTouchX = event.getX();
                mTouchY = event.getY();
                updatePressedIndex();
                if (mAppsView != null) {
                    mAppsView.stopScroll();
                }
                return true;

            case MotionEvent.ACTION_MOVE:
                mTouchX = event.getX();
                mTouchY = event.getY();
                updatePressedIndex();
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                if (mAppsView != null) {
                    mAppsView.setAlphabetDragLetter('\0', false);
                }
                // Keep the original start position until the release animation finishes; the
                // article's AnimatedContainer also animates from the active drag geometry.
                startReleaseAnimation();
                return true;

            default:
                return true;
        }
    }

    private void updatePressedIndex() {
        int index = getIndexForY(mTouchY);
        if (index != mPressedIndex) {
            if (mPressedIndex == -1) {
                mAnimatedPressedIndex = index;
            } else {
                animateSelectionTo(index);
            }
            mPressedIndex = index;
            performHapticFeedback(CLOCK_TICK);
            if (mAppsView != null) {
                char letter = LETTERS.charAt(index);
                if (index == STAR_INDEX) {
                    // Star: exit to the desktop. The apps list is only reachable via letters.
                    mAppsView.showDesktop();
                    mAppsView.setAlphabetDragLetter('\0', false);
                } else {
                    // Letter: the only way into the apps list — switch there and scroll to it.
                    mAppsView.showApps();
                    mAppsView.scrollToLetter(letter);
                    mAppsView.setAlphabetDragLetter(letter, true);
                }
            }
        }
        invalidate();
    }

    private void animateSelectionTo(int targetIndex) {
        cancelSelectionAnimation();
        float startIndex = mAnimatedPressedIndex >= 0f
                ? mAnimatedPressedIndex : mPressedIndex;
        mSelectionAnimator = ValueAnimator.ofFloat(startIndex, targetIndex);
        mSelectionAnimator.setDuration(LETTER_SELECTION_DURATION_MS);
        mSelectionAnimator.setInterpolator(new DecelerateInterpolator(1.5f));
        mSelectionAnimator.addUpdateListener(animation -> {
            mAnimatedPressedIndex = (float) animation.getAnimatedValue();
            invalidate();
        });
        mSelectionAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mSelectionAnimator == animation) {
                    mSelectionAnimator = null;
                    mAnimatedPressedIndex = targetIndex;
                    invalidate();
                }
            }
        });
        mSelectionAnimator.start();
    }

    private void cancelSelectionAnimation() {
        if (mSelectionAnimator != null) {
            // Clear the field first so cancel() cannot run the completion callback and snap the
            // curve to the previous target while a new letter is being selected.
            ValueAnimator animator = mSelectionAnimator;
            mSelectionAnimator = null;
            animator.cancel();
        }
    }

    private void startReleaseAnimation() {
        cancelSelectionAnimation();
        cancelFormationAnimation();
        if (mPressedIndex >= 0) {
            mAnimatedPressedIndex = mPressedIndex;
        }
        cancelReleaseAnimation();
        if (mPressedIndex == -1) {
            return;
        }
        mReleaseAnimator = ValueAnimator.ofFloat(mWaveProgress, 0f);
        mReleaseAnimator.setDuration(WAVE_RELEASE_DURATION_MS);
        mReleaseAnimator.setInterpolator(new DecelerateInterpolator());
        mReleaseAnimator.addUpdateListener(animation -> {
            mWaveProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        mReleaseAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mReleaseAnimator == animation) {
                    mReleaseAnimator = null;
                    mPressedIndex = -1;
                    mAnimatedPressedIndex = -1f;
                    mStartTouchX = 0f;
                    invalidate();
                }
            }
        });
        mReleaseAnimator.start();
    }

    private void cancelReleaseAnimation() {
        if (mReleaseAnimator != null) {
            mReleaseAnimator.cancel();
            mReleaseAnimator = null;
        }
    }

    /**
     * Animates the letter semicircle growing in around the pressed letter. This is the inverse of
     * the release collapse: {@code mWaveProgress} ramps from its current value to {@code 1f}, so
     * the letters (including the non-selected ones) slide smoothly into place instead of snapping.
     */
    private void startFormationAnimation() {
        cancelFormationAnimation();
        mFormationAnimator = ValueAnimator.ofFloat(mWaveProgress, 1f);
        mFormationAnimator.setDuration(WAVE_FORMATION_DURATION_MS);
        mFormationAnimator.setInterpolator(new DecelerateInterpolator());
        mFormationAnimator.addUpdateListener(animation -> {
            mWaveProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        mFormationAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mFormationAnimator == animation) {
                    mFormationAnimator = null;
                    mWaveProgress = 1f;
                    invalidate();
                }
            }
        });
        mFormationAnimator.start();
    }

    private void cancelFormationAnimation() {
        if (mFormationAnimator != null) {
            // Clear the field first so cancel() cannot run the completion callback and snap the
            // wave back to a stale value while a new animation is starting.
            ValueAnimator animator = mFormationAnimator;
            mFormationAnimator = null;
            animator.cancel();
        }
    }

    /** Returns the horizontal center of the resting alphabet at the configured edge margin. */
    private float getBaseCenterX() {
        float density = getResources().getDisplayMetrics().density;
        return getWidth() - getPaddingRight() - mTextPaint.measureText("A") / 2f
                - 10 * density;
    }

    /**
     * Returns a letter's horizontal position using the right-aligned branch of the reference
     * article. The selected letter gets the largest negative translation and the other letters
     * follow the same Gaussian bell curve as the Flutter implementation.
     */
    private float getLetterCenterX(int index, float slot) {
        float baseX = getBaseCenterX();
        if (mPressedIndex == -1 || mWaveProgress <= 0f) {
            return baseX;
        }

        float density = getResources().getDisplayMetrics().density;
        // The article works in Flutter logical pixels. Convert the Android view coordinates to dp
        // before applying its constants so the same curve is produced on every device density.
        float screenWidthDp = getWidth() / density;
        // A left-hand swipe should look like the finger is resting right on the alphabet, so
        // the wave bulges at the edge instead of stretching across the screen to the finger.
        float dragXDp = (mLeftHandTouch ? getBaseCenterX() : mTouchX) / density;
        boolean startedFromSecondHalf = mLeftHandTouch || mStartTouchX > getWidth() / 2f;

        float maxOffsetDp;
        if (!startedFromSecondHalf) {
            maxOffsetDp = screenWidthDp * ARTICLE_MAX_OFFSET_FRACTION;
        } else {
            maxOffsetDp = clamp(screenWidthDp - dragXDp + ARTICLE_FIXED_OFFSET_DP,
                    0f, screenWidthDp - ARTICLE_OFFSET_CLAMP_DP);
        }

        float divisor = Math.max(1f,
                (screenWidthDp - dragXDp) / 4f + ARTICLE_DIVISOR_BASE_DP);
        float selectedIndex = mAnimatedPressedIndex >= 0f
                ? mAnimatedPressedIndex : mPressedIndex;
        float distance = Math.abs(index - selectedIndex);
        float gaussian = (float) Math.exp(-(distance * distance) / divisor);
        float offsetDp = clamp(maxOffsetDp * gaussian,
                0f, screenWidthDp - ARTICLE_OFFSET_CLAMP_LIMIT_DP);
        float centerX = baseX - offsetDp * density * mWaveProgress;
        return clampToVisibleBounds(centerX);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private float clampToVisibleBounds(float centerX) {
        float halfTextWidth = mTextPaint.measureText("A") / 2f;
        // Coordinates are local to a view translated left by mMarginEndPx. Account for that
        // translation when enforcing the physical screen's left bound; the view's right edge
        // already includes the configured margin and therefore remains the safe right bound.
        float minX = getMinVisibleCenterX();
        float maxX = getWidth() - getPaddingRight() - halfTextWidth;
        return Math.max(minX, Math.min(maxX, centerX));
    }

    private float getMinVisibleCenterX() {
        float halfTextWidth = mTextPaint.measureText("A") / 2f;
        return Math.max(getPaddingLeft() + halfTextWidth,
                mMarginEndPx + halfTextWidth);
    }

    /**
     * trebufork: vertical center of the letter block. The block slides between the top and
     * bottom insets by the {@code position} fraction; the travel shrinks as the block height
     * grows, so a compressed alphabet can still be moved all the way to the requested edge.
     */
    private float getBlockCenterY() {
        float travel = Math.max(0f, getHeight() - 2 * mInsetPx - getBlockSpan());
        float top = mInsetPx + travel * mPositionFraction;
        return top + getBlockSpan() / 2f;
    }

    /** trebufork: vertical span of the letter block, compressed by the height setting. */
    private float getBlockSpan() {
        return Math.max(0f, (getHeight() - 2 * mInsetPx) * mHeightFraction);
    }

    /**
     * trebufork: top of the letter block, computed from the inset, position and height. Clamped
     * so the block never starts above the 30% top inset (thumb range) and never ends above the
     * configured bottom inset. When the block is too tall to respect both insets, the configured
     * position wins.
     */
    private float getBlockTop() {
        float rawTop = getBlockCenterY() - getBlockSpan() / 2f;
        float minTop = getHeight() * mTopInsetFraction;
        float maxTop = getHeight() - mInsetPx - getBlockSpan();
        if (minTop > maxTop) {
            return rawTop;
        }
        return Math.max(minTop, Math.min(rawTop, maxTop));
    }

    /** trebufork: bottom of the letter block, keeping the block span unchanged. */
    private float getBlockBottom() {
        return getBlockTop() + getBlockSpan();
    }

    private int getIndexForY(float y) {
        int count = LETTERS.length();
        float top = getBlockTop();
        float bottom = getBlockBottom();
        float slot = (bottom - top) / count;
        if (slot <= 0f) {
            return 0;
        }
        int index = (int) ((y - top) / slot);
        if (index < 0) {
            index = 0;
        } else if (index >= count) {
            index = count - 1;
        }
        return index;
    }
}