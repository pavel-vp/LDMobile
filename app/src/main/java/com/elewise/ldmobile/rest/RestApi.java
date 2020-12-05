package com.elewise.ldmobile.rest;

import com.elewise.ldmobile.api.*;

import kotlinx.coroutines.Deferred;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RestApi {

    // Авторизация
    @POST("Authorization")
    Call<ParamAuthorizationResponse> getAuthorizationToken(@Body ParamAuthorizationRequest request);

    // Проверка статуса активности сессии
    @POST("TokenActivityCheck")
    Call<ParamTokenActivityCheckResponse> tokenActivityCheck(@Body ParamTokenActivityCheckRequest request);

    // Загрузить список фильтров
    @POST("GetFilterSettings")
    Deferred<Response<ParamFilterSettingsResponse>> getFilterSettings(@Body ParamFilterSettingsRequest request);

    // Загрузить список документов
    @POST("GetDocuments")
    Deferred<Response<ParamDocumentsResponse>> getDocuments(@Body ParamDocumentsRequest request);

    // Загрузить детализацию по документу
    @POST("GetDocumentDetails")
    Deferred<Response<ParamDocumentDetailsResponse>> getDocumentDetails(@Body ParamDocumentDetailsRequest request);

    // Загрузить файл
    @POST("GetFile")
    Call<byte[]> getFile(@Body ParamGetFileRequest request);

    // Сохранеие файла подписи
    @POST("SaveFileSign")
    Call<ParamSaveFileSignResponse> saveFileSign(@Body ParamSaveFileSignRequest request);

    // Сохранеие файла подписи
    @POST("ExecDocument")
    Call<ParamExecDocumentResponse> execDocument(@Body ParamExecDocumentRequest request);

}
