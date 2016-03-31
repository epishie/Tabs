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

package com.epishie.ripley.feature.posts;

import android.os.Parcel;
import android.os.Parcelable;

public class PostViewModel implements Parcelable {
    private final String mTitle;
    private final String mByLine;
    private final String mScore;

    private PostViewModel(Builder builder) {
        mTitle = builder.mTitle;
        mByLine = builder.mByLine;
        mScore = builder.mScore;
    }

    public PostViewModel(Parcel in) {
        mTitle = in.readString();
        mByLine = in.readString();
        mScore = in.readString();
    }

    public String getTitle() {
        return mTitle;
    }

    public String getByLine() {
        return mByLine;
    }

    public String getScore() {
        return mScore;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTitle);
        dest.writeString(mByLine);
        dest.writeString(mScore);
    }

    public static final Creator<PostViewModel> CREATOR = new Creator<PostViewModel>() {
        @Override
        public PostViewModel createFromParcel(Parcel source) {
            return new PostViewModel(source);
        }

        @Override
        public PostViewModel[] newArray(int size) {
            return new PostViewModel[size];
        }
    };

    public static class Builder {
        private String mTitle = "";
        private String mByLine = "";
        private String mScore = "";

        public Builder setTitle(String title) {
            if (title != null && !title.isEmpty()) {
                mTitle = title;
            }
            return this;
        }

        public Builder setByLine(String byLine) {
            if (byLine != null && !byLine.isEmpty()) {
                mByLine = byLine;
            }
            return this;
        }

        public Builder setScore(String score) {
            if (score != null && !score.isEmpty()) {
                mScore = score;
            }
            return this;
        }

        public PostViewModel build() {
            return new PostViewModel(this);
        }
    }
}
