package com.example.presentations.questionnaire.domain.entity

import com.example.presentations.questionnaire.domain.models.FieldTypes

data class FormEntity(
    val title: String,
    val formFields: List<FieldTypes>
)
