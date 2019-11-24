package com.elewise.ldmobile.model

data class Document(
        val doc_type: String,
        val doc_id: Int,
        val doc_name: String,
        val vendor_name: String,
        val direction: String,
        val action_type: String,
        val date: String,
        val file_attached: Boolean

)
