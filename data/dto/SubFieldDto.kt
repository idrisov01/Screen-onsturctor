package com.example.presentations.questionnaire.data.dto

import kotlinx.serialization.Serializable


@Serializable
data class SubFieldDto(
    val code: String? = null,
    val field_name: String,
    val value: List<String?>? = null,
)