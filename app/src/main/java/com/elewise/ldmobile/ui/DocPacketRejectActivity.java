package com.elewise.ldmobile.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.elewise.ldmobile.R;

public class DocPacketRejectActivity extends AppCompatActivity {
    public static final int PARAM_RESULT_NOT = 3;
    public static final int PARAM_RESULT_OK = 2;
    public static final String PARAM_REASON = "param_reason";
    public static final String PARAM_LIST_DOCS = "param_list_docs";

    private Button btnRefuse;
    private Button btnCancel;
    private TextView tvMessage;
    private EditText edReason;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doc_packet_reject);

        String listDocs = getIntent().getStringExtra(PARAM_LIST_DOCS);

        btnRefuse = findViewById(R.id.btnRefuse);
        btnCancel = findViewById(R.id.btnCancel);
        edReason = findViewById(R.id.edReason);
        tvMessage = findViewById(R.id.tvMessage);
        tvMessage.setText(String.format(getString(R.string.dialog_reject_doc_packet_message), listDocs));

        btnRefuse.setOnClickListener(view -> {
            if (TextUtils.isEmpty(edReason.getText().toString())) {
                Toast.makeText(DocPacketRejectActivity.this, "Необходимо указать причину", Toast.LENGTH_LONG).show();
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
