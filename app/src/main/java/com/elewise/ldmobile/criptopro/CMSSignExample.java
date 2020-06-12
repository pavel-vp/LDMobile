/**
 * Copyright 2004-2013 Crypto-Pro. All rights reserved.
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

import com.elewise.ldmobile.MainApp;
import com.objsys.asn1j.runtime.Asn1BerDecodeBuffer;
import com.objsys.asn1j.runtime.Asn1BerEncodeBuffer;
import com.objsys.asn1j.runtime.Asn1Null;
import com.objsys.asn1j.runtime.Asn1ObjectIdentifier;
import com.objsys.asn1j.runtime.Asn1OctetString;
import com.objsys.asn1j.runtime.Asn1Type;
import com.objsys.asn1j.runtime.Asn1UTCTime;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Calendar;

//import com.elewise.ldmobile.criptopro.MyApplication;
import com.elewise.ldmobile.R;
import com.elewise.ldmobile.criptopro.base.SignData;
import com.elewise.ldmobile.criptopro.interfaces.ThreadExecuted;
import com.elewise.ldmobile.criptopro.util.Constants;
import com.elewise.ldmobile.criptopro.util.ContainerAdapter;
import com.elewise.ldmobile.criptopro.util.KeyStoreType;
import com.elewise.ldmobile.criptopro.util.Logger;
import com.elewise.ldmobile.criptopro.util.ProviderType;
import ru.CryptoPro.AdES.tools.AlgorithmUtility;
import ru.CryptoPro.JCP.ASN.CertificateExtensions.GeneralName;
import ru.CryptoPro.JCP.ASN.CertificateExtensions.GeneralNames;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.CMSVersion;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.CertificateChoices;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.CertificateSet;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.ContentInfo;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.DigestAlgorithmIdentifier;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.DigestAlgorithmIdentifiers;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.EncapsulatedContentInfo;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.IssuerAndSerialNumber;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.SignatureAlgorithmIdentifier;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.SignatureValue;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.SignedAttributes;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.SignedData;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.SignerIdentifier;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.SignerInfo;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.SignerInfos;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.ALL_PKIX1Explicit88Values;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.AlgorithmIdentifier;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Attribute;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Attribute_values;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.CertHash;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.CertificateSerialNumber;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.ESSCertIDv2;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.IssuerSerial;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Name;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.SigningCertificateV2;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Time;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88._SeqOfESSCertIDv2;
import ru.CryptoPro.JCP.params.OID;
import ru.CryptoPro.JCP.tools.Array;
import ru.CryptoPro.JCSP.JCSP;

/**
 * Класс CMSSignatureExample реализует пример
 * создания CMS подписи.
 *
 * 26/09/2013
 *
 */
public class CMSSignExample extends SignData {

    public static final String STR_CMS_OID_SIGNED = "1.2.840.113549.1.7.2";
    public static final String STR_CMS_OID_DATA = "1.2.840.113549.1.7.1";
    public static final String STR_CMS_OID_CONT_TYP_ATTR = "1.2.840.113549.1.9.3";
    public static final String STR_CMS_OID_DIGEST_ATTR = "1.2.840.113549.1.9.4";
    public static final String STR_CMS_OID_SIGN_TYM_ATTR = "1.2.840.113549.1.9.5";

    /**
     * Буферы для записи сообщений о проверке подписей.
     */
    StringBuffer validationResultOk = null, validationResultError = null;

    /**
     * Количество проверенных подписей.
     */
    private int validSignatureCount = 0;

    /**
     * Конструктор.
     *
     * @param signAttributes True, если требуется создать
     * подпись по атрибутам.
     * @param adapter Настройки примера.
     */
    protected CMSSignExample(boolean signAttributes, ContainerAdapter adapter) {

        super(adapter, signAttributes);
        needSignAttributes = signAttributes;

    }

