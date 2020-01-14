package com.elewise.ldmobile.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class DocumentItem (
  val line_name: String,
  val line_desc: String,
  val details: Array<DocLineDetail>
)