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

import android.support.v7.widget.RecyclerView;

import com.epishie.tabs.App;
import com.epishie.tabs.R;
import com.epishie.tabs.di.AppComponent;
import com.epishie.tabs.di.AppModule;
import com.epishie.tabs.di.DaggerAppComponent;
import com.epishie.tabs.feature.shared.model.Sort;
import com.epishie.tabs.feature.shared.repository.RedditRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import rx.Scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.android.api.Assertions.assertThat;
import static org.assertj.android.recyclerview.v7.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.shadows.support.v4.SupportFragmentTestUtil.startVisibleFragment;

@RunWith(RobolectricGradleTestRunner.class)
public class PostsFragmentTest {
    @Mock
    PostsFeature.Presenter mPresenter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        AppComponent component = DaggerAppComponent.builder()
                .appModule(new MockModule())
                .build();
        ((App) RuntimeEnvironment.application).setComponent(component);
    }

    @Test
    public void testCreateInstance() {
        PostsFragment fragment = PostsFragment.createInstance("gadgets", Sort.RISING);
        assertThat(fragment.getArguments()).hasKey(PostsFragment.PARAM_SUBREDDIT);
        assertThat(fragment.getArguments().getString(PostsFragment.PARAM_SUBREDDIT))
                .isEqualTo("gadgets");
        assertThat(fragment.getArguments()).hasKey(PostsFragment.PARAM_SORT);
        assertThat(fragment.getArguments().getInt(PostsFragment.PARAM_SORT))
                .isEqualTo(Sort.RISING.ordinal());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingParamSubreddit() {
        startVisibleFragment(new PostsFragment());
    }

    @Test
    public void testOnCreate() {
        PostsFragment fragment = PostsFragment.createInstance("gadgets", Sort.HOT);
        startVisibleFragment(fragment);

        verify(mPresenter).setView(fragment);
        verify(mPresenter).onLoad("gadgets", Sort.HOT);
    }

    @Test
    public void testShowPosts() {
        PostsFragment fragment = PostsFragment.createInstance("gadgets", Sort.HOT);
        startVisibleFragment(fragment);
        fragment.showPosts(mockPosts(10));
        RecyclerView posts = (RecyclerView) fragment.getView().findViewById(R.id.posts);
        posts.measure(0, 0);
        assertThat(posts).hasChildCount(10);
    }

    private List<PostViewModel> mockPosts(int count) {
        List<PostViewModel> posts = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            PostViewModel post = mock(PostViewModel.class);
            when(post.getTitle()).thenReturn("Title#" + i);
            posts.add(post);
        }
        return posts;
    }

    private class MockModule extends AppModule {
        @Override
        public PostsFeature.Presenter providePostsPresenter(RedditRepository repository,
                                                            @Named("main") Scheduler mainScheduler,
                                                            @Named("worker") Scheduler workerScheduler) {
            return mPresenter;
        }
    }
}