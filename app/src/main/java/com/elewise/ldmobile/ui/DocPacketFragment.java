package com.elewise.ldmobile.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elewise.ldmobile.R;
import com.elewise.ldmobile.model.*;
import com.elewise.ldmobile.api.*;
import com.elewise.ldmobile.api.data.*;
import com.elewise.ldmobile.service.Session;
import com.elewise.ldmobile.utils.ImageUtils;
import com.elewise.ldmobile.utils.MessageUtils;

public class DocPacketFragment extends Fragment {

    public static final int REQUEST_SUCCESS_CODE = 101;
    public static final int REQUEST_REJECT_CODE = 102;

    private Button btnSubscribe;
    private Button btnRefuse;
    private AlertDialog dialog;
    private ProgressDialog progressDialog;

    public DocPacketFragment() {
    }

    public static DocPacketFragment newInstance() {
        DocPacketFragment fragment = new DocPacketFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_doc_packet_header, container, false);
        progressDialog = new ProgressDialog(getActivity());

        ParamDocumentDetailsResponse detail = Session.getInstance().getCurrentDocumentDetail();

        LinearLayout llDynamicPart = rootView.findViewById(R.id.llDynamicPart);
        LinearLayout llButtons = rootView.findViewById(R.id.llButtons);
        btnSubscribe = rootView.findViewById(R.id.btnSubscribe);
        btnRefuse = rootView.findViewById(R.id.btnRefuse);

        if (detail.getUser_action() != null && detail.getUser_action().equals(ActionType.SIGN.getAction())) {
            llButtons.setVisibility(View.VISIBLE);
        } else {
            llButtons.setVisibility(View.GONE);
        }

        btnSubscribe.setOnClickListener(view -> {
            // todo Есть такой скрин в Figma. На какой параметр ориентироваться
            if (true) {
                Intent intent = new Intent(getContext(), DocPacketSuccessActivity.class);
                intent.putExtra(DocPacketSuccessActivity.PARAM_LIST_DOCS, "АВР 256, Т12 №1002");
                startActivityForResult(intent, REQUEST_SUCCESS_CODE);
            } else {
                dialog = MessageUtils.createDialog(this.getActivity(), R.string.alert_dialog_error, R.string.alert_dialog_error_subscribe_packet);
                dialog.show();
            }
        });

        btnRefuse.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), DocPacketRejectActivity.class);
            intent.putExtra(DocPacketRejectActivity.PARAM_LIST_DOCS, "АВР 256, Т12 №1002");
            startActivityForResult(intent, REQUEST_REJECT_CODE);
        });


        addDynamicPart(inflater, detail, llDynamicPart);
        addAttachments(inflater, rootView, detail);

        return rootView;
    }

    private void addAttachments(LayoutInflater inflater, View rootView, ParamDocumentDetailsResponse detail) {
        LinearLayout lvAttachemnt = rootView.findViewById(R.id.lvRelated);
        TextView tvRelatedHeader = rootView.findViewById(R.id.tvRelatedHeader);
        if (detail.getRelated_docs() != null) {
            tvRelatedHeader.setVisibility(View.VISIBLE);
            lvAttachemnt.setVisibility(View.VISIBLE);

            for (RelatedDoc item : detail.getRelated_docs()) {
                View convertView = inflater.inflate(R.layout.related_item, lvAttachemnt, false);

                ImageView ivDocType = convertView.findViewById(R.id.ivDocType);
                ImageView ivActionType = convertView.findViewById(R.id.ivActionType);
                TextView tvName = convertView.findViewById(R.id.tvName);

                ImageUtils.setDocTypeIconMini(ivDocType, item.getDoc_icon());
                ImageUtils.setActionIcon(ivActionType, item.getAction_icon());
                tvName.setText(item.getDoc_name());
                convertView.setTag(item);

                convertView.setOnClickListener(view -> {
                    RelatedDoc relatedDoc = (RelatedDoc) view.getTag();
                    showDocDetail(relatedDoc);
                });
                lvAttachemnt.addView(convertView);
            }
        } else {
            tvRelatedHeader.setVisibility(View.GONE);
            lvAttachemnt.setVisibility(View.GONE);
        }
    }

    private void addDynamicPart(LayoutInflater inflater, ParamDocumentDetailsResponse detail, LinearLayout llDynamicPart) {
        boolean isFirst = true;
        for (DocHeaderAttributes item: detail.getHeader_attributes()) {
            View convertView = inflater.inflate(R.layout.doc_header_item, llDynamicPart, false);
            TextView tvDesc = convertView.findViewById(R.id.tvDesc);
            TextView tvValue = convertView.findViewById(R.id.tvValue);
            if (isFirst) {
                ImageView ivDocType = convertView.findViewById(R.id.ivDocType);
                ivDocType.setVisibility(View.VISIBLE);
                ImageUtils.setDocTypeIconMini(ivDocType, Session.getInstance().getCurrentDocumentDetail().getDoc_icon());
                isFirst = false;
            }
            tvDesc.setText(item.getDesc());
            tvValue.setText(item.getValue());

            llDynamicPart.addView(convertView);
        }
    }

    private void showDocDetail(final  RelatedDoc relatedDoc) {
        progressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ParamDocumentDetailsResponse result = null;
                try {
                    result = Session.getInstance().getDocumentDetail(relatedDoc.getDoc_id(), relatedDoc.getDoc_type());
                    Session.getInstance().setCurrentDocumentDetail(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                handleDocumentDetailsResponse(result);
            }
        }).start();
    }

    private void handleDocumentDetailsResponse(final ParamDocumentDetailsResponse documentDetail) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.hide();
                if (documentDetail != null) {
                    if (documentDetail.getDoc_type().equals(DocType.PD.name())) {
                        Intent intent = new Intent();
                        intent.setClass(DocPacketFragment.this.getActivity(), DocPacketActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        DocPacketFragment.this.getActivity().startActivity(intent);
                    } else {
                        Intent intent = new Intent();
                        intent.setClass(DocPacketFragment.this.getActivity(), DocActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        DocPacketFragment.this.getActivity().startActivity(intent);
                    }
                } else {
                    // показать ошибку
                    dialog = MessageUtils.createDialog(DocPacketFragment.this.getActivity(), R.string.alert_dialog_error, R.string.error_load_data);
                    dialog.show();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dialog != null) dialog.dismiss();
        if (progressDialog != null) progressDialog.dismiss();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SUCCESS_CODE) {
            if (resultCode == DocPacketSuccessActivity.PARAM_RESULT_OK) {
                Toast.makeText(getContext(), "Пакет подписан!", Toast.LENGTH_LONG).show();
            }
        } else {
            if (requestCode == REQUEST_REJECT_CODE) {
                if (resultCode == DocPacketRejectActivity.PARAM_RESULT_OK) {
                    String reason = data.getStringExtra(DocPacketRejectActivity.PARAM_REASON);
                    Toast.makeText(getContext(), "Пакет отказан по причине "+reason, Toast.LENGTH_LONG).show();
                }
            }
        }
    }
 }