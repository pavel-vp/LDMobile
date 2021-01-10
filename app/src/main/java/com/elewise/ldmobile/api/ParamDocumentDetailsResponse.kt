package com.elewise.ldmobile.api

import com.elewise.ldmobile.api.data.*
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ParamDocumentDetailsResponse (
        val status: String,
        val message: String?,
        val doc_alt_type: String?,
        val doc_id: Int,
        val doc_title: String?,
        val doc_icon: String?,
        val header_attributes: List<DocHeaderAttributes>?,
        val lines: List<DocumentItem>?,
        val history: List<DocumentHistory>?,
        val attachments: List<DocumentAttachment>?,
        val related_docs: List<RelatedDoc>?,
        var buttons: ArrayList<ButtonDesc>?
)

