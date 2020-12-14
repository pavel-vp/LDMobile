package com.elewise.ldmobile.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.elewise.ldmobile.R;
import com.elewise.ldmobile.criptopro.base.GenKeyPairData;
import com.elewise.ldmobile.criptopro.util.KeyStoreType;
import com.elewise.ldmobile.criptopro.util.KeyStoreUtil;
import com.elewise.ldmobile.criptopro.util.ProviderType;
import com.elewise.ldmobile.model.CertificateInfo;
import com.elewise.ldmobile.service.Prefs;
import com.elewise.ldmobile.utils.Logger;
import com.elewise.ldmobile.widget.CertWidgetTrust;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.CryptoPro.JCSP.CSPConfig;
import ru.CryptoPro.JCSP.CSPProviderInterface;
import ru.CryptoPro.JCSP.support.BKSTrustStore;
import ru.cprocsp.ACSP.tools.common.CSPTool;
import ru.cprocsp.ACSP.tools.common.Constants;
import ru.cprocsp.ACSP.util.FileExplorerActivity;

import static ru.cprocsp.ACSP.tools.common.CSPLicenseConstants.CSP_50_LICENSE_DEFAULT;
import static ru.cprocsp.ACSP.tools.common.CSPLicenseConstants.LICENSE_STATUS_EXPIRED;
import static ru.cprocsp.ACSP.tools.common.CSPLicenseConstants.LICENSE_STATUS_INVALID;
import static ru.cprocsp.ACSP.tools.common.CSPLicenseConstants.LICENSE_STATUS_OK;
import static ru.cprocsp.ACSP.tools.common.CSPLicenseConstants.LICENSE_TYPE_EXPIRED;
import static ru.cprocsp.ACSP.tools.common.CSPLicenseConstants.LICENSE_TYPE_PERMANENT;
import static ru.cprocsp.ACSP.util.FileExplorerActivity.INTENT_EXTRA_IN_ONLY_DIRS;
import static ru.cprocsp.ACSP.util.FileExplorerActivity.INTENT_EXTRA_OUT_CHOSEN_OBJECT;

