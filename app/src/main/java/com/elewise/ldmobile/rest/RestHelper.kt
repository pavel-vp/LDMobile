package com.elewise.ldmobile.rest

import android.util.Log
import com.elewise.ldmobile.BuildConfig
import com.elewise.ldmobile.api.*
import com.elewise.ldmobile.model.ProcessType
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit


class RestHelper(baseUrl: String) {
    private val restApi: RestApi

    init {
        val client = createOkHttpClient()
        val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .client(client)
                .build()
        restApi = retrofit.create(RestApi::class.java)
    }

    private fun createOkHttpClient(): OkHttpClient {
        return if (BuildConfig.DEBUG) {
            OkHttpClient().newBuilder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .addInterceptor(createHttpLoggingInterceptor())
                    .build()
        } else {
            OkHttpClient().newBuilder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .addInterceptor(createHttpLoggingInterceptor())
                    .build()
        }
    }

    private fun createHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val httpLoggingInterceptor = HttpLoggingInterceptor { message ->
            Log.e("httpLog", message)
        }
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        return httpLoggingInterceptor
    }

    @Throws(IOException::class)
    fun getAuthorizationTokenSync(userName: String?, password: String?): ParamAuthorizationResponse? {
        val callRes = restApi.getAuthorizationToken(ParamAuthorizationRequest(userName!!, password!!))
        return callRes.execute().body()
    }

    @Throws(IOException::class)
    fun tokenActivityCheck(token: String?): ParamTokenActivityCheckResponse? {
        val callRes = restApi.tokenActivityCheck(ParamTokenActivityCheckRequest(token!!))
        return callRes.execute().body()
    }

    fun getDocumentsSync(token: String, size: Int, from: Int, processType: ProcessType, filterData: Array<FilterData>?): Deferred<Response<ParamDocumentsResponse>> {
        return restApi.getDocuments(ParamDocumentsRequest(token, size, from, processType.type, filterData))
    }

    fun getDocumentDetailsSync(token: String, doc_id: Int): Deferred<Response<ParamDocumentDetailsResponse>> {
        return restApi.getDocumentDetails(ParamDocumentDetailsRequest(token, doc_id))
    }

    fun getFilterSettings(token: String?): Deferred<Response<ParamFilterSettingsResponse>> {
        return restApi.getFilterSettings(ParamFilterSettingsRequest(token!!))
    }

    @Throws(IOException::class)
    fun getFile(request: ParamGetFileRequest?): ByteArray? {
        val callRes = restApi.getFile(request)
        return callRes.execute().body()
    }

    @Throws(IOException::class)
    fun saveFileSign(request: ParamSaveFileSignRequest?): ParamSaveFileSignResponse? {
        val callRes = restApi.saveFileSign(request)
        return callRes.execute().body()
    }

    @Throws(IOException::class)
    fun execDocument(request: ParamExecDocumentRequest?): ParamExecDocumentResponse? {
        val callRes = restApi.execDocument(request)
        return callRes.execute().body()
    }

    companion object {
        fun createNewInstance(baseUrl: String): RestHelper {
            return RestHelper(baseUrl)
        }
    }
}