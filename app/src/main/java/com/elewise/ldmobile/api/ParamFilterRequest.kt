package com.elewise.ldmobile.api

data class ParamFilterRequest (
  val username: String?,
  val quantity: String?,
  val action_type: String?,
  val begin_date: String?,
  val end_date: String?,
  val vendor_name: String?,
  val doc_type: String?
)
