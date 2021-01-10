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

import com.elewise.ldmobile.criptopro.base.SignData;
import com.elewise.ldmobile.criptopro.interfaces.ThreadExecuted;
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
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
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
    private Integer cAdESType;

    // Данные для подписи
    private byte[] dataToSign;

    private Date signDate;

    // Объект для передачи обратного вызова
    private OnSignedResult callback;

    /**
     * Конструктор.
     *
     * @param adapter Настройки примера.
     * @param type Тип подписи.
     */
    public CAdESSignVerifyExample(ContainerAdapter adapter, Integer type, byte[] dataToSign, Date signDate, OnSignedResult callback) {
        super(adapter, false);
        cAdESType = type;
        this.dataToSign = dataToSign;
        this.callback = callback;
        this.signDate = signDate;
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
                }

                String keyAlgName = getPrivateKey().getAlgorithm();
                String keyAlgOid = AlgorithmUtility.keyAlgToKeyAlgorithmOid(keyAlgName);
                String digestAlgOid = algorithmSelector.getDigestAlgorithmOid();

                Logger.log("Digest OID: " + algorithmSelector.getDigestAlgorithmOid());
                Logger.log("Encryption OID: " + keyAlgOid);

                // Формируем подпись.
                Collection<X509Certificate> chain = new ArrayList<>();
                chain.add(getCertificate());

                CAdESSignature cAdESSignature = new CAdESSignature(true);

                List<JcaX509CertificateHolder> tempCerts = new ArrayList<>();
                tempCerts.add(new JcaX509CertificateHolder(getCertificate()));

                cAdESSignature.setCertificateStore(new CollectionStore(tempCerts));

                Logger.log("Single signature type: " +
                    (cAdESType.equals(CAdESType.CAdES_BES)
                    ? "CAdES-BES" : "CAdES-X Long Type 1"));

                Logger.log("Add one signer: " +
                    getCertificate().getSubjectDN());

                if (cAdESType.equals(CAdESType.CAdES_BES)) {
                    final Hashtable table = new Hashtable();
                    Attribute attr = new Attribute(CMSAttributes.signingTime,
                            new DERSet(new Time(signDate))); // устанавливаем время подписи
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

                Logger.log("Verify CAdES signature of type: " +
                    (cAdESType.equals(CAdESType.CAdES_BES)
                        ? "CAdES-BES" : "CAdES-X Long Type 1"));

                Logger.log("Verification completed (OK)");

                callback.signedResult(new SignedResult(true, "", sign));
            } catch (Exception e) {
                callback.signedResult(new SignedResult(false, e.toString(), new byte[0]));
                // пробросим дальше для корректного завершения
                throw e;
            }
        }
    }
}