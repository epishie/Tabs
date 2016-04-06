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

import com.epishie.ripley.feature.shared.model.Post;
import com.epishie.ripley.feature.shared.model.Posts;
import com.epishie.ripley.feature.shared.repository.RedditRepository;
import com.epishie.ripley.feature.shared.repository.RedditRepository.FetchType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.schedulers.TestScheduler;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class PostsPresenterTest {
    @Mock
    RedditRepository mRepository;
    @Mock
    PostsFeature.View mView;
    TestScheduler mScheduler;
    PostsPresenter mPresenter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mView.getContext()).thenReturn(RuntimeEnvironment.application);

        mScheduler = new TestScheduler();
        mPresenter = new PostsPresenter(mRepository, mScheduler, mScheduler);
        mPresenter.setView(mView);
    }

    @Test
    public void testOnLoad() {
        mockPosts(10);
        mPresenter.onLoad("gadgets");
        mScheduler.advanceTimeBy(5, TimeUnit.SECONDS);

        verify(mRepository).getPosts("gadgets", FetchType.NORMAL);
        verify(mView).showPosts(anyListOf(PostViewModel.class));
    }

    @Test
    public void testOnLoadMore() {
        mockPosts(10);
        mPresenter.onLoadMore("gadgets");
        mScheduler.advanceTimeBy(5, TimeUnit.SECONDS);

        verify(mRepository).getPosts("gadgets", FetchType.NEXT);
        verify(mView).showPosts(anyListOf(PostViewModel.class));
    }

    @Test
    public void testOnRefresh() {
        mockPosts(10);
        mPresenter.onRefresh("gadgets");
        mScheduler.advanceTimeBy(5, TimeUnit.SECONDS);

        verify(mRepository).getPosts("gadgets", FetchType.REFRESH);
        verify(mView).showPosts(anyListOf(PostViewModel.class));
    }

    private void mockPosts(int count) {
        Posts posts = mock(Posts.class);
        List<Post> children = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Post post = mock(Post.class);
            when(post.getTitle()).thenReturn("Title#" + i);
            children.add(post);
        }
        when(posts.getChildren()).thenReturn(children);
        when(mRepository.getPosts(eq("gadgets"), any(FetchType.class))).thenReturn(Observable.just(posts));
    }
}