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

package com.epishie.ripley.di;

import com.epishie.ripley.BuildConfig;
import com.epishie.ripley.feature.posts.PostsFeature;
import com.epishie.ripley.feature.posts.PostsPresenter;
import com.epishie.ripley.feature.shared.repository.RedditRepository;
import com.epishie.ripley.feature.shared.repository.RetrofitRedditRepository;
import com.epishie.ripley.feature.subreddits.SubredditsFeature;
import com.epishie.ripley.feature.subreddits.SubredditsPresenter;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Module
public class AppModule {
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
    public RedditRepository provideRedditRepository() {
        return new RetrofitRedditRepository(BuildConfig.BASE_URL);
    }

    @Provides
    public SubredditsFeature.Presenter provideSubredditsPresenter(RedditRepository repository,
                                                                  @Named("main") Scheduler mainScheduler,
                                                                  @Named("worker") Scheduler workerScheduler) {
        return new SubredditsPresenter(repository, mainScheduler, workerScheduler);
    }

    @Provides
    public PostsFeature.Presenter providePostsPresenter(RedditRepository repository,
                                                        @Named("main") Scheduler mainScheduler,
                                                        @Named("worker") Scheduler workerScheduler) {
        return new PostsPresenter(repository, mainScheduler, workerScheduler);
    }
}
