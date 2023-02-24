package com.example.presentations.questionnaire.domain.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SubFieldEntity(
    val code: String?,
    val fieldName: String,
    val values: List<String>?,
) : Parcelable
