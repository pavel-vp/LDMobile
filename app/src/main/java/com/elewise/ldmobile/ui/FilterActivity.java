package com.elewise.ldmobile.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.elewise.ldmobile.R;
import com.elewise.ldmobile.api.*;
import com.elewise.ldmobile.service.Session;
import com.elewise.ldmobile.utils.MessageUtils;
import com.elewise.ldmobile.widget.BaseWidget;
import com.elewise.ldmobile.widget.CheckboxWidget;
import com.elewise.ldmobile.widget.DateWidget;
import com.elewise.ldmobile.widget.InputWidget;
import com.elewise.ldmobile.widget.SelectWidget;

import java.util.ArrayList;
import java.util.List;

public class FilterActivity extends AppCompatActivity {
    private Button btnApply;
    private Button btnClear;
    private LinearLayout llDynamicPart;
    private List<BaseWidget> dynamicViewList = new ArrayList();

    private ProgressDialog progressDialog;
    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        llDynamicPart = findViewById(R.id.llDynamicPart);
        btnApply = findViewById(R.id.btnApply);
        btnClear = findViewById(R.id.btnClear);

        btnClear.setOnClickListener(view -> {
            Session.getInstance().setFilterData(new FilterData[0]);
            finish();
        });

        btnApply.setOnClickListener(view -> {
            if (validateFilterData()) {
                Session.getInstance().setFilterData(getFilterData());
                finish();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.progress_dialog_load));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

    }

    private void loadData() {
        progressDialog.show();
        new Thread(() -> {
                    ParamFilterSettingsResponse
            response = Session.getInstance().getFilterSettings();
            handleFilterSettingsResponse(response);
        }).start();
    }

    private void handleFilterSettingsResponse(final ParamFilterSettingsResponse response) {
        runOnUiThread(() -> {
            progressDialog.cancel();

            String errorMessage = getString(R.string.error_load_data);

            if (response != null) {
                if (response.getStatus().equals(ResponseStatusType.S.name())) {
                    errorMessage = "";

                    for (FilterElement item : response.getFilters()) {
                        if (item.getType().equals("date")) {
                            DateWidget view = new DateWidget(this, item);
                            dynamicViewList.add(view);
                            llDynamicPart.addView(view);
                        } else if (item.getType().equals("string")) {
                            InputWidget view = new InputWidget(this, item);
                            dynamicViewList.add(view);
                            llDynamicPart.addView(view);
                        } else if (item.getType().equals("list")) {
                            SelectWidget view = new SelectWidget(this, item);
                            dynamicViewList.add(view);
                            llDynamicPart.addView(view);
                        } else if (item.getType().equals("checkbox")) {
                            CheckboxWidget view = new CheckboxWidget(this, item);
                            dynamicViewList.add(view);
                            llDynamicPart.addView(view);
                        } else {
                            Log.e("loadData", "uncnown filter type");
                        }
                    }
                } else if (response.getStatus().equals(ResponseStatusType.E.name())) {
                    if (!TextUtils.isEmpty(response.getMessage())) {
                        errorMessage = response.getMessage();
                    }
                } else if (response.getStatus().equals(ResponseStatusType.A.name())) {
                    errorMessage = getString(R.string.error_authentication);
                } else {
                    errorMessage = getString(R.string.error_unknown);
                    Log.e("getFilterSettings", "Unknown Status Type");
                }
            }

            if (!TextUtils.isEmpty(errorMessage)) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (progressDialog != null) progressDialog.dismiss();
        if (dialog != null) dialog.dismiss();

        super.onDestroy();
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

    private FilterData[] getFilterData() {
        ArrayList<FilterData> arrayList = new ArrayList();
        for (BaseWidget item : dynamicViewList) {
            if (!TextUtils.isEmpty(item.getValue1())) {
                arrayList.add(new FilterData(item.getName(), item.getValue1(), item.getValue2()));
            }
        }

        return arrayList.toArray(new FilterData[arrayList.size()]);
    }

    private Boolean validateFilterData() {
        for (BaseWidget item : dynamicViewList) {
            String value = item.validate();
            if (!TextUtils.isEmpty(value)) {
                MessageUtils.createDialog(this, getString(R.string.alert_dialog_error), getString(R.string.activity_filter_validate_error, value)).show();
                return false;
            }
        }
        return true;
    }

}
