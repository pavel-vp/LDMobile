package com.elewise.ldmobile.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ParamExecOperationRequest(val access_token: String,
                                     val action: String
)

enum class ParamExecOperationActionType { close_session , reset_filters }