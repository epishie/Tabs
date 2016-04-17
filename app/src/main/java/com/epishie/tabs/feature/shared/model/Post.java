/*
 * Copyright 2016 Epishie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epishie.tabs.feature.shared.model;

import java.util.List;

public class Post {
    String title;
    String url;
    String author;
    int num_comments;
    int score;
    long created_utc;
    List<Image> preview_images;

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getAuthor() {
        return author;
    }

    public int getCommentCount() {
        return num_comments;
    }

    public int getScore() {
        return score;
    }

    public long getCreated() {
        return created_utc;
    }

    public List<Image> getPreviewImages() {
        return preview_images;
    }

    public static class Image {
        String url;

        public String getUrl() {
            return url;
        }
    }
}
