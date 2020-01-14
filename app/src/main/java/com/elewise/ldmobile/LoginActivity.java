package com.elewise.ldmobile;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.elewise.ldmobile.ui.DocsActivity;
import com.elewise.ldmobile.utils.MessageUtils;

import java.util.concurrent.TimeUnit;

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

        Button login = (Button) findViewById(R.id.btnLogin);
        etLogin = (EditText)findViewById(R.id.etLogin);
        etPassword = (EditText)findViewById(R.id.etPassword);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runLogin(etLogin.getText().toString(), etPassword.getText().toString());
            }
        });
    }

    private void runLogin(final String userName, final String password) {
        progressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean result = false;
                try {
                    //result = Session.getInstance().getAuthToken(userName, password);
                    TimeUnit.SECONDS.sleep(1);
                    result = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                handleLoginResponse(result);
            }
        }).start();
    }

    private void handleLoginResponse(final boolean result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.hide();
                if (result) {
                    Intent intent = new Intent();
                    intent.setClass(LoginActivity.this, DocsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    LoginActivity.this.startActivity(intent);
                    LoginActivity.this.finish();
                } else {
                    // показать ошибку
                    dialog = MessageUtils.createDialog(LoginActivity.this, R.string.alert_dialog_error, R.string.alert_dialog_error_login);
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
