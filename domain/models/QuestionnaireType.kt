package com.example.presentations.questionnaire.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class QuestionnaireType : Parcelable {
    QUALIFICATION_FORM, EXTENDED_FORM
}