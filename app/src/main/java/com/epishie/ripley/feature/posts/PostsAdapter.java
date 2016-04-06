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
import android.widget.ImageView;
import android.widget.TextView;

import com.epishie.ripley.R;
import com.epishie.ripley.feature.shared.model.Post;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_LOADING = -1;
    private static final int TYPE_POST = 0;
    private static final int TYPE_POST_PREVIEW = 1;

    private final List<PostViewModel> mPosts;
    private LayoutInflater mInflater;
    private boolean mLoading;

    public PostsAdapter() {
        mPosts = new ArrayList<>();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mInflater = LayoutInflater.from(recyclerView.getContext());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_LOADING) {
            return new PostLoaderViewHolder(mInflater.inflate(R.layout.item_post_loader,
                    parent,
                    false));
        }
        if (viewType == TYPE_POST) {
            return new PostViewHolder(mInflater.inflate(R.layout.item_post,
                    parent,
                    false));
        }
        return new PostPreviewViewHolder(mInflater.inflate(R.layout.item_post_image,
                parent,
                false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        PostViewModel post = mPosts.get(position);
        if (holder instanceof PostPreviewViewHolder) {
            onBindViewHolder((PostPreviewViewHolder) holder, post);
        } else if (holder instanceof PostViewHolder) {
            onBindViewHolder((PostViewHolder) holder, post);
        }
    }

    private void onBindViewHolder(PostViewHolder holder, PostViewModel post) {
        holder.mTitle.setText(post.getTitle());
        holder.mByLine.setText(post.getByLine());
        holder.mScore.setText(post.getScore());
    }

    private void onBindViewHolder(PostPreviewViewHolder holder, PostViewModel post) {
        holder.mTitle.setText(post.getTitle());
        holder.mByLine.setText(post.getByLine());
        holder.mScore.setText(post.getScore());
        Picasso.with(mInflater.getContext())
                .load(post.getPreview())
                .into(holder.mPreview);
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    @Override
    public int getItemViewType(int position) {
        PostViewModel post = mPosts.get(position);
        if (post == null) {
            return TYPE_LOADING;
        }
        if (post.getPreview() == null || post.getPreview().isEmpty()) {
            return TYPE_POST;
        }

        return TYPE_POST_PREVIEW;
    }

    public void addPosts(@NonNull List<PostViewModel> posts) {
        hideLoader();
        int index = mPosts.size();
        mPosts.addAll(posts);
        notifyItemRangeInserted(index, posts.size());
    }

    public void showLoader() {
        if (mLoading) {
            return;
        }
        mLoading = true;
        mPosts.add(null);
        notifyItemInserted(mPosts.size() - 1);
    }

    public void hideLoader() {
        if (!mLoading || mPosts.isEmpty()) {
            return;
        }
        mLoading = false;
        int last = mPosts.size() - 1;
        if (mPosts.get(last) == null) {
            mPosts.remove(last);
            notifyItemRemoved(last);
        }
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

    protected static class PostPreviewViewHolder extends RecyclerView.ViewHolder {
        private final TextView mTitle;
        private final TextView mByLine;
        private final TextView mScore;
        private final ImageView mPreview;
        public PostPreviewViewHolder(View itemView) {
            super(itemView);
            mTitle = (TextView) itemView.findViewById(R.id.post_title);
            mByLine = (TextView) itemView.findViewById(R.id.post_by_line);
            mScore = (TextView) itemView.findViewById(R.id.post_score);
            mPreview = (ImageView) itemView.findViewById(R.id.post_preview);
        }
    }

    protected static class PostLoaderViewHolder extends RecyclerView.ViewHolder {
        public PostLoaderViewHolder(View itemView) {
            super(itemView);
        }
    }
}