public class SettingsCriptoProActivity extends BaseActivity
        implements AdapterView.OnItemSelectedListener {
    // todo обсудить пароль!!
    // Пароль к хранилищу доверенных сертификатов по умолчанию.
    private static final char[] DEFAULT_TRUST_STORE_PASSWORD = BKSTrustStore.STORAGE_PASSWORD;

    // Интент выбора контейнера (ACSP).
    public static final String INTENT_CONTAINER_SELECT = "ru.cprocsp.intent.SELECT_DIR";

    // Идентификатор запроса выбора контейнера.
    private static final int CONTAINER_SELECT_CODE_IN = 0;

    // Идентификатор запроса выбора контейнера.
    private static final int CERT_TRUST_SELECT_CODE_IN = 1;

    // Номер выбранного типа хранилища в списке.
    private int keyStoreTypeIndex = 0;

    // Номер выбранного типа провайдера в списке.
    private int providerTypeIndex = 0;

    private Spinner spClientList;
    // Адаптер списка алиасов контейнеров.
    private ArrayAdapter<String> containerAliasAdapter = null;

    private LinearLayout lvTrustCert;

    private FileExplorerActivity fileExplorerActivity;

    private AlertDialog dialogRemoveCert;
    private AlertDialog dialogRemoveContainer;

    private TextView tvLicenseNumber;
    private TextView tvLicenseStatus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_cripto_pro);

        updateActionBar(getString(R.string.activity_settings_cripto_pro_title));

        tvLicenseNumber = findViewById(R.id.tvLicenseNumber);
        tvLicenseStatus = findViewById(R.id.tvLicenseStatus);

        showContainerType();

        showProviderType();

        Button btnChangeLicense = findViewById(R.id.btnChangeLicense);
        btnChangeLicense.setOnClickListener(v -> {
            startActivity(new Intent(this, InputLicenseNumberActivity.class));
        });

        Button btnMoveContainers = findViewById(R.id.btnMoveContainers);
        btnMoveContainers.setOnClickListener(v -> {
            final Intent intent = new Intent(INTENT_CONTAINER_SELECT);
            intent.putExtra(INTENT_EXTRA_IN_ONLY_DIRS, true);
            startActivityForResult(intent, CONTAINER_SELECT_CODE_IN);
        });

        Button btnMoveCertTrust = findViewById(R.id.btnMoveCertTrust);
        btnMoveCertTrust.setOnClickListener(v -> {
            final Intent intent = new Intent(INTENT_CONTAINER_SELECT);
            intent.putExtra(INTENT_EXTRA_OUT_CHOSEN_OBJECT, true);
            startActivityForResult(intent, CERT_TRUST_SELECT_CODE_IN);
        });

        ImageButton btnRemoveContainer = findViewById(R.id.btnRemoveContainer);
        btnRemoveContainer.setOnClickListener(v -> {
            String alias = (String) spClientList.getSelectedItem();
            if (alias != null) {
                createAndShowDialogRemoveContainer(alias);
            }
        });

        // Список клиентских алиасов.
        spClientList = findViewById(R.id.spExamplesClientList);
        containerAliasAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item);
        // Способ отображения.

        containerAliasAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        spClientList.setAdapter(containerAliasAdapter);
        spClientList.setOnItemSelectedListener(this);

        updateContainerList();

        lvTrustCert = findViewById(R.id.lvTrustCert);
        updateTrustListAndCertList();
    }

    private void showLicenseNumberInfo() {
        CSPProviderInterface providerInfo = CSPConfig.INSTANCE.getCSPProviderInfo();
        String licenseNumber = providerInfo.getLicense().getSerialNumber();
        tvLicenseNumber.setText(licenseNumber);
        int licenseStatus = providerInfo.getLicense().getExistingLicenseStatus();

        if (licenseStatus == LICENSE_STATUS_OK && !providerInfo.getLicense().getSerialNumber().equals(CSP_50_LICENSE_DEFAULT)) {
            int licenseType = providerInfo.getLicense().getLicenseType();
            switch (licenseType) {
                case LICENSE_TYPE_PERMANENT: {
                    tvLicenseStatus.setText(getString(R.string.settings_license_type_permanent));
                    break;
                }
                case LICENSE_TYPE_EXPIRED: {
                    String days = String.valueOf(providerInfo.getLicense().getExpiredThroughDays());
                    tvLicenseStatus.setText(getString(R.string.settings_license_type_expired_days, days));
                    break;
                }
            }
        } else {
            if (licenseStatus == LICENSE_STATUS_EXPIRED) {
                tvLicenseStatus.setText(getString(R.string.settings_license_type_expired));
            } else {
                if (licenseStatus == LICENSE_STATUS_INVALID) {
                    tvLicenseStatus.setText(getString(R.string.settings_license_type_invalid));
                } else {
                    Log.e("error", "unknown license status type");
                }
            }
        }
    }

    private void showContainerType() {
        // Тип контейнера.
        Spinner spKeyStoreType = findViewById(R.id.spKeyStore);

        // Получение списка поддерживаемых типов хранилищ.
        List<String> keyStoreTypeList = KeyStoreType.getKeyStoreTypeList();

        // Создаем ArrayAdapter для использования строкового массива
        // и способа отображения объекта.
        ArrayAdapter<String> keyStoreTypeAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item,
                keyStoreTypeList);

        // Способ отображения.
        keyStoreTypeAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        spKeyStoreType.setAdapter(keyStoreTypeAdapter);
        spKeyStoreType.setOnItemSelectedListener(this);

        // Выбираем сохраненный ранее тип.
        keyStoreTypeIndex = keyStoreTypeAdapter.getPosition(KeyStoreType.currentType());
        spKeyStoreType.setSelection(keyStoreTypeIndex);
    }

    private void showProviderType() {
        Spinner spProviderType = findViewById(R.id.spProviderType);

        // Создаем ArrayAdapter для использования строкового массива
        // и способа отображения объекта.
        ArrayAdapter<CharSequence> providerTypeAdapter =
                ArrayAdapter.createFromResource(this,
                        R.array.providerTypes, android.R.layout.simple_spinner_item);

        // Способ отображения.
        providerTypeAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        spProviderType.setAdapter(providerTypeAdapter);
        spProviderType.setOnItemSelectedListener(this);

        // Выбираем сохраненный ранее тип.
        providerTypeIndex = providerTypeAdapter.getPosition(ProviderType.currentType());
        spProviderType.setSelection(providerTypeIndex);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showLicenseNumberInfo();

        // Необходимо для отображения диалоговых окон
        // ДСЧ, ввода пин-кода и сообщений.
        if (CSPConfig.INSTANCE != null) {
            CSPConfig.registerActivityContext(this);
        }
    }

    List<String> oldAliasesList = new ArrayList<>();

    private void updateContainerList() {
        List<String> aliasesList = KeyStoreUtil.aliases(
                KeyStoreType.currentType(),
                ProviderType.currentProviderType()
        );

        if (!aliasesList.equals(oldAliasesList)) {
            containerAliasAdapter.clear();
            containerAliasAdapter.addAll(aliasesList);
            containerAliasAdapter.notifyDataSetChanged();

            oldAliasesList.clear();
            oldAliasesList.addAll(aliasesList);
        }
    }

    public void updateTrustListAndCertList() {
        lvTrustCert.removeAllViews();

        for (CertificateInfo item: getTrustList()) {
            lvTrustCert.addView(new CertWidgetTrust(this, item, alias -> {
                createAndShowDialogRemoveCert(alias);
                return null;
            }));
        }
    }

    private void createAndShowDialogRemoveContainer(String alias) {
        if (dialogRemoveContainer== null) {
            dialogRemoveContainer= new AlertDialog
                    .Builder(this)
                    .setTitle(R.string.activity_settings_dialog_remove_key_store_title)
                    .setNegativeButton(R.string.activity_settings_dialog_cancel, null)
                    .setPositiveButton(R.string.activity_settings_dialog_remove, (dialog, which) -> {
                        KeyStoreUtil.removeContainer(alias);
                        updateContainerList();
                    })
                    .create();
        }
        dialogRemoveContainer.show();
    }

    private void createAndShowDialogRemoveCert(String alias) {
        if (dialogRemoveCert == null) {
            dialogRemoveCert = new AlertDialog
                    .Builder(this)
                    .setTitle(R.string.activity_settings_dialog_remove_cert_title)
                    .setNegativeButton(R.string.activity_settings_dialog_cancel, null)
                    .setPositiveButton(R.string.activity_settings_dialog_remove, (dialog, which) -> {
                        removeCertificate(alias);
                        updateTrustListAndCertList();
                    })
                    .create();
        }
        dialogRemoveCert.show();
    }

    private List<CertificateInfo> getTrustList() {
        List<CertificateInfo> res = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        try {
            FileInputStream storeStream = new FileInputStream(TRUST_STORE_PATH);
            KeyStore keyStore = KeyStore.getInstance(BKSTrustStore.STORAGE_TYPE);

            keyStore.load(storeStream, DEFAULT_TRUST_STORE_PASSWORD);
            storeStream.close();
//            KeyStore ts = KeyStore.getInstance(
//                    containerAdapter.getTrustStoreType(),
//                    containerAdapter.getTrustStoreProvider());
//
//            ts.load(containerAdapter.getTrustStoreStream(),
//                    containerAdapter.getTrustStorePassword());

            for (String alias : Collections.list(keyStore.aliases())) {
                if (!alias.startsWith("root")) {
                    StringBuilder stringBuffer = new StringBuilder();
                    X509Certificate certificate = ((X509Certificate)keyStore.getCertificate(alias));
                    stringBuffer.append("Серийный номер: " + certificate.getSerialNumber().toString(16));
                    stringBuffer.append("Владелец: " + certificate.getSubjectDN() + "\n");
                    stringBuffer.append("Издатель: " + certificate.getIssuerDN() + "\n");
                    stringBuffer.append("Действителен с: " + sdf.format(certificate.getNotBefore()) + "\n");
                    stringBuffer.append("Действителен по: " + sdf.format(certificate.getNotAfter()));

                    res.add(new CertificateInfo(alias, stringBuffer.toString()));
                }
            }
            return res;
        } catch (Exception e) {
            return res;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // first saving my state, so the bundle wont be empty.
        outState.putString("WORKAROUND_FOR_BUG_19917_KEY",
                "WORKAROUND_FOR_BUG_19917_VALUE");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        switch (adapterView.getId()) {

            case R.id.spKeyStore: {

                if (keyStoreTypeIndex != i) {

                    String keyStoreType = (String) adapterView.getItemAtPosition(i);
                    KeyStoreType.saveCurrentType(keyStoreType);

                    keyStoreTypeIndex = i;

                } // if

            }
            break;

            case R.id.spProviderType: {

                if (providerTypeIndex != i) {

                    String provType = (String) adapterView.getItemAtPosition(i);
                    ProviderType.saveCurrentType(provType);

                    providerTypeIndex = i;

                } // if

            }
            break;

            case R.id.spExamplesClientList: {
                String clientAlias = (String) adapterView.getItemAtPosition(i);
                Prefs.INSTANCE.saveContainerAlias(this, clientAlias);
            }
            break;

        } // switch

        ProviderType.currentProviderType();

        updateContainerList();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CONTAINER_SELECT_CODE_IN: {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    final String path = data.getStringExtra(INTENT_EXTRA_OUT_CHOSEN_OBJECT);
                    if (path != null) {
                        copyContainer(path);
                    }
                }
                break;
            }
            case CERT_TRUST_SELECT_CODE_IN: {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    final String path = data.getStringExtra(INTENT_EXTRA_OUT_CHOSEN_OBJECT);
                    if (path != null) {
                        copyCertTrust(path);
                    }
                }
                break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // Добавление обработчика в кнопку копирования контейнеров
    private void copyContainer(@NonNull String path) {
        Logger.log("*** Copy containers to the " +
                "application store ***");

        boolean copied = false;

        try {
            // Получаем исходную папку с контейнерами.
            if (path.isEmpty()) {
                Logger.log("Container directory is undefined.");
                return;

            }

            Logger.log("Source directory: " + path);
            CSPTool cspTool = new CSPTool(SettingsCriptoProActivity.this);

            copied = cspTool.getAppInfrastructure()
                    .copyContainerFromDirectory(path);

        } catch (Exception e) {
            Log.e(Constants.APP_LOGGER_TAG, e.getMessage(), e);
        }

        if (copied) {
            Toast.makeText(SettingsCriptoProActivity.this, R.string.activity_settings_cripto_pro_add_container_ok, Toast.LENGTH_LONG).show();
            updateContainerList();
        } else {
            Toast.makeText(SettingsCriptoProActivity.this, R.string.activity_settings_cripto_pro_add_container_failed, Toast.LENGTH_LONG).show();
        }
    }

    private InputStream chooseCertTrust;

    // Добавление обработчика в кнопку копирования корневого сертификата
    private void copyCertTrust(@NonNull String path) {
        try {
            checkTrustStore();

            Logger.log("*** Copy cert trust to the " +
                    "application store ***");

            // Получаем исходную папку с контейнерами.
            if (path.isEmpty()) {

                Logger.log("Cert trust' directory is undefined.");
                return;

            }

            boolean copied = false;

            try {
                Uri uri = Uri.parse("file://"+path);
                chooseCertTrust = getContentResolver().openInputStream(uri);
                loadCert(chooseCertTrust);
                copied = true;
            } catch (Exception e) {
                Log.e(Constants.APP_LOGGER_TAG, e.getMessage(), e);
            }

            if (copied) {
                Toast.makeText(SettingsCriptoProActivity.this, R.string.activity_settings_cripto_pro_add_cert_ok, Toast.LENGTH_LONG).show();
                updateTrustListAndCertList();
            } else {
                Toast.makeText(SettingsCriptoProActivity.this, R.string.activity_settings_cripto_pro_add_cert_failed, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e("save trust", e.toString());
        }
    }

    /**
     * Загрузка сертификата из потока в список.
     *
     * @param trustStream Поток данных.
     * @throws Exception
     */
    private void loadCert(InputStream trustStream) throws  Exception {

        try {
            final CertificateFactory factory = CertificateFactory.getInstance("X.509");
            List<X509Certificate> trustCert = (List<X509Certificate>)factory.generateCertificates(trustStream);

            for (X509Certificate item: trustCert) {
                saveTrustCert(trustStoreFile, item);
            }
        } finally {
            if (trustStream != null) {
                try {
                    trustStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    // Файл хранилища
    private File trustStoreFile = null;

    // Путь к хранилищу доверенных сертификатов для установки сертификатов.
    public final static String TRUST_STORE_PATH = CSPConfig.getBksTrustStore() + File.separator +
            BKSTrustStore.STORAGE_FILE_TRUST;

    // Проверка существования хранилища.
    private void checkTrustStore() throws Exception {
        trustStoreFile = new File(TRUST_STORE_PATH);
        if (!trustStoreFile.exists()) {
            throw new Exception("Trust store " + TRUST_STORE_PATH +
                    " doesn't exist");
        }
    }

    /**
     * Сохранение сертификата в хранилище.
     *
     * @param trustStoreFile Файл хранилища.
     * @param trustCert Корневой сертификат, добавляемый в хранилище.
     * @throws Exception
     */
    private void saveTrustCert(File trustStoreFile, X509Certificate trustCert) throws Exception {

        FileInputStream storeStream = new FileInputStream(TRUST_STORE_PATH);
        KeyStore keyStore = KeyStore.getInstance(BKSTrustStore.STORAGE_TYPE);

        keyStore.load(storeStream, DEFAULT_TRUST_STORE_PASSWORD);
        storeStream.close();

        Logger.log("Certificate sn: " +
                trustCert.getSerialNumber().toString(16) +
                ", subject: " + trustCert.getSubjectDN());

        // Будущий алиас корневого сертификата в хранилище.
        String trustCertAlias = trustCert.getSerialNumber().toString(16);

        // Вывод списка содержащищся в хранилище сертификатов.
        Logger.log("Current count of trusted certificates: " + keyStore.size());

        // Добавление сертификата, если его нет.

        boolean needAdd = (keyStore.getCertificateAlias(trustCert) == null);
        if (needAdd) {

            Logger.log("** Adding the trusted certificate " +
                    trustCert.getSubjectDN() + " with alias '" +
                    trustCertAlias + "' into the trust store");

            keyStore.setCertificateEntry(trustCertAlias, trustCert);

            FileOutputStream updatedTrustStore = new FileOutputStream(trustStoreFile);
            keyStore.store(updatedTrustStore, DEFAULT_TRUST_STORE_PASSWORD);

            Logger.log("The trusted certificate was added successfully.");

        } // if
        else {
            Logger.log("** Trusted certificate has already " +
                    "existed in the trust store.");
        } // else

    }

    private void removeCertificate(String alias) {
        try {
            FileInputStream storeStream = new FileInputStream(TRUST_STORE_PATH);
            KeyStore keyStore = KeyStore.getInstance(BKSTrustStore.STORAGE_TYPE);

            keyStore.load(storeStream, DEFAULT_TRUST_STORE_PASSWORD);
            storeStream.close();

            keyStore.deleteEntry(alias);
            keyStore.store(new FileOutputStream(TRUST_STORE_PATH), DEFAULT_TRUST_STORE_PASSWORD);
            Log.e("Removed access key", alias);
        } catch (Exception e) {
            Log.e("error remove cert", e.toString());
            Snackbar.make(spClientList, "error remove", Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialogRemoveCert != null) {
            dialogRemoveCert.dismiss();
        }
        if (dialogRemoveContainer != null) {
            dialogRemoveContainer.dismiss();
        }
    }
}
