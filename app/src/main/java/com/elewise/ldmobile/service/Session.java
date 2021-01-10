package com.elewise.ldmobile.service;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.elewise.ldmobile.MainApp;
import com.elewise.ldmobile.api.Document;
import com.elewise.ldmobile.api.FileSign;
import com.elewise.ldmobile.api.FilterData;
import com.elewise.ldmobile.api.ParamAuthorizationResponse;
import com.elewise.ldmobile.api.ParamDocumentDetailsResponse;
import com.elewise.ldmobile.api.ParamDocumentsResponse;
import com.elewise.ldmobile.api.ParamExecDocumentRequest;
import com.elewise.ldmobile.api.ParamExecDocumentResponse;
import com.elewise.ldmobile.api.ParamExecOperationRequest;
import com.elewise.ldmobile.api.ParamExecOperationResponse;
import com.elewise.ldmobile.api.ParamFilterSettingsResponse;
import com.elewise.ldmobile.api.ParamGetFileRequest;
import com.elewise.ldmobile.api.ParamGetFileResponse;
import com.elewise.ldmobile.api.ParamSaveFileSignRequest;
import com.elewise.ldmobile.api.ParamSaveFileSignResponse;
import com.elewise.ldmobile.api.ParamTokenActivityCheckResponse;
import com.elewise.ldmobile.api.data.DocumentItem;
import com.elewise.ldmobile.model.DocumentForList;
import com.elewise.ldmobile.model.ProcessType;
import com.elewise.ldmobile.rest.RestHelper;
import com.elewise.ldmobile.ui.LoginActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
    private @NonNull List<FilterData> filterData = new ArrayList<>();

    private Session(Context context) {
        this.context = context;
        createRestHelper();
    }

    public void errorAuth() {
        Prefs.INSTANCE.saveLastAuth(context, null);

        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public void createRestHelper() {
        String url = Prefs.INSTANCE.getConnectAddress(context);
        if (url.isEmpty()) {
            url = "http://url_stub.ru";
        }
        this.restHelper = RestHelper.Companion.createNewInstance(url);
    }


    public Deferred<Response<ParamAuthorizationResponse>> getAuthToken(@NonNull String userName, @NonNull String password) throws IOException {
        return restHelper.getAuthorizationTokenSync(userName, password, getDeviceId());
    }

    private String getDeviceId() {
        return android.provider.Settings.Secure.getString(
                context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
    }

    public Deferred<Response<ParamTokenActivityCheckResponse>> tokenActivityCheck(@NonNull String token) throws IOException {
        return restHelper.tokenActivityCheck(token);
    }

    public List<DocumentForList> groupDocByDate(Boolean needGroup, List<Document> documentList) {
        List<DocumentForList> result = new ArrayList<>();
        if (documentList != null) {
            String lastGroup = null;
            for (Document document : documentList) {
                if (needGroup) {
                    String group = document.getGroup() != null ? document.getGroup() : "";
                    if (lastGroup == null || !lastGroup.equals(group)) {
                        lastGroup = group;
                        result.add(new DocumentForList(document, true, lastGroup));
                    }
                }
                result.add(new DocumentForList(document, false, null));
            }
        }
        return result;
    }


    public Deferred<Response<ParamDocumentsResponse>> getDocuments(int from, ProcessType processType, @NonNull List<FilterData> filterData) throws IOException {
        return restHelper.getDocumentsSync(getToken(), docSize, from, processType, filterData);
    }

    public Deferred<Response<ParamFilterSettingsResponse>> getFilterSettings() throws IOException {
        return restHelper.getFilterSettings(getToken());
    }

    public Deferred<Response<ParamDocumentDetailsResponse>> getDocumentDetail(int docId) throws IOException {
        return restHelper.getDocumentDetailsSync(getToken(), docId);
    }

    public Deferred<Response<ParamGetFileResponse>> getFile(int fileId) throws IOException {
        ParamGetFileRequest request = new ParamGetFileRequest(getToken(), fileId);
        return restHelper.getFile(request);
    }

    public Deferred<Response<ParamSaveFileSignResponse>> saveFileSign(List<FileSign> signs) throws IOException {
        ParamSaveFileSignRequest request = new ParamSaveFileSignRequest(getToken(), signs);
        return restHelper.saveFileSign(request);
    }

    public Deferred<Response<ParamExecDocumentResponse>> execDocument(int docId, String buttonType, String action, String comment) throws IOException {
        ParamExecDocumentRequest request = new ParamExecDocumentRequest(getToken(), docId, buttonType, action, comment);
        return restHelper.execDocument(request);
    }

    public Deferred<Response<ParamExecOperationResponse>> execOperation(String action) throws IOException {
        ParamExecOperationRequest request = new ParamExecOperationRequest(getToken(), action);
        return restHelper.execOperation(request);
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

    public @NonNull List<FilterData> getFilterData() {
        return filterData;
    }

    public void setFilterData(@NonNull List<FilterData> filterData) {
        this.filterData = filterData;
    }

    public @NonNull String getToken() {
        ParamAuthorizationResponse auth = Prefs.INSTANCE.getLastAuth(context);
        return  auth == null ? "" : auth.getAccess_token();
    }

    private ParamAuthorizationResponse lastAuth = null;

    public void setLastAuth(ParamAuthorizationResponse auth) {
        lastAuth = auth;
        Prefs.INSTANCE.saveLastAuth(context, auth);
    }

    public @Nullable ParamAuthorizationResponse getLastAuth() {
        if (lastAuth == null) {
            lastAuth = Prefs.INSTANCE.getLastAuth(context);
        }
        return lastAuth;
    }

    private int docSize = 10;

    public int getDocSize() {
        return docSize;
    }

    public void setDocSize(int docSize) {
        this.docSize = docSize;
    }
}
