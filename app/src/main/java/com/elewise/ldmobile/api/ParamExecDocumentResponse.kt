package com.elewise.ldmobile.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ParamExecDocumentResponse (
    val status: String,
    val message: String?,
    val file_ids: Array<Int>?
)
