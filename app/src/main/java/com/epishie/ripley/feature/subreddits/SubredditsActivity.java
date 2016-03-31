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

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.epishie.ripley.App;
import com.epishie.ripley.R;
import com.epishie.ripley.feature.posts.PostsFragment;
import com.epishie.ripley.feature.shared.model.Subreddit;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class SubredditsActivity extends AppCompatActivity implements SubredditsFeature.View {
    @Inject
    protected SubredditsFeature.Presenter mPresenter;
    private ActionBarDrawerToggle mDrawerToggle;
    private SubredditsAdapter mAdapter;
    private ViewPager mPages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        injectDependencies();

        setContentView(R.layout.activity_subreddits);
        setupDrawer();
        setupView();

        if (savedInstanceState == null) {
            mPresenter.onLoad();
        } else {
            mAdapter.restoreState(savedInstanceState);
        }
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mAdapter.saveState(outState);
    }

    private void injectDependencies() {
        App app = (App) getApplication();
        app.getComponent().inject(this);
    }

    private void setupDrawer() {
        setTitle("");
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerToggle = new ActionBarDrawerToggle(this,
                drawer,
                toolbar,
                R.string.des_drawer_open,
                R.string.des_drawer_close);
    }

    private void setupView() {
        mPresenter.setView(this);
        mAdapter = new SubredditsAdapter(getSupportFragmentManager());
        mPages = (ViewPager) findViewById(R.id.pages);
        mPages.setAdapter(mAdapter);
        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setupWithViewPager(mPages);
    }

    @Override
    public void showSubreddits(List<SubredditViewModel> subreddits) {
        mAdapter.addSubreddits(subreddits);
    }

    private static class SubredditsAdapter extends FragmentStatePagerAdapter {
        private static final String STATE_SUBREDDITS = SubredditsAdapter.class.getName() + ".STATE_SUBREDDITS";
        private final List<SubredditViewModel> mSubreddits;

        public SubredditsAdapter(FragmentManager fm) {
            super(fm);
            mSubreddits = new ArrayList<>();
        }

        @Override
        public Fragment getItem(int position) {
            SubredditViewModel subreddit = mSubreddits.get(position);
            return PostsFragment.createInstance(subreddit.getName());
        }

        @Override
        public CharSequence getPageTitle(int position) {
            SubredditViewModel subreddit = mSubreddits.get(position);
            return subreddit.getName();
        }

        @Override
        public int getCount() {
            return mSubreddits.size();
        }

        public void addSubreddits(List<SubredditViewModel> subreddits) {
            mSubreddits.addAll(subreddits);
            notifyDataSetChanged();
        }

        public void saveState(Bundle outState)  {
            outState.putParcelableArrayList(STATE_SUBREDDITS, (ArrayList<SubredditViewModel>) mSubreddits);
        }

        public void restoreState(Bundle savedState) {
            mSubreddits.clear();
            List<SubredditViewModel> subreddits = savedState.getParcelableArrayList(STATE_SUBREDDITS);
            addSubreddits(subreddits);
        }
    }
}