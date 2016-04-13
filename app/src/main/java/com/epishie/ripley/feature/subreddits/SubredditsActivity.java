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

import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.util.Pair;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.epishie.ripley.App;
import com.epishie.ripley.R;
import com.epishie.ripley.feature.posts.PostsFragment;
import com.epishie.ripley.feature.shared.model.Sort;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

public class SubredditsActivity extends AppCompatActivity implements SubredditsFeature.View {
    private static final List<Pair<Integer, Integer>> SORT_OPTIONS = Arrays.asList(
            Pair.create(R.id.sort_hot, R.string.lbl_hot),
            Pair.create(R.id.sort_new, R.string.lbl_new),
            Pair.create(R.id.sort_rising, R.string.lbl_rising),
            Pair.create(R.id.sort_controversial, R.string.lbl_controversial),
            Pair.create(R.id.sort_top, R.string.lbl_top)
    );

    @Inject
    protected SubredditsFeature.Presenter mPresenter;
    private ActionBarDrawerToggle mDrawerToggle;
    private SubredditsAdapter mAdapter;
    private ViewPager mPages;
    private TabLayout mTabs;

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
        getSupportActionBar().setTitle(SORT_OPTIONS.get(mAdapter.getSort().ordinal()).second);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.subreddits, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        SubredditViewModel subreddit = mAdapter.getSubreddit(mPages.getCurrentItem());
        if (subreddit != null) {
            MenuItem sortItem = menu.findItem(SORT_OPTIONS.get(mAdapter.getSort().ordinal()).first);
            sortItem.setChecked(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            PostsFragment fragment = mAdapter.getFragment(mPages.getCurrentItem());
            if (fragment != null) {
                fragment.refresh();
            }
            return true;
        } else if (item.getGroupId() == R.id.sort) {
            for (int i = 0; i < SORT_OPTIONS.size(); i++) {
                if (SORT_OPTIONS.get(i).first == item.getItemId()) {
                    mAdapter.setSort(Sort.values()[i]);
                    getSupportActionBar().setTitle(SORT_OPTIONS.get(i).second);
                    invalidateOptionsMenu();
                    break;
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mAdapter.saveState(outState);
    }

    @Override
    public void showSubreddits(List<SubredditViewModel> subreddits) {
        mAdapter.addSubreddits(subreddits);
        invalidateOptionsMenu();
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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAdapter = new SubredditsAdapter(getSupportFragmentManager());
        mPages = (ViewPager) findViewById(R.id.pages);
        mPages.setAdapter(mAdapter);
        mTabs = (TabLayout) findViewById(R.id.tabs);
        mPages.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabs));
        mTabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (mPages.getCurrentItem() != tab.getPosition()) {
                    mPages.setCurrentItem(tab.getPosition(), true);
                }
                invalidateOptionsMenu();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // No-op
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // No-op
            }
        });
        mAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                mTabs.removeAllTabs();
                int count = mAdapter.getCount();
                for (int i = 0; i < count; i++) {
                    mTabs.addTab(mTabs.newTab()
                            .setText(mAdapter.getPageTitle(i))
                    );
                }
            }
        });
    }

    private static class SubredditsAdapter extends FragmentStatePagerAdapter {
        private static final String STATE_SUBREDDITS = SubredditsAdapter.class.getName() + ".STATE_SUBREDDITS";
        private static final String STATE_SORT = SubredditsAdapter.class.getName() + ".STATE_SORT";
        private final List<SubredditViewModel> mSubreddits;
        private final SparseArrayCompat<WeakReference<PostsFragment>> mFragments;
        private Sort mSort;

        public SubredditsAdapter(FragmentManager fm) {
            super(fm);
            mSubreddits = new ArrayList<>();
            mFragments = new SparseArrayCompat<>();
            mSort = Sort.HOT;
        }

        @Override
        public Fragment getItem(int position) {
            SubredditViewModel subreddit = mSubreddits.get(position);
            return PostsFragment.createInstance(subreddit.getName(), mSort);
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

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            PostsFragment fragment = (PostsFragment) super.instantiateItem(container, position);
            mFragments.put(position, new WeakReference<>(fragment));
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            mFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public void addSubreddits(List<SubredditViewModel> subreddits) {
            mSubreddits.addAll(subreddits);
            notifyDataSetChanged();
        }

        public SubredditViewModel getSubreddit(int position) {
            if (position < mSubreddits.size()) {
                return mSubreddits.get(position);
            }

            return null;
        }

        public Sort getSort() {
            return mSort;
        }

        public void setSort(Sort sort) {
            mSort = sort;
            notifyDataSetChanged();
        }

        public PostsFragment getFragment(int position) {
            WeakReference<PostsFragment> fragmentRef = mFragments.get(position);
            return fragmentRef == null ? null : fragmentRef.get();
        }

        public void saveState(Bundle outState)  {
            outState.putParcelableArrayList(STATE_SUBREDDITS, (ArrayList<SubredditViewModel>) mSubreddits);
            outState.putInt(STATE_SORT, mSort.ordinal());
        }

        public void restoreState(Bundle savedState) {
            mSubreddits.clear();
            List<SubredditViewModel> subreddits = savedState.getParcelableArrayList(STATE_SUBREDDITS);
            addSubreddits(subreddits);
            mSort = Sort.values()[savedState.getInt(STATE_SORT, 0)];
        }
    }
}