package com.elewise.ldmobile.utils;

import android.app.Activity;
import android.app.AlertDialog;

import com.elewise.ldmobile.R;

public class MessageUtils {
    public static AlertDialog createDialog(final Activity activity, final String title, final String msg) {
        // показываем ошибку
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title)
                .setMessage(msg)
                .setCancelable(false)
                .setNegativeButton(R.string.alert_dialog_ok,
                        (dialog, id) -> dialog.cancel());
        return builder.create();
    }
}
