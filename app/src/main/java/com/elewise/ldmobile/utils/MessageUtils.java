package com.elewise.ldmobile.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Handler;
import android.widget.Toast;

import com.elewise.ldmobile.MainApp;
import com.elewise.ldmobile.R;

public class MessageUtils {

    private static final Handler handler = new Handler(MainApp.getApplcationContext().getMainLooper());

///////////////////////////////
    public static void showToastMessage(final String msg) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainApp.getApplcationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    public static void showToastMessage(final String msgFormat, Object... objects ) {
        String msg = String.format(msgFormat, objects);
        showToastMessage(msg);
    }

    public static AlertDialog createDialog(Activity activity, int title, int msg) {
        return createDialog(activity, activity.getString(title), activity.getString(msg));
    }

//////////////////////////////////
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

////////////////////////////

    public static void showModalAndConfirm(final Activity activity, final String title, final String msg,
                                           final DialogInterface.OnClickListener listenerOk) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                // показываем ошибку
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(title)
                        .setMessage(msg)
                        .setCancelable(false)
                        .setPositiveButton("Да", listenerOk)
                        .setNegativeButton("Нет", null);
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    public static void showModalAndConfirm(final Activity activity, final String title, final String msgFormat,
                                           DialogInterface.OnClickListener listenerOk, Object... objects ) {
        String msg = String.format(msgFormat, objects);
        showModalAndConfirm(activity, title, msg, listenerOk);
    }

////////////////////

    public static void playSound(int idSound){
        MediaPlayer mPlayer = MediaPlayer.create(MainApp.getApplcationContext(), idSound);
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
        mPlayer.start();
    }

}
