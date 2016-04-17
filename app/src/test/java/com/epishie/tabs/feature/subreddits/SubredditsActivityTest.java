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

import android.os.Bundle;
import android.support.design.widget.TabLayout;

import com.epishie.tabs.App;
import com.epishie.tabs.R;
import com.epishie.tabs.di.AppComponent;
import com.epishie.tabs.di.AppModule;
import com.epishie.tabs.di.DaggerAppComponent;
import com.epishie.tabs.feature.shared.repository.RedditRepository;

import org.assertj.android.design.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ActivityController;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import rx.Scheduler;

import static org.assertj.android.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class SubredditsActivityTest {
    @Mock
    SubredditsFeature.Presenter mPresenter;
    ActivityController<SubredditsActivity> mController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        AppComponent component = DaggerAppComponent.builder()
                .appModule(new MockModule())
                .build();
        ((App) RuntimeEnvironment.application).setComponent(component);

        mController = Robolectric.buildActivity(SubredditsActivity.class);
    }

    @Test
    public void testOnCreate() {
        SubredditsActivity activity = mController.create().postCreate(null).start().resume().visible().get();

        verify(mPresenter).setView(activity);
        verify(mPresenter).onLoad();
    }

    @Test
    public void testShowSubreddits() {
        SubredditsActivity activity = mController.create().get();
        activity.showSubreddits(mockSubreddits(10));
        TabLayout tabs = (TabLayout) activity.findViewById(R.id.tabs);

        Assertions.assertThat(tabs).hasTabCount(10);
    }

    @Test
    public void testRestart() {
        SubredditsActivity activity = mController.create().get();
        activity.showSubreddits(mockSubreddits(10));
        Bundle state = new Bundle();
        mController.saveInstanceState(state).stop().destroy();
        mController = Robolectric.buildActivity(SubredditsActivity.class);
        activity = mController.create(state).get();
        TabLayout tabs = (TabLayout) activity.findViewById(R.id.tabs);

        Assertions.assertThat(tabs).hasTabCount(10);
    }


    private List<SubredditViewModel> mockSubreddits(int count) {
        List<SubredditViewModel> subreddits = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            SubredditViewModel subreddit = mock(SubredditViewModel.class);
            when(subreddit.getName()).thenReturn("Subreddit#" + i);
            subreddits.add(subreddit);
        }

        return subreddits;
    }

    private class MockModule extends AppModule {
        @Override
        public SubredditsFeature.Presenter provideSubredditsPresenter(RedditRepository repository,
                                                                      @Named("main") Scheduler mainScheduler,
                                                                      @Named("worker") Scheduler workerScheduler) {
            return mPresenter;
        }
    }
}