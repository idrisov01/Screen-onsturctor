package com.example.presentations.questionnaire.domain.models

sealed class FieldActions {

    object Send : FieldActions()

    data class Text(val field: FieldTypes.Text, val isFocused: Boolean) : FieldActions()
    data class Spinner(val field: FieldTypes.Spinner) : FieldActions()
    data class SingleSelect(val field: FieldTypes.SingleSelect) : FieldActions()
    data class MultipleSelect(val field: FieldTypes.MultipleSelect) : FieldActions()
    data class WorkExperience(val field: FieldTypes.WorkExperience) : FieldActions()
    data class SubField(val field: FieldTypes.MultipleFields) : FieldActions()
    data class Checkbox(val field: FieldTypes.Checkbox) : FieldActions()
    data class File(val field: FieldTypes.File) : FieldActions()

    data class WorkExperienceDetail(
        val field: FieldTypes.WorkExperience,
        val index: Int?,
    ) : FieldActions()

    data class SubFieldDetail(
        val field: FieldTypes.MultipleFields,
        val index: Int?,
    ) : FieldActions()
}
