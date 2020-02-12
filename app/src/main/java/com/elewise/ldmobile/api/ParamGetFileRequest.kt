package com.elewise.ldmobile.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class ParamGetFileRequest (
    val access_token: String,
    val file_id: Int
)

