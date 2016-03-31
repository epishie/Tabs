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

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.epishie.ripley.R;

import java.util.ArrayList;
import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostViewHolder> {
    private final List<PostViewModel> mPosts;
    private LayoutInflater mInflater;

    public PostsAdapter() {
        mPosts = new ArrayList<>();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mInflater = LayoutInflater.from(recyclerView.getContext());
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PostViewHolder(mInflater.inflate(R.layout.item_post,
                parent,
                false));
    }

    @Override
    public void onBindViewHolder(PostViewHolder holder, int position) {
        PostViewModel post = mPosts.get(position);
        holder.mTitle.setText(post.getTitle());
        holder.mByLine.setText(post.getByLine());
        holder.mScore.setText(post.getScore());
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public void addPosts(@NonNull List<PostViewModel> posts) {
        int index = mPosts.size();
        mPosts.addAll(posts);
        notifyItemRangeInserted(index, posts.size());
    }

    protected static class PostViewHolder extends RecyclerView.ViewHolder {
        private final TextView mTitle;
        private final TextView mByLine;
        private final TextView mScore;
        public PostViewHolder(View itemView) {
            super(itemView);
            mTitle = (TextView) itemView.findViewById(R.id.post_title);
            mByLine = (TextView) itemView.findViewById(R.id.post_by_line);
            mScore = (TextView) itemView.findViewById(R.id.post_score);
        }
    }
}
