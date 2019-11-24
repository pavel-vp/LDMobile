package com.elewise.ldmobile.rest;

import com.elewise.ldmobile.api.GetDocumentDetailsParameters;
import com.elewise.ldmobile.api.ParamAuthorizationRequest;
import com.elewise.ldmobile.api.ParamAuthorizationResponse;
import com.elewise.ldmobile.api.ParamGetDocumentDetailsRequest;
import com.elewise.ldmobile.api.ParamGetDocumentsRequest;
import com.elewise.ldmobile.api.ParamGetDocumentsResponse;
import com.elewise.ldmobile.model.ActionType;
import com.elewise.ldmobile.model.DocType;
import com.elewise.ldmobile.model.Document;
import com.elewise.ldmobile.model.DocumentAttachment;
import com.elewise.ldmobile.model.DocumentDetail;
import com.elewise.ldmobile.model.DocumentHistory;
import com.elewise.ldmobile.model.DocumentItem;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RestHelper {

    private static RestHelper restHelper = null;
    private static RestApi service = null;

    public static RestHelper getInstance(String baseUrl) {
        if (restHelper == null) {
            restHelper = new RestHelper(baseUrl);
        }
        return restHelper;
    }

    public RestHelper(String baseUrl) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();


        Retrofit retrofit = new Retrofit.Builder()
//                .addConverterFactory(SimpleXmlConverterFactory.create())
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                //.addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        service = retrofit.create(RestApi.class);
    }

    private void getAuthorizationToken(Callback<ParamAuthorizationResponse> cb, String userName, String password) {
        Call<ParamAuthorizationResponse> callRes = service.getAuthorizationToken(new ParamAuthorizationRequest(userName, password));
        callRes.enqueue(cb);
    }

    public ParamAuthorizationResponse getAuthorizationTokenSync(String userName, String password) throws IOException {
        Call<ParamAuthorizationResponse> callRes = service.getAuthorizationToken(new ParamAuthorizationRequest(userName, password));
        return callRes.execute().body();
    }


    public void getDocuments(Callback<ParamGetDocumentsResponse> cb, String token) {
        Call<ParamGetDocumentsResponse> callRes = service.getDocuments(new ParamGetDocumentsRequest(token, null));
        callRes.enqueue(cb);
    }

    public ParamGetDocumentsResponse getDocumentsSync(String token) throws IOException {
        //Call<ParamGetDocumentsResponse> callRes = service.getDocuments(new ParamGetDocumentsRequest(token, null));
        //return callRes.execute().body();
        Document[] docs = new Document[] {
                new Document(DocType.DOG.name(), 1, "Договор 1", "ООО Элма", "I", ActionType.APPROVE.name(), "10.10.2019", false),
                new Document(DocType.ACTSV.name(), 2, "Договор 1", "ООО Элма", "I", ActionType.APPROVE.name(), "10.10.2019", true),
                new Document(DocType.ACTVR.name(), 3, "Договор 1", "ООО Элма", "I", ActionType.SIGNATURE.name(), "11.10.2019", true),
                new Document(DocType.TORG12.name(), 4, "Договор 1", "ООО Элма", "I", ActionType.SIGNATURE.name(), "11.10.2019", false),
                new Document(DocType.SF.name(), 5, "Договор 1", "ООО Элма", "I", ActionType.APPROVE.name(), "12.10.2019", true),
                new Document(DocType.DOG.name(), 6, "Договор 1", "ООО Элма", "I", ActionType.APPROVE.name(), "13.10.2019", false)
        };
        return new ParamGetDocumentsResponse(docs.length, docs);
    }

    public void getDocumentDetails(Callback<DocumentDetail> cb, String token, String doc_type, Integer doc_id) {
        Call<DocumentDetail> callRes = service.getDocumentDetails(new ParamGetDocumentDetailsRequest(token, new GetDocumentDetailsParameters(doc_id, doc_type)));
        callRes.enqueue(cb);
    }

    public DocumentDetail getDocumentDetailsSync(String token, String doc_type, Integer doc_id) throws IOException {
        //Call<DocumentDetail> callRes = service.getDocumentDetails(new ParamGetDocumentDetailsRequest(token, new GetDocumentDetailsParameters(doc_id, doc_type)));
        //return callRes.execute().body();


        DocumentItem[] items = new DocumentItem[] {
                new DocumentItem("1", "Item 1", "11111", "pack", "Штука", "1", "1", "1", "223.11", "221.23", "18", "2323.44", "2433.33"),
                new DocumentItem("2", "Item 2", "11111", "pack", "Штука", "1", "1", "1", "223.11", "221.23", "18", "2323.44", "2433.33"),
                new DocumentItem("3", "Item 3", "11111", "pack", "Штука", "1", "1", "1", "223.11", "221.23", "18", "2323.44", "2433.33"),
                new DocumentItem("4", "Item 4", "11111", "pack", "Штука", "1", "1", "1", "223.11", "221.23", "18", "2323.44", "2433.33"),
                new DocumentItem("5", "Item 5", "11111", "pack", "Штука", "1", "1", "1", "223.11", "221.23", "18", "2323.44", "2433.33"),
                new DocumentItem("6", "Item 6", "11111", "pack", "Штука", "1", "1", "1", "223.11", "221.23", "18", "2323.44", "2433.33")
        };
        DocumentHistory[] history = new DocumentHistory[] {
                new DocumentHistory("1", "10.11.2018 11:00:00", "Иванов А.А.", "Документ создан"),
                new DocumentHistory("2", "10.11.2018 12:00:00", "Иванов А.А.", "Документ исправлен"),
                new DocumentHistory("3", "10.11.2018 13:00:00", "Иванов А.А.", "Документ исправлен"),
                new DocumentHistory("4", "10.11.2018 14:00:00", "Иванов А.А.", "Документ заапрувлен")
        };
        DocumentAttachment[] attachments = new DocumentAttachment[] {
                new DocumentAttachment(1, "attachemnt1.pdf"),
                new DocumentAttachment(2, "attachemnt2.pdf")
        };
        DocumentDetail docDetail = new DocumentDetail(DocType.DOG.name(), 1, "Договор 1", "ООО Элма", "I", ActionType.APPROVE.name(), "ООО Капитал", "ООО Грузов", "ООО Плательщиков", "ООО Заплатив", "ООО Другов", "ООО Ктотов", "ООО Делатель", "ООО Покупателев", "Приобретение товаров", "75875765", "121212.23", "2223334.22", "23244.22", items, history, attachments);
        return docDetail;
    }

}
