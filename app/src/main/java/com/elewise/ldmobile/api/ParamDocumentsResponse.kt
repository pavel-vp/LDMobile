package com.elewise.ldmobile.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ParamDocumentsResponse (
        val status: String,
        val message: String?,
        val last_record: Boolean,
        val group_flag: Boolean,
        var contents: List<Document>?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Document(
        val doc_alt_type: String?,
        val doc_id: Int,
        val doc_name: String?,
        val contractor: String?,
        val doc_icon: String?,
        val action_icon: String?,
        val attach_flag: Boolean?,
        val group: String?
)