    public byte[] sign(byte[] dataToSing) {
        try {
            Logger.log("Load key container to sign data.");

            // Тип контейнера по умолчанию.

            String keyStoreType = KeyStoreType.currentType();
            Logger.log("Default container type: " + keyStoreType);

            // Загрузка ключа и сертификата.

            load(askPinInDialog, keyStoreType,
                    containerAdapter.getClientAlias(),
                    containerAdapter.getClientPassword());

            if (getPrivateKey() == null) {
                Logger.log("Private key is null.");
                return new byte[0];
            } // if

            Logger.log("Compute attached signature");

            // Формируем совмещенную подпись.
            ContainerAdapter adapter = new ContainerAdapter(MainApp.getApplcationContext(), null, false);
            adapter.setProviderType(ProviderType.currentProviderType());
            adapter.setResources(MainApp.getApplcationContext().getResources());

//            final CertificateFactory factory = CertificateFactory.getInstance("X.509");
//            X509Certificate certificate = (X509Certificate) factory.generateCertificate(adapter.getResources().openRawResource(R.raw.kontur));

            byte[] signature = create(dataToSing, false,
                    new PrivateKey[]{getPrivateKey()}, new Certificate[]{getCertificate()/*, certificate*/},
                    true, true);

            Logger.log("--- SIGNATURE BEGIN ---");
            Logger.log(signature, true);
            Logger.log("--- SIGNATURE END ---");

            // Проверяем подпись.

            verify(signature, new Certificate[]{getCertificate()}, dataToSing);
            Logger.setStatusOK();

            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/mydir";
            writeFileOnInternalStorage("first_signed_file.docx.sgn", path, signature);

            return signature;
        } catch (Exception e) {
            return new byte[0];
        }
    }

