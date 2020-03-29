package com.elewise.ldmobile.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.elewise.ldmobile.R;
import com.elewise.ldmobile.model.*;
import com.elewise.ldmobile.api.*;
import com.elewise.ldmobile.service.Session;
import com.elewise.ldmobile.utils.ImageUtils;
import com.elewise.ldmobile.utils.MessageUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.progress_dialog_load));
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
        progressDialog.show();
        new Thread(() -> {
            FilterData[] filterData = Session.getInstance().getFilterData();
            List<DocumentForList> docsList = Session.getInstance().getDocuments(10, 0,
                    processType, filterData);

            handleDocumentsResponse(docsList);
        }).start();
    }

    private void handleDocumentsResponse(List<DocumentForList> docsList) {
        getActivity().runOnUiThread(() -> {
            progressDialog.cancel();

            if (docsList != null && !docsList.isEmpty()) {
                ((DocsAdapter) lvDocs.getAdapter()).setList(docsList);
                lvDocs.setVisibility(View.VISIBLE);
                tvNoResults.setVisibility(View.GONE);
            } else {
                lvDocs.setVisibility(View.GONE);
                tvNoResults.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showDocDetail(final Document document) {
        progressDialog.show();
        new Thread(() -> {
            try {
                ParamDocumentDetailsResponse result = Session.getInstance().getDocumentDetail(document.getDoc_id(), document.getDoc_type());
                if (result == null) {
                    showError(getString(R.string.error_unknown));
                } else {
                    if (result.getStatus().equals(ResponseStatusType.E.name())) {
                        String message = result.getMessage();
                        if (TextUtils.isEmpty(message)) showError(getString(R.string.error_unknown));
                        else showError(message);
                    } else if (result.getStatus().equals(ResponseStatusType.A.name())) {
                        showError(getString(R.string.error_authentication));
                    } else {
                        Session.getInstance().setCurrentDocumentDetail(result);
                        handleDocumentDetailsResponse(result);
                    }
                }
            } catch (Exception e) {
                showError(getString(R.string.error_unknown));
                e.printStackTrace();
            }
        }).start();
    }

    private void showError(String message) {
        getActivity().runOnUiThread(() -> {
            progressDialog.cancel();
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        });
    }

    private void handleDocumentDetailsResponse(final ParamDocumentDetailsResponse documentDetail) {
        getActivity().runOnUiThread(() -> {
            progressDialog.cancel();

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
        });
    }


    @Override
    public void onDestroy() {
        if (progressDialog!= null) progressDialog.dismiss();
        if (dialog != null) dialog.dismiss();

        super.onDestroy();
    }

    /**
     * Adapter
     */
    public class DocsAdapter extends BaseAdapter {
        private Context context;
        private List<DocumentForList> list;
        private SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

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
                if (sdf.format(new Date()).equals(list.get(position).getSectionTitle())) {
                    tvSectionTitle.setTypeface(null, Typeface.BOLD);
                    tvSectionTitle.setTextColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    tvSectionTitle.setTypeface(null, Typeface.NORMAL);
                    tvSectionTitle.setTextColor(getResources().getColor(R.color.colorAccent));
                }
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

                ImageUtils.INSTANCE.setIcon(getResources(), imgDocType, document.getDoc_icon());

                if (document.getDoc_type().equals(DocType.PD.name())) {
                    imgAction.setVisibility(View.GONE);
                } else {
                    imgAction.setVisibility(View.VISIBLE);
                    ImageUtils.INSTANCE.setIcon(getResources(), imgAction, document.getAction_icon());
                }

                convertView.setOnClickListener(view -> showDocDetail(document));
            }

            return convertView;
        }
    }
}
