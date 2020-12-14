package com.elewise.ldmobile;

import android.content.Context;
import androidx.multidex.MultiDexApplication;
import android.util.Log;

import com.elewise.ldmobile.criptopro.LicenseNewExample;
import com.elewise.ldmobile.criptopro.util.ProviderType;
import com.elewise.ldmobile.utils.Logger;

import org.apache.xml.security.utils.resolver.ResourceResolver;
import org.jcp.xml.dsig.internal.dom.XMLDSigRI;

import java.io.File;
import java.security.Provider;
import java.security.Security;

import ru.CryptoPro.AdES.AdESConfig;
import ru.CryptoPro.JCPxml.XmlInit;
import ru.CryptoPro.JCSP.CSPConfig;
import ru.CryptoPro.JCSP.CSPProviderInterface;
import ru.CryptoPro.JCSP.JCSP;
import ru.CryptoPro.JCSP.support.BKSTrustStore;
import ru.CryptoPro.reprov.RevCheck;
import ru.CryptoPro.ssl.util.cpSSLConfig;
import ru.cprocsp.ACSP.tools.common.Constants;

public class MainApp extends MultiDexApplication {
    private static Context context;

    public void onCreate() {
        super.onCreate();
        MainApp.context = getApplicationContext();

        Logger.init();

        if (!initProviders()) {
            Log.e(Constants.APP_LOGGER_TAG, "Couldn't initialize CSP.");
            return;

        }

        CSPProviderInterface providerInfo = CSPConfig.INSTANCE.getCSPProviderInfo();
        // check license
        providerInfo.getLicense().checkAndSave(providerInfo.getLicense().getSerialNumber(), false);

        // 1. Инициализация провайдеров: CSP и java-провайдеров
        // (Обязательная часть).

//        if (!initCSPProviders()) {
//            Log.i(Constants.APP_LOGGER_TAG, "Couldn't initialize CSP.");
//            return;
//        } // if
//
        initJavaProviders();
//
//        // 2. Копирование тестовых контейнеров для подписи,
//        // проверки подписи, шифрования и TLS (Примеры и вывод
//        // в лог).
//
//        initLogger();
//        installContainers();
//
//        // 3. Инициируем объект для управления выбором типа
//        // контейнера (Настройки).
//
//        KeyStoreType.init(this);
//
//        // 4. Инициируем объект для управления выбором типа
//        // провайдера (Настройки).
//
        ProviderType.init(this);
    }

    public static Context getApplcationContext() {
        return context;
    }

