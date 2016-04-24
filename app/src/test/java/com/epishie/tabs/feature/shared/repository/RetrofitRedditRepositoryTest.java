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

package com.epishie.tabs.feature.shared.repository;

import com.epishie.tabs.BuildConfig;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RetrofitRedditRepositoryTest {
    MockWebServer mServer;
    RedditRepository mRepository;
    TestScheduler mScheduler;

    @Before
    public void setUp() {
        mServer = new MockWebServer();
        TokenManager tokenManager = mock(TokenManager.class);
        when(tokenManager.getInterceptor()).thenReturn(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                return chain.proceed(chain.request());
            }
        });
        mRepository = new RetrofitRedditRepository(mServer.url("").toString(), tokenManager);
        mScheduler = new TestScheduler();
    }

    @Test
    public void testUserAgent() throws InterruptedException {
        mServer.enqueue(new MockResponse().setResponseCode(200));
        TestSubscriber subscriber = new TestSubscriber<>();
        // noinspection unchecked
        mRepository.getSubreddits("default")
                .subscribeOn(mScheduler)
                .subscribe(subscriber);
        mScheduler.advanceTimeBy(5, TimeUnit.SECONDS);

        RecordedRequest request = mServer.takeRequest(5, TimeUnit.SECONDS);
        assertThat(request.getHeader("User-Agent")).isEqualTo("android:" +
                BuildConfig.APPLICATION_ID +
                ":v" + BuildConfig.VERSION_NAME);
    }
}