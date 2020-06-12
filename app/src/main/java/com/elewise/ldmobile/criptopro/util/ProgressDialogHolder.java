/**
 * $RCSfileProgressDialogHolder.java,v $
 * version $Revision: 36379 $
 * created 12.03.2018 13:47 by afevma
 * last modified $Date: 2012-05-30 12:19:27 +0400 (Ср, 30 май 2012) $ by $Author: afevma $
 * <p/>
 * Copyright 2004-2018 Crypto-Pro. All rights reserved.
 * Этот файл содержит информацию, являющуюся
 * собственностью компании Крипто-Про.
 * <p/>
 * Любая часть этого файла не может быть скопирована,
 * исправлена, переведена на другие языки,
 * локализована или модифицирована любым способом,
 * откомпилирована, передана по сети с или на
 * любую компьютерную систему без предварительного
 * заключения соглашения с компанией Крипто-Про.
 */
package com.elewise.ldmobile.criptopro.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;

import com.elewise.ldmobile.R;

/**
 * Служебный класс ProgressDialogHolder
 * предназначен для вызова окна ожидания
 * в ходе длительной операции.
 *
 * @author Copyright 2004-2018 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class ProgressDialogHolder extends ProgressDialog {

    /**
     * Конструктор.
     *
     * @param context Контекст приложения.
     * @param cancelable True, если окно можно закрыть.
     */
    public ProgressDialogHolder(Context context, boolean
        cancelable) {

        super(context);

        setIndeterminate(true);
        setCancelable(cancelable);

        String message = context.getString(R.string.ProgressDialogExecuting);
        setMessage(message);
        /*
        setButton(DialogInterface.BUTTON_NEGATIVE,
            context.getString(android.R.string.cancel),
            new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    cancel();
                }

        });
        */
        setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {

                // Закрытие окна при нажатии на Back.
                if (keyCode == KeyEvent.KEYCODE_BACK) {

                    cancel();
                    return true;

                } // if

                return false;

            }

        });

    }

}
