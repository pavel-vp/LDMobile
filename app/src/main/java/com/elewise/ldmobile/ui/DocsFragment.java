package com.elewise.ldmobile.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.elewise.ldmobile.R;
import com.elewise.ldmobile.model.ActionType;
import com.elewise.ldmobile.model.DocType;
import com.elewise.ldmobile.model.Document;
import com.elewise.ldmobile.model.DocumentDetail;
import com.elewise.ldmobile.model.DocumentForList;
import com.elewise.ldmobile.service.Session;
import com.elewise.ldmobile.utils.MessageUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class DocsFragment extends Fragment {

    private ListView lvDocs;

    public static final String ARG_PAGE = "ARG_PAGE";

    private ActionType actionType;

    private ProgressDialog progressDialog;


    public static DocsFragment newInstance(ActionType actionType) {
        Bundle args = new Bundle();
        args.putString(ARG_PAGE, actionType.name());
        DocsFragment fragment = new DocsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            actionType = ActionType.valueOf(getArguments().getString(ARG_PAGE));
        }
        progressDialog = new ProgressDialog(getActivity());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_docs, container, false);

        lvDocs = (ListView) view.findViewById(R.id.lvDocs);
        registerForContextMenu(lvDocs);

        List<DocumentForList> docsList = Session.getInstance().getDocuments(actionType);

        // set adapter
        final DocsAdapter adapter = new DocsAdapter(getContext(), docsList);
        lvDocs.setAdapter(adapter);

        return view;
    }


    /**
     * MENU
     */

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()==R.id.lvDocs) {
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.menu_docs_list, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            case R.id.approve_success:
                // add stuff here
                askForApproveSuccessDocument();
                return true;
            case R.id.approve_reject:
                // edit stuff here
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void askForApproveSuccessDocument() {
        MessageUtils.showModalAndConfirm(getActivity(), "Внимание", "Согласовать документ?",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                },
                null);
    }

    /**
     * Adapter
     */
    public class DocsAdapter extends BaseAdapter {
        private Context context;
        private List<DocumentForList> list;

        public DocsAdapter() {
            super();
        }

        public DocsAdapter(Context context, List<DocumentForList> item) {
            this.context = context;
            this.list = item;
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
                convertView = inflater.inflate(R.layout.list_section, parent, false);
                TextView tvSectionTitle = (TextView) convertView.findViewById(R.id.tvSectionTitle);
                tvSectionTitle.setText(list.get(position).getSectionTitle());
            } else {
                // if list
                convertView = inflater.inflate(R.layout.list_item, parent, false);
                TextView tvDocTitle = convertView.findViewById(R.id.tvDocTitle);
                TextView tvDocBody = convertView.findViewById(R.id.tvDocBody);
                ImageView imgDocType = convertView.findViewById(R.id.imgDocType);
                ImageView imgAttache = convertView.findViewById(R.id.imgAttache);

                final Document document = list.get(position).getDocument();
                tvDocTitle.setText(document.getDoc_type());
                tvDocBody.setText(document.getDoc_name());
                if (document.getFile_attached()) {
                    imgAttache.setVisibility(View.VISIBLE);
                } else {
                    imgAttache.setVisibility(View.INVISIBLE);
                }
                if (document.getDoc_type().equals(DocType.ACTSV.name())) {
                    imgDocType.setImageResource(R.mipmap.type_actsv);
                }
                if (document.getDoc_type().equals(DocType.ACTVR.name())) {
                    imgDocType.setImageResource(R.mipmap.type_actvr);
                }
                if (document.getDoc_type().equals(DocType.DOG.name())) {
                    imgDocType.setImageResource(R.mipmap.type_dog);
                }
                if (document.getDoc_type().equals(DocType.SF.name())) {
                    imgDocType.setImageResource(R.mipmap.type_sf);
                }
                if (document.getDoc_type().equals(DocType.TORG12.name())) {
                    imgDocType.setImageResource(R.mipmap.type_torg12);
                }
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showDocDetail(document);
                    }
                });

            }

            return convertView;
        }
    }


    private void showDocDetail(final Document document) {
        progressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                DocumentDetail result = null;
                try {
                    result = Session.getInstance().getDocumentDetail(document);
                    Session.getInstance().setCurrentDocumentDetail(result);
                    TimeUnit.SECONDS.sleep(2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                handleDocumentDetailsResponse(result);
            }
        }).start();
    }

    private void handleDocumentDetailsResponse(final DocumentDetail documentDetail) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.hide();
                if (documentDetail != null) {
                    Intent intent = new Intent();
                    intent.setClass(DocsFragment.this.getActivity(), DocActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    DocsFragment.this.getActivity().startActivity(intent);
                } else {
                    // показать ошибку
                    MessageUtils.showModalMessage(DocsFragment.this.getActivity(), "Ошибка!", "Не получилось досталь детальные данные по документы!");
                }
            }
        });
    }


}
