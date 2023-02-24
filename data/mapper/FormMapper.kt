package com.example.presentations.questionnaire.data.mapper

import com.example.base.BaseMapper
import com.example.presentations.questionnaire.data.dto.FieldDto
import com.example.presentations.questionnaire.data.dto.FormDto
import com.example.presentations.questionnaire.domain.entity.FormEntity
import com.example.presentations.questionnaire.domain.models.FieldTypes
import javax.inject.Inject


class FormMapper @Inject constructor(
    private val fromFieldsMapper: BaseMapper<FieldDto, FieldTypes>,
) : BaseMapper<FormDto, FormEntity> {

    override fun map(from: FormDto): FormEntity {
        return FormEntity(
            title = from.title,
            formFields = from.fields.map { formFieldDto -> fromFieldsMapper.map(formFieldDto) }
        )
    }
}