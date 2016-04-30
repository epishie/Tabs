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

package com.epishie.tabs.di;

import android.content.Context;

import com.epishie.tabs.BuildConfig;
import com.epishie.tabs.feature.links.LinksFeature;
import com.epishie.tabs.feature.links.LinksPresenter;
import com.epishie.tabs.feature.shared.repository.PreferenceSessionRepository;
import com.epishie.tabs.feature.shared.repository.RedditRepository;
import com.epishie.tabs.feature.shared.repository.RetrofitRedditRepository;
import com.epishie.tabs.feature.shared.repository.SessionRepository;
import com.epishie.tabs.feature.shared.repository.TokenManager;
import com.epishie.tabs.feature.subreddits.SubredditsFeature;
import com.epishie.tabs.feature.subreddits.SubredditsPresenter;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Module
public class AppModule {
    private final Context mContext;

    public AppModule(Context context) {
        mContext = context;
    }

    @Singleton
    @Named("main")
    @Provides
    public Scheduler provideMainScheduler() {
        return AndroidSchedulers.mainThread();
    }

    @Singleton
    @Named("worker")
    @Provides
    public Scheduler provideWorkerScheduler() {
        return Schedulers.newThread();
    }

    @Singleton
    @Provides
    public RedditRepository provideRedditRepository(TokenManager tokenManager) {
        return new RetrofitRedditRepository(BuildConfig.BASE_URL, tokenManager);
    }

    @Singleton
    @Provides
    public SessionRepository provideSessionRepository() {
        return new PreferenceSessionRepository(mContext);
    }

    @Singleton
    @Provides
    public TokenManager provideTokenManager(SessionRepository sessionRepository) {
        return new TokenManager(BuildConfig.TOKEN_URL, BuildConfig.OAUTH_CLIENT_ID, sessionRepository);
    }

    @Provides
    public SubredditsFeature.Presenter provideSubredditsPresenter(RedditRepository repository,
                                                                  @Named("main") Scheduler mainScheduler,
                                                                  @Named("worker") Scheduler workerScheduler) {
        return new SubredditsPresenter(repository, mainScheduler, workerScheduler);
    }

    @Provides
    public LinksFeature.Presenter providePostsPresenter(RedditRepository repository,
                                                        @Named("main") Scheduler mainScheduler,
                                                        @Named("worker") Scheduler workerScheduler) {
        return new LinksPresenter(repository, mainScheduler, workerScheduler);
    }
}
