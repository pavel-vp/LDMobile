package com.elewise.ldmobile.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ParamGetDocumentsRequest (
   val access_token: String,
   val filters: ParamFilterRequest?
)
