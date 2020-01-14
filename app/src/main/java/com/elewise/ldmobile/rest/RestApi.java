package com.elewise.ldmobile.rest;

import com.elewise.ldmobile.api.ParamAuthorizationRequest;
import com.elewise.ldmobile.api.ParamAuthorizationResponse;
import com.elewise.ldmobile.api.ParamGetDocumentDetailsRequest;
import com.elewise.ldmobile.api.ParamGetDocumentsRequest;
import com.elewise.ldmobile.api.ParamGetDocumentsResponse;
import com.elewise.ldmobile.api.ParamRespDocumentDetailsResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RestApi {


    // Авторизация
    @POST("ldmobile/Authorization")
    Call<ParamAuthorizationResponse> getAuthorizationToken(@Body ParamAuthorizationRequest request);

    // Загрузить список документов
    @POST("ldmobile/GetDocuments")
    Call<ParamGetDocumentsResponse> getDocuments(@Body ParamGetDocumentsRequest request);

    // Загрузить детализацию по документу
    @POST("ldmobile/GetDocumentDetails")
    Call<ParamRespDocumentDetailsResponse> getDocumentDetails(@Body ParamGetDocumentDetailsRequest request);

}
