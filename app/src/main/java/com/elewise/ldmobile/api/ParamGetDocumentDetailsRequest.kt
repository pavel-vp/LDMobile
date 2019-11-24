package com.elewise.ldmobile.api

data class ParamGetDocumentDetailsRequest (
  val access_token: String,
  val parameters: GetDocumentDetailsParameters
)

data class GetDocumentDetailsParameters (
  val doc_id: Integer,
  val doc_type: String
)
