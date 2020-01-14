package com.elewise.ldmobile.ui;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.elewise.ldmobile.R;
import com.elewise.ldmobile.model.DocumentAttachment;
import com.elewise.ldmobile.model.DocumentHistory;

public class HistoryAdapter extends BaseAdapter {
    private Context context;
    private DocumentHistory[] histories;

    public HistoryAdapter() {
        super();
    }

    public HistoryAdapter(Context context, DocumentHistory[] histories) {
        this.context = context;
        this.histories = histories;
    }

    @Override
    public int getCount() {
        return histories.length;
    }

    @Override
    public Object getItem(int position) {
        return histories[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.list_hist_item, parent, false);

        TextView tvHistDate = convertView.findViewById(R.id.tvHistDate);
        tvHistDate.setText(histories[position].getHistory_date());

        TextView tvHistEmployee = convertView.findViewById(R.id.tvHistEmployee);
        tvHistEmployee.setText(histories[position].getEmployee());

        TextView tvHistText = convertView.findViewById(R.id.tvHistText);
        tvHistText.setText(histories[position].getText());

        return convertView;
    }

}

