package com.elewise.ldmobile.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.drm.DrmStore;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.elewise.ldmobile.R;
import com.elewise.ldmobile.model.ActionType;
import com.elewise.ldmobile.model.DocHeaderAttributes;
import com.elewise.ldmobile.api.ParamRespDocumentDetailsResponse;
import com.elewise.ldmobile.service.Session;
import com.elewise.ldmobile.utils.ImageUtils;

public class DocFragment extends Fragment {

    public static final int REQUEST_SUCCESS_CODE = 101;
    public static final int REQUEST_REJECT_CODE = 102;

    private Button btnSuccess;
    private Button btnReject;
    private AlertDialog dialog;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static DocFragment newInstance() {
        DocFragment fragment = new DocFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_doc_header, container, false);

        ParamRespDocumentDetailsResponse detail = Session.getInstance().getCurrentDocumentDetail();

        LinearLayout llButtons = rootView.findViewById(R.id.llButtons);
        LinearLayout llDynamicPart = rootView.findViewById(R.id.llDynamicPart);
        btnSuccess = rootView.findViewById(R.id.btnSuccess);
        btnReject = rootView.findViewById(R.id.btnReject);

        if (detail.getUser_action() != null && detail.getUser_action().equals(ActionType.APPROVE.getAction())) {
            llButtons.setVisibility(View.VISIBLE);
        } else {
            llButtons.setVisibility(View.GONE);
        }

        btnSuccess.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), DocSuccessActivity.class);
            startActivityForResult(intent, REQUEST_SUCCESS_CODE);
        });

        btnReject.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), DocRejectActivity.class);
            startActivityForResult(intent, REQUEST_REJECT_CODE);
        });

        boolean isFirst = true;
        for (DocHeaderAttributes item: detail.getHeader_attributes()) {
            View convertView = inflater.inflate(R.layout.doc_header_item, container, false);
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

        ListView lvAttachemnt = rootView.findViewById(R.id.lvAttachemnt);
        if (detail.getAttachments() != null && detail.getAttachments().length > 0) {
            lvAttachemnt.setVisibility(View.VISIBLE);
            lvAttachemnt.setAdapter(new AttachmentAdapter(this.getContext(), detail.getAttachments()));
        } else {
            lvAttachemnt.setVisibility(View.GONE);
        }

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SUCCESS_CODE) {
            if (resultCode == DocSuccessActivity.PARAM_RESULT_OK) {
                Toast.makeText(getContext(), "Согласовано!", Toast.LENGTH_LONG).show();
            }
        } else {
            if (requestCode == REQUEST_REJECT_CODE) {
                if (resultCode == DocRejectActivity.PARAM_RESULT_OK) {
                    String reason = data.getStringExtra(DocRejectActivity.PARAM_REASON);
                    Toast.makeText(getContext(), "Отклонено по причине "+reason, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (dialog != null) {
            dialog.dismiss();
        }
    }
 }