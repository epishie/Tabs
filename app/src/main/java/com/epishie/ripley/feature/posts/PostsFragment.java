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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.epishie.ripley.App;
import com.epishie.ripley.R;
import com.epishie.ripley.widget.InfiniteScrollListener;

import java.util.List;

import javax.inject.Inject;

public class PostsFragment extends Fragment implements PostsFeature.View {
    public static final String PARAM_SUBREDDIT = PostsFragment.class.getName() + ".PARAM_SUBREDDIT";
    @Inject
    protected PostsFeature.Presenter mPresenter;
    private String mSubreddit;
    private SwipeRefreshLayout mRefresher;
    private PostsAdapter mPostsAdapter;

    public static PostsFragment createInstance(@NonNull String subreddit) {
        PostsFragment fragment = new PostsFragment();
        Bundle args = new Bundle();
        args.putString(PARAM_SUBREDDIT, subreddit);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null ||
                !getArguments().containsKey(PARAM_SUBREDDIT) ||
                TextUtils.isEmpty(getArguments().getString(PARAM_SUBREDDIT))) {
            throw new IllegalArgumentException("PARAM_SUBREDDIT is required");
        }

        mSubreddit = getArguments().getString(PARAM_SUBREDDIT);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_posts, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPostsAdapter = new PostsAdapter();
        RecyclerView posts = (RecyclerView) view.findViewById(R.id.posts);
        LinearLayoutManager lm = new LinearLayoutManager(getContext());
        posts.setLayoutManager(lm);
        posts.setAdapter(mPostsAdapter);
        posts.addOnScrollListener(new InfiniteScrollListener(lm) {
            @Override
            public void onLoadMore() {
                mPresenter.onLoadMore(mSubreddit);
                mRefresher.setRefreshing(true);
            }
        });

        mRefresher = (SwipeRefreshLayout) view.findViewById(R.id.refresher);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        injectDependencies();

        mPresenter.setView(this);
        mRefresher.post(new Runnable() {
            @Override
            public void run() {
                mPresenter.onLoad(mSubreddit);
                mRefresher.setRefreshing(true);
            }
        });
    }

    @Override
    public void showPosts(List<PostViewModel> posts) {
        mPostsAdapter.addPosts(posts);
        mRefresher.setRefreshing(false);
    }


    private void injectDependencies() {
        App app = (App) getActivity().getApplication();
        app.getComponent().inject(this);
    }
}
