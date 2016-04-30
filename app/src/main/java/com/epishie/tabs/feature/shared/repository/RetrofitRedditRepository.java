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

package com.epishie.tabs.feature.shared.repository;

import android.support.v4.util.LruCache;
import android.support.v4.util.Pair;

import com.epishie.tabs.BuildConfig;
import com.epishie.tabs.error.*;
import com.epishie.tabs.feature.shared.model.Link;
import com.epishie.tabs.feature.shared.model.Listing;
import com.epishie.tabs.feature.shared.model.Sort;
import com.epishie.tabs.feature.shared.model.Subreddit;
import com.epishie.tabs.feature.shared.model.Thing;
import com.epishie.tabs.gson.ThingDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

public class RetrofitRedditRepository implements RedditRepository {
    private final Service mService;
    private final LruCache<String, Thing<Listing<Subreddit>>> mSubredditsCache;
    private final LruCache<Pair<String, Sort>, Thing<Listing<Link>>> mLinksCache;
    private final LruCache<String, List<Listing>> mLinkCache;

    public RetrofitRedditRepository(String baseUrl, TokenManager tokenManager) {
        HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
        logger.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logger)
                .addInterceptor(tokenManager.getInterceptor())
                .addInterceptor(mDecorator)
                .build();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Thing.class, new ThingDeserializer())
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
        mService = retrofit.create(Service.class);

        mSubredditsCache = new LruCache<>(5000 * 1024);
        mLinksCache = new LruCache<>(5000 * 1024);
        mLinkCache = new LruCache<>(5000 * 1024);
    }

    @Override
    public Observable<Thing<Listing<Subreddit>>> getSubreddits(String where) {
        return Observable.concat(Observable.just(mSubredditsCache.get("where")),
                getSubredditsOnline(where))
                .first(new Func1<Thing<Listing<Subreddit>>, Boolean>() {
                    @Override
                    public Boolean call(Thing<Listing<Subreddit>> subreddits) {
                        return subreddits != null ;
                    }
                });
    }

    @Override
    public Observable<Thing<Listing<Link>>> getLinks(String subreddit, Sort sort, FetchType fetchType) {
        Thing<Listing<Link>> cached = null;
        Pair<String, Sort> key = Pair.create(subreddit, sort);
        if (fetchType == FetchType.REFRESH) {
            mLinksCache.remove(key);
        } else if (fetchType == FetchType.NEXT) {
            cached = mLinksCache.remove(key);
        }
        return Observable.concat(Observable.just(mLinksCache.get(key)),
                getLinksOnline(subreddit, sort, cached))
                .first(new Func1<Thing<Listing<Link>>, Boolean>() {
                    @Override
                    public Boolean call(Thing<Listing<Link>> posts) {
                        return posts != null;
                    }
                });
    }

    @Override
    public Observable<List<Listing>> getLinkComments(String subreddit, String id) {
        return Observable.concat(Observable.just(mLinkCache.get(id)),
                getLinkOnline(subreddit, id))
                .first(new Func1<List<Listing>, Boolean>() {
                    @Override
                    public Boolean call(List<Listing> link) {
                        return link != null ;
                    }
                });
    }

    private Observable<Thing<Listing<Subreddit>>> getSubredditsOnline(final String where) {
        return mService.getSubreddits(where)
                .onErrorReturn(new Func1<Throwable, Thing<Listing<Subreddit>>>() {
                    @Override
                    public Thing<Listing<Subreddit>> call(Throwable throwable) {
                        if (throwable instanceof JsonParseException) {
                            throw new ResponseError();
                        }
                        throw new ConnectionError(throwable);
                    }
                })
                .doOnNext(new Action1<Thing<Listing<Subreddit>>>() {
                    @Override
                    public void call(Thing<Listing<Subreddit>> subreddits) {
                        if (subreddits == null) {
                            throw new ResponseError();
                        }
                        mSubredditsCache.put(where, subreddits);
                    }
                });
    }

    private Observable<Thing<Listing<Link>>> getLinksOnline(final String subreddit,
                                                            final Sort sort,
                                                            final Thing<Listing<Link>> cached) {
        String after = null;
        if (cached != null) {
            after = cached.getData().getAfter();
        }
        return mService.getLinks(subreddit, sort.toString(), after)
                .onErrorReturn(new Func1<Throwable, Thing<Listing<Link>>>() {
                    @Override
                    public Thing<Listing<Link>> call(Throwable throwable) {
                        if (throwable instanceof JsonParseException) {
                            throw new ResponseError();
                        }
                        throw new ConnectionError(throwable);
                    }
                })
                .doOnNext(new Action1<Thing<Listing<Link>>>() {
                    @Override
                    public void call(Thing<Listing<Link>> posts) {
                        if (posts == null) {
                            throw new ResponseError();
                        }
                        Pair<String, Sort> key = Pair.create(subreddit, sort);
                        if (cached != null) {
                            cached.getData().addChildren(posts.getData());
                            mLinksCache.put(key, posts);
                        } else {
                            mLinksCache.put(key, posts);
                        }
                    }
                });
    }

    private Observable<List<Listing>> getLinkOnline(final String subreddit,
                                                  final String id) {
        return mService.getLinkComments(subreddit, id)
                .onErrorReturn(new Func1<Throwable, List<Listing>>() {
                    @Override
                    public List<Listing> call(Throwable throwable) {
                        if (throwable instanceof JsonParseException ||
                                throwable instanceof IOException) {
                            throw new ResponseError();
                        }
                        throw new ConnectionError(throwable);
                    }
                })
                .doOnNext(new Action1<List<Listing>>() {
                    @Override
                    public void call(List<Listing> link) {
                        if (link == null ||
                                link.size() != 2) {
                            throw new ResponseError();
                        }
                        mLinkCache.put(id, link);
                    }
                });
    }

    private final Interceptor mDecorator = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request.Builder builder = chain.request().newBuilder();
            builder.addHeader("User-Agent", "android:" +
                    BuildConfig.APPLICATION_ID +
                    ":v" + BuildConfig.VERSION_NAME);
            return chain.proceed(builder.build());
        }
    };

    public interface Service {
        @GET("subreddits/{where}.json")
        Observable<Thing<Listing<Subreddit>>> getSubreddits(@Path("where") String where);
        @GET("r/{subreddit}/{sort}.json")
        Observable<Thing<Listing<Link>>> getLinks(@Path("subreddit") String subreddit,
                                                  @Path("sort") String sort,
                                                  @Query("after") String after);
        @GET("r/{subreddit}/comments/{id}.json")
        Observable<List<Listing>> getLinkComments(@Path("subreddit") String subreddit,
                                                  @Path("id") String id);
    }
}