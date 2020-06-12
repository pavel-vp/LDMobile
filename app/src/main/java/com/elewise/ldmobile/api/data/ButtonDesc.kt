package com.elewise.ldmobile.api.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class ButtonDesc(
        val type: String,
        val caption: String,
        val title: String,
        val text: String,
        val comment_flag: String
) : Serializable

enum class DocumentDetailButtonType {sign, not_sign, approve, reject, return_executor, annul_confirm, annul_reject}

enum class DocumentDetailButtonCommentFlag {N, Y, R}