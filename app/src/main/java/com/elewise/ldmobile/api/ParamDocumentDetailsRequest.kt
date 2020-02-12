package com.elewise.ldmobile.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ParamDocumentDetailsRequest (
  val access_token: String,
  val doc_id: Int,
  val doc_type: String
)
