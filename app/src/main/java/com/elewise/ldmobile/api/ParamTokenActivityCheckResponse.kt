package com.elewise.ldmobile.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ParamTokenActivityCheckResponse(
        val session_activity_status: String?,
        val status: String,
        val message: String?,
        val docs_size: Int?
)