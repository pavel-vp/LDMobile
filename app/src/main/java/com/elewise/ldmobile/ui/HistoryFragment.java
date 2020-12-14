package com.elewise.ldmobile.ui;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.elewise.ldmobile.R;
import com.elewise.ldmobile.api.ParamDocumentDetailsResponse;
import com.elewise.ldmobile.service.Session;

public class HistoryFragment extends Fragment {

    public static HistoryFragment newInstance() {
        HistoryFragment fragment = new HistoryFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ParamDocumentDetailsResponse detail = Session.getInstance().getCurrentDocumentDetail();
        View rootView = inflater.inflate(R.layout.fragment_doc_history, container, false);

        ListView lvHist = rootView.findViewById(R.id.lvHist);
        lvHist.setAdapter(new HistoryAdapter(this.getContext(), detail.getHistory()));

        return rootView;
    }
}