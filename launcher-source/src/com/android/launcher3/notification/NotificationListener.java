/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.launcher3.notification;

import static com.android.launcher3.util.Executors.UI_HELPER_EXECUTOR;
import static com.android.launcher3.util.SettingsCache.NOTIFICATION_BADGING_URI;

import static java.util.Collections.emptyList;

import android.app.Notification;
import android.app.NotificationChannel;
import android.os.Handler;
import android.os.Message;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.android.launcher3.dagger.LauncherComponentProvider;
import com.android.launcher3.dot.DotInfo;
import com.android.launcher3.util.PackageUserKey;
import com.android.launcher3.util.SettingsCache;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A {@link NotificationListenerService} that sends updates to
 * {@link NotificationRepository} when notifications are posted or canceled,
 * as well and when this service first connects.
 */
public class NotificationListener extends NotificationListenerService {

    public static final String TAG = "NotificationListener";

    private static final int MSG_NOTIFICATION_POSTED = 1;
    private static final int MSG_NOTIFICATION_REMOVED = 2;
    private static final int MSG_NOTIFICATION_FULL_REFRESH = 3;
    private static final int MSG_RANKING_UPDATE = 4;

    private static final Function<PackageUserKey, DotInfo> DOT_FACTOR = key -> new DotInfo();

    private final Handler mWorkerHandler;
    private final Ranking mTempRanking = new Ranking();

    private boolean mIsConnected = false;

    /** Maps packages to their DotInfo's . */
    private final Map<PackageUserKey, DotInfo> mPackageUserToDotInfos = new HashMap<>();

    /** Maps groupKey's to the corresponding group of notifications. */
    private final Map<String, NotificationGroup> mNotificationGroupMap = new HashMap<>();
    /** Maps keys to their corresponding current group key */
    private final Map<String, String> mNotificationGroupKeyMap = new HashMap<>();

    /**
     * trebufork: media notification small icons per package, consumed by the scrollable-home
     * media row (same source as the SystemUI shade player's app icon:
     * {@code sbn.notification.smallIcon}). Updated on the worker thread; read on any.
     */
    private static final Map<String, android.graphics.drawable.Drawable>
            sMediaSmallIcons = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Returns the small icon of the package's current media-style notification, or null when
     * the listener is not connected or the package has no such notification.
     */
    @Nullable
    public static android.graphics.drawable.Drawable getMediaSmallIcon(String packageName) {
        return sMediaSmallIcons.get(packageName);
    }

    /** Observers notified whenever a media small icon is added/removed/changed. */
    public interface MediaSmallIconListener {
        /** Called on the worker thread; re-post to the main thread if needed. */
        void onMediaSmallIconsChanged();
    }

    private static final java.util.List<MediaSmallIconListener> sMediaSmallIconListeners =
            new java.util.concurrent.CopyOnWriteArrayList<>();

    /** Registers an observer for media small icon changes (no-op if already present). */
    public static void addMediaSmallIconListener(MediaSmallIconListener listener) {
        if (!sMediaSmallIconListeners.contains(listener)) {
            sMediaSmallIconListeners.add(listener);
        }
        // Catch up: the media notifications may have arrived before registration.
        listener.onMediaSmallIconsChanged();
    }

    /** Removes a previously registered observer. */
    public static void removeMediaSmallIconListener(MediaSmallIconListener listener) {
        sMediaSmallIconListeners.remove(listener);
    }

    private static void notifyMediaSmallIconListeners() {
        for (MediaSmallIconListener listener : sMediaSmallIconListeners) {
            listener.onMediaSmallIconsChanged();
        }
    }

