package com.elewise.ldmobile.service;

import android.app.Activity;
import android.content.Context;

import com.elewise.ldmobile.api.ParamGetDocumentDetailsRequest;
import com.elewise.ldmobile.api.ParamGetDocumentsResponse;
import com.elewise.ldmobile.model.Document;
import com.elewise.ldmobile.model.ActionType;
import com.elewise.ldmobile.MainApp;
import com.elewise.ldmobile.R;
import com.elewise.ldmobile.api.ParamAuthorizationResponse;
import com.elewise.ldmobile.model.DocumentDetail;
import com.elewise.ldmobile.model.DocumentForList;
import com.elewise.ldmobile.rest.RestHelper;
import com.elewise.ldmobile.ui.DocsFragment;

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
    private DocumentDetail currentDocumentDetail;

    private Session(Context context) {
        this.context = context;
        this.restHelper = RestHelper.getInstance(context.getString(R.string.rest_server_base_url));
    }


    public boolean getAuthToken(String userName, String password) {
        try {
            ParamAuthorizationResponse response = restHelper.getAuthorizationTokenSync(userName, password);
            token = response.getAccess_token();
            if (token != null && "".equals(token)) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private List<DocumentForList> filterDocsByActionType(ActionType actionType, List<Document> documentList) {
        List<DocumentForList> result = new ArrayList<>();
        if (documentList != null) {
            String lastDate = null;
            for (Document document : documentList) {
                if (document.getAction_type().equals(actionType.name())) {
                    if (lastDate == null || !lastDate.equals(document.getDate())) {
                        lastDate = document.getDate();
                        result.add(new DocumentForList(document, true, lastDate));
                    }
                    result.add(new DocumentForList(document, false, null));

                }
            }
        }
        return result;
    }


    public List<DocumentForList> getDocuments(final ActionType actionType) {
        try {
            ParamGetDocumentsResponse response = restHelper.getDocumentsSync(token);
            return filterDocsByActionType(actionType, Arrays.asList(response.getContents()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public DocumentDetail getDocumentDetail(Document document) {
        try {
            DocumentDetail response = restHelper.getDocumentDetailsSync(token, document.getDoc_type(), document.getDoc_id());
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setCurrentDocumentDetail(DocumentDetail currentDocumentDetail) {
        this.currentDocumentDetail = currentDocumentDetail;
    }

    public DocumentDetail getCurrentDocumentDetail() {
        return currentDocumentDetail;
    }
}
