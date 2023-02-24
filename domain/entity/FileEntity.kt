package com.example.presentations.questionnaire.domain.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FileEntity(val fieldId: Int, val filesCount: Int) : Parcelable
