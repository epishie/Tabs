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

import android.util.Base64;

import com.epishie.tabs.feature.shared.model.Token;

import java.io.IOException;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

public class TokenManager {
    private static final String GRANT_TYPE = "https://oauth.reddit.com/grants/installed_client";
    private final String mClientId;
    private final SessionRepository mSessionRepository;
    private final Service mService;
    private Token mToken;

    public TokenManager(String baseUrl, String clientId, SessionRepository sessionRepository) {
        mClientId = clientId;
        mSessionRepository = sessionRepository;

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        mService = retrofit.create(Service.class);
    }

    public Interceptor getInterceptor() {
        return mInterceptor;
    }

    private final Interceptor mInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            if (mToken == null) {
                String credentials = Credentials.basic(mClientId, "");
                retrofit2.Response<Token> response = mService.authenticateApp(credentials,
                        GRANT_TYPE,
                        "12345678901234567890")
                        //mSessionRepository.getInstallationId())
                        .execute();
                if (!response.isSuccessful()) {
                    return null;
                }
                mToken = response.body();
            }
            Request.Builder builder = chain.request().newBuilder();
            builder.header("Authorization", mToken.getTokenType() + " " + mToken.getAccessToken());
            return chain.proceed(builder.build());
        }
    };

    interface Service {
        @FormUrlEncoded
        @POST("api/v1/access_token")
        Call<Token> authenticateApp(@Header("Authorization") String clientId,
                                    @Field("grant_type") String grantType,
                                    @Field("device_id") String deviceId);
    }
}
