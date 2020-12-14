package com.elewise.ldmobile.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.elewise.ldmobile.R;
import com.elewise.ldmobile.api.ParamDocumentDetailsResponse;
import com.elewise.ldmobile.api.data.DocumentItem;
import com.elewise.ldmobile.service.Session;

public class ItemsFragment extends Fragment {

    public static ItemsFragment newInstance() {
        ItemsFragment fragment = new ItemsFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final ParamDocumentDetailsResponse detail = Session.getInstance().getCurrentDocumentDetail();
        final View rootView = inflater.inflate(R.layout.fragment_doc_items, container, false);
        LinearLayout llDynamicPart = rootView.findViewById(R.id.llDynamicPart);

        if (detail != null && detail.getLines() != null) {
            for (DocumentItem item : detail.getLines()) {
                View convertView = inflater.inflate(R.layout.doc_line_item, container, false);
                TextView tvName = convertView.findViewById(R.id.tvName);
                TextView tvDesc = convertView.findViewById(R.id.tvDesc);
                tvName.setText(item.getLine_name());
                tvDesc.setText(item.getLine_desc());
                llDynamicPart.addView(convertView);
                convertView.setTag(item);

                convertView.setOnClickListener(view -> {
                    DocumentItem documentItem = (DocumentItem) view.getTag();
                    Session.getInstance().setCurrentDocumentItem(documentItem);
                    startActivity(new Intent(getContext(), DocLineDetailActivity.class));
                });
            }
        }
        return rootView;
    }
}