package com.elewise.ldmobile.model

import com.elewise.ldmobile.api.Document

data class DocumentForList (
        val document: Document?,
        val isSection: Boolean,
        val sectionTitle: String?
)