package com.elewise.ldmobile.rest;

import com.elewise.ldmobile.api.ParamAuthorizationRequest;
import com.elewise.ldmobile.api.ParamAuthorizationResponse;
import com.elewise.ldmobile.api.ParamDocumentDetailsRequest;
import com.elewise.ldmobile.api.ParamDocumentDetailsResponse;
import com.elewise.ldmobile.api.ParamDocumentsRequest;
import com.elewise.ldmobile.api.ParamDocumentsResponse;
import com.elewise.ldmobile.api.ParamExecDocumentRequest;
import com.elewise.ldmobile.api.ParamExecDocumentResponse;
import com.elewise.ldmobile.api.ParamExecOperationRequest;
import com.elewise.ldmobile.api.ParamExecOperationResponse;
import com.elewise.ldmobile.api.ParamFilterSettingsRequest;
import com.elewise.ldmobile.api.ParamFilterSettingsResponse;
import com.elewise.ldmobile.api.ParamGetFileRequest;
import com.elewise.ldmobile.api.ParamGetFileResponse;
import com.elewise.ldmobile.api.ParamSaveFileSignRequest;
import com.elewise.ldmobile.api.ParamSaveFileSignResponse;
import com.elewise.ldmobile.api.ParamTokenActivityCheckRequest;
import com.elewise.ldmobile.api.ParamTokenActivityCheckResponse;

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
