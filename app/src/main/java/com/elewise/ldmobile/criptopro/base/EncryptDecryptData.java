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
package com.elewise.ldmobile.criptopro.base;

import com.elewise.ldmobile.criptopro.util.AlgorithmSelector;
import com.elewise.ldmobile.criptopro.util.ContainerAdapter;
import com.objsys.asn1j.runtime.Asn1BerDecodeBuffer;
import com.objsys.asn1j.runtime.Asn1Exception;

import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.SubjectPublicKeyInfo;
import ru.CryptoPro.JCP.params.AlgIdInterface;
import ru.CryptoPro.JCP.params.AlgIdSpec;

/**
 * Служебный класс EncryptDecryptData предназначен для
 * реализации примеров шифрования.
 *
 * 27/05/2013
 *
 */
public abstract class EncryptDecryptData extends SignData {

    /**
     * Алгоритмы провайдера. Используются на стороне клиента.
     */
    protected AlgorithmSelector clientAlgSelector = null;

    /**
     * Алгоритмы провайдера. Используются на стороне сервера.
     */
    protected AlgorithmSelector serverAlgSelector = null;

    /**
     * Конструктор.
     *
     * @param adapter Настройки примера.
     */
    protected EncryptDecryptData(ContainerAdapter adapter) {

        super(adapter, false); // ignore

        clientAlgSelector = AlgorithmSelector.getInstance(adapter.getProviderType());
        serverAlgSelector = AlgorithmSelector.getInstance(adapter.getProviderType());

    }

    /**
     * Получение параметров сертификата.
     *
     * @param cert Сертификат.
     * @return Параметры сертификата.
     * @throws IOException
     */
    protected AlgIdInterface getKeyParams(Certificate cert)
        throws IOException {

        if (!(cert instanceof X509Certificate)) {
            throw new IOException("Certificate format is not X509");
        } // if

        byte[] encoded = cert.getPublicKey().getEncoded();
        Asn1BerDecodeBuffer buf = new Asn1BerDecodeBuffer(encoded);
        SubjectPublicKeyInfo keyInfo = new SubjectPublicKeyInfo();

        try {
            keyInfo.decode(buf);
        } catch (Asn1Exception e) {
            IOException ex = new IOException("Not GOST DH public key");
            ex.initCause(e);
            throw ex;
        }

        buf.reset();
        return new AlgIdSpec(keyInfo.algorithm);

    }

}
