package com.elewise.ldmobile.service;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.Toast;

import com.elewise.ldmobile.LoginActivity;
import com.elewise.ldmobile.MainApp;
import com.elewise.ldmobile.R;
import com.elewise.ldmobile.api.*;
import com.elewise.ldmobile.api.data.*;
import com.elewise.ldmobile.model.*;
import com.elewise.ldmobile.rest.RestHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kotlinx.coroutines.Deferred;
import retrofit2.Response;
import ru.CryptoPro.JCSP.support.BKSTrustStore;

public class Session {

    // Путь к хранилищу доверенных сертификатов для установки сертификатов.
    public final String TRUST_STORE_PATH = MainApp.getApplcationContext().getApplicationInfo().dataDir +
            File.separator + BKSTrustStore.STORAGE_DIRECTORY + File.separator +
            BKSTrustStore.STORAGE_FILE_TRUST;

    private static Session session;

    public static @NonNull Session getInstance() {
        if (session == null) {
            session = new Session(MainApp.getApplcationContext());
        }
        return session;
    }

    private Context context;
    private RestHelper restHelper;
    private ParamDocumentDetailsResponse currentDocumentDetail;
    private DocumentItem currentDocumentItem;
    private FilterData[] filterData = new FilterData[]{};

    private Session(Context context) {
        this.context = context;
        createRestHelper();
    }

    public void errorAuth() {
        Prefs.INSTANCE.saveToken(context, "");

        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public void createRestHelper() {
        this.restHelper = RestHelper.Companion.createNewInstance(Prefs.INSTANCE.getConnectAddress(context));
    }


    public ParamAuthorizationResponse getAuthToken(String userName, String password) {
        try {
            return restHelper.getAuthorizationTokenSync(userName, password);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ParamTokenActivityCheckResponse tokenActivityCheck(String token) {
        try {
            return restHelper.tokenActivityCheck(token);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<DocumentForList> groupDocByDate(List<Document> documentList) {
        List<DocumentForList> result = new ArrayList<>();
        if (documentList != null) {
            String lastDate = null;
            for (Document document : documentList) {
                if (lastDate == null || !lastDate.equals(document.getDoc_date())) {
                    lastDate = document.getDoc_date();
                    result.add(new DocumentForList(document, true, lastDate));
                }
                result.add(new DocumentForList(document, false, null));
            }
        }
        return result;
    }


    public Deferred<Response<ParamDocumentsResponse>> getDocuments(int size, int from, ProcessType processType, FilterData[] filterData) {
        return restHelper.getDocumentsSync(getToken(), size, from, processType, filterData);
    }

    public Deferred<Response<ParamFilterSettingsResponse>> getFilterSettings() {
        return restHelper.getFilterSettings(getToken());
    }

    public Deferred<Response<ParamDocumentDetailsResponse>> getDocumentDetail(int docId) {
        return restHelper.getDocumentDetailsSync(getToken(), docId);
    }

    public byte[] getFile(int fileId) {
        try {
            ParamGetFileRequest request = new ParamGetFileRequest(getToken(), fileId);
            return restHelper.getFile(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ParamSaveFileSignResponse saveFileSign(FileSign[] signs) {
        try {
            ParamSaveFileSignRequest request = new ParamSaveFileSignRequest(getToken(), signs);
            return restHelper.saveFileSign(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ParamExecDocumentResponse execDocument(int docId, String docType, String action, String comment) {
        try {
            ParamExecDocumentRequest request = new ParamExecDocumentRequest(getToken(), docId, docType, action, comment);
            return restHelper.execDocument(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void setCurrentDocumentDetail(ParamDocumentDetailsResponse currentDocumentDetail) {
        this.currentDocumentDetail = currentDocumentDetail;
    }

    public ParamDocumentDetailsResponse getCurrentDocumentDetail() {
        return currentDocumentDetail;
    }

    public DocumentItem getCurrentDocumentItem() {
        return currentDocumentItem;
    }

    public void setCurrentDocumentItem(DocumentItem currentDocumentItem) {
        this.currentDocumentItem = currentDocumentItem;
    }

    public FilterData[] getFilterData() {
        return filterData;
    }

    public void setFilterData(FilterData[] filterData) {
        this.filterData = filterData;
    }

    public String getToken() {
        return Prefs.INSTANCE.getToken(context);
    }

    public void setToken(String token) {
        Prefs.INSTANCE.saveToken(context, token);
    }
}
