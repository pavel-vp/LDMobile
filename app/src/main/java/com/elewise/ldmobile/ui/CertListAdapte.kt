package com.elewise.ldmobile.ui

import android.app.DialogFragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.elewise.ldmobile.R
import java.util.*

// todo remove
// Адаптер
private class CertListAdapte(val activity: AppCompatActivity, val dialog: DialogFragment): RecyclerView.Adapter<CertListAdapte.OptionHolder>() {

    var list: List<String> = ArrayList()
        set(list) {
            field = list
            notifyDataSetChanged()
        }

    fun getItem(position: Int) = list.get(position)

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): OptionHolder {
        val itemView = LayoutInflater.from(activity).inflate(R.layout.cert_widget, parent, false)
        val holder = OptionHolder(itemView)
        return holder
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: OptionHolder, position: Int) {
        holder.updateDisplay(getItem(position))
    }

    // Холдер
    private inner class OptionHolder(itemView: View): RecyclerView.ViewHolder(itemView)  {

        private val tvName: TextView
        private val llView: LinearLayout

        init {
            tvName = itemView.findViewById(R.id.tvName)
            llView = itemView.findViewById(R.id.llView)
        }

        fun updateDisplay(name: String) {
            tvName.text = name
            llView.setOnLongClickListener {
                dialog.show(activity.fragmentManager, "dialog_in_adapter")
                true
            }
        }
    }
}