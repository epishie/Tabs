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

import com.epishie.tabs.error.ConnectionError;
import com.epishie.tabs.error.ResponseError;
import com.epishie.tabs.feature.shared.model.Link;
import com.epishie.tabs.feature.shared.model.Listing;
import com.epishie.tabs.feature.shared.model.Sort;
import com.epishie.tabs.feature.shared.model.Thing;
import com.epishie.tabs.feature.shared.repository.RedditRepository.FetchType;

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

public class RetrofitRedditRepositoryLinksTest {
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
    public void testRequest() throws InterruptedException {
        mServer.enqueue(new MockResponse().setResponseCode(200));
        TestSubscriber subscriber = new TestSubscriber<>();
        // noinspection unchecked
        mRepository.getLinks("gadgets", Sort.HOT, FetchType.NORMAL)
                .subscribeOn(mScheduler)
                .subscribe(subscriber);
        mScheduler.advanceTimeBy(5, TimeUnit.SECONDS);

        RecordedRequest request = mServer.takeRequest(5, TimeUnit.SECONDS);
        assertThat(request.getPath()).isEqualTo("/r/gadgets/hot.json");
    }

    @Test
    public void testRequestCached() throws InterruptedException {
        mServer.enqueue(new MockResponse()
                .setBody(mockPosts("title1"))
                .setResponseCode(200));
        TestSubscriber subscriber = new TestSubscriber<>();
        // noinspection unchecked
        mRepository.getLinks("gadgets", Sort.HOT, FetchType.NORMAL)
                .subscribeOn(mScheduler)
                .subscribe(subscriber);
        mScheduler.advanceTimeBy(5, TimeUnit.SECONDS);
        mServer.enqueue(new MockResponse()
                .setBody(mockPosts("title2"))
                .setResponseCode(200));
        // noinspection unchecked
        mRepository.getLinks("gadgets", Sort.HOT, FetchType.NORMAL)
                .subscribeOn(mScheduler)
                .subscribe(subscriber);
        mScheduler.advanceTimeBy(5, TimeUnit.SECONDS);

        assertThat(mServer.getRequestCount()).isEqualTo(1);
    }

    @Test
    public void testRequestRefresh() throws InterruptedException {
        mServer.enqueue(new MockResponse()
                .setBody(mockPosts("title1"))
                .setResponseCode(200));
        mServer.enqueue(new MockResponse()
                .setBody(mockPosts("title2"))
                .setResponseCode(200));
        TestSubscriber subscriber1 = new TestSubscriber<>();
        // noinspection unchecked
        mRepository.getLinks("gadgets", Sort.HOT, FetchType.NORMAL)
                .subscribeOn(mScheduler)
                .subscribe(subscriber1);
        mScheduler.advanceTimeBy(5, TimeUnit.SECONDS);
        TestSubscriber subscriber2 = new TestSubscriber<>();
        // noinspection unchecked
        mRepository.getLinks("gadgets", Sort.HOT, FetchType.REFRESH)
                .subscribeOn(mScheduler)
                .subscribe(subscriber2);
        mScheduler.advanceTimeBy(5, TimeUnit.SECONDS);

        assertThat(mServer.getRequestCount()).isEqualTo(2);
    }

    @Test
    public void testRequestNext() throws InterruptedException {
        mServer.enqueue(new MockResponse()
                .setBody(mockPosts("title1", "first_token"))
                .setResponseCode(200));
        mServer.enqueue(new MockResponse()
                .setBody(mockPosts("title2", "second_token"))
                .setResponseCode(200));
        TestSubscriber<Thing<Listing<Link>>> subscriber1 = new TestSubscriber<>();
        mRepository.getLinks("gadgets", Sort.HOT, FetchType.NORMAL)
                .subscribeOn(mScheduler)
                .subscribe(subscriber1);
        mScheduler.advanceTimeBy(5, TimeUnit.SECONDS);
        TestSubscriber<Thing<Listing<Link>>> subscriber2 = new TestSubscriber<>();
        mRepository.getLinks("gadgets", Sort.HOT, FetchType.NEXT)
                .subscribeOn(mScheduler)
                .subscribe(subscriber2);
        mScheduler.advanceTimeBy(5, TimeUnit.SECONDS);

        mServer.takeRequest();
        RecordedRequest nextRequest = mServer.takeRequest();
        assertThat(nextRequest.getPath()).isEqualTo("/r/gadgets/hot.json?after=first_token");
        Thing<Listing<Link>> response = subscriber2.getOnNextEvents().get(0);
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getAfter()).isEqualTo("second_token");
        assertThat(response.getData().getChildren()).hasSize(1);
        assertThat(response.getData().getChildren().get(0).getData()).isNotNull();
        assertThat(response.getData().getChildren().get(0).getData().getTitle()).isEqualTo("title2");
    }

    @Test
    public void testRequestSort() throws InterruptedException {
        mServer.enqueue(new MockResponse().setResponseCode(200));
        TestSubscriber subscriber = new TestSubscriber<>();
        // noinspection unchecked
        mRepository.getLinks("gadgets", Sort.CONTROVERSIAL, FetchType.NORMAL)
                .subscribeOn(mScheduler)
                .subscribe(subscriber);
        mScheduler.advanceTimeBy(5, TimeUnit.SECONDS);

        RecordedRequest request = mServer.takeRequest(5, TimeUnit.SECONDS);
        assertThat(request.getPath()).isEqualTo("/r/gadgets/controversial.json");
    }

    @Test
    public void testConnectionError() {
        mServer.enqueue(new MockResponse().setResponseCode(404));
        TestSubscriber<Thing<Listing<Link>>> subscriber = new TestSubscriber<>();
        mRepository.getLinks("gadgets", Sort.HOT, FetchType.NORMAL)
                .subscribeOn(mScheduler)
                .subscribe(subscriber);
        mScheduler.advanceTimeBy(5, TimeUnit.SECONDS);

        subscriber.assertError(ConnectionError.class);
    }

    @Test
    public void testResponseError() {
        mServer.enqueue(new MockResponse()
                .setBody("")
                .setResponseCode(200));
        TestSubscriber<Thing<Listing<Link>>> subscriber = new TestSubscriber<>();
        mRepository.getLinks("gadgets", Sort.HOT, FetchType.NORMAL)
                .subscribeOn(mScheduler)
                .subscribe(subscriber);
        mScheduler.advanceTimeBy(5, TimeUnit.SECONDS);

        subscriber.assertError(ResponseError.class);
    }

    private String mockPosts(String title) {
        return mockPosts(title, "after_token");
    }

    private String mockPosts(String title, String token) {
        return "{" +
                "   \"data\": {" +
                "       \"after\": \"" + token + "\"," +
                "       \"children\": [" +
                "           {" +
                "               \"data\": {" +
                "                   \"title\": \"" + title + "\"" +
                "               }," +
                "               \"kind\": \"t3\"" +
                "           }" +
                "       ]" +
                "   }," +
                "   \"kind\": \"Listing\"" +
                "}";
    }
}