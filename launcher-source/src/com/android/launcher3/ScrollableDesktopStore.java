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
import android.content.SharedPreferences;
import android.os.UserHandle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * trebufork: persistent store for the scrollable-home desktop list.
 *
 * <p>The desktop is an independent, freely ordered list of apps and widgets shown by
 * {@link ScrollableAppsView} in desktop mode. It deliberately does <em>not</em> reuse the
 * paged-workspace model (BgDataModel / favorites table): entries are kept in a private
 * SharedPreferences file as JSON and only touched by the scrollable home, so the paged
 * workspace stays completely untouched.
 *
 * <p>App entries reference the package + user and are rendered from {@link AllAppsStore}
 * data. Widget entries reference a real app widget id allocated through the launcher's
 * {@link com.android.launcher3.widget.LauncherWidgetHolder}.
 *
 * <p>trebufork: folders ({@link #TYPE_FOLDER}) are top-level entries that hold a freely
 * ordered list of app/widget members. Members share the same id space as top-level items,
 * so an entry can move between the desktop list and a folder (drag in/out) without losing
 * its identity. Folders never nest (a folder cannot be a member of another folder).
 */
public class ScrollableDesktopStore {

    private static final String TAG = "ScrollableDesktopStore";
    private static final String PREFS_NAME = "scrollable_desktop";
    private static final String KEY_ITEMS = "items";

    private static final String FIELD_ID = "id";
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_PACKAGE = "package";
    private static final String FIELD_USER = "user";
    private static final String FIELD_WIDGET_ID = "widget_id";
    private static final String FIELD_PROVIDER = "provider";
    private static final String FIELD_SPAN_X = "span_x";
    private static final String FIELD_SPAN_Y = "span_y";
    private static final String FIELD_WIDTH_SCALE = "width_scale";
    private static final String FIELD_HEIGHT_SCALE = "height_scale";
    private static final String FIELD_POSITION_X = "position_x";
    private static final String FIELD_TITLE = "title";
    private static final String FIELD_MEMBERS = "members";
    // trebufork: horizontal alignment of an inline group's icons (0 = start, 1 = center,
    // 2 = end). Groups only.
    private static final String FIELD_ALIGN = "align";

    /** trebufork: inline group alignment — icons pinned to the row start (left). */
    public static final int ALIGN_START = 0;
    /** trebufork: inline group alignment — icons centered in the row. */
    public static final int ALIGN_CENTER = 1;
    /** trebufork: inline group alignment — icons pinned to the row end (right). */
    public static final int ALIGN_END = 2;

    public static final int TYPE_APP = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
    public static final int TYPE_WIDGET = LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET;
    public static final int TYPE_FOLDER = LauncherSettings.Favorites.ITEM_TYPE_FOLDER;
    // trebufork: inline group — a top-level entry holding several apps rendered as a single
    // left-aligned row of icons (no popup, no labels). Members are apps only.
    public static final int TYPE_GROUP = 100;

    /** A single desktop entry. Immutable identity (id/type), mutable payload. */
    public static class DesktopItem {
        public final long id;
        public final int type;
        public String packageName;
        public UserHandle user;
        public int appWidgetId;
        public String provider;
        public int spanX;
        public int spanY;
        // trebufork: widget size on the desktop, relative to the full list width (width) and
        // the natural aspect-ratio height (height). Persisted so resized widgets keep their size.
        public float widthScale = 1f;
        public float heightScale = 1f;
        // trebufork: horizontal position of the widget within its row, as a fraction of the
        // free space left of the list width (0 = left-aligned, 1 = right-aligned). Only
        // meaningful when widthScale < 1 (i.e. the widget does not fill the whole row).
        public float positionX = 0f;
        // trebufork: folder display title (folders only).
        public String title;
        // trebufork: folder members (folders only). Apps and widgets; never nested folders.
        public final List<DesktopItem> members = new ArrayList<>();
        // trebufork: inline group icon alignment (groups only), one of ALIGN_START / ALIGN_CENTER
        // / ALIGN_END. Folders ignore it.
        public int align = ALIGN_START;

        private DesktopItem(long id, int type) {
            this.id = id;
            this.type = type;
        }
    }

    /** Callback invoked (on the caller's thread) whenever the desktop content changes. */
    public interface OnChangeListener {
        void onDesktopChanged();
    }

    private final SharedPreferences mPrefs;
    private final List<DesktopItem> mItems = new ArrayList<>();
    private final List<OnChangeListener> mListeners = new ArrayList<>();
    private long mNextId = 1;

    public ScrollableDesktopStore(Context context) {
        mPrefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        load();
    }

    /** All top-level desktop entries, in list order. Do not mutate. */
    public List<DesktopItem> getItems() {
        return mItems;
    }

    /**
     * Adds an app to the desktop. Duplicate (package + user) entries are ignored.
     * @return the created entry, or the existing entry if the app was already present.
     */
    public DesktopItem addApp(String packageName, UserHandle user) {
        for (DesktopItem item : mItems) {
            if (item.type == TYPE_APP && item.packageName.equals(packageName)
                    && item.user != null && item.user.equals(user)) {
                return item;
            }
        }
        DesktopItem item = new DesktopItem(mNextId++, TYPE_APP);
        item.packageName = packageName;
        item.user = user;
        mItems.add(item);
        persist();
        return item;
    }

    /**
     * Adds a widget to the desktop. {@code provider} is the flattened component name of the
     * widget provider; spans record the intended size reported by the provider.
     */
    public DesktopItem addWidget(int appWidgetId, String provider, UserHandle user,
            int spanX, int spanY) {
        DesktopItem item = new DesktopItem(mNextId++, TYPE_WIDGET);
        item.appWidgetId = appWidgetId;
        item.provider = provider;
        item.user = user;
        item.spanX = spanX;
        item.spanY = spanY;
        mItems.add(item);
        persist();
        return item;
    }

    /** trebufork: creates an empty folder and appends it to the desktop. */
    public DesktopItem addFolder(String title) {
        DesktopItem item = new DesktopItem(mNextId++, TYPE_FOLDER);
        item.title = title;
        mItems.add(item);
        persist();
        return item;
    }

    /** trebufork: creates an empty inline group and appends it to the desktop. */
    public DesktopItem addGroup() {
        DesktopItem item = new DesktopItem(mNextId++, TYPE_GROUP);
        mItems.add(item);
        persist();
        return item;
    }

    /** Removes an entry by its stable id. Returns the removed item or null. */
    public DesktopItem remove(long id) {
        for (int i = 0; i < mItems.size(); i++) {
            if (mItems.get(i).id == id) {
                DesktopItem removed = mItems.remove(i);
                persist();
                return removed;
            }
        }
        return null;
    }

    /** trebufork: persists a new widget size (width/height scale) for the given entry. */
    public void setWidgetSize(long id, float widthScale, float heightScale) {
        for (DesktopItem item : mItems) {
            if (item.id == id) {
                item.widthScale = widthScale;
                item.heightScale = heightScale;
                persist();
                return;
            }
        }
    }

    /** trebufork: persists the horizontal position (0..1 of the free space) for a widget. */
    public void setWidgetPositionX(long id, float positionX) {
        for (DesktopItem item : mItems) {
            if (item.id == id) {
                item.positionX = Math.max(0f, Math.min(1f, positionX));
                persist();
                return;
            }
        }
    }

    /** Moves the entry at {@code fromIndex} to {@code toIndex} (both clamped). */
    public void move(int fromIndex, int toIndex) {
        if (fromIndex < 0 || fromIndex >= mItems.size()
                || toIndex < 0 || toIndex >= mItems.size() || fromIndex == toIndex) {
            return;
        }
        DesktopItem item = mItems.remove(fromIndex);
        mItems.add(toIndex, item);
        persist();
    }

    /** trebufork: inserts an entry back into the top-level list (drag out of a folder). */
    public void insertItem(int index, DesktopItem item) {
        if (item == null) {
            return;
        }
        index = Math.max(0, Math.min(index, mItems.size()));
        mItems.add(index, item);
        persist();
    }

    /** trebufork: finds a folder by id, or null. */
    public DesktopItem findFolder(long id) {
        for (DesktopItem item : mItems) {
            if (item.type == TYPE_FOLDER && item.id == id) {
                return item;
            }
        }
        return null;
    }

    /** trebufork: finds an inline group by id, or null. */
    public DesktopItem findGroup(long id) {
        for (DesktopItem item : mItems) {
            if (item.type == TYPE_GROUP && item.id == id) {
                return item;
            }
        }
        return null;
    }

    /** trebufork: renames a folder. */
    public void setFolderTitle(long id, String title) {
        DesktopItem folder = findFolder(id);
        if (folder != null) {
            folder.title = title;
            persist();
        }
    }

    /** trebufork: renames an inline group. */
    public void setGroupTitle(long id, String title) {
        DesktopItem group = findGroup(id);
        if (group != null) {
            group.title = title;
            persist();
        }
    }

    /** trebufork: sets an inline group's icon alignment (one of ALIGN_START/CENTER/END). */
    public void setGroupAlign(long id, int align) {
        DesktopItem group = findGroup(id);
        if (group != null) {
            group.align = Math.max(ALIGN_START, Math.min(ALIGN_END, align));
            persist();
        }
    }

    /**
     * trebufork: moves a top-level entry into a folder, appended at the end.
     * @return true if the item was moved.
     */
    public boolean addItemToFolder(long folderId, DesktopItem item) {
        DesktopItem folder = findFolder(folderId);
        if (folder == null || item == null || !mItems.remove(item)) {
            return false;
        }
        folder.members.add(item);
        persist();
        return true;
    }

    /**
     * trebufork: moves a top-level entry into a folder at {@code index}.
     * @return true if the item was moved.
     */
    public boolean addItemToFolder(long folderId, DesktopItem item, int index) {
        DesktopItem folder = findFolder(folderId);
        if (folder == null || item == null || !mItems.remove(item)) {
            return false;
        }
        index = Math.max(0, Math.min(index, folder.members.size()));
        folder.members.add(index, item);
        persist();
        return true;
    }

    /**
     * trebufork: removes a member from a folder and returns it (the caller decides whether to
     * re-add it to the top-level list via {@link #insertItem} or discard it).
     */
    public DesktopItem removeItemFromFolder(long folderId, long memberId) {
        DesktopItem folder = findFolder(folderId);
        if (folder == null) {
            return null;
        }
        for (int i = 0; i < folder.members.size(); i++) {
            if (folder.members.get(i).id == memberId) {
                DesktopItem removed = folder.members.remove(i);
                persist();
                return removed;
            }
        }
        return null;
    }

    /** trebufork: reorders a member within its folder. */
    public void moveInFolder(long folderId, int fromIndex, int toIndex) {
        DesktopItem folder = findFolder(folderId);
        if (folder == null || fromIndex < 0 || fromIndex >= folder.members.size()
                || toIndex < 0 || toIndex >= folder.members.size() || fromIndex == toIndex) {
            return;
        }
        DesktopItem item = folder.members.remove(fromIndex);
        folder.members.add(toIndex, item);
        persist();
    }

    /** trebufork: reorders a member within its inline group. */
    public void moveInGroup(long groupId, int fromIndex, int toIndex) {
        DesktopItem group = findGroup(groupId);
        if (group == null || fromIndex < 0 || fromIndex >= group.members.size()
                || toIndex < 0 || toIndex >= group.members.size() || fromIndex == toIndex) {
            return;
        }
        DesktopItem item = group.members.remove(fromIndex);
        group.members.add(toIndex, item);
        persist();
    }

    /**
     * trebufork: moves a top-level entry into an inline group, appended at the end.
     * @return true if the item was moved.
     */
    public boolean addItemToGroup(long groupId, DesktopItem item) {
        DesktopItem group = findGroup(groupId);
        if (group == null || item == null || !mItems.remove(item)) {
            return false;
        }
        group.members.add(item);
        persist();
        return true;
    }

    /**
     * trebufork: removes a member from an inline group and returns it (the caller decides
     * whether to re-add it to the top-level list via {@link #insertItem} or discard it).
     */
    public DesktopItem removeItemFromGroup(long groupId, long memberId) {
        DesktopItem group = findGroup(groupId);
        if (group == null) {
            return null;
        }
        for (int i = 0; i < group.members.size(); i++) {
            if (group.members.get(i).id == memberId) {
                DesktopItem removed = group.members.remove(i);
                persist();
                return removed;
            }
        }
        return null;
    }

    public void addListener(OnChangeListener listener) {
        mListeners.add(listener);
    }

    public void removeListener(OnChangeListener listener) {
        mListeners.remove(listener);
    }

    private void notifyChanged() {
        for (OnChangeListener listener : mListeners) {
            listener.onDesktopChanged();
        }
    }

    private void persist() {
        JSONArray array = new JSONArray();
        for (DesktopItem item : mItems) {
            try {
                array.put(serializeItem(item));
            } catch (JSONException e) {
                Log.e(TAG, "Failed to serialize desktop item " + item, e);
            }
        }
        mPrefs.edit().putString(KEY_ITEMS, array.toString()).apply();
        notifyChanged();
    }

    private JSONObject serializeItem(DesktopItem item) throws JSONException {
        JSONObject o = new JSONObject();
        o.put(FIELD_ID, item.id);
        o.put(FIELD_TYPE, item.type);
        if (item.type == TYPE_APP) {
            o.put(FIELD_PACKAGE, item.packageName);
            o.put(FIELD_USER, item.user == null ? 0 : item.user.getIdentifier());
        } else if (item.type == TYPE_WIDGET) {
            o.put(FIELD_WIDGET_ID, item.appWidgetId);
            o.put(FIELD_PROVIDER, item.provider == null ? "" : item.provider);
            o.put(FIELD_SPAN_X, item.spanX);
            o.put(FIELD_SPAN_Y, item.spanY);
            o.put(FIELD_WIDTH_SCALE, item.widthScale);
            o.put(FIELD_HEIGHT_SCALE, item.heightScale);
            o.put(FIELD_POSITION_X, item.positionX);
            o.put(FIELD_USER, item.user == null ? 0 : item.user.getIdentifier());
        } else {
            // trebufork: folder / inline group — title + nested members.
            o.put(FIELD_TITLE, item.title == null ? "" : item.title);
            o.put(FIELD_ALIGN, item.align);
            JSONArray members = new JSONArray();
            for (DesktopItem member : item.members) {
                members.put(serializeItem(member));
            }
            o.put(FIELD_MEMBERS, members);
        }
        return o;
    }

    /** Deserializes an item (top-level or folder member); returns null when the data is invalid. */
    private DesktopItem deserializeItem(JSONObject o) {
        int type = o.optInt(FIELD_TYPE, TYPE_APP);
        long id = o.optLong(FIELD_ID, 0);
        if (id >= mNextId) {
            mNextId = id + 1;
        }
        DesktopItem item = new DesktopItem(id, type);
        int userId = o.optInt(FIELD_USER, 0);
        item.user = UserHandle.of(userId);
        if (type == TYPE_APP) {
            item.packageName = o.optString(FIELD_PACKAGE, null);
            if (item.packageName == null) {
                return null;
            }
        } else if (type == TYPE_WIDGET) {
            item.appWidgetId = o.optInt(FIELD_WIDGET_ID, -1);
            item.provider = o.optString(FIELD_PROVIDER, null);
            item.spanX = o.optInt(FIELD_SPAN_X, 0);
            item.spanY = o.optInt(FIELD_SPAN_Y, 0);
            item.widthScale = (float) o.optDouble(FIELD_WIDTH_SCALE, 1f);
            item.heightScale = (float) o.optDouble(FIELD_HEIGHT_SCALE, 1f);
            item.positionX = (float) o.optDouble(FIELD_POSITION_X, 0f);
            if (item.appWidgetId < 0) {
                return null;
            }
        } else {
            // trebufork: folder / inline group — restore title and members.
            item.title = o.optString(FIELD_TITLE, null);
            item.align = o.optInt(FIELD_ALIGN, ALIGN_START);
            JSONArray members = o.optJSONArray(FIELD_MEMBERS);
            if (members != null) {
                for (int i = 0; i < members.length(); i++) {
                    JSONObject memberObject = members.optJSONObject(i);
                    if (memberObject == null) {
                        continue;
                    }
                    DesktopItem member = deserializeItem(memberObject);
                    if (member != null) {
                        item.members.add(member);
                    }
                }
            }
        }
        return item;
    }

    private void load() {
        mItems.clear();
        mNextId = 1;
        String raw = mPrefs.getString(KEY_ITEMS, null);
        if (raw == null) {
            return;
        }
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                DesktopItem item = deserializeItem(array.getJSONObject(i));
                if (item != null) {
                    mItems.add(item);
                }
            }
            // trebufork: drop empty inline groups left behind by older builds. A group is always
            // created together with an app, so an empty one is a phantom and must not be rendered.
            int before = mItems.size();
            mItems.removeIf(item -> item.type == TYPE_GROUP && item.members.isEmpty());
            if (mItems.size() != before) {
                persist();
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse desktop list, starting fresh", e);
            mItems.clear();
        }
    }
}
