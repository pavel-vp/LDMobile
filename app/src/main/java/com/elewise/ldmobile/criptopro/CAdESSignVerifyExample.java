/**
 * $RCSfileCAdESSignVerifyExample.java,v $
 * version $Revision: 36379 $
 * created 08.09.2014 18:23 by Yevgeniy
 * last modified $Date: 2012-05-30 12:19:27 +0400 (Ср, 30 май 2012) $ by $Author: afevma $
 *
 * Copyright 2004-2014 Crypto-Pro. All rights reserved.
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package com.elewise.ldmobile.criptopro;

import android.os.Environment;

import com.elewise.ldmobile.criptopro.base.SignData;
import com.elewise.ldmobile.criptopro.interfaces.ThreadExecuted;
import com.elewise.ldmobile.criptopro.util.Constants;
import com.elewise.ldmobile.criptopro.util.ContainerAdapter;
import com.elewise.ldmobile.criptopro.util.KeyStoreType;
import com.elewise.ldmobile.utils.Logger;

import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.asn1.cms.Time;
import org.bouncycastle.asn1.x509.Attribute;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.util.CollectionStore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import ru.CryptoPro.CAdES.CAdESSignature;
import ru.CryptoPro.CAdES.CAdESType;
import ru.CryptoPro.JCP.tools.AlgorithmUtility;
import ru.CryptoPro.JCSP.JCSP;

/**
 * Класс CAdESSignVerifyExample реализует пример CAdES-подписи.
 *
 * @author Copyright 2004-2014 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class CAdESSignVerifyExample extends SignData {

    /**
     * Служба штапов.
     */
    public static final String TSA_DEFAULT = "http://www.cryptopro.ru:80/tsp/";

    /**
     * Тип подписи.
     */
    private Integer cAdESType =  CAdESType.CAdES_BES;

    // Данные для подписи
    private byte[] dataToSign;

    // Объект для передачи обратного вызова
    private OnSignedResult callback;

    /**
     * Конструктор.
     *
     * @param adapter Настройки примера.
     * @param type Тип подписи.
     */
    public CAdESSignVerifyExample(ContainerAdapter adapter, Integer type, byte[] dataToSign, OnSignedResult callback) {
        super(adapter, false);
        cAdESType = type;
        this.dataToSign = dataToSign;
        this.callback = callback;
    }

    @Override
    public void getResult() throws Exception {
        getThreadResult(new CAdESSignatureThread());
    }

    /**
     * Класс CAdESSignatureThread реализует создание и
     * проверку CAdES подписи в отдельном потоке.
     *
     */
    private class CAdESSignatureThread implements ThreadExecuted {

        @Override
        public void execute() throws Exception {
            try {
                System.setProperty("com.sun.security.enableCRLDP", "true");
                System.setProperty("com.ibm.security.enableCRLDP", "true");
                System.setProperty("ocsp.enable", "true");

                Logger.log("Load key container to sign data.");

                // Тип контейнера по умолчанию.

                String keyStoreType = KeyStoreType.currentType();
                Logger.log("Default container type: " + keyStoreType);

                // Загрузка ключа и сертификата.

                load(askPinInDialog, keyStoreType,
                    containerAdapter.getClientAlias(),
                    containerAdapter.getClientPassword()
                );

                if (getPrivateKey() == null) {
                    Logger.log("Private key is null.");
                    return;
                } // if

                String keyAlgName = getPrivateKey().getAlgorithm();
                String keyAlgOid = AlgorithmUtility.keyAlgToKeyAlgorithmOid(keyAlgName);
                String digestAlgOid = algorithmSelector.getDigestAlgorithmOid();

                Logger.log("Digest OID: " + algorithmSelector.getDigestAlgorithmOid());
                Logger.log("Encryption OID: " + keyAlgOid);

                // Формируем подпись.
                Collection<X509Certificate> chain = new ArrayList<>();
                chain.add(getCertificate());

//                ContainerAdapter adapter = new ContainerAdapter(MainApp.getApplcationContext(), null, false);
//                adapter.setProviderType(ProviderType.currentProviderType());
//                adapter.setResources(MainApp.getApplcationContext().getResources());

                CAdESSignature cAdESSignature = new CAdESSignature(true);

                List<JcaX509CertificateHolder> tempCerts = new ArrayList<>();
                tempCerts.add(new JcaX509CertificateHolder(getCertificate()));
//                tempCerts.addAll(getTrustCertificateHolders());

                cAdESSignature.setCertificateStore(new CollectionStore(tempCerts));

                Logger.log("Single signature type: " +
                    (cAdESType.equals(CAdESType.CAdES_BES)
                    ? "CAdES-BES" : "CAdES-X Long Type 1"));

                Logger.log("Add one signer: " +
                    getCertificate().getSubjectDN());

                if (cAdESType.equals(CAdESType.CAdES_BES)) {
                    final Hashtable table = new Hashtable();
                    // todo date sign!!!
                    Attribute attr = new Attribute(CMSAttributes.signingTime,
                            new DERSet(new Time(new Date()))); // устанавливаем время подписи
                    table.put(attr.getAttrType(), attr);
                    AttributeTable attrTable = new AttributeTable(table);

                    cAdESSignature.addSigner(JCSP.PROVIDER_NAME, digestAlgOid,
                        keyAlgOid, getPrivateKey(), chain, cAdESType, null, false, attrTable, null);

                } else {
                    cAdESSignature.addSigner(JCSP.PROVIDER_NAME, digestAlgOid,
                        keyAlgOid, getPrivateKey(), chain, cAdESType, TSA_DEFAULT,
                            false);

                }

                ByteArrayOutputStream signatureStream =
                    new ByteArrayOutputStream();

                cAdESSignature.open(signatureStream);

                cAdESSignature.update(dataToSign);

                cAdESSignature.close();
                signatureStream.close();

                byte[] sign = signatureStream.toByteArray();
                Logger.log("sign true");

                // Проверяем подпись.
                cAdESSignature = new CAdESSignature(sign, dataToSign, cAdESType, false);
                cAdESSignature.verify(chain);


//                // Список всех подписантов в исходной подписи.
//                Collection<SignerInformation> srcSignerInfos = new ArrayList<SignerInformation>();
//
//                for (CAdESSigner signer : cAdESSignature.getCAdESSignerInfos()) {
//                    srcSignerInfos.add(signer.getSignerInfo());
//                }
//
//                // 2. Усовершенствование подписи.
//
//                // Получаем только первого подписанта CAdES-BES, его усовершенствуем. Остальных не трогаем.
//                CMSSignedData srcSignedData = new CMSSignedData(sign);//cAdESSignature.getSignedData();
//
//                List<JcaX509CertificateHolder> tempCerts1 = new ArrayList<>();
//                tempCerts1.add(new JcaX509CertificateHolder(getCertificates().get(1)));
////                tempCerts.addAll(getTrustCertificateHolders());
//
//                cAdESSignature.setCertificateStore(new CollectionStore(tempCerts1));
//
//                CAdESSigner srcSigner = cAdESSignature.getCAdESSignerInfo(0);
//
//                // Исключаем его из исходного списка, т.к. его место займет усовершенствованный подписант.
//                srcSignerInfos.remove(srcSigner.getSignerInfo());
//
//
//                // Улучшаем CAdES-BES до CAdES-X Long Type 1.
//                srcSigner.enhance(JCP.PROVIDER_NAME, JCP.GOST_DIGEST_OID, chain,
//                        TSA_DEFAULT, CAdESType.CAdES_X_Long_Type_1);
//
//                // Усовершенствованный подписант.
//                SignerInformation enhSigner = srcSigner.getSignerInfo();
//
//                // Добавляем его в исходный список подписантов.
//                srcSignerInfos.add(enhSigner);
//
//                // Список подписантов.
//                SignerInformationStore dstSignerInfoStore = new SignerInformationStore(srcSignerInfos);
//
//                // Обновляем исходную подпись c ее начальным списком подписантов на тот же,
//                // но с первым усовершенствованным подписантом.
//                CMSSignedData dstSignedData =
//                        CMSSignedData.replaceSigners(srcSignedData, dstSignerInfoStore);
//
//                sign = dstSignedData.getEncoded();

                Logger.log("Verify CAdES signature of type: " +
                    (cAdESType.equals(CAdESType.CAdES_BES)
                        ? "CAdES-BES" : "CAdES-X Long Type 1"));

                Logger.log("Verification completed (OK)");

                callback.signedResult(new SignedResult(true, "", sign));

//                String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/sign";
//                writeFileOnInternalStorage("first_signed_file.docx.sgn", path, sign);
            } catch (Exception e) {
                callback.signedResult(new SignedResult(false, e.toString(), new byte[0]));
                // пробросим дальше для корректного завершения
                throw e;
            }
        }
    }

    private List<X509Certificate> getCertificates() throws Exception{
        KeyStore ts = KeyStore.getInstance(
                containerAdapter.getTrustStoreType(),
                containerAdapter.getTrustStoreProvider());

        List<X509Certificate> res = new ArrayList<>();

        ts.load(containerAdapter.getTrustStoreStream(),
                containerAdapter.getTrustStorePassword());

        for (String alias: Collections.list(ts.aliases())) {
            X509Certificate cert = (X509Certificate) ts.getCertificate(alias);
            if (!alias.startsWith("root")) {
                res.add(0, (X509Certificate) ts.getCertificate(alias));
            }
        }
        return res;
    }

    private List<JcaX509CertificateHolder> getTrustCertificateHolders() throws Exception {
        KeyStore ts = KeyStore.getInstance(
                containerAdapter.getTrustStoreType(),
                containerAdapter.getTrustStoreProvider());

        List<JcaX509CertificateHolder> res = new ArrayList<>();

        ts.load(containerAdapter.getTrustStoreStream(),
                containerAdapter.getTrustStorePassword());

        for (String alias: Collections.list(ts.aliases())) {
            if (!alias.startsWith("root")) {
                res.add(new JcaX509CertificateHolder((X509Certificate) ts.getCertificate(alias)));
//                return res;
            }
        }

        return res;
    }

    public void writeFileOnInternalStorage(String sFileName, String path, byte[] sBody){
        File file = new File(path);
        if(!file.exists()){
            file.mkdir();
        }

        try{
            File gpxfile = new File(file, sFileName);
            FileOutputStream writer = new FileOutputStream(gpxfile);
            writer.write(sBody  );
            writer.flush();
            writer.close();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}