package com.elewise.ldmobile.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elewise.ldmobile.R;
import com.elewise.ldmobile.model.*;
import com.elewise.ldmobile.api.*;
import com.elewise.ldmobile.api.data.*;
import com.elewise.ldmobile.service.Session;
import com.elewise.ldmobile.widget.BaseWidget;
import com.elewise.ldmobile.widget.CheckboxWidget;
import com.elewise.ldmobile.widget.DateWidget;
import com.elewise.ldmobile.widget.InputWidget;
import com.elewise.ldmobile.widget.SelectWidget;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.kotlin.KotlinModule;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import kotlin.reflect.jvm.internal.impl.util.Check;

public class FilterActivity extends AppCompatActivity {

    private TextView tvDateFromName;
    private TextView tvDateToName;
    private Button btnApply;
    private Button btnClear;
    private LinearLayout llDynamicPart;
    private RelativeLayout rlDateFrom;
    private RelativeLayout rlDateTo;
    private TextView tvDateFrom;
    private TextView tvDateTo;
    private List<BaseWidget> dynamicViewList = new ArrayList();

    private Calendar cldr = Calendar.getInstance();
    private SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        tvDateFromName = findViewById(R.id.tvDateFromName);
        tvDateToName = findViewById(R.id.tvDateToName);
        rlDateFrom = findViewById(R.id.rlDateFrom);
        rlDateTo = findViewById(R.id.rlDateTo);
        tvDateFrom = findViewById(R.id.tvDateFrom);
        tvDateTo = findViewById(R.id.tvDateTo);
        llDynamicPart = findViewById(R.id.llDynamicPart);
        btnApply = findViewById(R.id.btnApply);
        btnClear = findViewById(R.id.btnClear);

        rlDateFrom.setOnClickListener(dateClickListener);
        rlDateTo.setOnClickListener(dateClickListener);
        btnClear.setOnClickListener(view -> {
            Session.getInstance().setFilterData(getFilterData(false));
            finish();
        });

        btnApply.setOnClickListener(view -> {
            // todo validate обсудить как, и нужно ли (required param)
            Session.getInstance().setFilterData(getFilterData(true));
            finish();
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initDisplay();
    }

    private void initDisplay() {
        ParamFilterSettingsResponse listElement = Session.getInstance().getFilterSettings();
        if (listElement == null) {
            Toast.makeText(this, R.string.error_load_data, Toast.LENGTH_LONG).show();
            return;
        }

        for (FilterElement item:  listElement.getFilters()) {
            if (item.getType().equals("date")) {
                if (item.getLast_value() != null) {
                    tvDateFromName.setText(item.getDesc());
                    tvDateFrom.setText(item.getLast_value());
                }
                if (item.getLast_value2() != null) {
                    tvDateToName.setText(item.getDesc());
                    tvDateTo.setText(item.getLast_value());
                }
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
                Log.e("initDisplay", "uncnown filter type");
            }
        }
    }

    private View.OnClickListener dateClickListener = view -> {
        TextView date = view.findViewById(R.id.tvDateFrom);
        TextView tvDate = (date == null)?view.findViewById(R.id.tvDateTo):date;

        try {
            cldr.setTime(sdf.parse(tvDate.getText().toString()));
        } catch (Exception e) {
            Log.e("error parse date", e.toString());
        }
        // date picker dialog
        new DatePickerDialog(FilterActivity.this,
                (v, year1, monthOfYear, dayOfMonth) ->
                        tvDate.setText(((dayOfMonth<10)?"0"+dayOfMonth:dayOfMonth) + "." + (monthOfYear + 1) + "." + year1)
                , cldr.get(Calendar.YEAR), cldr.get(Calendar.MONTH), cldr.get(Calendar.DAY_OF_MONTH))
                .show();
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public FilterData[] getFilterData(Boolean withDynamicPart) {
        ArrayList<FilterData> arrayList = new ArrayList();
        // FIXME: переделать на новые форматы передачи дат
        //arrayList.add(new FilterData("begin_date", tvDateFrom.getText().toString()));
        //arrayList.add(new FilterData("end_date", tvDateTo.getText().toString()));

        if (withDynamicPart) {
            for (BaseWidget item : dynamicViewList) {
                if (!TextUtils.isEmpty(item.getData())) {
                    arrayList.add(new FilterData(item.getName(), item.getData(), null));
                }
            }
        }

        return arrayList.toArray(new FilterData[arrayList.size()]);
    }
}
