/**
 * $RCSfileKeyStoreUtil.java,v $
 * version $Revision: 36379 $
 * created 14.06.2017 9:41 by afevma
 * last modified $Date: 2012-05-30 12:19:27 +0400 (Ср, 30 май 2012) $ by $Author: afevma $
 * <p/>
 * Copyright 2004-2017 Crypto-Pro. All rights reserved.
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package com.elewise.ldmobile.criptopro.util;

import android.util.Log;

import java.security.KeyStore;
import java.security.Provider;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCSP.JCSP;

/**
 * Служебный класс KeyStoreUtil для работы с
 * контейнерами.
 *
 * @author Copyright 2004-2017 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class KeyStoreUtil {

    /**
     * Java-провайдер Java CSP.
     */
    public static final Provider DEFAULT_PROVIDER = new JCSP();

    /**
     * Загрузка тех алиасов, которые находятся в хранилище storeType
     * с алгоритмом, сооветствующим типу providerType.
     *
     * @param storeType Тип контейнера.
     * @param providerType Тип провайдера.
     */
    public static List<String> aliases(String storeType,
                                       AlgorithmSelector.DefaultProviderType providerType) {

        List<String> aliasesList = new ArrayList<String>();

        try {

            KeyStore keyStore = KeyStore.getInstance(storeType, JCSP.PROVIDER_NAME);
            keyStore.load(null, null);

            Enumeration<String> aliases = keyStore.aliases();

            // Подбор алиасов.
            while (aliases.hasMoreElements()) {

                String alias = aliases.nextElement();
                X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);

                if (cert != null) {
                    String keyAlgorithm = cert.getPublicKey().getAlgorithm();

                    if (providerType.equals(AlgorithmSelector.DefaultProviderType.pt2001) &&
                       (keyAlgorithm.equalsIgnoreCase(JCP.GOST_EL_DEGREE_NAME))) {
                        aliasesList.add(alias);
                    } // if
                    else if (providerType.equals(AlgorithmSelector.DefaultProviderType.pt2012Short) &&
                        (keyAlgorithm.equalsIgnoreCase(JCP.GOST_EL_2012_256_NAME))) {
                        aliasesList.add(alias);
                    } // else
                    else if (providerType.equals(AlgorithmSelector.DefaultProviderType.pt2012Long) &&
                        (keyAlgorithm.equalsIgnoreCase(JCP.GOST_EL_2012_512_NAME))) {
                        aliasesList.add(alias);
                    } // else

                } // if

            } // while

        } catch (Exception e) {
            Log.e(Constants.APP_LOGGER_TAG, e.getMessage(), e);
        }

        return aliasesList;

    }

    public static void removeContainer(String name) {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStoreType.currentType(), JCSP.PROVIDER_NAME);
            keyStore.load(null, null);
            keyStore.deleteEntry(name);
            keyStore.store(null, null);
        } catch (Exception e) {
            Log.e("error", e.toString());
        }
    }
}
