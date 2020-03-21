package com.elewise.ldmobile.service;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.elewise.ldmobile.MainApp;
import com.elewise.ldmobile.R;
import com.elewise.ldmobile.api.*;
import com.elewise.ldmobile.api.data.*;
import com.elewise.ldmobile.model.*;
import com.elewise.ldmobile.rest.RestHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Session {

    private static Session session;

    public static Session getInstance() {
        if (session == null) {
            session = new Session(MainApp.getApplcationContext());
        }
        return session;
    }

    private Context context;
    private RestHelper restHelper;
    private String token;
    private ParamDocumentDetailsResponse currentDocumentDetail;
    private DocumentItem currentDocumentItem;
    private FilterData[] filterData = new FilterData[]{};

    private Session(Context context) {
        this.context = context;
        this.restHelper = RestHelper.getInstance(context.getString(R.string.rest_server_base_url));
    }


    public ParamAuthorizationResponse getAuthToken(String userName, String password) {
        try {
            return restHelper.getAuthorizationTokenSync(userName, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<DocumentForList> groupDocByDate(List<Document> documentList) {
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


    public List<DocumentForList> getDocuments(int size, int from, ProcessType processType, FilterData[] filterData) {
        try {
            ParamDocumentsResponse response = restHelper.getDocumentsSync(token, size, from, processType, filterData);
            return groupDocByDate(Arrays.asList(response.getContents()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ParamFilterSettingsResponse getFilterSettings() {
        try {
            return restHelper.getFilterSettings(token);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ParamDocumentDetailsResponse getDocumentDetail(int docId, String docType) {
        try {
            ParamDocumentDetailsResponse response = restHelper.getDocumentDetailsSync(token, docType, docId);
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] getFile(int fileId) {
        try {
            ParamGetFileRequest request = new ParamGetFileRequest(token, fileId);
            return restHelper.getFile(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ParamSaveFileSignResponse saveFileSign(FileSign[] signs) {
        try {
            ParamSaveFileSignRequest request = new ParamSaveFileSignRequest(token, signs);
            return restHelper.saveFileSign(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ParamExecDocumentResponse execDocument(String docId, String docType, String action, String comment) {
        try {
            ParamExecDocumentRequest request = new ParamExecDocumentRequest(token, docId, docType, action, comment);
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

    public void setToken(String token) {
        this.token = token;
    }
}
