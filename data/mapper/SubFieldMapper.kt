package com.example.presentations.questionnaire.data.mapper

import com.example.base.BaseMapper
import com.example.presentations.questionnaire.data.dto.SubFieldDto
import com.example.presentations.questionnaire.domain.entity.SubFieldEntity
import javax.inject.Inject


class SubFieldMapper @Inject constructor() : BaseMapper<SubFieldDto, SubFieldEntity> {

    override fun map(from: SubFieldDto): SubFieldEntity {

        return SubFieldEntity(
            code = from.code,
            fieldName = from.field_name,
            values = from.value?.map { value -> value.orEmpty() }
        )
    }
}