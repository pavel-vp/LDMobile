package com.elewise.ldmobile.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.elewise.ldmobile.R;
import com.elewise.ldmobile.criptopro.util.KeyStoreType;
import com.elewise.ldmobile.criptopro.util.KeyStoreUtil;
import com.elewise.ldmobile.criptopro.util.Logger;
import com.elewise.ldmobile.criptopro.util.ProviderType;
import com.elewise.ldmobile.service.Prefs;
import com.elewise.ldmobile.widget.CertWidgetTrust;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.CryptoPro.JCSP.CSPConfig;
import ru.CryptoPro.JCSP.support.BKSTrustStore;
import ru.cprocsp.ACSP.tools.common.CSPTool;
import ru.cprocsp.ACSP.tools.common.Constants;

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

    // Путь к контейнеру.
    private EditText etContainerFolder;

    // путь к выбранному корневому
    private EditText etCertTrustFolder;

    // Номер выбранного типа хранилища в списке.
    private int keyStoreTypeIndex = 0;

    // Номер выбранного типа провайдера в списке.
    private int providerTypeIndex = 0;

    private Spinner spClientList;
    // Адаптер списка алиасов контейнеров.
    private ArrayAdapter<String> containerAliasAdapter = null;

    private LinearLayout lvTrustCert;
    private ListView lvChain;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_cripto_pro);

        updateActionBar(getString(R.string.activity_settings_cripto_pro_title));

        initCopyContainersButton();

        initCopyCertTrustButton();

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

        // Тип провайдера.

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

        // Путь к контейнеру.

        etContainerFolder = findViewById(R.id.etContainerFolder);

        // Кнопка выбора контейнера.

        Button btOpenCopyContainer = findViewById(R.id.btOpenCopyContainer);
        btOpenCopyContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(INTENT_CONTAINER_SELECT);
                intent.putExtra("onlyDirs", true);
                startActivityForResult(intent, CONTAINER_SELECT_CODE_IN);

//                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
//                startActivityForResult(intent, CONTAINER_SELECT_CODE_IN);
            }
        });


        etCertTrustFolder = findViewById(R.id.etCertTrustFolder);
        // Кнопка выбора корневого сертификата.
        Button btOpenCopyCertTrust = findViewById(R.id.btOpenCopyCertTrust);
        btOpenCopyCertTrust.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Выбрать файл
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, CERT_TRUST_SELECT_CODE_IN);
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

        updatealiasList();

        updateTrustListAndCertList();
    }

    List<String> oldAliasesList = new ArrayList<>();

    private void updatealiasList() {
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
        lvTrustCert = findViewById(R.id.lvTrustCert);

        for (String item: getTrustList()) {
            lvTrustCert.addView(new CertWidgetTrust(this, item, new AddToChainDialogFragment()));
        }

        lvChain = findViewById(R.id.lvChain);
    }

    private List<String> getTrustList() {
        List<String> res = new ArrayList<>();
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
                    res.add(alias);
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

        updatealiasList();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CONTAINER_SELECT_CODE_IN: {
                if (resultCode == Activity.RESULT_OK && data != null) {
//                    final String chosenFilePath = data.getDataString();
                    final String chosenFilePath = data.getStringExtra("chosenObject");
                    etContainerFolder.setText(chosenFilePath);
                }
            }
            case CERT_TRUST_SELECT_CODE_IN: {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    final String chosenFilePath = data.getDataString();
                    etCertTrustFolder.setText(chosenFilePath);
                }
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // Добавление обработчика в кнопку копирования контейнеров
    private void initCopyContainersButton() {
        Button btCopyContainers = findViewById(R.id.btMoveContainers);

        // Копирование контейнера.
        btCopyContainers.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Logger.clear();
                Logger.log("*** Copy containers to the " +
                        "application store ***");

                // Получаем исходную папку с контейнерами.
                final String containerFolder = String.valueOf(etContainerFolder.getText());
                if (containerFolder == null || containerFolder.isEmpty()) {

                    Logger.log("Containers' directory is undefined.");
                    return;

                }

                boolean copied = false;

                try {

                    Logger.log("Source directory: " + containerFolder);
                    CSPTool cspTool = new CSPTool(v.getContext());

                    copied = cspTool.getAppInfrastructure()
                            .copyContainerFromDirectory(containerFolder);

                } catch (Exception e) {
                    Log.e(Constants.APP_LOGGER_TAG, e.getMessage(), e);
                }

                if (copied) {
                    Logger.setStatusOK();
                    Toast.makeText(SettingsCriptoProActivity.this, R.string.activity_settings_cripto_pro_copied_ok, Toast.LENGTH_LONG).show();
                    updatealiasList();
                } else {
                    Toast.makeText(SettingsCriptoProActivity.this, R.string.activity_settings_cripto_pro_copied_failed, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private InputStream chooseCertTrust;

    // Добавление обработчика в кнопку копирования корневого сертификата
    private void initCopyCertTrustButton() {
        try {
            Button btMoveCertTrust = findViewById(R.id.btMoveCertTrust);

            checkTrustStore();

            // Копирование корневого сертификата.
            btMoveCertTrust.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Logger.clear();
                    Logger.log("*** Copy cert trust to the " +
                            "application store ***");

                    // Получаем исходную папку с контейнерами.
                    final String containerFolder = String.valueOf(etCertTrustFolder.getText());
                    if (containerFolder == null || containerFolder.isEmpty()) {

                        Logger.log("Cert trust' directory is undefined.");
                        return;

                    }

                    boolean copied = false;

                    try {
                        Uri uri = Uri.parse(containerFolder);
                        chooseCertTrust = getContentResolver().openInputStream(uri);
                        loadCert(chooseCertTrust);
                        copied = true;
                    } catch (Exception e) {
                        Log.e(Constants.APP_LOGGER_TAG, e.getMessage(), e);
                    }

                    if (copied) {
                        Logger.setStatusOK();
                        Toast.makeText(SettingsCriptoProActivity.this, R.string.activity_settings_cripto_pro_copied_ok, Toast.LENGTH_LONG).show();
                        updatealiasList();
                    } else {
                        Toast.makeText(SettingsCriptoProActivity.this, R.string.activity_settings_cripto_pro_copied_failed, Toast.LENGTH_LONG).show();
                    }
                }
            });
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
}
