package com.elewise.ldmobile.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
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

    public static final int REQUEST_ONE_CODE = 101;
    public static final int REQUEST_TWO_CODE = 102;

    private Button btnOne;
    private Button btnTwo;
    private AlertDialog dialog;
    private ProgressDialog progressDialog;
    private Session session;
    ParamDocumentDetailsResponse documentDetail;

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
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.progress_dialog_load));

        session = Session.getInstance();

        documentDetail = session.getCurrentDocumentDetail();

        LinearLayout llDynamicPart = rootView.findViewById(R.id.llDynamicPart);
        LinearLayout llButtons = rootView.findViewById(R.id.llButtons);
        btnOne = rootView.findViewById(R.id.btnOne);
        btnTwo = rootView.findViewById(R.id.btnTwo);

        addButtons(llButtons);

        btnOne.setOnClickListener(view -> {
            // todo Есть такой скрин в Figma. На какой параметр ориентироваться
//            if (true) {
                Intent intent = new Intent(getContext(), DocPacketActionActivity.class);
                intent.putExtra(DocPacketActionActivity.PARAM_IN_DOC_DETAIL, documentDetail.getButtons()[0]);
                startActivityForResult(intent, REQUEST_ONE_CODE);
//            } else {
//                dialog = MessageUtils.createDialog(this.getActivity(), R.string.alert_dialog_error, R.string.alert_dialog_error_subscribe_packet);
//                dialog.show();
//            }
        });

        btnTwo.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), DocPacketActionActivity.class);
            intent.putExtra(DocPacketActionActivity.PARAM_IN_DOC_DETAIL, documentDetail.getButtons()[1]);
            startActivityForResult(intent, REQUEST_TWO_CODE);
        });


        addDynamicPart(inflater, documentDetail, llDynamicPart);
        addAttachments(inflater, rootView, documentDetail);

        return rootView;
    }

    private void addButtons(LinearLayout llButtons) {
        if (documentDetail.getButtons().length > 0) {
            llButtons.setVisibility(View.VISIBLE);

            int i = 0;
            while (i < documentDetail.getButtons().length) {
                ButtonDesc item = documentDetail.getButtons()[i];

                if (i == 0) {
                    btnOne.setText(item.getCaption());
                }

                if (i == 1) {
                    btnTwo.setText(item.getCaption());
                }

                i++;
            }
        } else {
            llButtons.setVisibility(View.GONE);
        }
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

                ImageUtils.INSTANCE.setIcon(getResources(), ivDocType, item.getDoc_icon());
                ImageUtils.INSTANCE.setIcon(getResources(), ivActionType, item.getAction_icon());
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
                ImageUtils.INSTANCE.setIcon(getResources(), ivDocType, session.getCurrentDocumentDetail().getDoc_icon());
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
                ParamDocumentDetailsResponse result = session.getDocumentDetail(relatedDoc.getDoc_id(), relatedDoc.getDoc_type());
                handleDocumentDetailsResponse(result);
            }
        }).start();
    }

    private void handleDocumentDetailsResponse(final ParamDocumentDetailsResponse response) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.cancel();

                String errorMessage = getString(R.string.error_load_data);

                if (response != null) {
                    if (response.getStatus().equals(ResponseStatusType.S.name())) {
                        session.setCurrentDocumentDetail(response);
                        if (response.getDoc_type().equals(DocType.PD.name())) {
                            Intent intent = new Intent();
                            intent.setClass(getActivity(), DocPacketActivity.class);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent();
                            intent.setClass(getActivity(), DocActivity.class);
                            startActivity(intent);
                        }
                        return;
                    } else if (response.getStatus().equals(ResponseStatusType.E.name())) {
                        if (!TextUtils.isEmpty(response.getMessage())) {
                            errorMessage = response.getMessage();
                        }
                    } else if (response.getStatus().equals(ResponseStatusType.A.name())) {
                        session.errorAuth();
                        return;
                    } else {
                        errorMessage = getString(R.string.error_unknown_status_type);
                    }
                }

                showError(errorMessage);
            }
        });
    }

    private void showError(String errorMessage) {
        // показать ошибку
        dialog = MessageUtils.createDialog(getActivity(), getString(R.string.alert_dialog_error), errorMessage);
        dialog.show();
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
        if (requestCode == REQUEST_ONE_CODE) {
            if (resultCode == DocPacketActionActivity.PARAM_RESULT_OK) {
                Toast.makeText(getContext(), R.string.action_success, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), R.string.action_error, Toast.LENGTH_LONG).show();
            }
        } else {
            if (requestCode == REQUEST_TWO_CODE) {
                if (resultCode == DocPacketActionActivity.PARAM_RESULT_OK) {
                    Toast.makeText(getContext(), R.string.action_success, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), R.string.action_error, Toast.LENGTH_LONG).show();
                }
            }
        }
    }
 }