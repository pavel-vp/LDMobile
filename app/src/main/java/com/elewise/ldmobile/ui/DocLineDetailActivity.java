package com.elewise.ldmobile.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.elewise.ldmobile.R;
import com.elewise.ldmobile.api.data.DocLineDetail;
import com.elewise.ldmobile.api.data.DocumentItem;
import com.elewise.ldmobile.service.Session;

public class DocLineDetailActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doc_line_detail);

        LinearLayout llDynamicPart = findViewById(R.id.llDynamicPart);
        DocumentItem documentItem = Session.getInstance().getCurrentDocumentItem();
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (DocLineDetail item: documentItem.getDetails()) {
            View convertView = inflater.inflate(R.layout.doc_line_detail_item, llDynamicPart, false);
            TextView tvDesc = convertView.findViewById(R.id.tvDesc);
            TextView tvValue = convertView.findViewById(R.id.tvValue);
            tvDesc.setText(item.getDesc());
            tvValue.setText(item.getValue());
            llDynamicPart.addView(convertView);
        }

        updateActionBar(documentItem.getLine_name());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

}
