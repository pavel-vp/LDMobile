package com.elewise.ldmobile.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ParamSaveFileSignRequest(
        val access_token: String,
        val signs: List<FileSign>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FileSign (
        val file_id: Int,
        var data: String?
)