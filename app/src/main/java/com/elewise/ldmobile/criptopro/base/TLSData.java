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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import com.elewise.ldmobile.criptopro.util.Constants;
import com.elewise.ldmobile.criptopro.util.ContainerAdapter;
import com.elewise.ldmobile.criptopro.util.KeyStoreType;
import com.elewise.ldmobile.criptopro.util.Logger;
import ru.CryptoPro.JCP.KeyStore.StoreInputStream;
import ru.CryptoPro.JCSP.JCSP;
import ru.CryptoPro.ssl.android.Provider;

/**
 * Служебный класс TLSData предназначен для
 * реализации примеров соединения по TLS.
 *
 * 30/05/2013
 *
 */
public abstract class TLSData extends EncryptDecryptData {

    /**
     * Конструктор.
     *
     * @param adapter Настройки примера.
     */
    protected TLSData(ContainerAdapter adapter) {
        super(adapter); // ignore
    }

    /*
    * Создание SSL контекста.
    *
    * @param callback Логгер.
    * @return готовый SSL контекст.
    * @throws Exception.
    */
    protected SSLContext createSSLContext()
        throws Exception {

        containerAdapter.printConnectionInfo();
        Logger.log("Init trusted store.");

        /**
         * Для чтения(!) доверенного хранилища доступна
         * реализация CertStore из Java CSP. В ее случае
         * можно не использовать пароль.
         */

        KeyStore ts = KeyStore.getInstance(
            containerAdapter.getTrustStoreType(),
            containerAdapter.getTrustStoreProvider());

        ts.load(containerAdapter.getTrustStoreStream(),
            containerAdapter.getTrustStorePassword());

        KeyManagerFactory kmf = KeyManagerFactory
            .getInstance(Provider.KEYMANGER_ALG, Provider.PROVIDER_NAME);

        if (containerAdapter.isUseClientAuth()) {

            // Тип контейнера по умолчанию.
            String keyStoreType = KeyStoreType.currentType();
            Logger.log("Init key store. Load containers. " +
                "Default container type: " + keyStoreType);

            KeyStore ks = KeyStore.getInstance(keyStoreType,
                JCSP.PROVIDER_NAME);

            // Явное указание контейнера.
            if (containerAdapter.getClientAlias() != null) {
                ks.load(new StoreInputStream(containerAdapter.getClientAlias()), null);
            } // if
            else {
                ks.load(null, null);
            } // else

            kmf.init(ks, containerAdapter.getClientPassword());

        } // if

        TrustManagerFactory tmf = TrustManagerFactory
            .getInstance(Provider.KEYMANGER_ALG, Provider.PROVIDER_NAME);
        tmf.init(ts);

        Logger.log("Create SSL context.");

        SSLContext sslCtx = SSLContext.getInstance(Provider.ALGORITHM,
            Provider.PROVIDER_NAME);

        sslCtx.init(containerAdapter.isUseClientAuth()
            ? kmf.getKeyManagers() : null, tmf.getTrustManagers(), null);

        Logger.log("SSL context completed.");
        return sslCtx;

    }

    /**
     * Вывод полученных данных.
     *
     * @param inputStream Входящий поток.
     * @throws Exception
     */
    public static void logData(InputStream inputStream)
        throws Exception {

        BufferedReader br = null;
        if (inputStream != null) {

            try {

                br = new BufferedReader(new InputStreamReader(
                    inputStream, Constants.DEFAULT_ENCODING));

                String input;
                Logger.log("*** Content begin ***");

                while ((input = br.readLine()) != null) {
                    Logger.log(input);
                } // while

                Logger.log("*** Content end ***");

            } finally {

                if (br != null) {

                    try {
                        br.close();
                    } catch (IOException e) {
                        // ignore
                    }

                }

            }

        }

    }

}