    private void rememberSmallIcon(StatusBarNotification sbn) {
        if (sbn.getNotification().isMediaNotification()) {
            try {
                android.graphics.drawable.Icon icon = sbn.getNotification().getSmallIcon();
                android.graphics.drawable.Drawable drawable =
                        icon == null ? null : icon.loadDrawable(this);
                Log.d(TAG, "rememberSmallIcon pkg=" + sbn.getPackageName()
                        + " icon=" + (drawable != null));
                if (drawable != null) {
                    sMediaSmallIcons.put(sbn.getPackageName(), drawable);
                } else {
                    sMediaSmallIcons.remove(sbn.getPackageName());
                }
                notifyMediaSmallIconListeners();
            } catch (Exception e) {
                Log.w(TAG, "failed to load media small icon", e);
            }
        }
    }

    private void forgetSmallIcon(StatusBarNotification sbn) {
        if (sbn.getNotification().isMediaNotification()) {
            sMediaSmallIcons.remove(sbn.getPackageName());
            notifyMediaSmallIconListeners();
        }
    }

    private SettingsCache mSettingsCache;
    private SettingsCache.OnChangeListener mNotificationSettingsChangedListener;

    public NotificationListener() {
        mWorkerHandler = new Handler(UI_HELPER_EXECUTOR.getLooper(), this::handleWorkerMessage);
    }

    private boolean handleWorkerMessage(Message message) {
        switch (message.what) {
            case MSG_NOTIFICATION_POSTED: {
                StatusBarNotification sbn = (StatusBarNotification) message.obj;
                // trebufork: remember the media small icon before the badge/UI filters —
                // the media row needs it for every media session, dot-valid or not.
                rememberSmallIcon(sbn);
                if (notificationIsValidForUI(sbn)) {
                    handleNotificationPosted(sbn);
                } else {
                    removeNotificationKeys(sbn);
                }
                return true;
            }
            case MSG_NOTIFICATION_REMOVED: {
                StatusBarNotification sbn = (StatusBarNotification) message.obj;
                forgetSmallIcon(sbn);
                NotificationGroup notificationGroup = mNotificationGroupMap.get(sbn.getGroupKey());
                String key = sbn.getKey();
                if (notificationGroup != null) {
                    notificationGroup.removeChildKey(key);
                    if (notificationGroup.isEmpty()) {
                        mNotificationGroupMap.remove(sbn.getGroupKey());
                    }
                }

                handleNotificationRemoved(sbn);
                return true;
            }
            case MSG_NOTIFICATION_FULL_REFRESH:
                Log.d(TAG, "full refresh connected=" + mIsConnected);
                handleNotificationFullRefresh(mIsConnected
                        ? Arrays.stream(getActiveNotificationsSafely(null))
                            .filter(this::notificationIsValidForUI)
                            .toList()
                        : emptyList());
                return true;
            case MSG_RANKING_UPDATE: {
                String[] keys = ((RankingMap) message.obj).getOrderedKeys();
                for (StatusBarNotification sbn : getActiveNotificationsSafely(keys)) {
                    updateGroupKeyIfNecessary(sbn);
                }
                return true;
            }
        }
        return false;
    }

    private void handleNotificationPosted(StatusBarNotification sbn) {
        PackageUserKey postedPackageUserKey = PackageUserKey.fromNotification(sbn);
        if (mPackageUserToDotInfos.computeIfAbsent(postedPackageUserKey, DOT_FACTOR)
                .addOrUpdateNotificationKey(NotificationKeyData.fromNotification(sbn))) {
            dispatchUpdate(postedPackageUserKey::equals);
        }
    }

    /** trebufork: dot bookkeeping without touching the media small icon map. */
    private void removeNotificationKeys(StatusBarNotification sbn) {
        PackageUserKey removedPackageUserKey = PackageUserKey.fromNotification(sbn);
        DotInfo oldDotInfo = mPackageUserToDotInfos.get(removedPackageUserKey);
        if (oldDotInfo != null
                && oldDotInfo.removeNotificationKey(NotificationKeyData.fromNotification(sbn))) {
            if (oldDotInfo.getNotificationKeys().isEmpty()) {
                mPackageUserToDotInfos.remove(removedPackageUserKey);
            }
            dispatchUpdate(removedPackageUserKey::equals);
        }
    }

