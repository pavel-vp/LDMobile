package com.elewise.ldmobile.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ParamGenerateDocumentResponse (
        val status: String,
        val message: String?,
        val file_id: String?
)
