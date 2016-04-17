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
import com.epishie.tabs.feature.shared.model.Posts;
import com.epishie.tabs.feature.shared.model.Sort;
import com.epishie.tabs.feature.shared.repository.RedditRepository.FetchType;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;

import static org.assertj.core.api.Assertions.assertThat;

public class RetrofitRedditRepositoryPostsTest {
    MockWebServer mServer;
    RedditRepository mRepository;
    TestScheduler mScheduler;

    @Before
    public void setUp() {
        mServer = new MockWebServer();
        mRepository = new RetrofitRedditRepository(mServer.url("").toString());
        mScheduler = new TestScheduler();
    }

    @Test
    public void testRequest() throws InterruptedException {
        mServer.enqueue(new MockResponse().setResponseCode(200));
        TestSubscriber<Posts> subscriber = new TestSubscriber<>();
        mRepository.getPosts("gadgets", Sort.HOT, FetchType.NORMAL)
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
        TestSubscriber<Posts> subscriber = new TestSubscriber<>();
        mRepository.getPosts("gadgets", Sort.HOT, FetchType.NORMAL)
                .subscribeOn(mScheduler)
                .subscribe(subscriber);
        mScheduler.advanceTimeBy(5, TimeUnit.SECONDS);
        mServer.enqueue(new MockResponse()
                .setBody(mockPosts("title2"))
                .setResponseCode(200));
        mRepository.getPosts("gadgets", Sort.HOT, FetchType.NORMAL)
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
        TestSubscriber<Posts> subscriber1 = new TestSubscriber<>();
        mRepository.getPosts("gadgets", Sort.HOT, FetchType.NORMAL)
                .subscribeOn(mScheduler)
                .subscribe(subscriber1);
        mScheduler.advanceTimeBy(5, TimeUnit.SECONDS);
        TestSubscriber<Posts> subscriber2 = new TestSubscriber<>();
        mRepository.getPosts("gadgets", Sort.HOT, FetchType.REFRESH)
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
        TestSubscriber<Posts> subscriber1 = new TestSubscriber<>();
        mRepository.getPosts("gadgets", Sort.HOT, FetchType.NORMAL)
                .subscribeOn(mScheduler)
                .subscribe(subscriber1);
        mScheduler.advanceTimeBy(5, TimeUnit.SECONDS);
        TestSubscriber<Posts> subscriber2 = new TestSubscriber<>();
        mRepository.getPosts("gadgets", Sort.HOT, FetchType.NEXT)
                .subscribeOn(mScheduler)
                .subscribe(subscriber2);
        mScheduler.advanceTimeBy(5, TimeUnit.SECONDS);

        mServer.takeRequest();
        RecordedRequest nextRequest = mServer.takeRequest();
        assertThat(nextRequest.getPath()).isEqualTo("/r/gadgets/hot.json?after=first_token");
        Posts response = subscriber2.getOnNextEvents().get(0);
        assertThat(response.getAfter()).isEqualTo("second_token");
        assertThat(response.getChildren()).hasSize(1);
        assertThat(response.getChildren().get(0).getTitle()).isEqualTo("title2");
    }

    @Test
    public void testRequestSort() throws InterruptedException {
        mServer.enqueue(new MockResponse().setResponseCode(200));
        TestSubscriber<Posts> subscriber = new TestSubscriber<>();
        mRepository.getPosts("gadgets", Sort.CONTROVERSIAL, FetchType.NORMAL)
                .subscribeOn(mScheduler)
                .subscribe(subscriber);
        mScheduler.advanceTimeBy(5, TimeUnit.SECONDS);

        RecordedRequest request = mServer.takeRequest(5, TimeUnit.SECONDS);
        assertThat(request.getPath()).isEqualTo("/r/gadgets/controversial.json");
    }

    @Test
    public void testConnectionError() {
        mServer.enqueue(new MockResponse().setResponseCode(404));
        TestSubscriber<Posts> subscriber = new TestSubscriber<>();
        mRepository.getPosts("gadgets", Sort.HOT, FetchType.NORMAL)
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
        TestSubscriber<Posts> subscriber = new TestSubscriber<>();
        mRepository.getPosts("gadgets", Sort.HOT, FetchType.NORMAL)
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
                "               }" +
                "           }" +
                "       ]" +
                "   }" +
                "}";
    }
}