    private void handleNotificationRemoved(StatusBarNotification sbn) {
        PackageUserKey removedPackageUserKey = PackageUserKey.fromNotification(sbn);
        DotInfo oldDotInfo = mPackageUserToDotInfos.get(removedPackageUserKey);
        if (oldDotInfo != null
                && oldDotInfo.removeNotificationKey(NotificationKeyData.fromNotification(sbn))) {
            if (oldDotInfo.getNotificationKeys().isEmpty()) {
                mPackageUserToDotInfos.remove(removedPackageUserKey);
            }
            dispatchUpdate(removedPackageUserKey::equals);
        }
    }

    private void handleNotificationFullRefresh(List<StatusBarNotification> activeNotifications) {
        // This will contain the PackageUserKeys which have updated dots.
        HashMap<PackageUserKey, DotInfo> updatedDots = new HashMap<>(mPackageUserToDotInfos);
        mPackageUserToDotInfos.clear();
        sMediaSmallIcons.clear();
        // trebufork: media small icons must be remembered for ALL active media
        // notifications, not only the ones that pass the badge/UI filters —
        // SystemUI's shade shows every media session regardless of the
        // title/channel filters the launcher applies to notification dots.
        for (StatusBarNotification sbn : getActiveNotificationsSafely(null)) {
            rememberSmallIcon(sbn);
        }
        for (StatusBarNotification notification : activeNotifications) {
            rememberSmallIcon(notification);
            PackageUserKey packageUserKey = PackageUserKey.fromNotification(notification);
            mPackageUserToDotInfos.computeIfAbsent(packageUserKey, DOT_FACTOR)
                    .addOrUpdateNotificationKey(NotificationKeyData.fromNotification(notification));
        }
        // The full refresh rebuilt the icon map: let the media row pick up the icons.
        notifyMediaSmallIconListeners();

        // Add and remove from updatedDots so it contains the PackageUserKeys of updated dots.
        for (PackageUserKey packageUserKey : mPackageUserToDotInfos.keySet()) {
            DotInfo prevDot = updatedDots.get(packageUserKey);
            DotInfo newDot = mPackageUserToDotInfos.get(packageUserKey);
            if (prevDot == null
                    || prevDot.getNotificationCount() != newDot.getNotificationCount()) {
                updatedDots.put(packageUserKey, newDot);
            } else {
                // No need to update the dot if it already existed (no visual change).
                // Note that if the dot was removed entirely, we wouldn't reach this point because
                // this loop only includes active notifications added above.
                updatedDots.remove(packageUserKey);
            }
        }
        if (!updatedDots.isEmpty()) {
            dispatchUpdate(updatedDots::containsKey);
        }
    }

    private void dispatchUpdate(Predicate<PackageUserKey> updatedDots) {
        LauncherComponentProvider.get(this).getNotificationRepository().dispatchUpdate(
                new HashMap<>(mPackageUserToDotInfos), updatedDots);
    }

    private @NonNull StatusBarNotification[] getActiveNotificationsSafely(@Nullable String[] keys) {
        StatusBarNotification[] result = null;
        try {
            result = getActiveNotifications(keys);
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException: failed to fetch notifications");
        }
        return result == null ? new StatusBarNotification[0] : result;
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Log.i(TAG, "onListenerConnected");
        mIsConnected = true;

        // Register an observer to rebind the notification listener when dots are re-enabled.
        mSettingsCache = SettingsCache.INSTANCE.get(this);
        mNotificationSettingsChangedListener = this::onNotificationSettingsChanged;
        mSettingsCache.register(NOTIFICATION_BADGING_URI,
                mNotificationSettingsChangedListener);
        onNotificationSettingsChanged(mSettingsCache.getValue(NOTIFICATION_BADGING_URI));

        onNotificationFullRefresh();
    }

    private void onNotificationSettingsChanged(boolean areNotificationDotsEnabled) {
        if (!areNotificationDotsEnabled && mIsConnected) {
            requestUnbind();
        }
    }

