package com.elewise.ldmobile.api

class ParamGetFileRequest (
    val access_token: String,
    val parameters: GetFileParameters
)

data class GetFileParameters (
   val file_id: Int
)