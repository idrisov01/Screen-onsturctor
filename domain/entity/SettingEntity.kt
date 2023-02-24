package com.example.presentations.questionnaire.domain.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SettingEntity(
    val key: String,
    val value: String
) : Parcelable
