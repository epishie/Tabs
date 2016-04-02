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

package com.epishie.ripley.feature.shared.repository;

import android.support.annotation.VisibleForTesting;
import android.support.v4.util.LruCache;

import com.epishie.ripley.error.*;
import com.epishie.ripley.feature.shared.model.Posts;
import com.epishie.ripley.feature.shared.model.Subreddits;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Map;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

public class RetrofitRedditRepository implements RedditRepository {
    private final Service mService;
    private final Gson mGson;

    private final LruCache<String, Subreddits> mSubredditsCache;
    private final LruCache<String, Posts> mPostsCache;

    public RetrofitRedditRepository(String baseUrl) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
        mService = retrofit.create(Service.class);
        mGson = new GsonBuilder().create();

        mSubredditsCache = new LruCache<>(5000 * 1024);
        mPostsCache = new LruCache<>(5000 * 1024);
    }

    @Override
    public Observable<Subreddits> getSubreddits() {
        return Observable.concat(Observable.just(mSubredditsCache.get("default")),
                getSubredditsOnline())
                .first(new Func1<Subreddits, Boolean>() {
                    @Override
                    public Boolean call(Subreddits subreddits) {
                        return subreddits != null;
                    }
                });
    }

    @Override
    public Observable<Posts> getPosts(String subreddit, FetchType fetchType) {
        Posts cached = null;
        if (fetchType == FetchType.REFRESH) {
            mPostsCache.remove(subreddit);
        } else if (fetchType == FetchType.NEXT) {
            cached = mPostsCache.remove(subreddit);
        }
        return Observable.concat(Observable.just(mPostsCache.get(subreddit)),
                getPostsOnline(subreddit, cached))
                .first(new Func1<Posts, Boolean>() {
                    @Override
                    public Boolean call(Posts posts) {
                        return posts != null;
                    }
                });
    }

    private Observable<Subreddits> getSubredditsOnline() {
        return mService.getSubreddits()
                .map(getSubredditsMapper())
                .onErrorReturn(new Func1<Throwable, Subreddits>() {
                    @Override
                    public Subreddits call(Throwable throwable) {
                        if (throwable instanceof BaseError) {
                            throw (BaseError) throwable;
                        }
                        throw new ConnectionError(throwable);
                    }
                })
                .doOnNext(new Action1<Subreddits>() {
                    @Override
                    public void call(Subreddits subreddits) {
                        mSubredditsCache.put("default", subreddits);
                    }
                });
    }

    private Func1<String, Subreddits> getSubredditsMapper() {
        return new Func1<String, Subreddits>() {
            @Override
            public Subreddits call(String s) {
                Subreddits subreddits = mGson.fromJson(normalizeList(s), Subreddits.class);
                if (subreddits == null) {
                    throw new ResponseError();
                }
                return subreddits;
            }
        };
    }

    private Observable<Posts> getPostsOnline(final String subreddit, final Posts cached) {
        String after = null;
        if (cached != null) {
            after = cached.getAfter();
        }
        return mService.getPosts(subreddit, after)
                .map(getPostsMapper())
                .onErrorReturn(new Func1<Throwable, Posts>() {
                    @Override
                    public Posts call(Throwable throwable) {
                        if (throwable instanceof BaseError) {
                            throw (BaseError) throwable;
                        }
                        throw new ConnectionError(throwable);
                    }
                })
                .doOnNext(new Action1<Posts>() {
                    @Override
                    public void call(Posts posts) {
                        if (cached != null) {
                            cached.addPosts(posts);
                            mPostsCache.put(subreddit, posts);
                        } else {
                            mPostsCache.put(subreddit, posts);
                        }
                    }
                });
    }

    private Func1<String, Posts> getPostsMapper() {
        return new Func1<String, Posts>() {
            @Override
            public Posts call(String s) {
                Posts posts = mGson.fromJson(normalizeList(s), Posts.class);
                if (posts == null) {
                    throw new ResponseError();
                }
                return posts;
            }
        };
    }

    @VisibleForTesting
    protected JsonElement normalizeList(String json) {
        JsonElement jsonElement = new JsonParser().parse(json);
        // Root
        if (!jsonElement.isJsonObject()) {
            return jsonElement;
        }

        // List Object
        JsonObject listObject = jsonElement.getAsJsonObject();
        if (!listObject.has("data") || !listObject.get("data").isJsonObject()) {
            return jsonElement;
        }

        // List data
        JsonObject listData = listObject.getAsJsonObject("data");
        listObject.remove("data");
        for (Map.Entry<String, JsonElement> entry : listData.entrySet()) {
            if (entry.getKey().equals("children")) {
                listObject.remove("children");
                if (!entry.getValue().isJsonArray()) {
                    continue;
                }
                JsonArray children = entry.getValue().getAsJsonArray();
                JsonArray parsedChildren = new JsonArray();
                for (JsonElement child : children) {
                    JsonElement parsedChild = normalizeChild(child);
                    if (parsedChild != null) {
                        parsedChildren.add(parsedChild);
                    }
                }

                listObject.add("children", parsedChildren);
                continue;
            }
            listObject.add(entry.getKey(), entry.getValue());
        }

        return jsonElement;
    }

    private JsonElement normalizeChild(JsonElement jsonElement) {
        if (!jsonElement.isJsonObject()) {
            return jsonElement;
        }
        JsonObject childObject = jsonElement.getAsJsonObject();
        if (!childObject.has("data") || !childObject.get("data").isJsonObject()) {
            return jsonElement;
        }
        JsonObject childData = childObject.getAsJsonObject("data");
        childObject.remove("data");
        for (Map.Entry<String, JsonElement> entry : childData.entrySet()) {
            childObject.add(entry.getKey(), entry.getValue());
        }
        normalizePreview(childObject);

        return childObject;
    }

    private void normalizePreview(JsonObject jsonObject) {
        if (!jsonObject.has("preview") || !jsonObject.get("preview").isJsonObject()) {
            return;
        }
        JsonObject preview = jsonObject.getAsJsonObject("preview");
        if (!preview.has("images") || !preview.get("images").isJsonArray()) {
            return;
        }
        JsonArray previewImages = new JsonArray();
        JsonArray images = preview.getAsJsonArray("images");
        for (int i = 0; i < images.size(); i++) {
            if (!images.get(i).isJsonObject()) {
                continue;
            }
            JsonObject imageObject = images.get(i).getAsJsonObject();
            if (!imageObject.has("source") && !imageObject.get("source").isJsonObject()) {
                continue;
            }
            JsonObject sourceObject = imageObject.getAsJsonObject("source");
            previewImages.add(sourceObject);
        }
        if (previewImages.size() > 0) {
            jsonObject.add("preview_images", previewImages);
        }
    }

    public interface Service {
        @GET("subreddits/default.json")
        Observable<String> getSubreddits();
        @GET("r/{subreddit}.json")
        Observable<String> getPosts(@Path("subreddit") String subreddit,
                                    @Query("after") String after);
    }
}