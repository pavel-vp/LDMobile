/**
 * $RCSfileLogger.java,v $
 * version $Revision: 36379 $
 * created 21.08.2017 13:15 by afevma
 * last modified $Date: 2012-05-30 12:19:27 +0400 (Ср, 30 май 2012) $ by $Author: afevma $
 *
 *
 * Copyright 2004-2017 Crypto-Pro. All rights reserved.
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package com.elewise.ldmobile.utils

import android.util.Log

/**
 * Служебный класс Logger предназначен
 * для логирования операций.
 *
 * @author Copyright 2004-2017 Crypto-Pro. All rights reserved.
 * @.Version
 */
class Logger {
    /**
     * Добавление сообщений в лог.
     *
     * @param message Сообщение.
     */
    fun append(message: String?) {
        if (message != null) {
            Log.d(LOG_TAG, message)
        }
    }

    companion object {
        /**
         * Тэг лога.
         */
        const val LOG_TAG = "ACSP_Embedded"

        /**
         * Экземпляр лога.
         */
        @JvmField
        var INSTANCE: Logger? = null
        @JvmStatic
        fun init() {
            INSTANCE = Logger()
        }

        @JvmStatic
        fun log(message: String?) {
            if (message != null) {
                Log.d(LOG_TAG, message)
            }
        }
    }
}