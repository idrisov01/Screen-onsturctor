package com.example.presentations.questionnaire.domain.entity

import android.os.Parcelable


@kotlinx.parcelize.Parcelize
data class ItemSelectorEntity(
    val value: String,
    val isSelected: Boolean,
) : Parcelable