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

import com.epishie.tabs.feature.shared.model.Link;
import com.epishie.tabs.feature.shared.model.Listing;
import com.epishie.tabs.feature.shared.model.Sort;
import com.epishie.tabs.feature.shared.model.Thing;
import com.epishie.tabs.feature.shared.repository.RedditRepository;
import com.epishie.tabs.feature.shared.repository.RedditRepository.FetchType;

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
import static org.mockito.Matchers.anyString;
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
        mPresenter.onLoad("gadgets", Sort.HOT);
        mScheduler.advanceTimeBy(5, TimeUnit.SECONDS);

        verify(mRepository).getLinks("gadgets", Sort.HOT, FetchType.NORMAL);
        verify(mView).showPosts(anyListOf(PostViewModel.class));
    }

    @Test
    public void testOnLoadMore() {
        mockPosts(10);
        mPresenter.onLoadMore("gadgets", Sort.NEW);
        mScheduler.advanceTimeBy(5, TimeUnit.SECONDS);

        verify(mRepository).getLinks("gadgets", Sort.NEW, FetchType.NEXT);
        verify(mView).showPosts(anyListOf(PostViewModel.class));
    }

    @Test
    public void testOnRefresh() {
        mockPosts(10);
        mPresenter.onRefresh("gadgets", Sort.RISING);
        mScheduler.advanceTimeBy(5, TimeUnit.SECONDS);

        verify(mRepository).getLinks("gadgets", Sort.RISING, FetchType.REFRESH);
        verify(mView).showPosts(anyListOf(PostViewModel.class));
    }

    @SuppressWarnings("unchecked")
    private void mockPosts(int count) {
        List<Thing<Link>> children = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Link link = mock(Link.class);
            when(link.getTitle()).thenReturn("Title#" + i);
            Thing<Link> linkThing = mock(Thing.class);
            when(linkThing.getData()).thenReturn(link);
            children.add(linkThing);
        }
        Listing<Link> linkListing = mock(Listing.class);
        when(linkListing.getChildren()).thenReturn(children);
        Thing<Listing<Link>> linksThing = mock(Thing.class);
        when(mRepository.getLinks(eq("gadgets"), any(Sort.class), any(FetchType.class)))
                .thenReturn(Observable.just(linksThing));
    }
}