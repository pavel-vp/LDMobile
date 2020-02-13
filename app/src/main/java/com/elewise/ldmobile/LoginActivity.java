package com.elewise.ldmobile;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import com.elewise.ldmobile.service.Session;
import com.elewise.ldmobile.ui.DocsActivity;
import com.elewise.ldmobile.utils.MessageUtils;

public class LoginActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private EditText etLogin;
    private EditText etPassword;
    private AlertDialog dialog;

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

        login.setOnClickListener(view -> {
            runLogin(etLogin.getText().toString(), etPassword.getText().toString());
        });

        getSupportActionBar().hide();
    }

    private void runLogin(final String userName, final String password) {
        progressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String errorMessage = "";
                try {
                    errorMessage = Session.getInstance().getAuthToken(userName, password);
                    //TimeUnit.SECONDS.sleep(1);
                    //result = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                handleLoginResponse(errorMessage);
            }
        }).start();
    }

    private void handleLoginResponse(final String errorMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.hide();
                if (TextUtils.isEmpty(errorMessage)) {
                    Intent intent = new Intent();
                    intent.setClass(LoginActivity.this, DocsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    LoginActivity.this.startActivity(intent);
                    LoginActivity.this.finish();
                } else {
                    // показать ошибку
                    dialog = MessageUtils.createDialog(LoginActivity.this, getString(R.string.alert_dialog_error), errorMessage);
                    dialog.show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null) progressDialog.dismiss();
        if (dialog != null) dialog.dismiss();
    }
}
