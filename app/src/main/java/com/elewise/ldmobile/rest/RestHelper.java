package com.elewise.ldmobile.rest;

import com.elewise.ldmobile.api.*;
import com.elewise.ldmobile.model.ProcessType;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class RestHelper {

    private static RestHelper restHelper = null;
    private static RestApi service = null;

    public static RestHelper getInstance(String baseUrl) {
        if (restHelper == null) {
            restHelper = new RestHelper(baseUrl);
        }
        return restHelper;
    }

    public RestHelper(String baseUrl) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        service = retrofit.create(RestApi.class);
    }

    public ParamAuthorizationResponse getAuthorizationTokenSync(String userName, String password) throws IOException {
        Call<ParamAuthorizationResponse> callRes = service.getAuthorizationToken(new ParamAuthorizationRequest(userName, password));
        return callRes.execute().body();
    }


    public ParamDocumentsResponse getDocumentsSync(String token, int size, int from, ProcessType processType, String orderBy, String direction, FilterData[] filterData) throws IOException {
        Call<ParamDocumentsResponse> callRes = service.getDocuments(new ParamDocumentsRequest(token, size, from, processType.getType(), filterData));
        return callRes.execute().body();
    }

    public ParamDocumentDetailsResponse getDocumentDetailsSync(String token, String doc_type, Integer doc_id) throws IOException {
        Call<ParamDocumentDetailsResponse> callRes = service.getDocumentDetails(new ParamDocumentDetailsRequest(token, doc_id, doc_type));
        return callRes.execute().body();
    }

    public ParamFilterSettingsResponse getFilterSettings(String token) throws IOException {
        Call<ParamFilterSettingsResponse> callRes = service.getFilterSettings(new ParamFilterSettingsRequest(token));
        return callRes.execute().body();
    }
}
