package com.example.presentations.questionnaire.data.mapper

import com.example.base.BaseMapper
import com.example.common.extension.orFalse
import com.example.common.utils.DateUtils
import com.example.common.utils.MaskUtils
import com.example.presentations.questionnaire.data.dto.FieldDto
import com.example.presentations.questionnaire.data.dto.FileDto
import com.example.presentations.questionnaire.data.dto.SettingDto
import com.example.presentations.questionnaire.data.dto.SubFieldDto
import com.example.presentations.questionnaire.domain.entity.FileEntity
import com.example.presentations.questionnaire.domain.entity.ItemSelectorEntity
import com.example.presentations.questionnaire.domain.entity.SettingEntity
import com.example.presentations.questionnaire.domain.entity.SubFieldEntity
import com.example.presentations.questionnaire.domain.models.FieldEnums
import com.example.presentations.questionnaire.domain.models.FieldSettings
import com.example.presentations.questionnaire.domain.models.FieldTypes
import com.example.presentations.questionnaire.domain.models.TextInputTypes
import javax.inject.Inject


class FieldsMapper @Inject constructor(
    private val settingMapper: BaseMapper<SettingDto, SettingEntity>,
    private val fileMapper: BaseMapper<FileDto?, FileEntity?>,
    private val subFieldMapper: BaseMapper<SubFieldDto, SubFieldEntity>,
) : BaseMapper<FieldDto, FieldTypes> {

    override fun map(from: FieldDto): FieldTypes {

        return when (FieldEnums.valueOf(from.type)) {
            FieldEnums.FIELD_TEXT -> FieldTypes.Text(
                code = from.code,
                fieldId = from.field_name,
                hint = from.title,
                isRequired = from.is_required,
                description = from.description,
                myValues = from.my_value,
                mask = MaskUtils.parseMask(from.settings),
                inputType = TextInputTypes.typeOf(
                    from.settings?.firstOrNull { settingDto ->
                        settingDto.key == FieldSettings.INPUT_TYPE.key
                    }?.value
                ),
                fieldIsEmpty = from.my_value?.isEmpty()
            )
            FieldEnums.FIELD_LIST -> FieldTypes.Spinner(
                code = from.code,
                fieldId = from.field_name,
                hint = from.title,
                title = from.settings?.firstOrNull { settingDto ->
                    settingDto.key == FieldSettings.TITLE.key
                }?.value.orEmpty(),
                isRequired = from.is_required,
                description = from.description,
                values = from.values,
                myValues = from.my_value,
                fieldIsEmpty = from.my_value?.isEmpty()
            )
            FieldEnums.FIELD_DATE -> {

                val settings = from.settings?.map { settingDto ->
                    settingMapper.map(settingDto)
                }.orEmpty().toMutableList()

                settings.firstOrNull { settingEntity -> settingEntity.key == FieldSettings.MIN_DATE.key }
                    ?: run {
                        settings.add(
                            SettingEntity(FieldSettings.MIN_DATE.key, DateUtils.DEFAULT_MIN_DATE)
                        )
                    }

                settings.firstOrNull { settingEntity -> settingEntity.key == FieldSettings.MAX_DATE.key }
                    ?: run {
                        settings.add(
                            SettingEntity(FieldSettings.MAX_DATE.key, DateUtils.DEFAULT_MAX_DATE)
                        )
                    }

                FieldTypes.Text(
                    code = from.code,
                    fieldId = from.field_name,
                    hint = from.title,
                    isRequired = from.is_required,
                    description = from.description,
                    myValues = from.my_value,
                    mask = MaskUtils.setupMaskForDate(),
                    inputType = TextInputTypes.NUMBER,
                    settings = settings,
                    fieldIsEmpty = from.my_value?.isEmpty()
                )
            }
            FieldEnums.FIELD_SINGLE_SELECT -> FieldTypes.SingleSelect(
                code = from.code,
                fieldId = from.field_name,
                hint = from.title,
                isRequired = from.is_required,
                description = from.description,
                myValues = from.my_value,
                values = from.values?.map { value ->
                    ItemSelectorEntity(
                        value = value,
                        isSelected = from.my_value?.any { myValue -> myValue == value }.orFalse()
                    )
                },
                settings = from.settings?.map { settingDto -> settingMapper.map(settingDto) },
                fieldIsEmpty = from.my_value?.isEmpty()
            )
            FieldEnums.FIELD_MULTIPLE_SELECT -> FieldTypes.MultipleSelect(
                code = from.code,
                fieldId = from.field_name,
                hint = from.title,
                isRequired = from.is_required,
                description = from.description,
                values = from.values?.map { value ->
                    ItemSelectorEntity(
                        value = value,
                        isSelected = from.my_value?.any { myValue -> myValue == value }.orFalse()
                    )
                },
                myValues = from.my_value,
                settings = from.settings?.map { settingDto -> settingMapper.map(settingDto) },
                fieldIsEmpty = from.my_value?.isEmpty()
            )
            FieldEnums.FIELD_EXPERIENCE -> FieldTypes.WorkExperience(
                code = from.code,
                fieldId = from.field_name,
                title = from.title,
                isRequired = from.is_required,
                description = from.description.orEmpty(),
                mySubfieldsValues = from.my_subfields_values?.map { workList ->
                    workList.map { workPlaceDto -> subFieldMapper.map(workPlaceDto) }
                }.orEmpty(),
                settings = from.settings?.map { settingDto -> settingMapper.map(settingDto) },
                subFields = from.subfields?.map { subfieldDto -> map(subfieldDto) },
                fieldIsEmpty = from.my_subfields_values?.isEmpty()
            )
            FieldEnums.FIELD_CHECKBOX -> FieldTypes.Checkbox(
                code = from.code,
                fieldId = from.field_name,
                hint = from.title,
                hiddenKey = from.settings?.firstOrNull { settingDto ->
                    settingDto.key == FieldSettings.HIDDEN_KEY.key
                }?.value,
                isRequired = from.is_required
            )
            FieldEnums.FIELD_FILE -> FieldTypes.File(
                code = from.code,
                fieldId = from.field_name,
                title = from.title,
                isRequired = from.is_required,
                description = from.description,
                myValues = from.my_value,
                myFiles = fileMapper.map(from.my_files),
                settings = from.settings?.map { settingDto -> settingMapper.map(settingDto) }
            )
            FieldEnums.FIELD_MULTIPLE_FIELDS -> FieldTypes.MultipleFields(
                code = from.code,
                fieldId = from.field_name,
                title = from.title,
                hint = from.settings?.firstOrNull { settingDto -> settingDto.key == FieldSettings.HINT.key }?.value,
                isRequired = from.is_required,
                description = from.description.orEmpty(),
                mySubfieldsValues = from.my_subfields_values?.map { subFields ->
                    subFields.map { subFieldDto -> subFieldMapper.map(subFieldDto) }
                }.orEmpty(),
                settings = from.settings?.map { settingDto -> settingMapper.map(settingDto) },
                subFields = from.subfields?.map { subfieldDto -> map(subfieldDto) },
                fieldIsEmpty = from.my_subfields_values?.isEmpty()
            )
        }
    }
}