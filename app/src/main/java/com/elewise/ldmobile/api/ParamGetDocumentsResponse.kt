package com.elewise.ldmobile.api

import com.elewise.ldmobile.model.Document
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ParamGetDocumentsResponse (
   val returned_quantity: Int?,
   val contents: Array<Document>?
)