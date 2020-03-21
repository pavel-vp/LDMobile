package com.elewise.ldmobile.utils

import android.content.res.Resources
import android.widget.ImageView


object ImageUtils {
    fun setIcon(resources: Resources, imageView: ImageView, iconName: String) {
        val resId = resources.getIdentifier(iconName, "drawable", "com.elewise.ldmobile")
        imageView.setImageResource(resId)
    }
}
