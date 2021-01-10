package com.elewise.ldmobile.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ParamDocumentsRequest (
   val access_token: String,
   val size: Int,
   val from: Int,
   val direction: String,
   val filters: List<FilterData>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FilterData (
        val name: String?,
        val value: String?,
        val value2: String?
)

