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
import java.util.List;

import com.elewise.ldmobile.criptopro.base.SignData;
import com.elewise.ldmobile.criptopro.interfaces.ThreadExecuted;
import com.elewise.ldmobile.criptopro.util.Constants;
import com.elewise.ldmobile.criptopro.util.ContainerAdapter;
import com.elewise.ldmobile.criptopro.util.KeyStoreType;
import com.elewise.ldmobile.criptopro.util.Logger;

import ru.CryptoPro.AdES.tools.AlgorithmUtility;
import ru.CryptoPro.CAdES.CAdESSignature;
import ru.CryptoPro.CAdES.CAdESType;
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
    private Integer cAdESType = CAdESType.CAdES_BES;

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
                    cAdESSignature.addSigner(JCSP.PROVIDER_NAME, digestAlgOid,
                        keyAlgOid, getPrivateKey(), chain, cAdESType, null, false);

                }
                else {

                    cAdESSignature.addSigner(JCSP.PROVIDER_NAME, digestAlgOid,
                        keyAlgOid, getPrivateKey(), chain, cAdESType, TSA_DEFAULT,
                            false);

                }

                ByteArrayOutputStream signatureStream =
                    new ByteArrayOutputStream();

                Logger.log("Compute signature for message '" +
                    Constants.MESSAGE + "'");

                cAdESSignature.open(signatureStream);

                cAdESSignature.update(dataToSign);

                cAdESSignature.close();
                signatureStream.close();

                byte[] sign = signatureStream.toByteArray();
                Logger.log(sign, true);

                // Проверяем подпись.

                Logger.log("Verify CAdES signature of type: " +
                    (cAdESType.equals(CAdESType.CAdES_BES)
                        ? "CAdES-BES" : "CAdES-X Long Type 1"));

                cAdESSignature = new CAdESSignature(sign, dataToSign, cAdESType, false);
                cAdESSignature.verify(chain);

                Logger.log("Verification completed (OK)");
                Logger.setStatusOK();

                callback.signedResult(new SignedResult(true, "", sign));

                String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/mydir";
                writeFileOnInternalStorage("first_signed_file.docx.sgn", path, sign);
            } catch (Exception e) {
                callback.signedResult(new SignedResult(false, e.toString(), new byte[0]));
                // пробросим дальше для корректного завершения
                throw e;
            }
        }

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
                return res;
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