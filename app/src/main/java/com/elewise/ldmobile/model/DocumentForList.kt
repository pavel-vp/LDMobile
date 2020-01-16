package com.elewise.ldmobile.model

data class DocumentForList (
        val document: Document,
        val isSection: Boolean,
        val sectionTitle: String?
)