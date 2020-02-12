package com.elewise.ldmobile.api.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ButtonDesc(
        val type: String,
        val caption: String,
        val title: String,
        val text: String,
        val comment_flag: String

)