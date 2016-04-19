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
import android.text.TextUtils;

import java.util.UUID;

public class PreferenceSessionRepository implements SessionRepository {
    private static final String PREFS_NAME = "session";
    private static final String KEY_INSTALLATION_ID = "installation_id";

    private final Context mContext;

    public PreferenceSessionRepository(Context context) {
        mContext = context;
    }

    @Override
    public String getInstallationId() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String installationId = sharedPreferences.getString(KEY_INSTALLATION_ID, "");
        if (TextUtils.isEmpty(installationId)) {
            installationId = UUID.randomUUID().toString();
            sharedPreferences.edit().putString(KEY_INSTALLATION_ID, installationId).apply();
        }
        return installationId;
    }
}
