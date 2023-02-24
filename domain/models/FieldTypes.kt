package com.example.presentations.questionnaire.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.example.common.extension.asString
import com.example.common.extension.orFalse
import com.example.common.extension.orTrue
import com.example.presentations.questionnaire.domain.entity.FileEntity
import com.example.presentations.questionnaire.domain.entity.ItemSelectorEntity
import com.example.presentations.questionnaire.domain.entity.SettingEntity
import com.example.presentations.questionnaire.domain.entity.SubFieldEntity
import ru.tinkoff.decoro.slots.Slot

enum class FieldEnums {
    FIELD_TEXT,
    FIELD_LIST,
    FIELD_DATE,
    FIELD_FILE,
    FIELD_CHECKBOX,
    FIELD_EXPERIENCE,
    FIELD_MULTIPLE_SELECT,
    FIELD_SINGLE_SELECT,
    FIELD_MULTIPLE_FIELDS
}

sealed class FieldTypes : Parcelable {

    companion object {

        val FieldTypes.fieldId: String?
            get() = when (this) {
                is Text -> fieldId
                is Checkbox -> fieldId
                is MultipleSelect -> fieldId
                is SingleSelect -> fieldId
                is Spinner -> fieldId
                is WorkExperience -> fieldId
                is File -> fieldId
                is MultipleFields -> fieldId
                else -> null
            }

        val FieldTypes.code: String?
            get() = when (this) {
                is Text -> code
                is Checkbox -> code
                is MultipleSelect -> code
                is SingleSelect -> code
                is Spinner -> code
                is WorkExperience -> code
                is File -> code
                is MultipleFields -> code
                else -> null
            }

        val FieldTypes.myValue: String?
            get() = when (this) {
                is Text -> this.myValues?.joinToString()
                is Checkbox -> this.isChecked.asString()
                is MultipleSelect -> this.myValues?.joinToString()
                is SingleSelect -> this.myValues?.joinToString()
                is Spinner -> this.myValues?.joinToString()
                is WorkExperience -> this.mySubfieldsValues.joinToString()
                is File -> this.myFiles?.filesCount.asString()
                is MultipleFields -> this.mySubfieldsValues.joinToString()
                else -> null
            }

        val FieldTypes.isRequired: Boolean
            get() = when (this) {
                is Text -> isRequired
                is MultipleSelect -> isRequired
                is SingleSelect -> isRequired
                is Spinner -> isRequired
                is WorkExperience -> isRequired
                is File -> isRequired
                is MultipleFields -> isRequired
                else -> false
            }

        val FieldTypes.fieldIsEmpty: Boolean
            get() = when (this) {
                is Text -> fieldIsEmpty.orTrue()
                is MultipleSelect -> fieldIsEmpty.orTrue()
                is SingleSelect -> fieldIsEmpty.orTrue()
                is Spinner -> fieldIsEmpty.orTrue()
                is WorkExperience -> fieldIsEmpty.orTrue()
                is File -> fieldIsEmpty.orTrue()
                is MultipleFields -> fieldIsEmpty.orTrue()
                else -> false
            }

        val FieldTypes.isValidData: Boolean
            get() = when (this) {
                is Text -> this.isValidData.orFalse()
                else -> true
            }
    }

    @Parcelize
    object Divider : FieldTypes()

    @Parcelize
    object Space : FieldTypes()

    @Parcelize
    object Progress : FieldTypes()

    @Parcelize
    data class Title(val title: String) : FieldTypes()

    @Parcelize
    data class Send(val isEnable: Boolean = false) : FieldTypes()

    @Parcelize
    data class Text(
        val code: String?,
        val fieldId: String,
        val hint: String,
        val isRequired: Boolean,
        val description: String?,
        val myValues: List<String>?,
        val mask: List<Slot>? = null,
        val enteredText: String? = null,
        val inputType: TextInputTypes,
        val settings: List<SettingEntity>? = null,
        val fieldIsEmpty: Boolean? = null,
        val isValidData: Boolean? = null,
    ) : FieldTypes()

