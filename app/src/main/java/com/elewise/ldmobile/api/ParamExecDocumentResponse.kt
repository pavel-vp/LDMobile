package com.elewise.ldmobile.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ParamExecDocumentResponse (
    val status: String,
    val message: String?,
    val server_datetime: String,
    val file_ids: List<Int>?
)
