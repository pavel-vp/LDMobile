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
package com.elewise.ldmobile.criptopro.util;

import com.elewise.ldmobile.utils.Logger;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Класс шаблона для группировки свойств удаленного хоста.
 *
 * @author 2014/01/24
 *
 */
public final class RemoteConnectionInfo {

    /**
     * Https-порт по умолчанию.
     */
    private static final int DEFAULT_PORT = 443;

    /**
     * Хост.
     */
    private String hostAddress = null;

    /**
     * Порт.
     */
    private int hostPort = 0;

    /**
     * Страница.
     */
    private String hostPage = null;

    /**
     * Использование client auth.
     */
    private boolean useClientAuth = false;

    /**
     * Конструктор.
     *
     * @param ha Удаленный сервер.
     * @param hp Порт.
     * @param upg Страница.
     * @param ca True, если используется client auth.
     */
    public RemoteConnectionInfo(String ha, int hp,
                                String upg, boolean ca) {
        hostAddress = ha;
        hostPort = hp;
        hostPage = upg;
        useClientAuth = ca;
    }

    /**
     * Получение адреса хоста.
     *
     * @return адрес хоста.
     */
    public String getHostAddress() {
        return  hostAddress;
    }

    /**
     * Получение порта.
     *
     * @return порт.
     */
    public int getHostPort() {
        return hostPort;
    }

    /**
     * Получение страницы.
     *
     * @return страница.
     */
    public String getHostPage() {
        return hostPage;
    }

    /**
     * Проверка использования client auth.
     *
     * @return true, если используем.
     */
    public boolean isUseClientAuth() {
        return useClientAuth;
    }

    /**
     * Получение полного URL.
     *
     * @return url ресурса.
     */
    public String toUrl() {

        URL url = null;

        try {
            url = new URL("https", hostAddress,
                hostPort, "/" + hostPage);
        } catch (MalformedURLException e) {
            // ignore
        }

        return url != null ? url.toString() : null;
    }

    /**
     * Вывод инофрмации о хосте.
     *
     */
    public void print() {
        Logger.log("Remote host: " + getHostAddress() + ":" + getHostPort() +
            "\nPage: " + getHostPage() +
            "\n[client auth: " + isUseClientAuth() + "]");
    }

    /**
     * Удаленный хост, не поддерживающий новую cipher suite
     * (только ГОСТ 2001), без клиентской аутентификации.
     */
    public static final RemoteConnectionInfo host2001NoAuth =
        new RemoteConnectionInfo("tlsgost-2001.cryptopro.ru",
            DEFAULT_PORT, "index.html", false);

    /**
     * Удаленный хост, не поддерживающий новую cipher suite
     * (только ГОСТ 2001), с клиентской аутентификацией.
     */
    public static final RemoteConnectionInfo host2001ClientAuth =
        new RemoteConnectionInfo("tlsgost-2001auth.cryptopro.ru",
            DEFAULT_PORT, "index.html", true);

    /**
     * Удаленный хост, поддерживающий новую cipher suite
     * (ГОСТ 2001, ГОСТ 2012), короткий хеш ГОСТ 2012, без
     * клиентской аутентификации.
     */
    public static final RemoteConnectionInfo host2012256NoAuth =
        new RemoteConnectionInfo("tlsgost-256.cryptopro.ru",
            DEFAULT_PORT, "index.html", false);

    /**
     * Удаленный хост, поддерживающий новую cipher suite
     * (ГОСТ 2001, ГОСТ 2012), короткий хеш ГОСТ 2012, с
     * клиентской аутентификацией.
     */
    public static final RemoteConnectionInfo host2012256ClientAuth =
        new RemoteConnectionInfo("tlsgost-256auth.cryptopro.ru",
            DEFAULT_PORT, "index.html", true);

    /**
     * Удаленный хост, поддерживающий новую cipher suite
     * (ГОСТ 2001, ГОСТ 2012), длинный хеш ГОСТ 2012, без
     * клиентской аутентификации.
     */
    public static final RemoteConnectionInfo host2012512NoAuth =
        new RemoteConnectionInfo("tlsgost-512.cryptopro.ru",
            DEFAULT_PORT, "index.html", false);

    /**
     * Удаленный хост, поддерживающий новую cipher suite
     * (ГОСТ 2001, ГОСТ 2012), длинный хеш ГОСТ 2012, с
     * клиентской аутентификацией.
     */
    public static final RemoteConnectionInfo host2012512ClientAuth =
        new RemoteConnectionInfo("tlsgost-512auth.cryptopro.ru",
            DEFAULT_PORT, "index.html", true);

}
