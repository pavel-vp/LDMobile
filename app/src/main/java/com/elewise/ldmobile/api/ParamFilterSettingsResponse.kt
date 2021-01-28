package com.elewise.ldmobile.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ParamFilterSettingsResponse(
        val status: String,
        val message: String?,
        val filters: List<FilterElement>?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FilterElement(val name: String,
                         val desc: String,
                         val desc2: String?,
                         val type: String,
                         val last_value: String?,
                         val last_value2: String?,
                         val list: List<FilterElementListItem>?,
                         val disabled: Boolean,
                         val required: Boolean
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FilterElementListItem(val code: String,
                                 val desc: String)