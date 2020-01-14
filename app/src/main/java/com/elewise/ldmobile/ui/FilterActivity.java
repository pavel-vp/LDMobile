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
import android.widget.TextView;
import android.widget.Toast;

import com.elewise.ldmobile.R;
import com.elewise.ldmobile.model.FilterData;
import com.elewise.ldmobile.model.FilterElement;
import com.elewise.ldmobile.model.FilterElementList;
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
    private Button btnDateFrom;
    private Button btnDateTo;
    private LinearLayout llDynamicPart;
    private Button btnApply;
    private Button btnClear;
    private List<BaseWidget> dynamicViewList = new ArrayList();

    private Calendar cldr = Calendar.getInstance();
    private SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        tvDateFromName = findViewById(R.id.tvDateFromName);
        tvDateToName = findViewById(R.id.tvDateToName);
        btnDateFrom = findViewById(R.id.btnDateFrom);
        btnDateTo = findViewById(R.id.btnDateTo);
        llDynamicPart = findViewById(R.id.llDynamicPart);
        btnApply = findViewById(R.id.btnApply);
        btnClear = findViewById(R.id.btnClear);

        btnDateFrom.setOnClickListener(dateClickListener);
        btnDateTo.setOnClickListener(dateClickListener);
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
        FilterElementList listElement = Session.getInstance().getFilterSettings();
        if (listElement == null) {
            Toast.makeText(this, R.string.error_load_data, Toast.LENGTH_LONG).show();
            return;
        }

        for (FilterElement item:  listElement.getFilters()) {
            if (item.getType().equals("date")) {
                if (item.getName().equals("begin_date")) {
                    if (item.getLast_value() != null) {
                        tvDateFromName.setText(item.getDesc());
                        btnDateFrom.setText(item.getLast_value());
                    }
                } else if (item.getName().equals("end_date")) {
                    if (item.getLast_value() != null) {
                        tvDateToName.setText(item.getDesc());
                        btnDateTo.setText(item.getLast_value());
                    }
                } else {
                    DateWidget view = new DateWidget(this, item);
                    dynamicViewList.add(view);
                    llDynamicPart.addView(view);
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
        Button btnDate = (Button) view;
        try {
            cldr.setTime(sdf.parse(btnDate.getText().toString()));
        } catch (Exception e) {
            Log.e("error parse date", e.toString());
        }
        // date picker dialog
        new DatePickerDialog(FilterActivity.this,
                (v, year1, monthOfYear, dayOfMonth) ->
                        btnDate.setText(((dayOfMonth<10)?"0"+dayOfMonth:dayOfMonth) + "." + (monthOfYear + 1) + "." + year1)
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

        arrayList.add(new FilterData("begin_date", btnDateFrom.getText().toString()));
        arrayList.add(new FilterData("end_date", btnDateTo.getText().toString()));

        if (withDynamicPart) {
            for (BaseWidget item : dynamicViewList) {
                if (!TextUtils.isEmpty(item.getData())) {
                    arrayList.add(new FilterData(item.getName(), item.getData()));
                }
            }
        }

        return arrayList.toArray(new FilterData[arrayList.size()]);
    }
}
