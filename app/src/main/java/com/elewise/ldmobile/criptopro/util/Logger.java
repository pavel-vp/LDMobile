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

import android.content.res.Resources;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.elewise.ldmobile.R;
import ru.CryptoPro.JCP.tools.Encoder;

/**
 * Служебный класс LogCallback предназначен
 * для записи в поле сообщений и установки
 * статуса.
 *
 * 30/05/2013
 *
 */
public class Logger {

    /**
     * Поле для записи.
     */
    private EditText logger = null;

    /**
     * Поле для статуса.
     */
    private TextView showStatus = null;

    /**
     * Описание статуса.
     */
    private String statusFieldValue = null;
    private String statusUnknown = null;
    private String statusOK = null;
    private String statusFailed = null;

    /**
     * Экземпляр логгера.
     */
    private static Logger INSTANCE;

    /**
     * Инициализация логгера.
     *
     * @param resources Ресурсы приложения.
     * @param log Графический объект для вывода лога.
     * @param status  Графический объект для вывода
     * статуса.
     */
    public static void init(Resources resources,
                            EditText log, TextView status) {
        INSTANCE = new Logger(resources, log, status);
    }

    /**
     * Конструктор.
     *
     * @param resources Ресурсы приложения.
     * @param log Графический объект для вывода лога.
     * @param status  Графический объект для вывода
     * статуса.
     */
    private Logger(Resources resources,
                   EditText log, TextView status) {

        logger = log;
        showStatus = status;

        statusFieldValue = resources.getString(R.string.StatusField);
        statusUnknown = resources.getString(R.string.StatusUnknown);
        statusOK = resources.getString(R.string.StatusOK);
        statusFailed = resources.getString(R.string.StatusError);
    }

    /**
     * Запись сообщения в поле.
     *
     * @param message Сообщение.
     */
    public static void log(String message) {

        if (INSTANCE != null) {
            INSTANCE.internalLog(message);
        }

    }

    /**
     *  Запись сообщения в поле.
     *
     * @param message Сообщение.
     * @param base64 True, если нужно конвертировать
     * в base64.
     */
    public static void log(byte[] message, boolean base64) {

        if (INSTANCE != null) {
            INSTANCE.internalLog(base64 ? toBase64(message) : new String(message));
        }

    }

    /**
     * Запись сообщения в поле.
     *
     * @param message Сообщение.
     */
    private synchronized void internalLog(final String message) {

        if (logger != null) {

            logger.post(new Runnable() {
                public void run() {
                    logger.append("\n" + message);
                }
            });

        } // if
        else {
            Log.i(Constants.APP_LOGGER_TAG, message);
        } // else

    }

    /**
     * Конвертация в base64.
     *
     * @param data Исходные данные.
     * @return конвертированная строка.
     */
    private static String toBase64(byte[] data) {
        Encoder enc = new Encoder();
        return enc.encode(data);
    }

    /**
     * Очистка поля.
     */
    public static void clear() {

        if (INSTANCE != null) {
            INSTANCE.internalClear();
        }

    }

    /**
     * Очистка поля.
     */
    private synchronized void internalClear() {

        if (logger != null) {

            logger.post(new Runnable() {
                public void run() {
                    logger.setText("");
                }
            });

        } // if

        if (showStatus != null) {

            showStatus.post(new Runnable() {
                public void run() {
                    showStatus.setText(statusFieldValue + ": " +
                        statusUnknown);
                }
            });

        } // if
    }

    /**
     * Задание статуса провала.
     *
     */
    public static void setStatusFailed() {

        if (INSTANCE != null) {
            INSTANCE.setStatus(INSTANCE.statusFailed);
        }

    }

    /**
     * Задание статуса успеха.
     *
     */
    public static void setStatusOK() {

        if (INSTANCE != null) {
            INSTANCE.setStatus(INSTANCE.statusOK);
        }

    }

    /**
     * Отображение строки статуса.
     *
     * @param status Строка статуса.
     */
    private synchronized void setStatus(final String status) {

        if (showStatus != null) {

            showStatus.post(new Runnable() {
                public void run() {
                    showStatus.setText(statusFieldValue +
                        ": " + status);
                }
            });

        } // if
    }

}
