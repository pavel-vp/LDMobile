package com.elewise.ldmobile.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.elewise.ldmobile.R;

public class DocSuccessActivity extends AppCompatActivity {
    public static final int PARAM_RESULT_N0T = 3;
    public static final int PARAM_RESULT_OK = 2;

    private Button btnSuccess;
    private Button btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doc_success);

        btnSuccess = findViewById(R.id.btnSuccess);
        btnCancel = findViewById(R.id.btnCancel);

        btnSuccess.setOnClickListener(view -> {
            setResult(PARAM_RESULT_OK);
            finish();
        });

        btnCancel.setOnClickListener(view -> {
            setResult(PARAM_RESULT_N0T);
            finish();
        });
    }
}
