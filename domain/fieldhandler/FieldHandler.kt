package com.example.presentations.questionnaire.domain.fieldhandler

import com.example.presentations.questionnaire.domain.models.FieldActions
import com.example.presentations.questionnaire.domain.models.FieldTypes


interface FieldHandler {

    val updatedFields: List<FieldTypes>
    fun handleFieldAction(allFields: List<FieldTypes>, action: FieldActions)
    fun checkButtonState(allFields: List<FieldTypes>)
}