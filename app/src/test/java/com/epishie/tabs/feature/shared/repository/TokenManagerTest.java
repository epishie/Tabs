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

import android.util.Base64;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;

import java.io.IOException;

import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricGradleTestRunner.class)
public class TokenManagerTest {
    MockWebServer mServer;
    TokenManager mTokenManager;
    OkHttpClient mClient;

    @Before
    public void setUp() throws IOException {
        mServer = new MockWebServer();
        mServer.start();

        mTokenManager = new TokenManager(mServer.url("").toString(),
                "CLIENT_ID",
                mock(SessionRepository.class));
        mClient = new OkHttpClient.Builder()
                .addInterceptor(mTokenManager.getInterceptor())
                .build();
    }

    @Test
    public void testInterceptor() throws IOException, InterruptedException {
        mServer.enqueue(mockAccessTokenResponse());
        mServer.enqueue(new MockResponse());
        HttpUrl url = mServer.url("test");
        Request request = new Request.Builder()
                .url(url)
                .build();
        mClient.newCall(request).execute();

        RecordedRequest accessTokenRequest = mServer.takeRequest();
        assertThat(accessTokenRequest.getPath()).isEqualTo("/api/v1/access_token");
        assertThat(accessTokenRequest.getHeader("Authorization"))
                .isEqualTo(Credentials.basic("CLIENT_ID", ""));
        // TODO body assertions
        RecordedRequest decoratedRequest = mServer.takeRequest();
        assertThat(decoratedRequest.getHeader("Authorization"))
                .isEqualTo("bearer TEST_ACCESS_TOKEN");
    }

    private MockResponse mockAccessTokenResponse() {
        String response = "{" +
                "   \"access_token\": \"TEST_ACCESS_TOKEN\"," +
                "   \"token_type\": \"bearer\"" +
                "}";
        return new MockResponse()
                .setBody(response);
    }
}