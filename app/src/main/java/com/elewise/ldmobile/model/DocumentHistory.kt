package com.elewise.ldmobile.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class DocumentHistory (
  val history_date: String?,
  val employee: String?,
  val text: String?
)
