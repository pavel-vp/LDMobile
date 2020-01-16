package com.elewise.ldmobile.rest;

import android.util.Log;

import com.elewise.ldmobile.api.GetDocumentDetailsParameters;
import com.elewise.ldmobile.api.ParamAuthorizationRequest;
import com.elewise.ldmobile.api.ParamAuthorizationResponse;
import com.elewise.ldmobile.api.ParamGetDocumentDetailsRequest;
import com.elewise.ldmobile.api.ParamGetDocumentsRequest;
import com.elewise.ldmobile.api.ParamGetDocumentsResponse;
import com.elewise.ldmobile.model.ProcessType;
import com.elewise.ldmobile.model.DocHeaderAttributes;
import com.elewise.ldmobile.model.DocLineDetail;
import com.elewise.ldmobile.model.DocType;
import com.elewise.ldmobile.model.Document;
import com.elewise.ldmobile.model.DocumentAttachment;
import com.elewise.ldmobile.model.FilterData;
import com.elewise.ldmobile.model.FilterElementList;
import com.elewise.ldmobile.api.ParamRespDocumentDetailsResponse;
import com.elewise.ldmobile.model.DocumentHistory;
import com.elewise.ldmobile.model.DocumentItem;
import com.elewise.ldmobile.model.RelatedDoc;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.kotlin.KotlinModule;

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

    public ParamGetDocumentsResponse getDocumentsSync(String token, int size, int from, ProcessType processType, String orderBy, String direction, FilterData[] filterData) throws IOException {
        //Call<ParamGetDocumentsResponse> callRes = service.getDocuments(new ParamGetDocumentsRequest(token, null));
        //return callRes.execute().body();

        boolean flagFilterContractorElma = false;
        for (FilterData item: filterData) {
            if (item.getName().equals("contractor") && item.getValue().equals("ООО Элма")) {
                flagFilterContractorElma = true;
            }
        }

        Document[] docs;
        if (flagFilterContractorElma) {
            if (processType.equals(ProcessType.INBOX)) {
                docs = new Document[]{
                        new Document(DocType.DOG.name(), 1, "10.10.2019", "Договор 167", "ООО Элма", "dog", "ok_gray", true, "approve"),
                        new Document(DocType.PD.name(), 2, "10.10.2019", "Пакет документов", "ООО Элма", "pd", "ok_green", false, "sign"),
                        new Document(DocType.TG12.name(), 4, "11.10.2019", "Договор 1", "ООО Элма", "tg12", "lock_green", false, "approve"),
                        new Document(DocType.DOG.name(), 6, "13.10.2019", "Договор 1", "ООО Элма", "dog", "ok_red", false, "approve"),
                        new Document(DocType.PD.name(), 7, "10.10.2019", "Пакет документов(без связанных)", "ООО Элма", "pd", "ok_gray", false, "sign"),
                        new Document(DocType.PD.name(), 8, "10.10.2019", "Пакет документов(без связанных и без подписания)", "ООО Элма", "pd", "lock_red", false, null),
                };
            } else {
                docs = new Document[]{
                        new Document(DocType.TG12.name(), 4, "11.10.2019", "Договор 1", "ООО Элма", "tg12", "lock_green", false, "approve"),
                        new Document(DocType.DOG.name(), 6, "13.10.2019", "Договор 1", "ООО Элма", "dog", "ok_red", false, "approve")
                };
            }
        } else {
            if (processType.equals(ProcessType.INBOX)) {
                docs = new Document[]{
                        new Document(DocType.DOG.name(), 1, "10.10.2019", "Договор 167", "ООО Элма", "dog", "ok_gray", true, "approve"),
                        new Document(DocType.PD.name(), 2, "10.10.2019", "Пакет документов", "ООО Элма", "pd", "ok_green", false, "sign"),
                        new Document(DocType.AVR.name(), 3, "11.10.2019", "Договор 123", "ООО Софт", "avr", "lock_red", true, null),
                        new Document(DocType.TG12.name(), 4, "11.10.2019", "Договор 1", "ООО Элма", "tg12", "lock_green", false, "approve"),
                        new Document(DocType.SF.name(), 5, "12.10.2019", "Договор 1", "ООО Север", "sf", "ok_green", true, "approve"),
                        new Document(DocType.DOG.name(), 6, "13.10.2019", "Договор 1", "ООО Элма", "dog", "ok_red", false, "approve"),
                        new Document(DocType.PD.name(), 7, "10.10.2019", "Пакет документов(без связанных)", "ООО Элма", "pd", "ok_gray", false, "sign"),
                        new Document(DocType.PD.name(), 8, "10.10.2019", "Пакет документов(без связанных и без подписания)", "ООО Элма", "pd", "lock_red", false, null),
                };
            } else {
                docs = new Document[]{
                        new Document(DocType.TG12.name(), 11, "11.10.2019", "Договор 1", "ООО Элма", "tg12", "lock_green", false, "approve"),
                        new Document(DocType.SF.name(), 12, "12.10.2019", "Договор 1", "ООО Софт", "sf", "ok_green", true, "approve"),
                        new Document(DocType.DOG.name(), 13, "13.10.2019", "Договор 1", "ООО Элма", "dog", "ok_red", false, "approve")
                };
            }
        }
        return new ParamGetDocumentsResponse(docs.length, docs);
    }

    public void getDocumentDetails(Callback<ParamRespDocumentDetailsResponse> cb, String token, String doc_type, Integer doc_id) {
        Call<ParamRespDocumentDetailsResponse> callRes = service.getDocumentDetails(new ParamGetDocumentDetailsRequest(token, new GetDocumentDetailsParameters(doc_id, doc_type)));
        callRes.enqueue(cb);
    }

    public ParamRespDocumentDetailsResponse getDocumentDetailsSync(String token, String doc_type, Integer doc_id) throws IOException {
        //Call<ParamRespDocumentDetailsResponse> callRes = service.getDocumentDetails(new ParamGetDocumentDetailsRequest(token, new GetDocumentDetailsParameters(doc_id, doc_type)));
        //return callRes.execute().body();

        DocHeaderAttributes[] docHeaderAttributes = new DocHeaderAttributes[] {
                new DocHeaderAttributes("Тип документа", "УПД"),
                new DocHeaderAttributes("Исполнитель", "ООО «Элма»"),
                new DocHeaderAttributes("Заказчик", "ООО «Капитал»"),
                new DocHeaderAttributes("Основание", "Договор №123"),
                new DocHeaderAttributes("Сумма без НДС  ", "200 руб" ),
                new DocHeaderAttributes("Сумма С НДС  ", "240 руб"),
                new DocHeaderAttributes("Тип документа", "УПД"),
                new DocHeaderAttributes("Исполнитель", "ООО «Элма»"),
                new DocHeaderAttributes("Заказчик", "ООО «Капитал»"),
                new DocHeaderAttributes("Основание", "Договор №123"),
                new DocHeaderAttributes("Сумма без НДС  ", "200 руб" ),
                new DocHeaderAttributes("Сумма С НДС  ", "240 руб")
        };

        DocLineDetail[] docLineDetails1 = new DocLineDetail[] {
                new DocLineDetail("Описание", "Услуги за март 2019"),
                new DocLineDetail("Единица измерения", "Рубли"),
                new DocLineDetail("Количество", "1"),
                new DocLineDetail("Цена", "20 000,00 Р"),
                new DocLineDetail("Стоймость без НДС", "20 000,00 Р"),
                new DocLineDetail("НДС", "4 000,00 Р"),
                new DocLineDetail("Стоймость с НДС", "24 000,00 Р"),
        };

        DocLineDetail[] docLineDetails2 = new DocLineDetail[] {
                new DocLineDetail("Описание", "Услуги за апрель 2019"),
                new DocLineDetail("Единица измерения", "Рубли"),
                new DocLineDetail("Количество", "1"),
                new DocLineDetail("Цена", "100 000,00 Р"),
                new DocLineDetail("Стоймость без НДС", "100 000,00 Р"),
                new DocLineDetail("НДС", "20 000,00 Р"),
                new DocLineDetail("Стоймость с НДС", "120 000,00 Р"),
        };

        DocumentItem[] items = new DocumentItem[] {
                new DocumentItem("1. Услуги за март 2019", "Сумма: 24 000 Р", docLineDetails1),
                new DocumentItem("2. Услуги за апрель 2019", "Сумма: 120 000 Р", docLineDetails2)
        };
        DocumentHistory[] history = new DocumentHistory[] {
                new DocumentHistory("10.11.2018 в 11:00", "Иванов А.А.", "Документ создан"),
                new DocumentHistory("10.11.2018 в 12:00", "Иванов А.А.", "Поставлена подпись от имени сотрудника Иванов Алексей с ролью руководитель"),
                new DocumentHistory("10.11.2018 в 13:00", "Иванов А.А.", "Документ исправлен"),
                new DocumentHistory("10.11.2018 в 14:00", "Иванов А.А.", "Документ заапрувлен")
        };

        DocumentAttachment[] attachments = null;
        if (doc_id == 1 || doc_id == 3 || doc_id == 5 || doc_id == 12) {
            attachments = new DocumentAttachment[]{
                    new DocumentAttachment(1, "attachemnt1.pdf"),
                    new DocumentAttachment(2, "attachemnt2.pdf")
            };
        }

        String userAction = null;
        if (doc_id == 1 || doc_id == 4 || doc_id == 5 || doc_id == 6 || doc_id == 11 || doc_id == 12 || doc_id == 13) {
            userAction = "approve";
        }
        if (doc_id == 2 || doc_id == 7) userAction = "sign";

        RelatedDoc[] related = null;
        if (doc_id == 2)
            related = new RelatedDoc[] {
            new RelatedDoc("Акт выполненных работ №2", 1, DocType.DOG.name(), "dog", "ok_red"),
            new RelatedDoc("Договорной документ №120",3, DocType.AVR.name(), "avr", "ok_green"),
            new RelatedDoc("Товарная накладная №100", 4, DocType.TG12.name(), "tg12", "lock_gray")
        };

        String docTitle = "Договор 1";
        if (doc_id == 1) docTitle = "Договор 167";
        if (doc_id == 2) docTitle = "Пакет документов";
        if (doc_id == 3) docTitle = "Договор 123";
        if (doc_id == 7) docTitle = "Пакет документов(без связанных)";
        if (doc_id == 8) docTitle = "Пакет документов(без связанных и без подписания)";

        String docIcon = doc_type.toLowerCase();

        ParamRespDocumentDetailsResponse docDetail = new ParamRespDocumentDetailsResponse(doc_type, doc_id, docTitle, docIcon,
                docHeaderAttributes, items, history, attachments, related, userAction);
        return docDetail;
    }

    public FilterElementList getFilterSettings() throws IOException {

        String filterSpecification = "{\"filters\": [{ \"name\":\"begin_date\"\n" +
                "                   , \"desc\":\"Дата документа с\"\n" +
                "                   , \"type\":\"date\"\n" +
                "                   , \"last_value\":\"22.11.2019\"\n" +
                "                   , \"disabled\":\"false\"\n" +
                "                   , \"required\":\"false\"}                         \n" +
                "                   ,{ \"name\":\"end_date\"\n" +
                "                     , \"desc\":\"Дата документа по\"\n" +
                "                     , \"type\":\"date\"\n" +
                "                     , \"last_value\":\"01.12.2019\"\n" +
                "                     , \"disabled\":\"false\"\n" +
                "                     , \"required\":\"false\"}\n" +
//                "                   ,{ \"name\":\"date\"\n" +
//                "                     , \"desc\":\"Дата(дополнительно)\"\n" +
//                "                     , \"type\":\"date\"\n" +
//                "                     , \"last_value\":\"07.12.2019\"\n" +
//                "                     , \"disabled\":\"false\"\n" +
//                "                     , \"required\":\"false\"}\n" +
                "                   ,{ \"name\":\"document_number\"\n" +
                "                     , \"desc\":\"Номер документа\"\n" +
                "                     , \"type\":\"string\"\n" +
                "                     , \"last_value\":\"123456\"\n" +
                "                     , \"disabled\":\"false\"\n" +
                "                     , \"required\":\"false\"}\n" +
                "                   ,{\"name\":\"contractor\"\n" +
                "                     ,\"desc\":\"Контрагент\"\n" +
                "                     ,\"type\":\"string\"\n" +
                "                     ,\"last_value\":\"ООО Элма\"\n" +
                "                    , \"disabled\":\"false\"\n" +
                "                    , \"required\":\"false\"}                               \n" +
                "                   ,{\"name\":\"action\"\n" +
                "                     ,\"desc\":\"Действие\"\n" +
                "                     ,\"type\":\"list\"\n" +
                "                     ,\"last_value\":\"sign\"\n" +
                "                     ,\"list\":[{\"code\":\"approve\"\n" +
                "                                  ,\"desc\":\"Согласование\"}\n" +
                "                                 ,{\"code\":\"sign\"\n" +
                "                                   ,\"desc\":\"Подписание\"}\n" +
                "                                 ,{\"code\":\"all\"\n" +
                "                                   ,\"desc\":\"Все\"}]\n" +
                "                     , \"disabled\":\"false\"\n" +
                "                     , \"required\":\"true\"}             \n" +
                "                   ,{\"name\":\"my_document\"\n" +
                "                     ,\"desc\":\"Мои докуметы\"\n" +
                "                     ,\"type\":\"checkbox\"\n" +
                "                     ,\"last_value\":\"true\"\n" +
                "                     , \"disabled\":\"true\"\n" +
                "                     , \"required\":\"true\" }]}";
        try {
            return  new ObjectMapper().registerModule(new KotlinModule()).readValue(filterSpecification, FilterElementList.class);
        } catch (Exception e) {
            Log.e("parse filter data", e.toString());
        }
        return null;
    }
}
