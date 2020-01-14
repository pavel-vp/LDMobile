package com.elewise.ldmobile.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class FilterElement(val name: String,
                         val desc: String,
                         val type: String,
                         val last_value: String?,
                         val list: Array<FilterElementListItem>?,
                         val disabled: Boolean,
                         val required: Boolean)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FilterElementListItem(val code: String,
                                 val desc: String)