package com.elewise.ldmobile.rest;

import com.elewise.ldmobile.api.*;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RestApi {

    // Авторизация
    @POST("ldmobile/Authorization")
    Call<ParamAuthorizationResponse> getAuthorizationToken(@Body ParamAuthorizationRequest request);

    // Загрузить список фильтров
    @POST("ldmobile/GetFilterSettings")
    Call<ParamFilterSettingsResponse> getFilterSettings(@Body ParamFilterSettingsRequest request);

    // Загрузить список документов
    @POST("ldmobile/GetDocuments")
    Call<ParamDocumentsResponse> getDocuments(@Body ParamDocumentsRequest request);

    // Загрузить детализацию по документу
    @POST("ldmobile/GetDocumentDetails")
    Call<ParamDocumentDetailsResponse> getDocumentDetails(@Body ParamDocumentDetailsRequest request);

    // Загрузить файл
    @POST("ldmobile/GetFile")
    Call<byte[]> getFile(@Body ParamGetFileRequest request);

    // Сохранеие файла подписи
    @POST("ldmobile/SaveFileSign")
    Call<ParamSaveFileSignResponse> saveFileSign(@Body ParamSaveFileSignRequest request);

    // Сохранеие файла подписи
    @POST("ldmobile/ExecDocument")
    Call<ParamExecDocumentResponse> execDocument(@Body ParamExecDocumentRequest request);

}