    private void onNotificationFullRefresh() {
        mWorkerHandler.obtainMessage(MSG_NOTIFICATION_FULL_REFRESH).sendToTarget();
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        Log.i(TAG, "onListenerDisconnected");
        mIsConnected = false;
        mSettingsCache.unregister(NOTIFICATION_BADGING_URI, mNotificationSettingsChangedListener);
        onNotificationFullRefresh();
    }

    @Override
    public void onNotificationPosted(final StatusBarNotification sbn) {
        if (sbn != null) {
            mWorkerHandler.obtainMessage(MSG_NOTIFICATION_POSTED, sbn).sendToTarget();
        }
    }

    @Override
    public void onNotificationRemoved(final StatusBarNotification sbn) {
        if (sbn != null) {
            mWorkerHandler.obtainMessage(MSG_NOTIFICATION_REMOVED, sbn).sendToTarget();
        }
    }

    @Override
    public void onNotificationRankingUpdate(RankingMap rankingMap) {
        mWorkerHandler.obtainMessage(MSG_RANKING_UPDATE, rankingMap).sendToTarget();
    }

    @WorkerThread
    private void updateGroupKeyIfNecessary(StatusBarNotification sbn) {
        String childKey = sbn.getKey();
        String oldGroupKey = mNotificationGroupKeyMap.get(childKey);
        String newGroupKey = sbn.getGroupKey();
        if (oldGroupKey == null || !oldGroupKey.equals(newGroupKey)) {
            // The group key has changed.
            mNotificationGroupKeyMap.put(childKey, newGroupKey);
            if (oldGroupKey != null && mNotificationGroupMap.containsKey(oldGroupKey)) {
                // Remove the child key from the old group.
                NotificationGroup oldGroup = mNotificationGroupMap.get(oldGroupKey);
                oldGroup.removeChildKey(childKey);
                if (oldGroup.isEmpty()) {
                    mNotificationGroupMap.remove(oldGroupKey);
                }
            }
        }
        if (sbn.isGroup() && newGroupKey != null) {
            // Maintain group info so we can cancel the summary when the last child is canceled.
            NotificationGroup notificationGroup = mNotificationGroupMap.get(newGroupKey);
            if (notificationGroup == null) {
                notificationGroup = new NotificationGroup();
                mNotificationGroupMap.put(newGroupKey, notificationGroup);
            }
            boolean isGroupSummary = (sbn.getNotification().flags
                    & Notification.FLAG_GROUP_SUMMARY) != 0;
            if (isGroupSummary) {
                notificationGroup.setGroupSummaryKey(childKey);
            } else {
                notificationGroup.addChildKey(childKey);
            }
        }
    }

    /**
     * Returns true for notifications that have an intent and are not headers for grouped
     * notifications and should be shown in the notification popup.
     */
    @WorkerThread
    private boolean notificationIsValidForUI(StatusBarNotification sbn) {
        Notification notification = sbn.getNotification();
        updateGroupKeyIfNecessary(sbn);

        getCurrentRanking().getRanking(sbn.getKey(), mTempRanking);
        if (!mTempRanking.canShowBadge()) {
            return false;
        }
        if (mTempRanking.getChannel().getId().equals(NotificationChannel.DEFAULT_CHANNEL_ID)) {
            // Special filtering for the default, legacy "Miscellaneous" channel.
            if ((notification.flags & Notification.FLAG_ONGOING_EVENT) != 0) {
                return false;
            }
        }

        CharSequence title = notification.extras.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence text = notification.extras.getCharSequence(Notification.EXTRA_TEXT);
        boolean missingTitleAndText = TextUtils.isEmpty(title) && TextUtils.isEmpty(text);
        boolean isGroupHeader = (notification.flags & Notification.FLAG_GROUP_SUMMARY) != 0;
        return !isGroupHeader && !missingTitleAndText;
    }
}
