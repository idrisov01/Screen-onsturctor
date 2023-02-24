package com.example.presentations.questionnaire.data.mapper

import com.example.base.BaseMapper
import com.example.presentations.questionnaire.data.dto.SettingDto
import com.example.presentations.questionnaire.domain.entity.SettingEntity
import javax.inject.Inject


class SettingMapper @Inject constructor() : BaseMapper<SettingDto, SettingEntity> {

    override fun map(from: SettingDto): SettingEntity {
        return SettingEntity(key = from.key, value = from.value)
    }
}