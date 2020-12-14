package com.elewise.ldmobile.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elewise.ldmobile.R;
import com.elewise.ldmobile.api.*;
import com.elewise.ldmobile.api.data.*;
import com.elewise.ldmobile.service.Prefs;
import com.elewise.ldmobile.service.Session;
import com.elewise.ldmobile.utils.ImageUtils;

import static com.elewise.ldmobile.ui.DocPacketActionActivity.PARAM_IN_DOC_DETAIL;

public class DocFragment extends Fragment {

    public static final int REQUEST_ONE_CODE = 101;
    public static final int REQUEST_TWO_CODE = 102;

    private Button btnOne;
    private Button btnTwo;

    ParamDocumentDetailsResponse documentDetail;

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

        documentDetail = Session.getInstance().getCurrentDocumentDetail();

        LinearLayout llButtons = rootView.findViewById(R.id.llButtons);
        LinearLayout llDynamicPart = rootView.findViewById(R.id.llDynamicPart);
        btnOne = rootView.findViewById(R.id.btnOne);
        btnTwo = rootView.findViewById(R.id.btnTwo);

        addButtons(llButtons);

        btnOne.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), DocPacketActionActivity.class);
            intent.putExtra(PARAM_IN_DOC_DETAIL, documentDetail.getButtons()[0]);
            startActivityForResult(intent, REQUEST_ONE_CODE);
        });

        btnTwo.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), DocPacketActionActivity.class);
            intent.putExtra(PARAM_IN_DOC_DETAIL, documentDetail.getButtons()[1]);
            startActivityForResult(intent, REQUEST_TWO_CODE);
        });

        boolean isFirst = true;
        for (DocHeaderAttributes item: documentDetail.getHeader_attributes()) {
            View convertView = inflater.inflate(R.layout.doc_header_item, container, false);
            TextView tvDesc = convertView.findViewById(R.id.tvDesc);
            TextView tvValue = convertView.findViewById(R.id.tvValue);
            if (isFirst) {
                ImageView ivDocType = convertView.findViewById(R.id.ivDocType);
                ivDocType.setVisibility(View.VISIBLE);
                ImageUtils.INSTANCE.setIcon(getResources(), ivDocType, Session.getInstance().getCurrentDocumentDetail().getDoc_icon());
                isFirst = false;
            }
            tvDesc.setText(item.getDesc());
            tvValue.setText(item.getValue());
            llDynamicPart.addView(convertView);
        }

        if (documentDetail.getAttachments() != null && documentDetail.getAttachments().length > 0) {
            for (DocumentAttachment item: documentDetail.getAttachments()) {
                View convertView = inflater.inflate(R.layout.attachment_item, container, false);
                TextView tvAttacheName = convertView.findViewById(R.id.tvAttacheName);
                tvAttacheName.setText(item.getFile_name());
                tvAttacheName.setTag(item.getFile_id());
                convertView.setOnClickListener(view -> showAttachment((Integer) view.getTag()));
                llDynamicPart.addView(convertView);
            }
        }

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

    private void showAttachment(Integer file_id) {
        // Вызвать вебстраницу с урлем
        Intent i = new Intent(Intent.ACTION_VIEW);
        String url = Prefs.INSTANCE.getConnectAddress(getContext()) + "";
        i.setData(Uri.parse(url));
        startActivity(i);
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
            } else {

            }
        }
    }
 }