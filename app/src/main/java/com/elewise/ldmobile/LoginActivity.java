package com.elewise.ldmobile;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.elewise.ldmobile.api.AuthStatusType;
import com.elewise.ldmobile.api.ParamAuthorizationRequest;
import com.elewise.ldmobile.api.ParamAuthorizationResponse;
import com.elewise.ldmobile.api.ParamTokenActivityCheckResponse;
import com.elewise.ldmobile.api.ResponseSessionActivityStatus;
import com.elewise.ldmobile.service.Prefs;
import com.elewise.ldmobile.service.Session;
import com.elewise.ldmobile.ui.DocsActivity;
import com.elewise.ldmobile.ui.SettingsActivity;
import com.elewise.ldmobile.utils.MessageUtils;

public class LoginActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private EditText etLogin;
    private EditText etPassword;
    private FloatingActionButton fbSettings;
    private AlertDialog dialog;
    private Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Вход в систему...");

        Button login = findViewById(R.id.btnLogin);
        etLogin = findViewById(R.id.etLogin);
        etPassword = findViewById(R.id.etPassword);
        fbSettings = findViewById(R.id.fbSettings);
        etLogin.setText(Prefs.INSTANCE.getLastLogin(this));

        session = Session.getInstance();

        login.setOnClickListener(view -> {
            if (TextUtils.isEmpty(etLogin.getText().toString())) {
                showError(getString(R.string.activity_login_enter_login));
                return;
            }

            if (TextUtils.isEmpty(etPassword.getText().toString())) {
                showError(getString(R.string.activity_login_enter_password));
                return;
            }

            runLogin(etLogin.getText().toString(), etPassword.getText().toString());
        });

        fbSettings.setOnClickListener(view -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });

        getSupportActionBar().hide();

        // проверим активность сохраненной сессии, если есть
        if (!TextUtils.isEmpty(session.getToken())) {
            tokenActivityCheck(session.getToken());
        }
    }

    private void tokenActivityCheck(String token) {
        progressDialog.show();
        new Thread(() -> {
            ParamTokenActivityCheckResponse response = session.tokenActivityCheck(token);
            handleTokenActivityCheck(response);
        }).start();
    }

    private void runLogin(final String userName, final String password) {
        progressDialog.show();
        new Thread(() -> {
            ParamAuthorizationResponse response = session.getAuthToken(userName, password);
            handleLoginResponse(response);
        }).start();
    }

    private void handleTokenActivityCheck(final ParamTokenActivityCheckResponse response) {
        runOnUiThread(() -> {
            if (response != null) {
                // tood check activity_status null!! nullpointer
                if (response.getSession_activity_status().equals(ResponseSessionActivityStatus.Y.name())) {
                    // сессия еще активна, перейдем сразу к документам
                    openDocsActivity();
                } else {
                    if (!TextUtils.isEmpty(response.getMessage())) {
                        Toast.makeText(LoginActivity.this, response.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    progressDialog.cancel();
                }
            } else {
                progressDialog.cancel();
            }
        });
    }

    private void handleLoginResponse(final ParamAuthorizationResponse response) {
        runOnUiThread(() -> {
            progressDialog.cancel();

            // вполнено с ошибками
            String errorMessage = getString(R.string.error_load_data);
            if (response != null) {
                if (response.getStatus().equals(AuthStatusType.S.name())) {
                    // успешно
                    session.setToken(response.getAccess_token());
                    Prefs.INSTANCE.saveLastLogin(LoginActivity.this, etLogin.getText().toString());
                    openDocsActivity();
                    return;
                } else {
                    if (!TextUtils.isEmpty(response.getMessage()))
                        errorMessage = response.getMessage();
                }
            }
            showError(errorMessage);
        });
    }

    private void showError(String errorMessage) {
        // показать ошибку
        dialog = MessageUtils.createDialog(LoginActivity.this, getString(R.string.alert_dialog_error), errorMessage);
        dialog.show();
    }

    private void openDocsActivity() {
        Intent intent = new Intent();
        intent.setClass(LoginActivity.this, DocsActivity.class);
        LoginActivity.this.startActivity(intent);
        LoginActivity.this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null) progressDialog.dismiss();
        if (dialog != null) dialog.dismiss();
    }
}
