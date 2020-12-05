package com.elewise.ldmobile.utils

import android.content.res.Resources
import android.widget.ImageView


object ImageUtils {
    fun setIcon(resources: Resources, imageView: ImageView, iconName: String?) {
        iconName?.let {
            val resId = resources.getIdentifier(it, "drawable", "com.elewise.ldmobile")
            imageView.setImageResource(resId)
        }
    }
}
