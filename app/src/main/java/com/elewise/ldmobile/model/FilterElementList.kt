package com.elewise.ldmobile.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class FilterElementList(val filters: List<FilterElement>): Serializable