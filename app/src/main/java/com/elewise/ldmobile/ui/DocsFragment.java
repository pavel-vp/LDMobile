package com.elewise.ldmobile.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.elewise.ldmobile.R;
import com.elewise.ldmobile.model.*;
import com.elewise.ldmobile.api.*;
import com.elewise.ldmobile.api.data.*;
import com.elewise.ldmobile.service.Session;
import com.elewise.ldmobile.utils.ImageUtils;
import com.elewise.ldmobile.utils.MessageUtils;

import java.util.ArrayList;
import java.util.List;

public class DocsFragment extends Fragment {
    private TextView tvNoResults;
    private ListView lvDocs;
    public static final String ARG_PAGE = "ARG_PAGE";
    private ProcessType processType;
    private ProgressDialog progressDialog;
    AlertDialog dialog;


    public static DocsFragment newInstance(ProcessType processType) {
        Bundle args = new Bundle();
        args.putString(ARG_PAGE, processType.name());
        DocsFragment fragment = new DocsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            processType = ProcessType.valueOf(getArguments().getString(ARG_PAGE));
        }
        progressDialog = new ProgressDialog(getActivity());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_docs, container, false);
        tvNoResults = view.findViewById(R.id.tvNoResults);
        lvDocs = view.findViewById(R.id.lvDocs);
        registerForContextMenu(lvDocs);

        // set adapter
        final DocsAdapter adapter = new DocsAdapter(getContext(), new ArrayList<>());
        lvDocs.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getDocuments();
    }

    private void getDocuments() {
        FilterData[] filterData = Session.getInstance().getFilterData();
        // todo defaultValue?? В апи есть такие параметры как size, from, orderBy, direction. Как и откуда их брать
        List<DocumentForList> docsList = Session.getInstance().getDocuments(10, 0,
                processType, "doc_date",
                "asc", filterData);
        if (docsList != null && !docsList.isEmpty()) {
            ((DocsAdapter) lvDocs.getAdapter()).setList(docsList);
            lvDocs.setVisibility(View.VISIBLE);
            tvNoResults.setVisibility(View.GONE);
        } else {
            lvDocs.setVisibility(View.GONE);
            tvNoResults.setVisibility(View.VISIBLE);
        }
    }

    private void showDocDetail(final Document document) {
        progressDialog.show();
        new Thread(() -> {
            ParamDocumentDetailsResponse result = null;
            try {
                result = Session.getInstance().getDocumentDetail(document.getDoc_id(), document.getDoc_type());
                Session.getInstance().setCurrentDocumentDetail(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
            handleDocumentDetailsResponse(result);
        }).start();
    }

    private void handleDocumentDetailsResponse(final ParamDocumentDetailsResponse documentDetail) {
        getActivity().runOnUiThread(() -> {
            progressDialog.hide();
            if (documentDetail != null) {
                if (documentDetail.getDoc_type().equals(DocType.PD.name())) {
                    Intent intent = new Intent();
                    intent.setClass(DocsFragment.this.getActivity(), DocPacketActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    DocsFragment.this.getActivity().startActivity(intent);
                } else {
                    Intent intent = new Intent();
                    intent.setClass(DocsFragment.this.getActivity(), DocActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    DocsFragment.this.getActivity().startActivity(intent);
                }
            } else {
                // показать ошибку
                dialog = MessageUtils.createDialog(DocsFragment.this.getActivity(), R.string.alert_dialog_error, R.string.error_load_data);
                dialog.show();
            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (progressDialog!= null) progressDialog.dismiss();
        if (dialog != null) dialog.dismiss();
    }

    /**
     * Adapter
     */
    public class DocsAdapter extends BaseAdapter {
        private Context context;
        private List<DocumentForList> list;

        public DocsAdapter(Context context, List<DocumentForList> item) {
            this.context = context;
            this.list = item;
        }

        public void setList(List<DocumentForList> list) {
            this.list = list;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (list.get(position).isSection()) {
                // if section header
                convertView = inflater.inflate(R.layout.list_group_date, parent, false);
                TextView tvSectionTitle = convertView.findViewById(R.id.tvSectionTitle);
                tvSectionTitle.setText(String.format(getString(R.string.docs_activity_date_group), list.get(position).getSectionTitle()));
            } else {
                // if list
                convertView = inflater.inflate(R.layout.list_doc_item, parent, false);
                TextView tvDocTitle = convertView.findViewById(R.id.tvDocTitle);
                TextView tvDocBody = convertView.findViewById(R.id.tvDocBody);
                ImageView imgDocType = convertView.findViewById(R.id.imgDocType);
                ImageView imgAttache = convertView.findViewById(R.id.imgAttache);
                ImageView imgAction = convertView.findViewById(R.id.imgAction);

                final Document document = list.get(position).getDocument();
                tvDocTitle.setText(document.getContractor());
                tvDocBody.setText(document.getDoc_name());
                if (document.getAttach_flag()) {
                    imgAttache.setVisibility(View.VISIBLE);
                } else {
                    imgAttache.setVisibility(View.INVISIBLE);
                }

                ImageUtils.setDocTypeIcon(imgDocType, document.getDoc_icon());
                if (document.getDoc_type().equals(DocType.PD.name())) {
                    imgAction.setVisibility(View.GONE);
                } else {
                    imgAction.setVisibility(View.VISIBLE);
                    ImageUtils.setActionIcon(imgAction, document.getAction_icon());
                }

                convertView.setOnClickListener(view -> showDocDetail(document));
            }

            return convertView;
        }
    }
}
