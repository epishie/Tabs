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

package com.epishie.ripley.feature.shared.repository;

import android.support.annotation.VisibleForTesting;

import com.epishie.ripley.error.ConnectionError;
import com.epishie.ripley.error.ResponseError;
import com.epishie.ripley.feature.shared.model.Subreddits;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Map;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.GET;
import rx.Observable;
import rx.functions.Func1;

public class RetrofitRedditRepository implements RedditRepository {
    private final Service mService;
    private final Gson mGson;

    public RetrofitRedditRepository(String baseUrl) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
        mService = retrofit.create(Service.class);
        mGson = new GsonBuilder().create();
    }

    @Override
    public Observable<Subreddits> getSubreddits() {
        return mService.getSubreddits()
                .map(getSubredditsMapper())
                .onErrorReturn(new Func1<Throwable, Subreddits>() {
                    @Override
                    public Subreddits call(Throwable throwable) {
                        throw new ConnectionError(throwable);
                    }
                });
    }

    private Func1<String, Subreddits> getSubredditsMapper() {
        return new Func1<String, Subreddits>() {
            @Override
            public Subreddits call(String s) {
                Subreddits subreddits = mGson.fromJson(normalizeList(s), Subreddits.class);
                if (subreddits == null) {
                    throw new ResponseError();
                }
                return subreddits;
            }
        };
    }

    @VisibleForTesting
    protected JsonElement normalizeList(String json) {
        JsonElement jsonElement = new JsonParser().parse(json);
        // Root
        if (!jsonElement.isJsonObject()) {
            return jsonElement;
        }

        // List Object
        JsonObject listObject = jsonElement.getAsJsonObject();
        if (!listObject.has("data") || !listObject.get("data").isJsonObject()) {
            return jsonElement;
        }

        // List data
        JsonObject listData = listObject.getAsJsonObject("data");
        listObject.remove("data");
        for (Map.Entry<String, JsonElement> entry : listData.entrySet()) {
            if (entry.getKey().equals("children")) {
                listObject.remove("children");
                if (!entry.getValue().isJsonArray()) {
                    continue;
                }
                JsonArray children = entry.getValue().getAsJsonArray();
                JsonArray parsedChildren = new JsonArray();
                for (JsonElement child : children) {
                    JsonElement parsedChild = normalizeChild(child);
                    if (parsedChild != null) {
                        parsedChildren.add(parsedChild);
                    }
                }

                listObject.add("children", parsedChildren);
                continue;
            }
            listObject.add(entry.getKey(), entry.getValue());
        }

        return jsonElement;
    }

    private JsonElement normalizeChild(JsonElement jsonElement) {
        if (!jsonElement.isJsonObject()) {
            return jsonElement;
        }
        JsonObject childObject = jsonElement.getAsJsonObject();
        if (!childObject.has("data") || !childObject.get("data").isJsonObject()) {
            return jsonElement;
        }
        JsonObject childData = childObject.getAsJsonObject("data");
        childObject.remove("data");
        for (Map.Entry<String, JsonElement> entry : childData.entrySet()) {
            childObject.add(entry.getKey(), entry.getValue());
        }

        return childObject;
    }

    public interface Service {
        @GET("subreddits/default.json")
        Observable<String> getSubreddits();
    }
}