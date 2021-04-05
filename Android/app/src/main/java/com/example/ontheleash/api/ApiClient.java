package com.example.ontheleash.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static ApiClient mInstance;
    private static final String BASE_URL = "https://na-povodke.ru/";
    private final Retrofit mRetrofit;

    private ApiClient() {
        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static ApiClient getInstance() {
        if (mInstance == null) {
            mInstance = new ApiClient();
        }
        return mInstance;
    }

    public OnTheLeashApi getApi() {
        return mRetrofit.create(OnTheLeashApi.class);
    }
}
