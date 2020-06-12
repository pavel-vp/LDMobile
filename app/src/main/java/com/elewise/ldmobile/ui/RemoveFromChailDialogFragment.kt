package com.elewise.ldmobile.ui

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import com.elewise.ldmobile.MainApp
import com.elewise.ldmobile.R

class RemoveFromChailDialogFragment: DialogFragment() {

    private var alias: String = ""

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(MainApp.getApplcationContext())
            .setTitle(R.string.dialog_delete_from_chain_title)
            .setPositiveButton(R.string.dialog_delete_button) { dialogInterface, i ->  }
            .setNegativeButton(R.string.dialog_cancel_button) { dialogInterface, i ->  }
            .create()
    }

    fun showDialog(alias: String) {
        this.alias = alias
        this.dialog.show()
    }
}