    /**
     * Инициализация CSP провайдера. Добавление
     * нативного провайдера Java CSP, SSL-провайдера
     * и Revocation-провайдера в список Security.
     *
     * Происходит один раз при инициализации.
     * Возможно только после инициализации в CSPConfig!
     *
     * @return true в случае успешной инициализации.
     */
    private boolean initProviders() {

        //
        // Инициализация провайдера CSP. Должна выполняться
        // один раз в главном потоке приложения, т.к. использует
        // статические переменные.
        //
        // Далее может быть использована версия функции инициализации:
        // 1) расширенная - initEx(): она содержит init() и дополнительно
        // выполняет загрузку java-провайдеров (Java CSP, RevCheck, Java TLS)
        // и настройку некоторых параметров, например, Java TLS;
        // 2) обычная - init(): без загрузки java-провайдеров и настройки
        // параметров.
        //
        // Для совместного использования ГОСТ и не-ГОСТ TLS НЕ следует
        // переопределять свойства System.getProperty(javax.net.*) и
        // Security.setProperty(ssl.*).
        //
        // Ниже используется обычная версия init() функции инициализации
        // и свойства TLS НЕ переопределяются, т.к. в приложении нет примеров
        // работы с УЦ 1.5, которые обращаются к свойствам по умолчанию.
        //
        // 1. Создаем инфраструктуру CSP и копируем ресурсы
        // в папку. В случае ошибки мы, например, выводим окошко
        // (или как-то иначе сообщаем) и завершаем работу.
        //

        int initCode  = CSPConfig.init(this);
        if (initCode != CSPConfig.CSP_INIT_OK) {

            Log.e("Error CSP initiation", "Error occurred during CSP initiation: " + initCode);
            return false;

        } // if

        // %%% Инициализация остальных провайдеров %%%

        //
        // 2. Загрузка Java CSP (хеш, подпись, шифрование,
        // генерация контейнеров).
        //

        if (Security.getProvider(JCSP.PROVIDER_NAME) == null) {
            Security.addProvider(new JCSP());
        } // if

        //
        // 3. Загрузка Java TLS (TLS).
        //
        // Необходимо переопределить свойства, чтобы
        // использовались менеджеры из cpSSL, а не
        // Harmony.
        //
        // Внимание!
        // Чтобы не мешать не-ГОСТовой реализации, ряд свойств внизу *.ssl не
        // следует переопределять. При этом не исключены проблемы в работе с
        // ГОСТом там, где TLS-реализация клиента обращается к дефолтным алгоритмам
        // реализаций этих factory (особенно: apache http client или HttpsURLConnection
        // без SSLSocketFactory и с System.setProperty(javax.net.*)).
        //
        // Если инициализировать провайдер в CSPConfig с помощью initEx(), то
        // свойства будут включены там, поэтому выше используется упрощенная
        // версия инициализации.
        //
        // Security.setProperty("ssl.KeyManagerFactory.algorithm",   ru.CryptoPro.ssl.Provider.KEYMANGER_ALG);
        // Security.setProperty("ssl.TrustManagerFactory.algorithm", ru.CryptoPro.ssl.Provider.KEYMANGER_ALG);
        //
        // Security.setProperty("ssl.SocketFactory.provider",       "ru.CryptoPro.ssl.SSLSocketFactoryImpl");
        // Security.setProperty("ssl.ServerSocketFactory.provider", "ru.CryptoPro.ssl.SSLServerSocketFactoryImpl");
        //

        if (Security.getProvider(ru.CryptoPro.ssl.Provider.PROVIDER_NAME) == null) {
            Security.addProvider(new ru.CryptoPro.ssl.Provider());
        } // if

        //
        // 4. Провайдер хеширования, подписи, шифрования
        // по умолчанию.
        //

        cpSSLConfig.setDefaultSSLProvider(JCSP.PROVIDER_NAME);

        //
        // 5. Загрузка Revocation Provider (CRL, OCSP).
        //

        if (Security.getProvider(RevCheck.PROVIDER_NAME) == null) {
            Security.addProvider(new RevCheck());
        } // if

        //
        // 6. Отключаем проверку цепочки штампа времени (CAdES-T),
        // чтобы не требовать него CRL.
        //

        System.setProperty("ru.CryptoPro.CAdES.validate_tsp", "false");

        //
        // 7. Таймауты для CRL на всякий случай.
        //

        System.setProperty("com.sun.security.crl.timeout",  "5");
        System.setProperty("ru.CryptoPro.crl.read_timeout", "5");

        // 8. Включаем возможность онлайновой проверки
        // статуса сертификата.
        //
        // Для TLS проверку цепочки сертификатов другой стороны
        // можно отключить, если создать параметр
        // Enable_revocation_default=false в файле android_pref_store
        // (shared preferences), см.
        // {@link ru.CryptoPro.JCP.tools.pref_store#AndroidPrefStore}.

        System.setProperty("com.sun.security.enableCRLDP", "true");
        System.setProperty("com.ibm.security.enableCRLDP", "true");

        // 9. Дополнительно задаем путь к хранилищу доверенных
        // сертификатов.
        // Не обязательно, если нет кода, использующего такой
        // способ получения списка доверенных сертификатов.
        //
        // Внимание!
        // Чтобы не мешать не-ГОСТовой реализации, ряд свойств внизу *.ssl и
        // javax.net.* НЕ следует переопределять. Но при этом не исключены проблемы
        // в работе с ГОСТом там, где TLS-реализация клиента обращается к дефолтным
        // алгоритмам реализаций этих factory (особенно: apache http client или
        // HttpsURLConnection без передачи SSLSocketFactory).
        // Здесь эти свойства НЕ включены, т.к. нет примеров работы с УЦ 1.5,
        // использующих алгоритмы по умолчанию.
        //
        // final String trustStorePath = getLocalTrustStorePath();
        // final String trustStorePassword = String.valueOf(getLocalTrustStorePassword());
        //
        // Log.d(Constants.APP_LOGGER_TAG, "Default trust store: " + trustStorePath);
        //
        // System.setProperty("javax.net.ssl.trustStoreType", getLocalTrustStoreType());
        // System.setProperty("javax.net.ssl.trustStore", trustStorePath);
        // System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);

        return true;

    }

//   /* /************************ Инициализация провайдера ************************/
//
//    /**
//     * Инициализация CSP провайдера.
//     *
//     * @return True в случае успешной инициализации.
//     */
//    private boolean initCSPProviders() {
//
//        // Инициализация провайдера CSP. Должна выполняться
//        // один раз в главном потоке приложения, т.к. использует
//        // статические переменные.
//        //
//        // 1. Создаем инфраструктуру CSP и копируем ресурсы
//        // в папку. В случае ошибки мы, например, выводим окошко
//        // (или как-то иначе сообщаем) и завершаем работу.
//
//        int initCode   = CSPConfig.initEx(this);
//        boolean initOk = initCode == CSPConfig.CSP_INIT_OK;
//
//        // Если инициализация не удалась, то сообщим об ошибке.
//        if (!initOk) {
//
//            switch (initCode) {
//
//                // Не передан контекст приложения (null). Он необходим,
//                // чтобы произвести копирование ресурсов CSP, создание
//                // папок, смену директории CSP и т.п.
//                case CSPConfig.CSP_INIT_CONTEXT:
//                    Log.e("initCSPProviders", "Couldn't initialize context.");
//                    break;
//
//                /**
//                 * Не удается создать инфраструктуру CSP (папки): нет
//                 * прав (нарушен контроль целостности) или ошибки.
//                 * Подробности в logcat.
//                 */
//                case CSPConfig.CSP_INIT_CREATE_INFRASTRUCTURE:
//                    Log.e("initCSPProviders", "Couldn't create CSP infrastructure.");
//                    break;
//
//                /**
//                 * Не удается скопировать все или часть ресурсов CSP -
//                 * конфигурацию, лицензию (папки): нет прав (нарушен
//                 * контроль целостности) или ошибки.
//                 * Подробности в logcat.
//                 */
//                case CSPConfig.CSP_INIT_COPY_RESOURCES:
//                    Log.e("initCSPProviders", "Couldn't copy CSP resources.");
//                    break;
//
//                /**
//                 * Не удается задать рабочую директорию для загрузки
//                 * CSP. Подробности в logcat.
//                 */
//                case CSPConfig.CSP_INIT_CHANGE_WORK_DIR:
//                    Log.e("initCSPProviders", "Couldn't change CSP working directory.");
//                    break;
//
//                /**
//                 * Неправильная лицензия.
//                 */
//                case CSPConfig.CSP_INIT_INVALID_LICENSE:
//                    Log.e("initCSPProviders", "Invalid CSP serial number.");
//                    break;
//
//                /**
//                 * Не удается создать хранилище доверенных сертификатов
//                 * для CAdES API.
//                 */
//                case CSPConfig.CSP_TRUST_STORE_FAILED:
//                    Log.e("initCSPProviders", "Couldn't create trust store for CAdES API.");
//                    break;
//
//                /**
//                 * Не удается сохранить путь к библиотекам провайдера
//                 * в конфиг.
//                 */
//                case CSPConfig.CSP_STORE_LIBRARY_PATH:
//                    Log.e("initCSPProviders", "Couldn't store native library path to config.");
//                    break;
//
//                /**
//                 * Ошибка контроля целостности.
//                 */
//                case CSPConfig.CSP_INIT_INVALID_INTEGRITY:
//                    Log.e("initCSPProviders", "Integrity control failure.");
//                    break;
//
//            } // switch
//
//        } // if
//
//        return initOk;
//    }
//
//    /**
//     * Добавление нативного провайдера Java CSP,
//     * SSL-провайдера и Revocation-провайдера в
//     * список Security. Инициализируется JCPxml,
//     * CAdES.
//     *
//     * Происходит один раз при инициализации.
//     * Возможно только после инициализации в CSPConfig!
//     *
//     */
    private void initJavaProviders() {

        // Задание провайдера по умолчанию для CAdES.

        AdESConfig.setDefaultProvider(JCSP.PROVIDER_NAME);

        // Инициализация XML DSig (хеш, подпись).

//        XmlInit.init();

        // Добавление реализации поиска узла по ID.

//        ResourceResolver.registerAtStart(XmlInit.JCP_XML_DOCUMENT_ID_RESOLVER);

        // Добавление XMLDSigRI провайдера, так как его
        // использует XAdES.

//        Provider xmlDSigRi = new XMLDSigRI();
//        Security.addProvider(xmlDSigRi);

        Provider provider = Security.getProvider("XMLDSig");
        if (provider != null) {

            Security.getProvider("XMLDSig").put("XMLSignatureFactory.DOM",
                    "ru.CryptoPro.JCPxml.dsig.internal.dom.DOMXMLSignatureFactory");

            Security.getProvider("XMLDSig").put("KeyInfoFactory.DOM",
                    "ru.CryptoPro.JCPxml.dsig.internal.dom.DOMKeyInfoFactory");

        } // if

        // Включаем возможность онлайновой проверки статуса
        // сертификата.
        //
        // Для TLS проверку цепочки сертификатов другой стороны
        // можно отключить, если создать параметр
        // Enable_revocation_default=false в файле android_pref_store
        // (shared preferences), см.
        // {@link ru.CryptoPro.JCP.tools.pref_store#AndroidPrefStore}.

        System.setProperty("com.sun.security.enableCRLDP", "true");
        System.setProperty("com.ibm.security.enableCRLDP", "true");
        System.setProperty("ocsp.enable", "true");

        // Отключаем требование проверки сертификата и хоста.
        System.setProperty("tls_prohibit_disabled_validation", "true");
//        System.setProperty("CERTIFICATE_INCLUDE_WHOLE_CHAIN", "true");

        // Настройки TLS для генерации контейнера и выпуска сертификата
        // в УЦ 2.0, т.к. обращение к УЦ 2.0 будет выполняться по протоколу
        // HTTPS и потребуется авторизация по сертификату. Указываем тип
        // хранилища с доверенным корневым сертификатом, путь к нему и пароль.

        final String trustStorePath = getApplicationInfo().dataDir + File.separator +
                BKSTrustStore.STORAGE_DIRECTORY + File.separator + BKSTrustStore.STORAGE_FILE_TRUST;

        final String trustStorePassword = String.valueOf(BKSTrustStore.STORAGE_PASSWORD);
        Log.d(Constants.APP_LOGGER_TAG, "Default trust store: " + trustStorePath);

        System.setProperty("javax.net.ssl.trustStoreType", BKSTrustStore.STORAGE_TYPE);
        System.setProperty("javax.net.ssl.trustStore", trustStorePath);
        System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);

    }
//
//    /**
//     * Инициализация объекта для отображения логов.
//     *
//     */
//    private void initLogger() {
//
//        // Поле для вывода логов и метка для отображения
//        // статуса.
//
////        EditText etLog = (EditText) findViewById(R.id.etLog);
////        etLog.setMinLines(10);
////
////        TextView tvOpStatus = (TextView) findViewById(R.id.tvOpStatus);
//
////        Logger.init(getResources(), etLog, tvOpStatus);
////        Logger.clear();
//
//    }
//
//    /**
//     * Копирование тестовых контейнеров для подписи,
//     * шифрования, обмена по TLS из архива в папку
//     * keys приложения.
//     *
//     */
//    private void installContainers() {
////        final CSPTool cspTool = new CSPTool(this);
////        cspTool.getAppInfrastructure().copyContainerFromArchive(R.raw.keys);
//    }
//
}
