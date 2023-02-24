package com.example.presentations.questionnaire.domain.entity


data class QuestionnaireEntity(
    val canDelete: Boolean,
    val qualificationForm: List<FormEntity>?,
    val extendedForm: List<FormEntity>?,
)
