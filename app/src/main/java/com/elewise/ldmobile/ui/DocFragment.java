package com.elewise.ldmobile.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.elewise.ldmobile.R;
import com.elewise.ldmobile.model.DocumentDetail;
import com.elewise.ldmobile.model.DocumentItem;
import com.elewise.ldmobile.service.Session;

public class DocFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    private int currentPos = 0;

    public DocFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static DocFragment newInstance(int sectionNumber) {
        DocFragment fragment = new DocFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = null;
        int sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
        switch (sectionNumber) {
            case 1: { // Детальные данные по документу
                rootView = getResourceForDocHeader(inflater, container);
                break;
            }
            case 2: { // Таблица позиций
                rootView = getResourceForItems(inflater, container);
                break;
            }
            case 3: { // История
                rootView = getResourceForHistory(inflater, container);
                break;
            }

        }
        return rootView;
    }

    private View getResourceForDocHeader(LayoutInflater inflater, ViewGroup container) {
        DocumentDetail detail = Session.getInstance().getCurrentDocumentDetail();

        View rootView = inflater.inflate(R.layout.fragment_doc_header, container, false);

        TextView tvVendorNameTitle = rootView.findViewById(R.id.tvVendorNameTitle);
        TextView tvVendorName = rootView.findViewById(R.id.tvVendorNameTitle);
        tvVendorName.setText(detail.getVendor_name());

        TextView tvSupOrgTitle = rootView.findViewById(R.id.tvSupOrgTitle);
        TextView tvSupOrg = rootView.findViewById(R.id.tvSupOrg);
        tvSupOrg.setText(detail.getSup_org());

        TextView tvShipOrgTitle = rootView.findViewById(R.id.tvShipOrgTitle);
        TextView tvShipOrg = rootView.findViewById(R.id.tvShipOrg);
        tvShipOrg.setText(detail.getShip_org());

        TextView tvConsOrgTitle = rootView.findViewById(R.id.tvConsOrgTitle);
        TextView tvConsOrg = rootView.findViewById(R.id.tvConsOrg);
        tvConsOrg.setText(detail.getCons_org());

        TextView tvPayerOrgTitle = rootView.findViewById(R.id.tvPayerOrgTitle);
        TextView tvPayerOrg = rootView.findViewById(R.id.tvPayerOrg);
        tvPayerOrg.setText(detail.getPayer_org());

        TextView tvPerfOrgTitle = rootView.findViewById(R.id.tvPerfOrgTitle);
        TextView tvPerfOrg = rootView.findViewById(R.id.tvPerfOrg);
        tvPerfOrg.setText(detail.getPerf_org());

        TextView tvCustOrgTitle = rootView.findViewById(R.id.tvCustOrgTitle);
        TextView tvCustOrg = rootView.findViewById(R.id.tvCustOrg);
        tvCustOrg.setText(detail.getCust_org());

        TextView tvBuyerOrgTitle = rootView.findViewById(R.id.tvBuyerOrgTitle);
        TextView tvBuyerOrg = rootView.findViewById(R.id.tvBuyerOrg);
        tvBuyerOrg.setText(detail.getBuyer_org());

        TextView tvReasonTitle = rootView.findViewById(R.id.tvReasonTitle);
        TextView tvReason = rootView.findViewById(R.id.tvReason);
        tvReason.setText(detail.getReason());

        TextView tvCFOTitle = rootView.findViewById(R.id.tvCFOTitle);
        TextView tvCFO = rootView.findViewById(R.id.tvCFO);
        tvCFO.setText(detail.getCfo());

        TextView tvAmountWOTaxTitle = rootView.findViewById(R.id.tvAmountWOTaxTitle);
        TextView tvAmountWOTax = rootView.findViewById(R.id.tvAmountWOTax);
        tvAmountWOTax.setText(detail.getTotal_amount_without_tax());

        TextView tvAmountTaxTitle = rootView.findViewById(R.id.tvAmountTaxTitle);
        TextView tvAmountTax = rootView.findViewById(R.id.tvAmountTax);
        tvAmountTax.setText(detail.getTotal_tax_amount());

        TextView tvAmountWTaxTitle = rootView.findViewById(R.id.tvAmountWTaxTitle);
        TextView tvAmountWTax = rootView.findViewById(R.id.tvAmountWTax);
        tvAmountWTax.setText(detail.getTotal_amount_with_tax());

        ListView lvAttachemnt = rootView.findViewById(R.id.lvAttachemnt);
        if (detail.getAttachments() != null && detail.getAttachments().length > 0) {
            lvAttachemnt.setVisibility(View.VISIBLE);
            lvAttachemnt.setAdapter(new AttachmentAdapter(this.getContext(), detail.getAttachments()));

        } else {
            lvAttachemnt.setVisibility(View.GONE);
        }

        return rootView;
    }

    private View getResourceForItems(LayoutInflater inflater, ViewGroup container) {
        final DocumentDetail detail = Session.getInstance().getCurrentDocumentDetail();
        final View rootView = inflater.inflate(R.layout.fragment_doc_items, container, false);

        TextView tvDocName = rootView.findViewById(R.id.tvDocName);
        tvDocName.setText(detail.getDoc_name());

        showItem(rootView, detail.getItems()[currentPos]);

        Button btnMovePrev = rootView.findViewById(R.id.btnMovePrev);
        btnMovePrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (DocFragment.this.currentPos > 0) {
                    DocFragment.this.currentPos--;
                }
                showItem(rootView, detail.getItems()[currentPos]);
            }
        });

        Button btnMoveNext = rootView.findViewById(R.id.btnMoveNext);
        btnMoveNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (DocFragment.this.currentPos < (detail.getItems().length-1)) {
                    DocFragment.this.currentPos++;
                }
                showItem(rootView, detail.getItems()[currentPos]);
            }
        });

        return rootView;
    }

    private void showItem(View rootView, DocumentItem item) {
        TextView tvPositionNumTitle = rootView.findViewById(R.id.tvPositionNumTitle);
        TextView tvPositionNum = rootView.findViewById(R.id.tvPositionNum);
        tvPositionNum.setText(item.getLine_num());

        TextView tvDescriptionTitle = rootView.findViewById(R.id.tvDescriptionTitle);
        TextView tvDescription = rootView.findViewById(R.id.tvDescription);
        tvDescription.setText(item.getDescription());

        TextView tvUomTitle = rootView.findViewById(R.id.tvUomTitle);
        TextView tvUom = rootView.findViewById(R.id.tvUom);
        tvUom.setText(item.getUom());

        TextView tvQuantityTitle = rootView.findViewById(R.id.tvQuantityTitle);
        TextView tvQuantity = rootView.findViewById(R.id.tvQuantity);
        tvQuantity.setText(item.getQuantity());

        TextView tvPriceTitle = rootView.findViewById(R.id.tvPriceTitle);
        TextView tvPrice = rootView.findViewById(R.id.tvPrice);
        tvPrice.setText(item.getPrice());

        TextView tvItemAmountWOTaxTitle = rootView.findViewById(R.id.tvItemAmountWOTaxTitle);
        TextView tvItemAmountWOTax = rootView.findViewById(R.id.tvItemAmountWOTax);
        tvItemAmountWOTax.setText(item.getAmount_without_tax());

        TextView tvItemTaxTitle = rootView.findViewById(R.id.tvItemTaxTitle);
        TextView tvItemTax = rootView.findViewById(R.id.tvItemTax);
        tvItemTax.setText(item.getTax());

        TextView tvItemAmountTaxTitle = rootView.findViewById(R.id.tvItemAmountTaxTitle);
        TextView tvItemAmountTax = rootView.findViewById(R.id.tvItemAmountTax);
        tvItemAmountTax.setText(item.getTax_amount());

        TextView tvItemAmountWTaxTitle = rootView.findViewById(R.id.tvItemAmountWTaxTitle);
        TextView tvItemAmountWTax = rootView.findViewById(R.id.tvItemAmountWTax);
        tvItemAmountWTax.setText(item.getAmount_with_tax());

    }

    private View getResourceForHistory(LayoutInflater inflater, ViewGroup container) {
        DocumentDetail detail = Session.getInstance().getCurrentDocumentDetail();
        View rootView = inflater.inflate(R.layout.fragment_doc_history, container, false);

        TextView tvHistDocName = rootView.findViewById(R.id.tvHistDocName);
        tvHistDocName.setText(detail.getDoc_name());

        ListView lvHist = rootView.findViewById(R.id.lvHist);
        lvHist.setAdapter(new HistoryAdapter(this.getContext(), detail.getHistory()));

        return rootView;
    }
}