    @Parcelize
    data class Spinner(
        val code: String?,
        val fieldId: String,
        val hint: String,
        val title: String,
        val isRequired: Boolean,
        val description: String?,
        val values: List<String>?,
        val myValues: List<String>?,
        val settings: List<SettingEntity>? = null,
        val fieldIsEmpty: Boolean? = null,
    ) : FieldTypes()

    @Parcelize
    data class SingleSelect(
        val code: String?,
        val fieldId: String,
        val hint: String,
        val isRequired: Boolean,
        val description: String?,
        val values: List<ItemSelectorEntity>?,
        val myValues: List<String>?,
        val settings: List<SettingEntity>?,
        val fieldIsEmpty: Boolean? = null,
    ) : FieldTypes()

    @Parcelize
    data class MultipleSelect(
        val code: String?,
        val fieldId: String,
        val hint: String,
        val isRequired: Boolean,
        val description: String?,
        val values: List<ItemSelectorEntity>?,
        val myValues: List<String>?,
        val settings: List<SettingEntity>?,
        val fieldIsEmpty: Boolean? = null,
    ) : FieldTypes()

    @Parcelize
    data class WorkExperience(
        val code: String?,
        val fieldId: String,
        val title: String,
        val isRequired: Boolean,
        val description: String?,
        val settings: List<SettingEntity>? = null,
        val mySubfieldsValues: List<List<SubFieldEntity>>,
        val subFields: List<FieldTypes>? = null,
        val fieldIsEmpty: Boolean? = null,
        val hasUpperDivider: Boolean = false,
        val hasLowerDivider: Boolean = false,
    ) : FieldTypes() {

        companion object {
            fun WorkExperience.mapToMultipleFields(): MultipleFields {
                return MultipleFields(
                    code = code,
                    fieldId = fieldId,
                    title = title,
                    isRequired = isRequired,
                    description = description,
                    settings = settings,
                    mySubfieldsValues = mySubfieldsValues,
                    subFields = subFields,
                    fieldIsEmpty = fieldIsEmpty,
                    hasUpperDivider = hasUpperDivider,
                    hasLowerDivider = hasLowerDivider,
                    hint = "Добавьте предыдущие места работы"
                )
            }
        }
    }

    @Parcelize
    data class Checkbox(
        val code: String?,
        val fieldId: String,
        val hint: String,
        val hiddenKey: String? = null,
        val isRequired: Boolean,
        val isChecked: Boolean = false,
        val settings: List<SettingEntity>? = null,
    ) : FieldTypes()

    @Parcelize
    data class File(
        val code: String?,
        val fieldId: String,
        val title: String,
        val description: String?,
        val isRequired: Boolean,
        val myFiles: FileEntity? = null,
        val settings: List<SettingEntity>?,
        val value: String? = null,
        val myValues: List<String>? = null,
        val mySubfieldsValues: String? = null,
        val subfields: String? = null,
        val fieldIsEmpty: Boolean? = null,
    ) : FieldTypes()

    @Parcelize
    data class MultipleFields(
        val code: String?,
        val fieldId: String,
        val title: String,
        val hint: String?,
        val isRequired: Boolean,
        val description: String?,
        val settings: List<SettingEntity>? = null,
        val mySubfieldsValues: List<List<SubFieldEntity>>,
        val subFields: List<FieldTypes>? = null,
        val fieldIsEmpty: Boolean? = null,
        val hasUpperDivider: Boolean = false,
        val hasLowerDivider: Boolean = false,
    ) : FieldTypes() {

        companion object {
            fun MultipleFields.mapToWorkExperience(): WorkExperience {
                return WorkExperience(
                    code = code,
                    fieldId = fieldId,
                    title = title,
                    isRequired = isRequired,
                    description = description,
                    settings = settings,
                    mySubfieldsValues = mySubfieldsValues,
                    subFields = subFields,
                    fieldIsEmpty = fieldIsEmpty,
                    hasUpperDivider = hasUpperDivider,
                    hasLowerDivider = hasLowerDivider
                )
            }
        }
    }
}
