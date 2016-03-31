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

import com.epishie.ripley.R;
import com.epishie.ripley.util.FormatUtil;
import com.epishie.ripley.feature.shared.model.Post;
import com.epishie.ripley.feature.shared.model.Posts;
import com.epishie.ripley.feature.shared.repository.RedditRepository;

import java.util.ArrayList;
import java.util.List;

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
    public void onLoad(String subreddit) {
        mRepository.getPosts(subreddit)
                .observeOn(mMainScheduler)
                .subscribeOn(mWorkerScheduler)
                .map(getPostsMapper())
                .subscribe(new Subscriber<List<PostViewModel>>() {
                    @Override
                    public void onCompleted() { }

                    @Override
                    public void onError(Throwable e) { }

                    @Override
                    public void onNext(List<PostViewModel> postViewModels) {
                        mView.showPosts(postViewModels);
                    }
                });
    }

    private Func1<Posts, List<PostViewModel>> getPostsMapper() {
        return new Func1<Posts, List<PostViewModel>>() {
            @Override
            public List<PostViewModel> call(Posts posts) {
                List<PostViewModel> mappedPosts = new ArrayList<>();
                for (Post post : posts.getChildren()) {
                    PostViewModel.Builder builder = new PostViewModel.Builder();
                    builder.setTitle(post.getTitle());
                    builder.setScore(FormatUtil.getScore(post.getScore()));
                    Context context = mView.getContext();
                    builder.setByLine(context.getResources().getString(R.string.lbl_post_by_line,
                            post.getAuthor(),
                            post.getCommentCount(),
                            FormatUtil.getTimeElapsed(post.getCreated() * 1000l,
                                    System.currentTimeMillis())));
                    mappedPosts.add(builder.build());
                }

                return mappedPosts;
            }
        };
    }
}
