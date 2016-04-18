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

package com.epishie.tabs.feature.shared.repository;

import android.content.Context;
import android.content.SharedPreferences;

import org.assertj.android.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.android.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class PreferenceSessionRepositoryTest {
    PreferenceSessionRepository mRepository;
    SharedPreferences mSharedPreferences;

    @Before
    public void setUp() {
        Context context = RuntimeEnvironment.application;
        mRepository = new PreferenceSessionRepository(context);
        mSharedPreferences = context.getSharedPreferences("session", Context.MODE_PRIVATE);
        mSharedPreferences.edit().clear().commit();
    }

    @Test
    public void testGetInstallationIdMissing() {
        String installationId = mRepository.getInstallationId();
        assertThat(mSharedPreferences).contains("installation_id", installationId);
    }

    public void testGetInstallationIdExisting() {
        mSharedPreferences.edit().putString("installation_id", "TEST_INSTALLATION_ID").commit();
        String installationId = mRepository.getInstallationId();
        assertThat(installationId).isEqualTo("TEST_INSTALLATION_ID");
    }
}