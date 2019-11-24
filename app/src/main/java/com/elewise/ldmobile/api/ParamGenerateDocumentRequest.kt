package com.elewise.ldmobile.api

data class ParamGenerateDocumentRequest (
        val access_token: String,
        val data: ParametersGenerateDocument
)

data class ParametersGenerateDocument(
        val doc_id: String,
        val doc_type: String
)