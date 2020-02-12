package com.elewise.ldmobile.service;

import android.content.Context;
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


    public boolean getAuthToken(String userName, String password) {
        try {
            ParamAuthorizationResponse response = restHelper.getAuthorizationTokenSync(userName, password);
            token = response.getAccess_token();
            if (!"".equals(token)) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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


    public List<DocumentForList> getDocuments(int size, int from, ProcessType processType, String orderBy, String direction, FilterData[] filterData) {
        try {
            ParamDocumentsResponse response = restHelper.getDocumentsSync(token, size, from, processType, orderBy, direction, filterData);
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
}
