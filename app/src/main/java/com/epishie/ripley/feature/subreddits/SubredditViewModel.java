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

package com.epishie.ripley.feature.subreddits;

import android.os.Parcel;
import android.os.Parcelable;

import com.epishie.ripley.feature.shared.model.Sort;

public class SubredditViewModel implements Parcelable {
    private final String mName;

    public SubredditViewModel(String name) {
        mName = name;
    }

    public SubredditViewModel(Parcel in) {
        mName = in.readString();
    }

    public String getName() {
        return mName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
    }

    public static final Creator<SubredditViewModel> CREATOR = new Creator<SubredditViewModel>() {
        @Override
        public SubredditViewModel createFromParcel(Parcel source) {
            return new SubredditViewModel(source);
        }

        @Override
        public SubredditViewModel[] newArray(int size) {
            return new SubredditViewModel[size];
        }
    };
}
