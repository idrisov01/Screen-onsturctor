package com.example.presentations.questionnaire.domain.fieldvalidator

import com.example.presentations.questionnaire.domain.models.FieldTypes


interface FieldValidator {

    fun checkRequiredFieldIsFill(allFields: List<FieldTypes>, field: FieldTypes): FieldTypes?

    fun checkFieldIsCorrect(field: FieldTypes): FieldTypes?

    fun isValidText(field: FieldTypes.Text): Boolean
}