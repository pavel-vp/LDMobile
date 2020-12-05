package com.elewise.ldmobile.api.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class RelatedDoc(val doc_name: String,
                      val doc_id: Int,
                      val doc_alt_type: String,
                      val doc_icon: String,
                      val action_icon: String)