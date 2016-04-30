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

package com.epishie.tabs.gson;

import com.epishie.tabs.feature.shared.model.Comment;
import com.epishie.tabs.feature.shared.model.Link;
import com.epishie.tabs.feature.shared.model.Listing;
import com.epishie.tabs.feature.shared.model.Subreddit;
import com.epishie.tabs.feature.shared.model.Thing;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class ThingDeserializer implements JsonDeserializer<Thing> {
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String KIND = "kind";
    private static final String DATA = "data";

    @Override
    public Thing deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!json.isJsonObject()) {
            throw new JsonParseException("Expected json object");
        }
        JsonObject jsonObject = json.getAsJsonObject();
        if (!jsonObject.has(KIND)) {
            throw new JsonParseException("Expected to have \"kind\" field");
        }
        JsonElement kindElement = jsonObject.get(KIND);
        if (!kindElement.isJsonPrimitive()) {
            throw new JsonParseException("Expected to have a primitive \"kind\" field");
        }
        if (!jsonObject.has(DATA)) {
            throw new JsonParseException("Expected to have a\"data\" field");
        }
        String kind = kindElement.getAsString();
        String id = null;
        String name = null;
        if (jsonObject.has(ID)) {
            JsonElement idElement = jsonObject.get(ID);
            id = idElement.getAsString();
        }
        if (jsonObject.has(NAME)) {
            JsonElement nameElement = jsonObject.get(NAME);
            name = nameElement.getAsString();
        }
        Thing thing = null;
        switch (kind) {
            case Link.KIND:
                Link link = context.deserialize(jsonObject.get(DATA), Link.class);
                thing = new Thing<>(id, name, link);
                break;
            case Listing.KIND:
                Listing listing = context.deserialize(jsonObject.get(DATA), Listing.class);
                // noinspection unchecked
                thing = new Thing(id, name, listing);
                break;
            case Subreddit.KIND:
                Subreddit subreddit = context.deserialize(jsonObject.get(DATA), Subreddit.class);
                // noinspection unchecked
                thing = new Thing(id, name, subreddit);
                break;
            case Comment.KIND:
                Comment comment = context.deserialize(jsonObject.get(DATA), Comment.class);
                // noinspection unchecked
                thing = new Thing(id, name, comment);
                break;
        }
        return thing;
    }
}