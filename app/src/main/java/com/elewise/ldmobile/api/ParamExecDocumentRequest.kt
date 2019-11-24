package com.elewise.ldmobile.api

data class ParamExecDocumentRequest (
        val access_token: String,
        val action: String,
        val data: ParameterExecDocument,
        val signatures: Array<SignatureExecDocument>
)

data class ParameterExecDocument(
        val doc_id: String,
        val doc_type: String,
        val comment: String?
)

data class SignatureExecDocument(
        val sign_name: String,
        val file_name: String
)