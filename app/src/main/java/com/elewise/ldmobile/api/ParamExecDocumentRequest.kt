package com.elewise.ldmobile.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ParamExecDocumentRequest (
        val access_token: String,
        val doc_id: Int,
        val doc_alt_type: String,
        val action: String,
        val comment: String?
)
