package com.example.presentations.questionnaire.data.dto


@kotlinx.serialization.Serializable
data class FormDto(
    val title: String,
    val fields: List<FieldDto>
)
