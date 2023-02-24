package com.example.presentations.questionnaire.vm

import com.example.base.BaseViewEvent
import com.example.presentations.questionnaire.domain.models.FieldActions


sealed class QuestionnaireEvent : BaseViewEvent {
    object OnBackPressed : QuestionnaireEvent()
    object RepeatRequest : QuestionnaireEvent()
    object SendQuestionnaire : QuestionnaireEvent()
    object RemoveDraft : QuestionnaireEvent()
    data class FieldAction(val action: FieldActions) : QuestionnaireEvent()
}
