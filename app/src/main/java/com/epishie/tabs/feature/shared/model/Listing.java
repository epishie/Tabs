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

package com.epishie.tabs.feature.shared.model;

import android.support.annotation.NonNull;

import java.util.List;

public class Listing<T> {
    public static final String KIND = "Listing";

    String before;
    String after;
    String modhash;
    List<Thing<T>> children;

    public String getBefore() {
        return before;
    }

    public String getAfter() {
        return after;
    }

    public String getModhash() {
        return modhash;
    }

    public List<Thing<T>> getChildren() {
        return children;
    }

    public void addChildren(@NonNull Listing<T> listing) {
        children.addAll(listing.getChildren());
        before = listing.before;
        after = listing.after;
    }
}