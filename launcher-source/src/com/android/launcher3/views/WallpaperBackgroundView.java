/*
 * Copyright (C) 2024 The Trebufork Project
 *
 * Self-contained wallpaper background view. Renders the wallpaper bitmap
 * directly inside the launcher, eliminating dependence on WM's wallpaper
 * surface (which can race and leave a black screen during transitions).
 */
package com.android.launcher3.views;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * Renders the device wallpaper as a simple background behind the launcher.
 * This replaces WM's wallpaper surface entirely for the launcher window.
 *
 * Benefits:
 * - No race conditions with WM during fast transitions
 * - No black screen when WM loses the wallpaper leash
 * - Consistent rendering across all launcher states
 *
 * The zoom (depth) effect is handled by {@link #setZoom(float)} which
 * scales the wallpaper similar to how WM's wallpaperZoomOut works.
 */
public class WallpaperBackgroundView extends View {

    private static final String TAG = "WallpaperBg";

    private final WallpaperManager mWallpaperManager;
    private final Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private final Matrix mMatrix = new Matrix();

    private @Nullable Bitmap mWallpaperBitmap;
    private float mZoom = 0f;      // 0 = fully zoomed out (normal), 1 = fully zoomed in
    private float mOffsetX = 0f;   // horizontal offset for parallax [-1..1]
    private float mOffsetY = 0f;   // vertical offset [-1..1]

    // For parallax: the wallpaper bitmap is wider than the screen
    private int mScreenW, mScreenH;
    private int mBitmapW, mBitmapH;

    private boolean mAttached;

    private final BroadcastReceiver mWallpaperChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadWallpaper();
        }
    };

    public WallpaperBackgroundView(Context context) {
        this(context, null);
    }

    public WallpaperBackgroundView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WallpaperBackgroundView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mWallpaperManager = context.getSystemService(WallpaperManager.class);
        setWillNotDraw(false);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mAttached = true;
        // Load synchronously on the first frame so the wallpaper is visible
        // immediately after a (re)start — no black screen, no abrupt pop-in.
        loadWallpaperSynchronous();
        IntentFilter filter = new IntentFilter(Intent.ACTION_WALLPAPER_CHANGED);
        getContext().registerReceiver(mWallpaperChangedReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAttached = false;
        try {
            getContext().unregisterReceiver(mWallpaperChangedReceiver);
        } catch (Exception ignored) {
        }
    }

    /**
     * Load the wallpaper bitmap synchronously on the calling (UI) thread.
     * WallpaperManager.getDrawable() is a cheap binder call returning the
     * cached drawable, so this is fine to run at attach time.
     */
    private void loadWallpaperSynchronous() {
        try {
            applyWallpaper(mWallpaperManager.getDrawable(), "Loaded wallpaper: ");
        } catch (Exception e) {
            Log.e(TAG, "Error loading wallpaper synchronously", e);
        }
        if (mWallpaperBitmap == null) {
            // At boot the wallpaper file (or the READ_MEDIA_IMAGES grant from
            // the module's service.sh) may not be ready yet. Poll instead of
            // leaving a black background.
            retryLoadWallpaper();
        }
    }

    /**
     * Poll for the wallpaper after boot until it becomes available
     * (~2 minutes max, then gives up). This covers the window between the
     * launcher activity attaching and the module's service.sh granting
     * READ_MEDIA_IMAGES.
     */
    private void retryLoadWallpaper() {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.postDelayed(new Runnable() {
            int attempts = 0;

            @Override
            public void run() {
                // Stop once loaded, once detached, or after ~2 minutes.
                if (mWallpaperBitmap != null || !mAttached) return;
                if (++attempts > 120) return;
                try {
                    if (applyWallpaper(mWallpaperManager.getDrawable(), "Loaded wallpaper (retry): ")) {
                        return;
                    }
                } catch (Exception ignored) {
                }
                mainHandler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    /**
     * Load the wallpaper bitmap from WallpaperManager.
     * Done on a worker thread to avoid blocking the UI (wallpaper picker path).
     */
    private void loadWallpaper() {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        new Thread(() -> {
            try {
                Drawable wallpaperDrawable = mWallpaperManager.getDrawable();
                mainHandler.post(() -> applyWallpaper(wallpaperDrawable, "Loaded wallpaper: "));
            } catch (Exception e) {
                Log.e(TAG, "Error loading wallpaper", e);
            }
        }).start();
    }

    /**
     * Converts the wallpaper drawable to a bitmap and applies it.
     * Returns true if a bitmap was applied.
     */
    private boolean applyWallpaper(Drawable wallpaperDrawable, String logPrefix) {
        Bitmap bitmap = null;
        if (wallpaperDrawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) wallpaperDrawable).getBitmap();
        } else if (wallpaperDrawable != null) {
            int w = wallpaperDrawable.getIntrinsicWidth();
            int h = wallpaperDrawable.getIntrinsicHeight();
            if (w > 0 && h > 0 && w < 8192 && h < 8192) {
                bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                Canvas c = new Canvas(bitmap);
                wallpaperDrawable.setBounds(0, 0, w, h);
                wallpaperDrawable.draw(c);
            }
        }
        if (bitmap == null) {
            Log.w(TAG, "Failed to load wallpaper bitmap");
            return false;
        }
        mWallpaperBitmap = bitmap;
        mBitmapW = bitmap.getWidth();
        mBitmapH = bitmap.getHeight();
        Log.d(TAG, logPrefix + mBitmapW + "x" + mBitmapH);
        invalidate();
        return true;
    }

    /**
     * Sets the wallpaper zoom level (depth effect).
     * 0 = fully zoomed out (home state), 1 = fully zoomed in (app open).
     * This matches the semantics of WallpaperManager.setWallpaperZoomOut().
     */
    public void setZoom(float zoom) {
        float clamped = Math.max(0f, Math.min(1f, zoom));
        if (Math.abs(clamped - mZoom) < 0.001f) return;
        mZoom = clamped;
        invalidate();
    }

    /**
     * Sets horizontal offset for parallax scrolling.
     * -1.0 = fully left, 0.0 = centered, 1.0 = fully right.
     */
    public void setOffsetX(float offset) {
        if (Math.abs(offset - mOffsetX) < 0.001f) return;
        mOffsetX = offset;
        invalidate();
    }

    /**
     * Sets vertical offset.
     */
    public void setOffsetY(float offset) {
        if (Math.abs(offset - mOffsetY) < 0.001f) return;
        mOffsetY = offset;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mScreenW = w;
        mScreenH = h;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mWallpaperBitmap == null || mScreenW == 0 || mScreenH == 0) {
            // Draw black background while wallpaper loads (matches WM behavior)
            canvas.drawColor(0xFF000000);
            return;
        }

        // Calculate scale: wallpaper is typically wider than screen for parallax
        // At zoom=0: fit height, allow horizontal parallax
        // At zoom=1: scale up (zoom in effect)
        float baseScale = Math.max(
                (float) mScreenW / mBitmapW,
                (float) mScreenH / mBitmapH);

        // Zoom effect: scale from baseScale to larger
        float zoomScale = baseScale * (1f + mZoom * 0.4f);

        // Apply offset (parallax)
        float scaledW = mBitmapW * zoomScale;
        float scaledH = mBitmapH * zoomScale;

        float dx = (mScreenW - scaledW) * 0.5f + mOffsetX * (scaledW - mScreenW) * 0.5f;
        float dy = (mScreenH - scaledH) * 0.5f + mOffsetY * (scaledH - mScreenH) * 0.5f;

        mMatrix.reset();
        mMatrix.setScale(zoomScale, zoomScale);
        mMatrix.postTranslate(dx, dy);

        canvas.drawBitmap(mWallpaperBitmap, mMatrix, mPaint);
    }

    /**
     * Force-reload the wallpaper (e.g., after wallpaper picker returns).
     */
    public void refreshWallpaper() {
        loadWallpaper();
    }
}
