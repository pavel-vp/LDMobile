/**
 * $RCSfileLicenseNewExample.java,v $
 * version $Revision: 36379 $
 * created 28.08.2017 11:11 by afevma
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
package com.elewise.ldmobile.criptopro;

import com.elewise.ldmobile.interfaces.Executable;
import com.elewise.ldmobile.utils.Logger;

import ru.CryptoPro.JCSP.CSPProviderInterface;
import ru.cprocsp.ACSP.tools.common.CSPLicenseConstants;
import ru.cprocsp.ACSP.tools.license.LicenseInterface;

/**
 * Пример LicenseNewExample предназначен для
 * установки новой лицензии.
 *
 * @author Copyright 2004-2017 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class LicenseNewExample implements Executable<Boolean> {

    /**
     * Новая лицензия CSP.
     */
    private String license;// = "5050G-M0000-01GPD-9BUCU-GW9NG"; // Лицензия CSP 5.0

    /**
     * Параметры провайдера.
     */
    private final CSPProviderInterface providerInfo;

    /**
     * Конструктор.
     *
     * @param providerInfo Параметры провайдера.
     */
    public LicenseNewExample(CSPProviderInterface providerInfo, String license) {
        this.providerInfo = providerInfo;
        this.license = license;
    }

    /**
     * Получение номера лицензии.
     *
     * @return номер лицензии.
     */

    @Override
    public Boolean execute() throws Exception {
        // Проверка текущей лицензии.

        LicenseInterface licenseInterface = providerInfo.getLicense();
        String number = licenseInterface.getSerialNumber();

        Logger.INSTANCE.append("Checking current license "
            + number + "...");

        int licenseStatus = licenseInterface.checkAndSave();
        Logger.INSTANCE.append("Checking completed.");

        printLicenseInfo(licenseInterface, licenseStatus);

        // Проверка новой лицензии.

        Logger.INSTANCE.append("Installing license: " + this.license + "...");

        licenseStatus = licenseInterface.checkAndSave(this.license, false);
        Logger.INSTANCE.append("Installing completed.");

        printLicenseInfo(licenseInterface, licenseStatus);
        return licenseStatus == 0;

    }

    /**
     * Вывод информации о лицензии.
     *
     * @param license Лицензия.
     * @param licenseStatus Статус лицензии.
     */
    private void printLicenseInfo(LicenseInterface license, int licenseStatus) {

        Logger.INSTANCE.append("Result: " + licenseStatus);
        int licenseType = license.getLicenseType();

        if (licenseStatus == CSPLicenseConstants.LICENSE_STATUS_OK) {
            Logger.INSTANCE.append("Status: OK");
        } // if
        else {

            if (licenseType == CSPLicenseConstants.LICENSE_TYPE_EXPIRED) {
                Logger.INSTANCE.append("Status: expired");
            } // if
            else {
                Logger.INSTANCE.append("Status: invalid");
            } // else

        } // else

        if (licenseStatus != CSPLicenseConstants.LICENSE_STATUS_INVALID) {

            if (licenseType == CSPLicenseConstants.LICENSE_TYPE_EXPIRED) {
                Logger.INSTANCE.append("expiredThroughDays: expired");
            } // if
            else if (licenseType == CSPLicenseConstants.LICENSE_TYPE_PERMANENT) {
                Logger.INSTANCE.append("expiredThroughDays: permanent");
            } // else if
            else {
                Logger.INSTANCE.append("expiredThroughDays: " +
                    license.getExpiredThroughDays());
            } // else

        } // if

        Logger.INSTANCE.append("Installation date: " +
            license.getLicenseInstallDateAsString());

        if (licenseStatus != CSPLicenseConstants.LICENSE_STATUS_OK) {
            // throw new Exception("Could not check or/and install license."); // лицензия в примере может быть истекшей
            Logger.INSTANCE.append("Could not check or/and install license. Continue.");
        } // if

    }

}
