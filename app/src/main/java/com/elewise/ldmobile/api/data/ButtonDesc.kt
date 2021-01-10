package com.elewise.ldmobile.api.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class ButtonDesc(
        val sign_flag: Boolean, // если тру, то before, after
        val type: String,
        val caption: String,
        val title: String,
        val text: String,
        val comment_flag: Boolean
) : Serializable