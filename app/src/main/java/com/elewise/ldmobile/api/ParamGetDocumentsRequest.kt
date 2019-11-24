package com.elewise.ldmobile.api

data class ParamGetDocumentsRequest (
   val access_token: String,
   val filters: ParamFilterRequest?
)
