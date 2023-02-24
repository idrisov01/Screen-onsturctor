package com.example.presentations.questionnaire.data.mapper

import com.example.base.BaseMapper
import com.example.presentations.questionnaire.data.dto.FileDto
import com.example.presentations.questionnaire.domain.entity.FileEntity
import javax.inject.Inject


class FileMapper @Inject constructor() : BaseMapper<FileDto?, FileEntity?> {

    override fun map(from: FileDto?): FileEntity? {

        if (from == null) return null

        return FileEntity(
            fieldId = from.field_id,
            filesCount = from.my_files_count
        )
    }
}