package com.elewise.ldmobile.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.elewise.ldmobile.R;
import com.elewise.ldmobile.model.DocumentAttachment;

/**
 * Adapter
 */
public class AttachmentAdapter extends BaseAdapter {
    private Context context;
    private DocumentAttachment[] attachments;

    public AttachmentAdapter(Context context, DocumentAttachment[] attachments) {
        this.context = context;
        this.attachments = attachments;
    }

    @Override
    public int getCount() {
        return attachments.length;
    }

    @Override
    public Object getItem(int position) {
        return attachments[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.attachment_item, parent, false);

        TextView tvAttacheName = convertView.findViewById(R.id.tvAttacheName);
        tvAttacheName.setText(attachments[position].getFile_name());
        tvAttacheName.setTag(attachments[position].getFile_id());

        convertView.setOnClickListener(view -> showAttachment((Integer) view.getTag()));
        return convertView;
    }

    private void showAttachment(Integer file_id) {
        // Вызвать вебстраницу с урлем
        Intent i = new Intent(Intent.ACTION_VIEW);
        String url = this.context.getResources().getString(R.string.rest_server_base_url) + "";
        i.setData(Uri.parse(url));
        this.context.startActivity(i);
    }

}
