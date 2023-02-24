package com.example.presentations.questionnaire.data.mapper

import com.example.base.BaseMapper
import com.example.presentations.questionnaire.data.dto.FormDto
import com.example.presentations.questionnaire.data.dto.QuestionnaireDto
import com.example.presentations.questionnaire.domain.entity.FormEntity
import com.example.presentations.questionnaire.domain.entity.QuestionnaireEntity
import javax.inject.Inject


class QuestionnaireMapper @Inject constructor(
    private val formMapper: BaseMapper<FormDto, FormEntity>,
) : BaseMapper<QuestionnaireDto, QuestionnaireEntity> {

    override fun map(from: QuestionnaireDto): QuestionnaireEntity {
        return QuestionnaireEntity(
            canDelete = from.can_delete,
            qualificationForm = from.qualification_form?.map { fieldDto -> formMapper.map(fieldDto) },
            extendedForm = from.extended_form?.map { fieldDto -> formMapper.map(fieldDto) }
        )
    }
}