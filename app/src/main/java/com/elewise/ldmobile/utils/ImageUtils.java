package com.elewise.ldmobile.utils;

import android.widget.ImageView;

import com.elewise.ldmobile.R;

public class ImageUtils {

    public static void setDocTypeIcon(ImageView imgDocType, String docType) {
        if (docType.equals("asv")) {
            imgDocType.setImageResource(R.drawable.ic_doc_type_asv);
        }
        if (docType.equals("avr")) {
            imgDocType.setImageResource(R.drawable.ic_doc_type_avr);
        }
        if (docType.equals("dog")) {
            imgDocType.setImageResource(R.drawable.ic_doc_type_dog);
        }
        if (docType.equals("isf")) {
            imgDocType.setImageResource(R.drawable.ic_doc_type_isf);
        }
        if (docType.equals("iup")) {
            imgDocType.setImageResource(R.drawable.ic_doc_type_iup);
        }
        if (docType.equals("ksf")) {
            imgDocType.setImageResource(R.drawable.ic_doc_type_ksf);
        }
        if (docType.equals("nd")) {
            imgDocType.setImageResource(R.drawable.ic_doc_type_nd);
        }
        if (docType.equals("pd")) {
            imgDocType.setImageResource(R.drawable.ic_doc_type_pd);
        }
        if (docType.equals("sf")) {
            imgDocType.setImageResource(R.drawable.ic_doc_type_sf);
        }
        if (docType.equals("tg12")) {
            imgDocType.setImageResource(R.drawable.ic_doc_type_tg12);
        }
        if (docType.equals("ukd")) {
            imgDocType.setImageResource(R.drawable.ic_doc_type_ukd);
        }
        if (docType.equals("upd")) {
            imgDocType.setImageResource(R.drawable.ic_doc_type_upd);
        }
    }

    public static void setDocTypeIconMini(ImageView imgDocType, String docType) {
        if (docType.equals("asv")) {
            imgDocType.setImageResource(R.drawable.ic_doc_type_asv_mini);
        }
        if (docType.equals("avr")) {
            imgDocType.setImageResource(R.drawable.ic_doc_type_avr_mini);
        }
        if (docType.equals("dog")) {
            imgDocType.setImageResource(R.drawable.ic_doc_type_dog_mini);
        }
        if (docType.equals("isf")) {
            imgDocType.setImageResource(R.drawable.ic_doc_type_isf_mini);
        }
        if (docType.equals("iup")) {
            imgDocType.setImageResource(R.drawable.ic_doc_type_iup_mini);
        }
        if (docType.equals("ksf")) {
            imgDocType.setImageResource(R.drawable.ic_doc_type_ksf_mini);
        }
        if (docType.equals("nd")) {
            imgDocType.setImageResource(R.drawable.ic_doc_type_nd_mini);
        }
        if (docType.equals("pd")) {
            imgDocType.setImageResource(R.drawable.ic_doc_type_pd_mini);
        }
        if (docType.equals("sf")) {
            imgDocType.setImageResource(R.drawable.ic_doc_type_sf_mini);
        }
        if (docType.equals("tg12")) {
            imgDocType.setImageResource(R.drawable.ic_doc_type_tg12_mini);
        }
        if (docType.equals("ukd")) {
            imgDocType.setImageResource(R.drawable.ic_doc_type_ukd_mini);
        }
        if (docType.equals("upd")) {
            imgDocType.setImageResource(R.drawable.ic_doc_type_upd_mini);
        }
    }

    public static void setActionIcon(ImageView imgAction, String docType) {
        if (docType.equals("ok_gray")) {
            imgAction.setImageResource(R.drawable.ic_state_ok_gray);
        }
        if (docType.equals("ok_green")) {
            imgAction.setImageResource(R.drawable.ic_state_ok_green);
        }
        if (docType.equals("ok_red")) {
            imgAction.setImageResource(R.drawable.ic_state_ok_red);
        }
        if (docType.equals("lock_gray")) {
            imgAction.setImageResource(R.drawable.ic_state_lock_gray);
        }
        if (docType.equals("lock_green")) {
            imgAction.setImageResource(R.drawable.ic_state_lock_green);
        }
        if (docType.equals("lock_red")) {
            imgAction.setImageResource(R.drawable.ic_state_lock_red);
        }
    }
}
