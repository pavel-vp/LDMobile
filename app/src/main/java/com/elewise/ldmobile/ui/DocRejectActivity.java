package com.elewise.ldmobile.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.elewise.ldmobile.R;

// todo unused
public class DocRejectActivity extends AppCompatActivity {
    public static final int PARAM_RESULT_NOT = 3;
    public static final int PARAM_RESULT_OK = 2;
    public static final String PARAM_REASON = "param_reason";

    private Button btnReject;
    private Button btnCancel;
    private EditText edReason;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doc_reject);

        btnReject = findViewById(R.id.btnReject);
        btnCancel = findViewById(R.id.btnCancel);
        edReason = findViewById(R.id.edReason);

        btnReject.setOnClickListener(view -> {
            if (TextUtils.isEmpty(edReason.getText().toString())) {
                Toast.makeText(DocRejectActivity.this, "Необходимо указать причину", Toast.LENGTH_LONG).show();
            } else {
                Intent intent = new Intent();
                intent.putExtra(PARAM_REASON, edReason.getText().toString());
                setResult(PARAM_RESULT_OK, intent);
                finish();
            }
        });

        btnCancel.setOnClickListener(view -> {
            setResult(PARAM_RESULT_NOT);
            finish();
        });
    }
}
