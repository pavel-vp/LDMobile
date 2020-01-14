package com.elewise.ldmobile.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class RelatedDoc(val doc_name: String,
                      val doc_id: Int,
                      val doc_type: String,
                      val doc_icon: String,
                      val action_icon: String)