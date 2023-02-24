package com.example.presentations.questionnaire.data.dto

import kotlinx.serialization.Serializable


@Serializable
data class QuestionnaireDto(
    val can_delete: Boolean = false,
    val qualification_form: List<FormDto>? = null,
    val extended_form: List<FormDto>? = null,
)
