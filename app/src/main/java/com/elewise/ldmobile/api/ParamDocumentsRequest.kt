package com.elewise.ldmobile.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ParamDocumentsRequest (
   val access_token: String,
   val size: Int = 9999,
   val from: Int = 0,
   val direction: String,
   val filters: Array<FilterData>?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FilterData (
        val name: String?,
        val value: String?,
        val value2: String?
)

