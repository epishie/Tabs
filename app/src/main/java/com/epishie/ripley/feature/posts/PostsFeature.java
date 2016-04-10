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

package com.epishie.ripley.feature.posts;

import android.content.Context;

import java.util.List;

public interface PostsFeature {
    interface View {
        void showPosts(List<PostViewModel> posts);
        void refresh();
        Context getContext();
    }
    interface Presenter {
        void setView(View view);
        void onLoad(String subreddit);
        void onLoadMore(String subreddit);
        void onRefresh(String subreddit);
    }
}