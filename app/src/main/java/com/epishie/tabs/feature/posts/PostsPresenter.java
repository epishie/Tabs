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

package com.epishie.tabs.feature.posts;

import android.content.Context;

import com.epishie.tabs.R;
import com.epishie.tabs.feature.shared.model.Link;
import com.epishie.tabs.feature.shared.model.Listing;
import com.epishie.tabs.feature.shared.model.Sort;
import com.epishie.tabs.feature.shared.model.Thing;
import com.epishie.tabs.feature.shared.repository.RedditRepository.FetchType;
import com.epishie.tabs.util.FormatUtil;
import com.epishie.tabs.feature.shared.repository.RedditRepository;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Func1;

public class PostsPresenter implements PostsFeature.Presenter {
    private final RedditRepository mRepository;
    private final Scheduler mMainScheduler;
    private final Scheduler mWorkerScheduler;
    private PostsFeature.View mView;

    public PostsPresenter(RedditRepository repository,
                          Scheduler mainScheduler,
                          Scheduler workerScheduler) {
        mRepository = repository;
        mMainScheduler = mainScheduler;
        mWorkerScheduler = workerScheduler;
    }

    @Override
    public void setView(PostsFeature.View view) {
        mView = view;
    }

    @Override
    public void onLoad(String subreddit, Sort sort) {
        handleResponse(mRepository.getLinks(subreddit, sort, FetchType.NORMAL));
    }

    @Override
    public void onLoadMore(String subreddit, Sort sort) {
        handleResponse(mRepository.getLinks(subreddit, sort, FetchType.NEXT));
    }

    @Override
    public void onRefresh(String subreddit, Sort sort) {
        handleResponse(mRepository.getLinks(subreddit, sort, FetchType.REFRESH));
    }

    private void handleResponse(Observable<Thing<Listing<Link>>> observable) {
        observable.subscribeOn(mWorkerScheduler)
                .map(new Func1<Thing<Listing<Link>>, List<PostViewModel>>() {
                    @Override
                    public List<PostViewModel> call(Thing<Listing<Link>> thing) {
                        List<PostViewModel> mappedPosts = new ArrayList<>();
                        if (thing.getData() == null ||
                                thing.getData().getChildren() == null) {
                            return mappedPosts;
                        }
                        for (Thing<Link> linkThing : thing.getData().getChildren()) {
                            Link link = linkThing.getData();
                            PostViewModel.Builder builder = new PostViewModel.Builder();
                            builder.setTitle(link.getTitle());
                            builder.setScore(FormatUtil.getScore(link.getScore()));
                            Context context = mView.getContext();
                            builder.setByLine(context.getResources().getString(R.string.lbl_post_by_line,
                                    link.getAuthor(),
                                    link.getNumComments(),
                                    FormatUtil.getTimeElapsed(link.getCreatedUtc() * 1000l,
                                            System.currentTimeMillis())));
                            if (link.getPreview() != null &&
                                    link.getPreview().getImages() != null &&
                                    !link.getPreview().getImages().isEmpty()) {
                                builder.setPreview(link.getPreview()
                                        .getImages()
                                        .get(0)
                                        .getSource()
                                        .getUrl());
                            }
                            mappedPosts.add(builder.build());
                        }

                        return mappedPosts;
                    }
                })
                .observeOn(mMainScheduler)
                .subscribe(new Subscriber<List<PostViewModel>>() {
                    @Override
                    public void onCompleted() { }

                    @Override
                    public void onError(Throwable e) { }

                    @Override
                    public void onNext(List<PostViewModel> posts) {
                        mView.showPosts(posts);
                    }
                });
    }
}
