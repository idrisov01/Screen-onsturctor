package com.example.presentations.questionnaire.data.dto

import kotlinx.serialization.Serializable


@Serializable
data class FieldDto(
    val code: String? = null,
    val title: String,
    val field_name: String,
    val type: String,
    val description: String? = null,
    val is_required: Boolean,
    val settings: List<SettingDto>? = null,
    val values: List<String>? = null,
    val my_value: List<String>? = null,
    val my_files: FileDto? = null,
    val my_subfields_values: List<List<SubFieldDto>>? = null,
    val subfields: List<FieldDto>? = null,
)
