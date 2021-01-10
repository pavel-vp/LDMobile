package com.elewise.ldmobile.rest;

import com.elewise.ldmobile.api.*;
import com.elewise.ldmobile.api.ParamExecOperationResponse;
import com.elewise.ldmobile.api.ParamGetFileResponse;

import kotlinx.coroutines.Deferred;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RestApi {

    // Авторизация
    @POST("Authorization")
    Deferred<Response<ParamAuthorizationResponse>> getAuthorizationToken(@Body ParamAuthorizationRequest request);

    // Проверка статуса активности сессии
    @POST("TokenActivityCheck")
    Deferred<Response<ParamTokenActivityCheckResponse>> tokenActivityCheck(@Body ParamTokenActivityCheckRequest request);

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
    Deferred<Response<ParamGetFileResponse>> getFile(@Body ParamGetFileRequest request);

    // Сохранеие файла подписи
    @POST("SaveFileSign")
    Deferred<Response<ParamSaveFileSignResponse>> saveFileSign(@Body ParamSaveFileSignRequest request);

    // выполнение операции с документом
    @POST("ExecDocument")
    Deferred<Response<ParamExecDocumentResponse>> execDocument(@Body ParamExecDocumentRequest request);

    // выполнение произвольной операции
    @POST("ExecOperation")
    Deferred<Response<ParamExecOperationResponse>> execOperation(@Body ParamExecOperationRequest request);
}
