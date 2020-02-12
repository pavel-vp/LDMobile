package com.elewise.ldmobile.api.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class DocHeaderAttributes(
        val desc: String,
        val value: String,
        val icon: String?)