    public void writeFileOnInternalStorage(String sFileName, String path, byte[] sBody){
        File file = new File(path);
//        if(!file.exists()){
        file.mkdir();
//        }

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

    @Override
    public void getResult() throws Exception {
        getThreadResult(new CMSSignThread());
    }

    /**
     * Класс CMSSignThread реализует подпись и
     * проверку CMS в отдельном потоке.
     *
     */
    private class CMSSignThread implements ThreadExecuted {

        @Override
        public void execute() throws Exception {



        }

    }

    /**
     * Создание CMS подписи на хэш данных.
     *
     * @param data Подписываемые данные.
     * @param isExternalDigest True, если вместо данных
     * передается хэш данных (например, при создании подписи
     * формата CAdES-BES).
     * @param certs Список сертификатов подписи подписантов.
     * @param keys Список ключей подписантов.
     * @param detached True, если подпись отсоединенная.
     * @return ЭЦП CMS.
     * @param addSignCertV2 Добавление аттрибута signingCertificateV2
     * для получения подписи формата CAdES-BES.
     * @throws Exception
     */
    byte[] create(byte[] data, boolean isExternalDigest, PrivateKey[]
        keys, Certificate[] certs, boolean detached, boolean addSignCertV2)
        throws Exception {

        Logger.log("*** Create CMS signature" +
            (needSignAttributes ? " on signed attributes" : "") +
            " ***");

        final ContentInfo all = new ContentInfo();
        all.contentType = new Asn1ObjectIdentifier(
            new OID(STR_CMS_OID_SIGNED).value);

        final SignedData cms = new SignedData();
        all.content = cms;
        cms.version = new CMSVersion(1);

        cms.digestAlgorithms = new DigestAlgorithmIdentifiers(1);
        final DigestAlgorithmIdentifier a = new DigestAlgorithmIdentifier(
            new OID(algorithmSelector.getDigestAlgorithmOid()).value);
        a.parameters = new Asn1Null();
        cms.digestAlgorithms.elements[0] = a;

        Logger.log("Prepare encapsulated content information.");

        // Нельзя сделать подпись совмещенной, если нет данных, а
        // есть только хэш с них.

        if (isExternalDigest && !detached) {
            throw new Exception("Signature is attached but external " +
                "digest is available only (not data)");
        } // if

        if (detached) {
            cms.encapContentInfo = new EncapsulatedContentInfo(
                new Asn1ObjectIdentifier(
                    new OID(STR_CMS_OID_DATA).value), null);
        } // if
        else {
            cms.encapContentInfo = new EncapsulatedContentInfo(
                new Asn1ObjectIdentifier(new OID(STR_CMS_OID_DATA).value),
                    new Asn1OctetString(data));
        } // else

        // Сертификаты.

        Logger.log("Enumerate certificates.");

        final int nCerts = certs.length;
        cms.certificates = new CertificateSet(nCerts);
        cms.certificates.elements = new CertificateChoices[nCerts];

        for (int i = 0; i < cms.certificates.elements.length; i++) {

            final ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Certificate certificate =
                new ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Certificate();

            final Asn1BerDecodeBuffer decodeBuffer =
                new Asn1BerDecodeBuffer(certs[i].getEncoded());

            certificate.decode(decodeBuffer);
            cms.certificates.elements[i] = new CertificateChoices();
            cms.certificates.elements[i].set_certificate(certificate);

        } // for


        final Signature signature = Signature.getInstance(
            algorithmSelector.getSignatureAlgorithmName());

        byte[] sign;

        // Подписанты (signerInfos).

        Logger.log("Prepare signature infos.");

        final int nSigners = keys.length;
        cms.signerInfos = new SignerInfos(nSigners);
        for (int i = 0; i < cms.signerInfos.elements.length; i++) {

            Logger.log("** Create signer info $ " + i + " **");

            cms.signerInfos.elements[i] = new SignerInfo();
            cms.signerInfos.elements[i].version = new CMSVersion(1);
            cms.signerInfos.elements[i].sid = new SignerIdentifier();

            Logger.log("Add certificate info.");

            final byte[] encodedName = ((X509Certificate) certs[i])
                .getIssuerX500Principal().getEncoded();

            final Asn1BerDecodeBuffer nameBuf =
                new Asn1BerDecodeBuffer(encodedName);

            final Name name = new Name();
            name.decode(nameBuf);

            final CertificateSerialNumber num = new CertificateSerialNumber(
                ((X509Certificate) certs[i]).getSerialNumber());
            cms.signerInfos.elements[i].sid.set_issuerAndSerialNumber(
                new IssuerAndSerialNumber(name, num));

            cms.signerInfos.elements[i].digestAlgorithm =
                new DigestAlgorithmIdentifier(
                    new OID(algorithmSelector.getDigestAlgorithmOid()).value);

            cms.signerInfos.elements[i].digestAlgorithm.parameters = new Asn1Null();

            String keyAlgOid = AlgorithmUtility.keyAlgToKeyAlgorithmOid(
                getPrivateKey().getAlgorithm());

            cms.signerInfos.elements[i].signatureAlgorithm =
                new SignatureAlgorithmIdentifier(new OID(keyAlgOid).value);

            cms.signerInfos.elements[i].signatureAlgorithm.parameters = new Asn1Null();

            byte[] data2hash;

            if (needSignAttributes) {

                Logger.log("Need to calculate digest on signed attributes.");

                final int kMax = addSignCertV2 ? 4 : 3;
                cms.signerInfos.elements[i].signedAttrs = new SignedAttributes(kMax);

                Logger.log("Count of signed attributes: " + kMax);

                // content-type

                Logger.log("Add content-type.");

                int k = 0;
                cms.signerInfos.elements[i].signedAttrs.elements[k] =
                    new Attribute(new OID(STR_CMS_OID_CONT_TYP_ATTR).value,
                        new Attribute_values(1));

                final Asn1Type cont_type = new Asn1ObjectIdentifier(
                    new OID(STR_CMS_OID_DATA).value);

                cms.signerInfos.elements[i].signedAttrs
                    .elements[k].values.elements[0] = cont_type;

                // signing-time

                Logger.log("Add signing-time.");

                k += 1;
                cms.signerInfos.elements[i].signedAttrs.elements[k] =
                    new Attribute(new OID(STR_CMS_OID_SIGN_TYM_ATTR).value,
                        new Attribute_values(1));

                final Time time = new Time();
                final Asn1UTCTime UTCTime = new Asn1UTCTime();

                // Текущая дата календаря.
                UTCTime.setTime(Calendar.getInstance());
                time.set_utcTime(UTCTime);

                cms.signerInfos.elements[i].signedAttrs
                    .elements[k].values.elements[0] = time.getElement();

                // message-digest

                Logger.log("Add message-digest.");

                k += 1;
                cms.signerInfos.elements[i].signedAttrs.elements[k] =
                    new Attribute(new OID(STR_CMS_OID_DIGEST_ATTR).value,
                        new Attribute_values(1));
                final byte[] message_digest_b;

                Logger.log("Signing data is digest: " + isExternalDigest);

                // Если вместо данных у нас хеш, то сразу его передаем,
                // ничего не вычисляем.

                if (isExternalDigest) {
                    message_digest_b = data;
                } // if
                else {

                    if (detached) {
                        message_digest_b = digest(data,
                            algorithmSelector.getDigestAlgorithmName());
                    } // if
                    else {
                        message_digest_b = digest(cms.encapContentInfo.eContent.value,
                            algorithmSelector.getDigestAlgorithmName());
                    } // else

                } // else

                final Asn1Type message_digest = new Asn1OctetString(message_digest_b);

                cms.signerInfos.elements[i].signedAttrs
                    .elements[k].values.elements[0] = message_digest;

                // Добавление signingCertificateV2 в подписанные аттрибуты,
                // чтобы подпись стала похожа на CAdES-BES.

                if (addSignCertV2) {

                    Logger.log("Add signing-certificateV2.");

                    // Аттрибут с OID'ом id_aa_signingCertificateV2.

                    k += 1;
                    cms.signerInfos.elements[i].signedAttrs.elements[k] =
                        new Attribute(new OID(ALL_PKIX1Explicit88Values
                            .id_aa_signingCertificateV2).value,
                                new Attribute_values(1));

                    // Идентификатор алгоритма, который использовался
                    // для хеширования контекста сертификата ключа подписи.

                    final DigestAlgorithmIdentifier digestAlgorithmIdentifier =
                        new DigestAlgorithmIdentifier(
                            new OID(algorithmSelector.getDigestAlgorithmOid()).value);

                    // Хеш сертификата ключа подписи.

                    final CertHash certHash = new CertHash(digest(certs[i].getEncoded(),
                        algorithmSelector.getDigestAlgorithmName()));

                    // Issuer name из сертификата ключа подписи.

                    GeneralName generalName = new GeneralName();
                    generalName.set_directoryName(name);

                    GeneralNames generalNames = new GeneralNames();
                    generalNames.elements = new GeneralName[1];
                    generalNames.elements[0] = generalName;

                    // Комбинируем издателя и серийный номер.

                    IssuerSerial issuerSerial = new IssuerSerial(generalNames, num);

                    ESSCertIDv2 essCertIDv2 = new ESSCertIDv2(digestAlgorithmIdentifier,
                        certHash, issuerSerial);

                    _SeqOfESSCertIDv2 essCertIDv2s = new _SeqOfESSCertIDv2(1);
                    essCertIDv2s.elements = new ESSCertIDv2[1];
                    essCertIDv2s.elements[0] = essCertIDv2;

                    // Добавляем сам аттрибут.

                    SigningCertificateV2 signingCertificateV2 =
                        new SigningCertificateV2(essCertIDv2s);

                    cms.signerInfos.elements[i].signedAttrs
                        .elements[k].values.elements[0] = signingCertificateV2;

                } // if

                // Данные для хэширования.

                Asn1BerEncodeBuffer encBufSignedAttr = new Asn1BerEncodeBuffer();
                cms.signerInfos.elements[i].signedAttrs.encode(encBufSignedAttr);
                data2hash = encBufSignedAttr.getMsgCopy();

            } // if
            else {
                data2hash = data;
            } // else

            signature.initSign(keys[i]);
            signature.update(data2hash);
            sign = signature.sign();

            cms.signerInfos.elements[i].signature = new SignatureValue(sign);

        } // for

        // CMS подпись.

        Logger.log("Produce CMS signature.");

        final Asn1BerEncodeBuffer asnBuf = new Asn1BerEncodeBuffer();
        all.encode(asnBuf, true);
        return asnBuf.getMsgCopy();

    }

    /**
     * Проверка CMS подписи.
     *
     * @param buffer CMS подпись.
     * @param certs Сертификаты для проверки ЭЦП. Может быть null.
     * @param data Подписанные данные. Может быть null.
     * @throws Exception
     *
     */
    void verify(byte[] buffer, Certificate[] certs, byte[]
        data) throws Exception {

        Logger.log("*** Verify CMS signature ***");

        validationResultOk = new StringBuffer("");
        validationResultError = new StringBuffer("");

        Logger.log("Decode CMS signature.");

        final Asn1BerDecodeBuffer asnBuf = new Asn1BerDecodeBuffer(buffer);
        final ContentInfo all = new ContentInfo();
        all.decode(asnBuf);

        if (!new OID(STR_CMS_OID_SIGNED).eq(all.contentType.value)) {
            throw new Exception("Not supported.");
        } // if

        Logger.log("Extract encapsulated content information.");

        final SignedData cms = (SignedData) all.content;
        final byte[] text;
        if (cms.encapContentInfo.eContent != null) {
            text = cms.encapContentInfo.eContent.value;
        } // if
        else if (data != null) {
            text = data;
        } // else
        else {
            throw new Exception("Signature has to " +
                "include a content for verify.");
        } // else

        Logger.log("Source data: " + new String(text));
        Logger.log("Extract digest OID.");

        OID digestOid = null;

        final DigestAlgorithmIdentifier digestAlgorithmIdentifier =
            new DigestAlgorithmIdentifier(
                new OID(algorithmSelector.getDigestAlgorithmOid()).value);

        for (int i = 0; i < cms.digestAlgorithms.elements.length; i++) {
            if (cms.digestAlgorithms.elements[i].algorithm
                    .equals(digestAlgorithmIdentifier.algorithm)) {
                digestOid = new OID(cms.digestAlgorithms
                    .elements[i].algorithm.value);
                break;
            } // if
        } // for

        if (digestOid == null) {
            throw new Exception("Unknown digest OID.");
        } // if

        final OID eContTypeOID = new OID(cms.encapContentInfo.eContentType.value);
        if (cms.certificates != null) {

            // Проверка на вложенных сертификатах.

            Logger.log("Validation on certificates founded in the signature.");

            for (int i = 0; i < cms.certificates.elements.length; i++) {

                final Asn1BerEncodeBuffer encBuf = new Asn1BerEncodeBuffer();
                cms.certificates.elements[i].encode(encBuf);

                final CertificateFactory cf = CertificateFactory.getInstance("X.509");
                final X509Certificate cert = (X509Certificate) cf
                    .generateCertificate(encBuf.getInputStream());

                for (int j = 0; j < cms.signerInfos.elements.length; j++) {

                    Logger.log("** Verify signer info $ " + j + " **");

                    final SignerInfo info = cms.signerInfos.elements[j];
                    if (!digestOid.equals(new OID(info.digestAlgorithm.algorithm.value))) {
                        throw new Exception("It isn't signed on certificate.");
                    } // if

                    final boolean checkResult = verifyOnCert(cert,
                        cms.signerInfos.elements[j], text, eContTypeOID, true);
                    writeLog(checkResult, j, i, cert);

                } // for
            } // for

        } // if
        else if (certs != null) {

            // Проверка на указанных сертификатах.

            Logger.log("Certificates for validation not found in " +
                "the signature.\nTry verify on specified certificates...");

            for (int i = 0; i < certs.length; i++) {

                final X509Certificate cert = (X509Certificate) certs[i];

                for (int j = 0; j < cms.signerInfos.elements.length; j++) {

                    Logger.log("** Verify signer info $ " + j + " **");

                    final SignerInfo info = cms.signerInfos.elements[j];
                    if (!digestOid.equals(new OID(info.digestAlgorithm.algorithm.value))) {
                        throw new Exception("It isn't signed on certificate.");
                    } // if

                    final boolean checkResult = verifyOnCert(cert,
                        cms.signerInfos.elements[j], text, eContTypeOID, true);
                    writeLog(checkResult, j, i, cert);

                } // for
            } // for

        } // else
        else {
            Logger.log("Certificates for validation are not found.");
        } // else

        if (validSignatureCount == 0) {
            Logger.log("Signatures are invalid: " + validationResultError);
            return;
        } // if

        if (cms.signerInfos.elements.length > validSignatureCount) {
            Logger.log("Some signatures are invalid:" +
                validationResultOk + validationResultError);
            return;
        } // if

        Logger.log("All signatures are valid:" + validationResultOk);

    }

    /**
     * Попытка проверки подписи на указанном сертификате.
     * Проверка может быть выполнена как по отсортированным
     * подписанным аттрибутам, так и по несортированным.
     *
     * @param cert Сертификат для проверки.
     * @param text Данные для проверки.
     * @param info ЭЦП.
     * @param eContentTypeOID Тип подписанного содержимого.
     * @param needSortSignedAttributes True, если необходимо проверить
     * подпись по отсортированным подписанным аттрибутам. По умолчанию
     * подписанные аттрибуты сортируются перед кодированием.
     * @return True, если подпись корректна.
     * @throws Exception ошибки
     */
    private boolean verifyOnCert(X509Certificate cert,
                                 SignerInfo info, byte[] text, OID eContentTypeOID,
                                 boolean needSortSignedAttributes) throws Exception {

        // Подпись.
        final byte[] sign = info.signature.value;

        // Данные для проверки подписи.
        final byte[] data;

        if (info.signedAttrs == null) {
            // Аттрибуты подписи не присутствуют.
            // Данные для проверки подписи.
            data = text;
        } // if
        else {

            Logger.log("Signed attributes are found.");

            // Присутствуют аттрибуты подписи (Signed Attributes).
            final Attribute[] signAttrElem = info.signedAttrs.elements;

            // Проверка аттрибута signing-certificateV2.
            final Asn1ObjectIdentifier signingCertificateV2Oid = new Asn1ObjectIdentifier(
                (new OID(ALL_PKIX1Explicit88Values.id_aa_signingCertificateV2)).value);
            Attribute signingCertificateV2Attr = null;

            for (int r = 0; r < signAttrElem.length; r++) {
                final Asn1ObjectIdentifier oid = signAttrElem[r].type;
                if (oid.equals(signingCertificateV2Oid)) {
                    signingCertificateV2Attr = signAttrElem[r];
                    break;
                } // if
            } // for

            if (signingCertificateV2Attr != null) {

                SigningCertificateV2 signingCertificateV2 = (SigningCertificateV2)
                    signingCertificateV2Attr.values.elements[0];
                _SeqOfESSCertIDv2 essCertIDv2s = signingCertificateV2.certs;

                for (int s = 0; s < essCertIDv2s.elements.length; s++) {

                    ESSCertIDv2 essCertIDv2 = essCertIDv2s.elements[s];

                    CertHash expectedCertHash = essCertIDv2.certHash;
                    AlgorithmIdentifier expectedHashAlgorithm = essCertIDv2.hashAlgorithm;

                    IssuerSerial expectedIssuerSerial = essCertIDv2.issuerSerial;
                    Asn1BerEncodeBuffer encodedExpectedIssuerSerial = new Asn1BerEncodeBuffer();
                    expectedIssuerSerial.encode(encodedExpectedIssuerSerial);

                    OID expectedHashAlgorithmOid = new OID(expectedHashAlgorithm.algorithm.value);
                    CertHash actualCertHash = new CertHash(
                        digest(cert.getEncoded(), expectedHashAlgorithmOid.toString()));

                    ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Certificate certificate =
                        new ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Certificate();
                    Asn1BerDecodeBuffer decodeBuffer =
                        new Asn1BerDecodeBuffer(cert.getEncoded());
                    certificate.decode(decodeBuffer);

                    GeneralName[] issuerName = new GeneralName[1];
                    issuerName[0] = new GeneralName(GeneralName._DIRECTORYNAME,
                        certificate.tbsCertificate.issuer);
                    GeneralNames issuerNames = new GeneralNames(issuerName);

                    IssuerSerial actualIssuerSerial = new IssuerSerial(issuerNames,
                        certificate.tbsCertificate.serialNumber);
                    Asn1BerEncodeBuffer encodedActualIssuerSerial = new Asn1BerEncodeBuffer();
                    actualIssuerSerial.encode(encodedActualIssuerSerial);

                    if ( !(Arrays.equals(actualCertHash.value, expectedCertHash.value) &&
                        Arrays.equals(encodedActualIssuerSerial.getMsgCopy(),
                        encodedActualIssuerSerial.getMsgCopy())) ) {

                        Logger.log("Certificate stored in signing-certificateV2 " +
                            "is not equal to " + cert.getSubjectDN());
                        return false;

                    } // if

                } // for

            } // if

            // Проверка аттрибута content-type.

            final Asn1ObjectIdentifier contentTypeOid =
                new Asn1ObjectIdentifier((new OID(STR_CMS_OID_CONT_TYP_ATTR)).value);
            Attribute contentTypeAttr = null;

            for (int r = 0; r < signAttrElem.length; r++) {
                final Asn1ObjectIdentifier oid = signAttrElem[r].type;
                if (oid.equals(contentTypeOid)) {
                    contentTypeAttr = signAttrElem[r];
                    break;
                } // if
            } // for

            if (contentTypeAttr == null) {
                throw new Exception("content-type attribute isn't not presented.");
            } // if

            if (!new Asn1ObjectIdentifier(eContentTypeOID.value)
                .equals(contentTypeAttr.values.elements[0])) {
                throw new Exception("content-type attribute OID is not " +
                    "equal to eContentType OID.");
            } // if

            // Проверка аттрибута message-digest.

            final Asn1ObjectIdentifier messageDigestOid =
                new Asn1ObjectIdentifier((new OID(STR_CMS_OID_DIGEST_ATTR)).value);

            Attribute messageDigestAttr = null;

            for (int r = 0; r < signAttrElem.length; r++) {
                final Asn1ObjectIdentifier oid = signAttrElem[r].type;
                if (oid.equals(messageDigestOid)) {
                    messageDigestAttr = signAttrElem[r];
                    break;
                } // if
            } // for

            if (messageDigestAttr == null) {
                throw new Exception("message-digest attribute is not presented.");
            } // if

            final Asn1Type open = messageDigestAttr.values.elements[0];
            final Asn1OctetString hash = (Asn1OctetString) open;
            final byte[] md = hash.value;

            // Вычисление messageDigest.

            final byte[] dm = digest(text,
                algorithmSelector.getDigestAlgorithmName());

            if (!Array.toHexString(dm).equals(Array.toHexString(md))) {
                throw new Exception("Verification of message-digest attribute failed.");
            } // if

            // Проверка аттрибута signing-time.

            final Asn1ObjectIdentifier signTimeOid = new Asn1ObjectIdentifier(
                (new OID(STR_CMS_OID_SIGN_TYM_ATTR)).value);

            Attribute signTimeAttr = null;

            for (int r = 0; r < signAttrElem.length; r++) {
                final Asn1ObjectIdentifier oid = signAttrElem[r].type;
                if (oid.equals(signTimeOid)) {
                    signTimeAttr = signAttrElem[r];
                    break;
                } // if
            } // for

            if (signTimeAttr != null) {
                // Проверка (необязательно).
                Time sigTime = (Time)signTimeAttr.values.elements[0];
                Asn1UTCTime time = (Asn1UTCTime) sigTime.getElement();
                Logger.log("Signing Time: " + time);
            }

            //данные для проверки подписи
            final Asn1BerEncodeBuffer encBufSignedAttr = new Asn1BerEncodeBuffer();
            info.signedAttrs.needSortSignedAttributes = needSortSignedAttributes;
            info.signedAttrs.encode(encBufSignedAttr);

            data = encBufSignedAttr.getMsgCopy();
        }

        Logger.log("Verify signature.");

        // Проверяем подпись.

        Signature signature = Signature.getInstance(
            algorithmSelector.getSignatureAlgorithmName(),
            JCSP.PROVIDER_NAME);

        signature.initVerify(cert);
        signature.update(data);

        boolean verified = signature.verify(sign);
        Logger.log("Signature verified: " + verified);

        // Если подпись некорректна, но нас есть подписанные аттрибуты,
        // то пробуем проверить подпись также, отключив сортировку аттрибутов
        // перед кодированием в байтовый массив.

        if (!verified && info.signedAttrs != null && needSortSignedAttributes) {
            Logger.log("Try to disable sort of the signed attributes.");
            return verifyOnCert(cert, info, text, eContentTypeOID, false);
        }

        return verified;
    }

    /**
     * Составление сообщения о проверке.
     *
     * @param checkResult Флаг, прошла ли проверка.
     * @param signNum Номер подписи.
     * @param certNum Номер сертификата.
     * @param cert Сертификат.
     */
    private void writeLog(boolean checkResult,
        int signNum, int certNum, X509Certificate cert) {

        if (checkResult) {
            validationResultOk.append("\n");
            validationResultOk.append("sign[");
            validationResultOk.append(signNum);
            validationResultOk.append("] - Valid signature on cert[");
            validationResultOk.append(certNum);
            validationResultOk.append("] (");
            validationResultOk.append(cert.getSubjectX500Principal());
            validationResultOk.append(")");
            validSignatureCount += 1;
        } else {
            validationResultError.append("\n");
            validationResultError.append("sign[");
            validationResultError.append(signNum);
            validationResultError.append("] - Invalid signature on cert[");
            validationResultError.append(certNum);
            validationResultError.append("] (");
            validationResultError.append(cert.getSubjectX500Principal());
            validationResultError.append(")");
        }

    }

    /**
     * @param bytes Хэшируемые данные.
     * @param digestAlgorithmName Алгоритм хэширования.
     * @return хэш данных.
     * @throws Exception
     */
    private byte[] digest(byte[] bytes, String digestAlgorithmName)
        throws Exception {

        final ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        final MessageDigest digest = MessageDigest.getInstance(digestAlgorithmName);
        final DigestInputStream digestStream = new DigestInputStream(stream, digest);

        while (digestStream.available() != 0) {
            digestStream.read();
        } // while

        return digest.digest();
    }
}
