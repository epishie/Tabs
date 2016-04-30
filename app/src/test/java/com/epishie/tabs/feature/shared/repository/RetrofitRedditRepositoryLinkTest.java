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
import com.epishie.tabs.feature.shared.model.Listing;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
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

public class RetrofitRedditRepositoryLinkTest {
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
        TestSubscriber<List<Listing>> subscriber = new TestSubscriber<>();
        mRepository.getLinkComments("gadgets", "12345")
                .subscribeOn(mScheduler)
                .subscribe(subscriber);
        mScheduler.advanceTimeBy(5, TimeUnit.SECONDS);

        RecordedRequest request = mServer.takeRequest(5, TimeUnit.SECONDS);
        assertThat(request.getPath()).isEqualTo("/r/gadgets/comments/12345.json");
    }

    @Test
    public void testConnectionError() {
        mServer.enqueue(new MockResponse().setResponseCode(404));
        TestSubscriber<List<Listing>> subscriber = new TestSubscriber<>();
        mRepository.getLinkComments("gadgets", "12345")
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
        TestSubscriber<List<Listing>> subscriber = new TestSubscriber<>();
        mRepository.getLinkComments("gadgets", "12345")
                .subscribeOn(mScheduler)
                .subscribe(subscriber);
        mScheduler.advanceTimeBy(5, TimeUnit.SECONDS);

        subscriber.assertError(ResponseError.class);
    }

    @Test
    public void testResponseOk() {
        String json = "[" +
                "   {" +
                "       \"data\": {" +
                "       }," +
                "       \"kind\": \"Listing\"" +
                "   }," +
                "   {" +
                "       \"data\": {" +
                "       }," +
                "       \"kind\": \"Listing\"" +
                "   }" +
                "]";
        mServer.enqueue(new MockResponse()
                .setBody(json)
                .setResponseCode(200));
        TestSubscriber<List<Listing>> subscriber = new TestSubscriber<>();
        mRepository.getLinkComments("gadgets", "12345")
                .subscribeOn(mScheduler)
                .subscribe(subscriber);
        mScheduler.advanceTimeBy(5, TimeUnit.SECONDS);

        subscriber.assertValueCount(1);
    }

    @Test
    public void testResponseCached() {
        String json = "[" +
                "   {" +
                "       \"data\": {" +
                "       }," +
                "       \"kind\": \"Listing\"" +
                "   }," +
                "   {" +
                "       \"data\": {" +
                "       }," +
                "       \"kind\": \"Listing\"" +
                "   }" +
                "]";
        mServer.enqueue(new MockResponse()
                .setBody(json)
                .setResponseCode(200));
        TestSubscriber<List<Listing>> subscriber = new TestSubscriber<>();
        mRepository.getLinkComments("gadgets", "12345")
                .subscribeOn(mScheduler)
                .subscribe(subscriber);
        mScheduler.advanceTimeBy(5, TimeUnit.SECONDS);
        mRepository.getLinkComments("gadgets", "12345")
                .subscribeOn(mScheduler)
                .subscribe(subscriber);
        mScheduler.advanceTimeBy(5, TimeUnit.SECONDS);

        subscriber.assertValueCount(1);
        assertThat(mServer.getRequestCount()).isEqualTo(1);
    }
}