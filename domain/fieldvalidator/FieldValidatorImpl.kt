package com.example.presentations.questionnaire.domain.fieldvalidator

import com.example.common.extension.isEqualToMask
import com.example.common.extension.orFalse
import com.example.common.utils.DateUtils
import com.example.presentations.questionnaire.domain.models.FieldSettings
import com.example.presentations.questionnaire.domain.models.FieldTypes
import com.example.presentations.questionnaire.domain.models.FieldTypes.Companion.fieldId
import javax.inject.Inject


class FieldValidatorImpl @Inject constructor() : FieldValidator {

    override fun checkRequiredFieldIsFill(
        allFields: List<FieldTypes>,
        field: FieldTypes,
    ): FieldTypes? {

        return when (
            val newField = allFields.firstOrNull { f -> f.fieldId == field.fieldId }
        ) {

            is FieldTypes.MultipleSelect -> {
                return newField.copy(
                    fieldIsEmpty = newField.isRequired && newField.myValues.isNullOrEmpty()
                )
            }
            is FieldTypes.SingleSelect -> {
                return newField.copy(
                    fieldIsEmpty = newField.isRequired && newField.myValues.isNullOrEmpty()
                )
            }
            is FieldTypes.Spinner -> {
                return newField.copy(
                    fieldIsEmpty = newField.isRequired && newField.myValues.isNullOrEmpty()
                )
            }
            is FieldTypes.Text -> {
                return newField.copy(
                    fieldIsEmpty = newField.isRequired && newField.myValues?.firstOrNull().isNullOrBlank()
                )
            }
            is FieldTypes.WorkExperience -> {
                return newField.copy(
                    fieldIsEmpty = newField.isRequired && newField.mySubfieldsValues.isEmpty()
                )
            }
            is FieldTypes.MultipleFields -> {
                return newField.copy(
                    fieldIsEmpty = newField.isRequired && newField.mySubfieldsValues.isEmpty()
                )
            }
            is FieldTypes.File -> {
                return newField.copy(
                    fieldIsEmpty = newField.isRequired && newField.myFiles?.filesCount == 0
                )
            }
            else -> null
        }
    }

    override fun checkFieldIsCorrect(field: FieldTypes): FieldTypes? {

        return when (field) {

            is FieldTypes.Text -> {
                return field.copy(isValidData = isValidText(field))
            }
            else -> null
        }
    }

    override fun isValidText(field: FieldTypes.Text): Boolean {

        val isDate = field.settings?.any { setting ->
            setting.key == FieldSettings.MIN_DATE.key || setting.key == FieldSettings.MAX_DATE.key
        }.orFalse()

        return when {

            field.myValues?.isNotEmpty() == true && isDate -> {

                val isValidToMask = if (field.mask != null) {
                    field.myValues.first().isEqualToMask(field.mask)
                } else {
                    true
                }

                isValidToMask && isValidDate(field)
            }
            field.mask != null && !field.myValues?.firstOrNull().isNullOrEmpty() -> {
                return field.myValues?.first().isEqualToMask(field.mask)
            }
            else -> true
        }
    }

    private fun isValidDate(field: FieldTypes.Text): Boolean {

        val startDate = field.settings
            ?.firstOrNull { s -> s.key == FieldSettings.MIN_DATE.key }
            ?.value ?: return true

        val endDate = field.settings
            .firstOrNull { s -> s.key == FieldSettings.MAX_DATE.key }
            ?.value ?: return true

        val myValuesDate = field.myValues?.firstOrNull() ?: return true

        return DateUtils.isWithinRange(myValuesDate, startDate, endDate)
    }

}