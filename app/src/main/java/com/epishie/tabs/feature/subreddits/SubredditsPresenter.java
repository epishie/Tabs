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

package com.epishie.tabs.feature.subreddits;

import com.epishie.tabs.feature.shared.model.Listing;
import com.epishie.tabs.feature.shared.model.Subreddit;
import com.epishie.tabs.feature.shared.model.Thing;
import com.epishie.tabs.feature.shared.repository.RedditRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Func1;

public class SubredditsPresenter implements SubredditsFeature.Presenter {
    private final RedditRepository mRepository;
    private final Scheduler mMainScheduler;
    private final Scheduler mWorkerScheduler;
    private final Pattern mUrlPattern;
    private SubredditsFeature.View mView;

    public SubredditsPresenter(RedditRepository repository,
                               Scheduler mainScheduler,
                               Scheduler workerScheduler) {
        mRepository = repository;
        mMainScheduler = mainScheduler;
        mWorkerScheduler = workerScheduler;

        mUrlPattern = Pattern.compile("/r/(.*)/");
    }

    @Override
    public void setView(SubredditsFeature.View view) {
        mView = view;
    }

    @Override
    public void onLoad() {
        mRepository.getSubreddits("default")
                .subscribeOn(mWorkerScheduler)
                .map(new Func1<Thing<Listing<Subreddit>>, List<SubredditViewModel>>() {
                    @Override
                    public List<SubredditViewModel> call(Thing<Listing<Subreddit>> thing) {
                        List<SubredditViewModel> mappedSubreddits = new ArrayList<>();
                        for (Thing<Subreddit> subredditThing : thing.getData().getChildren()) {
                            Subreddit subreddit = subredditThing.getData();
                            Matcher urlMatcher = mUrlPattern.matcher(subreddit.getUrl());
                            String name = subreddit.getUrl();
                            if (urlMatcher.find()) {
                                name = urlMatcher.group(1);
                            }
                            mappedSubreddits.add(new SubredditViewModel(name, subreddit.getDescriptionHtml()));
                        }
                        return mappedSubreddits;
                    }
                })
                .observeOn(mMainScheduler)
                .subscribe(new Subscriber<List<SubredditViewModel>>() {
                    @Override
                    public void onCompleted() { }

                    @Override
                    public void onError(Throwable e) {
                        throw new RuntimeException(e);
                    }

                    @Override
                    public void onNext(List<SubredditViewModel> subredditViewModels) {
                        mView.showSubreddits(subredditViewModels);
                    }
                });
    }
}
