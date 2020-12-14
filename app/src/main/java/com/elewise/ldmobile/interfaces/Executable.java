/**
 * $RCSfileExecutable.java,v $
 * version $Revision: 36379 $
 * created 21.08.2017 16:03 by afevma
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
package com.elewise.ldmobile.interfaces;

import ru.CryptoPro.JCP.JCP;

/**
 * Служебный интерфейс Executable предназначен
 * для реализации примеров.
 *
 * @author Copyright 2004-2017 Crypto-Pro. All rights reserved.
 * @.Version
 */
public interface Executable<T> {

    /**
     * Алгоритм ключа.
     */
    String KEY_ALGORITHM = JCP.GOST_EL_2012_256_NAME;

    /**
     * Алгоритм подписи.
     */
    String SIG_ALGORITHM = JCP.GOST_SIGN_2012_256_NAME;

    /**
     * Выполнение задачи.
     *
     * @return результат.
     * @throws Exception
     */
    T execute() throws Exception;

}
