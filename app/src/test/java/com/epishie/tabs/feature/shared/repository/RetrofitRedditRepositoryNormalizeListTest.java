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

import com.epishie.tabs.feature.shared.model.Subreddits;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RetrofitRedditRepositoryNormalizeListTest {
    static Gson mGson;
    RetrofitRedditRepository mRepository;

    @BeforeClass
    public static void setUpClass() {
        mGson = new GsonBuilder().create();
    }

    @Before
    public void setUp() {
        TokenManager tokenManager = mock(TokenManager.class);
        when(tokenManager.getInterceptor()).thenReturn(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                return chain.proceed(chain.request());
            }
        });
        mRepository = new RetrofitRedditRepository("http://example.com", tokenManager);
    }

    @Test
    public void testEmptyString() {
        JsonElement jsonElement = mRepository.normalizeList("");
        Subreddits subreddits = mGson.fromJson(jsonElement, Subreddits.class);
        assertThat(subreddits).isNull();
    }

    @Test
    public void testMissingSubredditsData() {
        String json = "{}";
        JsonElement jsonElement = mRepository.normalizeList(json);
        Subreddits subreddits = mGson.fromJson(jsonElement, Subreddits.class);
        assertThat(subreddits).isEqualToComparingFieldByField(new Subreddits());
    }

    @Test
    public void testInvalidSubredditsData() {
        String json = "{" +
                "   \"data\": \"Invalid\"" +
                "}";
        JsonElement jsonElement = mRepository.normalizeList(json);
        Subreddits subreddits = mGson.fromJson(jsonElement, Subreddits.class);
        assertThat(subreddits).isEqualToComparingFieldByField(new Subreddits());
    }

    @Test
    public void testMissingSubredditsChildren() {
        String json = "{" +
                "   \"data\": {" +
                "       \"after\": \"token\"" +
                "   }" +
                "}";
        JsonElement jsonElement = mRepository.normalizeList(json);
        Subreddits subreddits = mGson.fromJson(jsonElement, Subreddits.class);
        assertThat(subreddits.getAfter()).isEqualTo("token");
    }

    @Test
    public void testInvalidSubredditsChildren() {
        String json = "{" +
                "   \"data\": {" +
                "       \"after\": \"token\", " +
                "       \"children\": \"Invalid\"" +
                "   }" +
                "}";
        JsonElement jsonElement = mRepository.normalizeList(json);
        Subreddits subreddits = mGson.fromJson(jsonElement, Subreddits.class);
        assertThat(subreddits.getAfter()).isEqualTo("token");
        assertThat(subreddits.getChildren()).isNull();
    }

    @Test
    public void testEmptySubredditsChildren() {
        String json = "{" +
                "   \"data\": {" +
                "       \"after\": \"token\", " +
                "       \"children\": []" +
                "   }" +
                "}";
        JsonElement jsonElement = mRepository.normalizeList(json);
        Subreddits subreddits = mGson.fromJson(jsonElement, Subreddits.class);
        assertThat(subreddits.getAfter()).isEqualTo("token");
        assertThat(subreddits.getChildren()).isEmpty();
    }

    @Test
    public void testMissingSubredditData() {
        String json = "{" +
                "   \"data\": {" +
                "       \"after\": \"token\", " +
                "       \"children\": [" +
                "           {" +
                "           }" +
                "       ]" +
                "   }" +
                "}";
        JsonElement jsonElement = mRepository.normalizeList(json);
        Subreddits subreddits = mGson.fromJson(jsonElement, Subreddits.class);
        assertThat(subreddits.getAfter()).isEqualTo("token");
        assertThat(subreddits.getChildren()).isNotEmpty();
        assertThat(subreddits.getChildren().get(0)).isNotNull();
    }

    @Test
    public void testInvalidSubredditData() {
        String json = "{" +
                "   \"data\": {" +
                "       \"after\": \"token\", " +
                "       \"children\": [" +
                "           {" +
                "               \"data\": \"invalid\"" +
                "           }" +
                "       ]" +
                "   }" +
                "}";
        JsonElement jsonElement = mRepository.normalizeList(json);
        Subreddits subreddits = mGson.fromJson(jsonElement, Subreddits.class);
        assertThat(subreddits.getAfter()).isEqualTo("token");
        assertThat(subreddits.getChildren()).isNotEmpty();
        assertThat(subreddits.getChildren().get(0)).isNotNull();
    }

    @Test
    public void tesValidSubredditData() {
        String json = "{" +
                "   \"data\": {" +
                "       \"after\": \"token\", " +
                "       \"children\": [" +
                "           {" +
                "               \"data\": {" +
                "                   \"url\": \"/r/gadgets/\"" +
                "               }" +
                "           }" +
                "       ]" +
                "   }" +
                "}";
        JsonElement jsonElement = mRepository.normalizeList(json);
        Subreddits subreddits = mGson.fromJson(jsonElement, Subreddits.class);
        assertThat(subreddits.getAfter()).isEqualTo("token");
        assertThat(subreddits.getChildren()).isNotEmpty();
        assertThat(subreddits.getChildren().get(0).getUrl()).isEqualToIgnoringCase("/r/gadgets/");
    }
}