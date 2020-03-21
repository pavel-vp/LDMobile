package com.elewise.ldmobile.rest;

import android.util.Log;

import com.elewise.ldmobile.BuildConfig;
import com.elewise.ldmobile.api.*;
import com.elewise.ldmobile.model.ProcessType;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;

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

    private OkHttpClient createOkHttpClient() {
        if (BuildConfig.DEBUG) {
            return new OkHttpClient().newBuilder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
//                    .addInterceptor(authInterceptor())
                    .addInterceptor(createHttpLoggingInterceptor())
                    .build();
        } else {
            return new OkHttpClient().newBuilder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .addInterceptor(createHttpLoggingInterceptor())
//                    .addInterceptor(authInterceptor())
                    .build();
        }
    }

    private HttpLoggingInterceptor createHttpLoggingInterceptor() {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(message -> {
            Log.e("httpLog", message);
        });
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return httpLoggingInterceptor;
    }

    public RestHelper(String baseUrl) {
        OkHttpClient client = createOkHttpClient();


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


    public ParamDocumentsResponse getDocumentsSync(String token, int size, int from, ProcessType processType, FilterData[] filterData) throws IOException {
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

    public byte[] getFile(ParamGetFileRequest request) throws IOException {
        Call<byte[]> callRes = service.getFile(request);
        return callRes.execute().body();
    }

    public ParamSaveFileSignResponse saveFileSign(ParamSaveFileSignRequest request) throws IOException {
        Call<ParamSaveFileSignResponse> callRes = service.saveFileSign(request);
        return callRes.execute().body();
    }

    public ParamExecDocumentResponse execDocument(ParamExecDocumentRequest request) throws IOException {
        Call<ParamExecDocumentResponse> callRes = service.execDocument(request);
        return callRes.execute().body();
    }
}
