package com.elewise.ldmobile.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ParamGenerateDocumentRequest (
        val access_token: String,
        val data: ParametersGenerateDocument
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ParametersGenerateDocument(
        val doc_id: String,
        val doc_type: String
)