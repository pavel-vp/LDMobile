package com.elewise.ldmobile.api

import com.elewise.ldmobile.model.Document

data class ParamGetDocumentsResponse (
   val returned_quantity: Int?,
   val contents: Array<Document>?
)