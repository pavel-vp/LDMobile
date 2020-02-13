package com.elewise.ldmobile.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ParamAuthorizationResponse (
        val status: String,
        val message: String?,
        val access_token: String,
        val role: String?,
        val certificate_status: String?
)

enum class AuthStatusType {S, E, A}
