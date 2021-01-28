package com.elewise.ldmobile.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ParamGetFileResponse(val base64: String?,
                                val file_name: String,
                                val status: String)