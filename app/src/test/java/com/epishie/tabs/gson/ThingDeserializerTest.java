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
import com.epishie.tabs.feature.shared.model.Thing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ThingDeserializerTest {
    Gson mGson;

    @Before
    public void setUp() {
        mGson = new GsonBuilder()
                .registerTypeAdapter(Thing.class, new ThingDeserializer())
                .create();
    }

    @Test(expected = JsonParseException.class)
    public void testRootIsNotObject() {
        String json = "[]";
        mGson.fromJson(json, Thing.class);
    }

    @Test(expected = JsonParseException.class)
    public void testRootHasNoKind() {
        String json = "{" +
                "}";
        mGson.fromJson(json, Thing.class);
    }

    @Test(expected = JsonParseException.class)
    public void testRootKindIsNotObject() {
        String json = "{" +
                "   \"kind\": {}" +
                "}";
        mGson.fromJson(json, Thing.class);
    }

    @Test
    public void testKindIsLink() {
        String json = "{" +
                "   \"id\": \"TEST_ID\"," +
                "   \"name\": \"TEST_NAME\"," +
                "   \"kind\": \"t3\"," +
                "   \"data\": {" +
                "       \"title\": \"TEST_TITLE\"" +
                "   }" +
                "}";
        // noinspection unchecked
        Thing<Link> linkThing = mGson.fromJson(json, Thing.class);
        assertThat(linkThing).isNotNull();
        assertThat(linkThing.getId()).isEqualTo("TEST_ID");
        assertThat(linkThing.getName()).isEqualTo("TEST_NAME");
        assertThat(linkThing.getData()).isNotNull();
        assertThat(linkThing.getData().getTitle()).isEqualTo("TEST_TITLE");
    }

    @Test
    public void testKindIsListing() {
        String json = "{" +
                "   \"data\": {" +
                "       \"before\": \"TEST_BEFORE\"," +
                "       \"after\": \"TEST_AFTER\"," +
                "       \"modhash\": \"TEST_MODHASH\"," +
                "       \"children\": [" +
                "           {" +
                "               \"data\": {" +
                "                   \"title\": \"TEST_TITLE_1\"" +
                "               }," +
                "               \"kind\": \"t3\"" +
                "           }," +
                "           {" +
                "               \"data\": {" +
                "                   \"title\": \"TEST_TITLE_2\"" +
                "               }," +
                "               \"kind\": \"t3\"" +
                "           }" +
                "       ]" +
                "   }," +
                "   \"kind\": \"Listing\"" +
                "}";
        // noinspection unchecked
        Thing<Listing<Link>> listingThing = mGson.fromJson(json, Thing.class);
        assertThat(listingThing).isNotNull();
        assertThat(listingThing.getId()).isNull();
        assertThat(listingThing.getName()).isNull();
        assertThat(listingThing.getData()).isNotNull();
        assertThat(listingThing.getData().getBefore()).isEqualTo("TEST_BEFORE");
        assertThat(listingThing.getData().getAfter()).isEqualTo("TEST_AFTER");
        assertThat(listingThing.getData().getModhash()).isEqualTo("TEST_MODHASH");
        assertThat(listingThing.getData().getChildren()).hasSize(2);
        assertThat(listingThing.getData().getChildren().get(0).getData()).isNotNull();
        assertThat(listingThing.getData().getChildren().get(0).getData().getTitle()).isEqualTo("TEST_TITLE_1");
        assertThat(listingThing.getData().getChildren().get(1).getData()).isNotNull();
        assertThat(listingThing.getData().getChildren().get(1).getData().getTitle()).isEqualTo("TEST_TITLE_2");
    }

    @Test
    public void testKindIsComment() {
        String json = "{" +
                "   \"kind\": \"t1\"," +
                "   \"data\": {" +
                "       \"author\": \"TEST_AUTHOR\"," +
                "       \"body_html\": \"TEST_BODY_HTML\"" +
                "   }" +
                "}";
        // noinspection unchecked
        Thing<Comment> commentThing = mGson.fromJson(json, Thing.class);
        assertThat(commentThing).isNotNull();
        assertThat(commentThing.getData()).isNotNull();
        assertThat(commentThing.getData().getAuthor()).isEqualTo("TEST_AUTHOR");
        assertThat(commentThing.getData().getBodyHtml()).isEqualTo("TEST_BODY_HTML");
    }
}