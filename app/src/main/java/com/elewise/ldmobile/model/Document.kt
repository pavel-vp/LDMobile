package com.elewise.ldmobile.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Document(
        val doc_type: String,
        val doc_id: Int,
        val doc_date: String,
        val doc_name: String,
        val contractor: String,
        val doc_icon: String,
        val action_icon: String,
        val attach_flag: Boolean,
        val action: String?
)
