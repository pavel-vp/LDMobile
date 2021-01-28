package com.elewise.ldmobile.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ParamExecOperationResponse(val status: String)