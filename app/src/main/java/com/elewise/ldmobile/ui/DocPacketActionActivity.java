package com.elewise.ldmobile.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elewise.ldmobile.R;
import com.elewise.ldmobile.api.ParamDocumentDetailsResponse;
import com.elewise.ldmobile.api.ParamExecDocumentResponse;
import com.elewise.ldmobile.api.data.ButtonDesc;
import com.elewise.ldmobile.api.data.DocumentDetailButtonCommentFlag;
import com.elewise.ldmobile.criptopro.CAdESSignVerifyExample;
import com.elewise.ldmobile.criptopro.util.ContainerAdapter;
import com.elewise.ldmobile.criptopro.util.Logger;
import com.elewise.ldmobile.criptopro.util.ProviderType;
import com.elewise.ldmobile.service.Prefs;
import com.elewise.ldmobile.service.Session;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import ru.CryptoPro.CAdES.CAdESType;
import ru.CryptoPro.JCSP.CSPConfig;
import ru.CryptoPro.JCSP.support.BKSTrustStore;
import ru.cprocsp.ACSP.tools.common.Constants;

import static com.elewise.ldmobile.ui.SettingsCriptoProActivity.TRUST_STORE_PATH;

public class DocPacketActionActivity extends AppCompatActivity {
    public static final int PARAM_RESULT_NOT = 3;
    public static final int PARAM_RESULT_OK = 2;
    public static final String PARAM_IN_DOC_DETAIL = "param_in_doc_detail";

    private Button btnOk;
    private Button btnCancel;
    private TextView tvTitle;
    private TextView tvText;
    private EditText edComment;
    private ButtonDesc buttonDesc;
    private LinearLayout llComment;
    private ProgressDialog progressDialog;
    private Session session;

    private byte[] bytesData;

    private final int REQUEST_FILE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doc_packet_reject);
        buttonDesc = (ButtonDesc) getIntent().getSerializableExtra(PARAM_IN_DOC_DETAIL);

        btnOk = findViewById(R.id.btnOk);
        btnCancel = findViewById(R.id.btnCancel);
        edComment = findViewById(R.id.edComment);
        tvTitle = findViewById(R.id.tvTitle);
        tvText = findViewById(R.id.tvText);
        llComment = findViewById(R.id.llComment);

        tvTitle.setText(buttonDesc.getTitle());
        tvText.setText(buttonDesc.getText());
        btnOk.setText(buttonDesc.getCaption());

        session = Session.getInstance();

        if (buttonDesc.getComment_flag().equals(DocumentDetailButtonCommentFlag.N.name())) {
            llComment.setVisibility(View.GONE);
        } else {
            llComment.setVisibility(View.VISIBLE);
        }

        btnOk.setOnClickListener(view -> {
            if (buttonDesc.getComment_flag().equals(DocumentDetailButtonCommentFlag.R.name()) && TextUtils.isEmpty(edComment.getText().toString())) {
                Toast.makeText(DocPacketActionActivity.this, getString(R.string.dialog_refect_doc_packet_need_specify_comment),
                        Toast.LENGTH_LONG).show();
            } else {
                execDocument();

                sign();

                Intent intent = new Intent();
                setResult(PARAM_RESULT_OK, intent);
            }
        });

        btnCancel.setOnClickListener(view -> {
            setResult(PARAM_RESULT_NOT);
            finish();
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.progress_dialog_load));

        // Выбрать файл
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_FILE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // todo remove
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final boolean success = (resultCode == Activity.RESULT_OK);

        switch (requestCode) {

            case REQUEST_FILE: {
                Uri uri = Uri.parse(data.getDataString());
                // загружаем файл выбранный
                try {
                    bytesData = readFile(getContentResolver().openInputStream(uri));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public void onResume() {

        super.onResume();

        // Необходимо для отображения диалоговых окон
        // ДСЧ, ввода пин-кода и сообщений.

        if (CSPConfig.INSTANCE != null) {
            CSPConfig.registerActivityContext(this);
        }

    }


    private void sign() {
        try {
            // Сборка универсального ContainerAdapter.

            // Клиентский контейнер (подписант, отправитель, TLS).
            // todo несколько контейнеров??
            String clientAlias = Prefs.INSTANCE.getContainerAlias(this);
            CharSequence clientPasswordSequence = "1";
            char[] clientPassword = null;

            if (clientPasswordSequence != null) {
                clientPassword = clientPasswordSequence.toString().toCharArray();
            } // if

            // Контейнер получателя.
            String serverAlias = null;

            // Настройки примера.

            ContainerAdapter adapter = new ContainerAdapter(this,
                    clientAlias, clientPassword, serverAlias, null);

            adapter.setProviderType(ProviderType.currentProviderType());
            adapter.setResources(getResources()); // для примера установки сертификатов

            // Используется общее для всех хранилище корневых
            // сертификатов cacerts.

//            final String trustStorePath = this.getApplicationInfo().dataDir +
//                    File.separator + BKSTrustStore.STORAGE_DIRECTORY + File.separator +
//                    BKSTrustStore.STORAGE_FILE_TRUST;

            Logger.log("Example trust store: " + TRUST_STORE_PATH);

            adapter.setTrustStoreProvider(BouncyCastleProvider.PROVIDER_NAME);
            adapter.setTrustStoreType(BKSTrustStore.STORAGE_TYPE);

            adapter.setTrustStoreStream(new FileInputStream(TRUST_STORE_PATH));
            adapter.setTrustStorePassword(BKSTrustStore.STORAGE_PASSWORD);

            CAdESSignVerifyExample example = new CAdESSignVerifyExample(adapter, CAdESType.CAdES_BES, bytesData,
                    result -> {
                        Log.e("signedResult", result.toString());
                        if (!result.getSucces()) {
                            setResult(PARAM_RESULT_NOT);
                        }
                        finish();
                    });

            example.getResult();

//            CMSSimpleSignExample example = new CMSSimpleSignExample(adapter);
//            byte[] signature = example.sign(bytesData);
//            example.getResult();

        } catch (Exception e) {
            Logger.log(e.getMessage());
            Logger.setStatusFailed();
            Log.e(Constants.APP_LOGGER_TAG, e.getMessage(), e);
        }
    }

    private void execDocument() {
//        progressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ParamDocumentDetailsResponse docDetails = session.getCurrentDocumentDetail();
                ParamExecDocumentResponse result = session.execDocument(docDetails.getDoc_id(), docDetails.getDoc_alt_type(), "sign_before", edComment.getText().toString());
                Log.e("execDocumentResult", result.toString());
            }
        }).start();
    }

    public byte[] readFile(InputStream ios) throws IOException {
        ByteArrayOutputStream ous = null;
        try {
            byte[] buffer = new byte[4096];
            ous = new ByteArrayOutputStream();
            int read = 0;
            while ((read = ios.read(buffer)) != -1) {
                ous.write(buffer, 0, read);
            }
        }finally {
            try {
                if (ous != null)
                    ous.close();
            } catch (IOException e) {
            }

            try {
                if (ios != null)
                    ios.close();
            } catch (IOException e) {
            }
        }
        return ous.toByteArray();
    }

}
