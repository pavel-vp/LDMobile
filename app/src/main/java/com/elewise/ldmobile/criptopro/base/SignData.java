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

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import com.elewise.ldmobile.criptopro.interfaces.HashData;
import com.elewise.ldmobile.criptopro.interfaces.ThreadExecuted;
import com.elewise.ldmobile.criptopro.util.*;
import com.elewise.ldmobile.utils.Logger;

import ru.CryptoPro.JCP.KeyStore.JCPPrivateKeyEntry;
import ru.CryptoPro.JCP.params.JCPProtectionParameter;
import ru.CryptoPro.JCSP.JCSP;

/**
 * Служебный класс ISignData предназначен для
 * релизации примеров работы с подписью.
 *
 * 27/05/2013
 *
 */
public abstract class SignData implements HashData, Containers {

    /**
     * Флаг ввода пин-кода в окне CSP, а не программно.
     */
    protected boolean askPinInDialog = true;

    /**
     * Загруженный закрытый ключ для подписи.
     */
    private PrivateKey privateKey = null;

    /**
     * Загруженный сертификат ключа подписи для
     * проверки подписи.
     */
    private X509Certificate certificate = null;

    /**
     * Алгоритмы провайдера. Используются при подписи.
     */
    protected AlgorithmSelector algorithmSelector = null;

    /**
     * Флаг необходимости создания подписи на
     * подписываемые аттрибуты.
     */
    protected boolean needSignAttributes = false;

    /**
     * Настройки примера.
     */
    protected ContainerAdapter containerAdapter = null;

    /**
     * Фабрика сертификатов.
     */
    protected static final CertificateFactory CERT_FACTORY;

    static {
        try {
            CERT_FACTORY = CertificateFactory.getInstance("X.509");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Получение закрытого ключа.
     *
     * @return закрытый ключ.
     */
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    /**
     * Получение сертификата ключа.
     *
     * @return сертификат ключа.
     */
    public X509Certificate getCertificate() {
        return certificate;
    }

    /**
     * Конструктор.
     *
     * @param adapter Настройки примера.
     * @param signAttributes True, если требуется создать
     * подпись по атрибутам.
     */
    protected SignData(ContainerAdapter adapter, boolean signAttributes) {

        algorithmSelector  = AlgorithmSelector.getInstance(adapter.getProviderType());
        needSignAttributes = signAttributes;
        containerAdapter   = adapter;

    }

    /**
     * Загрузка ключа и сертификата из контейнера. Если параметр
     * askPinInWindow равен true, то переданный сюда пароль не
     * имеет значения, он будет запрошен в окне CSP только при
     * непосредственной работе с ключом. Если же параметр равен
     * false, то этот пароль будет задан однажды и, если он
     * правильный, больше не понадобится вводить его в окне CSP.
     *
     * @param askPinInWindow True, если будем вводить пин-код в
     * окне.
     * @param storeType Тип ключевого контейнера.
     * @param alias Алиас ключа.
     * @param password Пароль к ключу.
     */
    public void load(boolean askPinInWindow, String storeType,
                     String alias, char[] password) throws Exception {

        if (privateKey != null && certificate != null) {
            return;
        } // if

        // Загрузка контейнеров.

        KeyStore keyStore = KeyStore.getInstance(storeType, JCSP.PROVIDER_NAME);
        keyStore.load(null, null);

        if (askPinInWindow) {

            privateKey  = (PrivateKey) keyStore.getKey(alias, password);
            certificate = (X509Certificate) keyStore.getCertificate(alias);

        } // if
        else {

            JCPProtectionParameter protectedParam =
                new JCPProtectionParameter(password,
                    true, true); // допускаем, что сертификата может не быть

            JCPPrivateKeyEntry entry = (JCPPrivateKeyEntry)
                keyStore.getEntry(alias, protectedParam);

            privateKey  = entry.getPrivateKey();
            certificate = (X509Certificate) entry.getCertificate();

        } // else

        // Отображение информации о ключе.

        if (privateKey == null || certificate == null) {
            throw new Exception("Private key or/and certificate is null.");
        } // if
        else {
            Log.i(Constants.APP_LOGGER_TAG, "Certificate: " +
                certificate.getSubjectDN());
        } // else

        Logger.log("Read private key:" + privateKey);
        Logger.log("Read certificate:" + certificate.getSubjectDN() +
            ", public key: " + certificate.getPublicKey());

    }

    /**
     * Работа примера в потоке. Запускается выполнение
     * задачи в отдельном потоке (обычно при подключении
     * к интернету).
     *
     * @param task Выполняемая задача.
     * @throws Exception
     */
    public void getThreadResult(ThreadExecuted task) throws Exception {
        getThreadResult(containerAdapter.getContext(), task);
    }

    /**
     * Работа примера в потоке. Запускается выполнение
     * задачи в отдельном потоке (обычно при подключении
     * к интернету).
     *
     * @param context Контекст приложения.
     * @param task Выполняемая задача.
     * @throws Exception
     */
    public static void getThreadResult(Context context,
                                       ThreadExecuted task) throws Exception {

        // Формирование окна ожидания.

        Logger.log("Prepare progress dialog.");

        ProgressDialogHolder progressDialog =
            new ProgressDialogHolder(context, false);

        Logger.log("Prepare client thread.");

        // Запуск потока с задачей, который можно
        // прервать. Окно будет закрыто в нем.

        final ClientThread clientThread = new ClientThread(task, progressDialog);
        clientThread.setPriority(Thread.NORM_PRIORITY);

        class CancelListener implements DialogInterface.OnCancelListener {

            @Override
            public void onCancel(DialogInterface dialog) {

                if (clientThread.isAlive()) {

                    Logger.log("Client thread interrupted!");
                    clientThread.interrupt();

                } // if

            }

        }

        // Обработка отмены.
        progressDialog.setOnCancelListener(new CancelListener());

        Logger.log("Show progress dialog.");
        progressDialog.show();

        Logger.log("Start client thread.");
        clientThread.start();

    }

}
