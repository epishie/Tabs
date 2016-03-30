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

package com.epishie.ripley.feature.subreddits;

import com.epishie.ripley.feature.shared.model.Subreddit;
import com.epishie.ripley.feature.shared.model.Subreddits;
import com.epishie.ripley.feature.shared.repository.RedditRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.schedulers.TestScheduler;

import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SubredditsPresenterTest {
    @Mock
    RedditRepository mRepository;
    @Mock
    SubredditsFeature.View mView;
    TestScheduler mScheduler;
    SubredditsPresenter mPresenter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        mScheduler = new TestScheduler();
        mPresenter = new SubredditsPresenter(mRepository, mScheduler, mScheduler);
        mPresenter.setView(mView);
    }

    @Test
    public void testOnLoad() {
        mockSubreddits(10);
        mPresenter.onLoad();
        mScheduler.advanceTimeBy(5, TimeUnit.SECONDS);

        verify(mRepository).getSubreddits();
        verify(mView).showSubreddits(anyListOf(SubredditViewModel.class));
    }

    public void mockSubreddits(int count) {
        Subreddits subreddits = mock(Subreddits.class);
        List<Subreddit> subredditList = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Subreddit subreddit = mock(Subreddit.class);
            when(subreddit.getUrl()).thenReturn("URL#" + i);
            subredditList.add(subreddit);
        }
        when(mRepository.getSubreddits()).thenReturn(Observable.just(subreddits));
    }
}