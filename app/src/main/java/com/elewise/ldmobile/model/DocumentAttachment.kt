package com.elewise.ldmobile.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class DocumentAttachment (
   val file_id: Int,
   val file_name: String
)
