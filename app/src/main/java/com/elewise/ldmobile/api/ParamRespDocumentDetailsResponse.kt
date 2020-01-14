package com.elewise.ldmobile.api

import com.elewise.ldmobile.model.*
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ParamRespDocumentDetailsResponse (
        val doc_type: String,
        val doc_id: Int,
        val doc_title: String,
        val doc_icon: String,
        val header_attributes: Array<DocHeaderAttributes>,
        val items: Array<DocumentItem>?,
        val history: Array<DocumentHistory>?,
        val attachments: Array<DocumentAttachment>?,
        val related_docs: Array<RelatedDoc>?,
        val user_action: String?
)

