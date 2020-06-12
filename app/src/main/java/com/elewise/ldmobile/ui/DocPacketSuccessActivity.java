package com.elewise.ldmobile.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import com.elewise.ldmobile.R;

// todo unused
public class DocPacketSuccessActivity extends AppCompatActivity {
    public static final int PARAM_RESULT_NOT = 3;
    public static final int PARAM_RESULT_OK = 2;
    public static final String PARAM_LIST_DOCS = "param_list_docs";

    private Button btnSubscribe;
    private Button btnCancel;
    private TextView tvMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doc_packet_success);
        String listDocs = getIntent().getStringExtra(PARAM_LIST_DOCS);

        btnSubscribe = findViewById(R.id.btnSubscribe);
        btnCancel = findViewById(R.id.btnCancel);
        tvMessage = findViewById(R.id.tvText);
        tvMessage.setText(String.format(getString(R.string.dialog_succes_doc_packet_message), listDocs));

        btnSubscribe.setOnClickListener(view -> {
            setResult(PARAM_RESULT_OK);
            finish();
        });

        btnCancel.setOnClickListener(view -> {
            setResult(PARAM_RESULT_NOT);
            finish();
        });
    }
}
