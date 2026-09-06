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

import android.view.View;

/**
 * trebufork: sizing contract shared by widget rows ({@link ScrollableWidgetRow}) and the media
 * player row ({@link ScrollableMediaRowView}) so {@link ScrollableWidgetResizeFrame} can resize
 * both the same way: width/height relative to the list width and the natural content height,
 * plus a horizontal position within the row's free space.
 */
public interface ScrollableResizableRow {

    /** The resizable content child of the row (the widget host view or the media content). */
    View getWidgetView();

    /** Sets the size: width relative to the full row width, height multiplier on natural. */
    void setScales(float widthScale, float heightScale);

    /** Sets the horizontal position (0..1 of the free space). */
    void setPositionX(float positionX);
}
