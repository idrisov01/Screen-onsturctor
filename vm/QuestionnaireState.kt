package com.example.presentations.questionnaire.vm

import com.example.base.BaseViewState
import com.example.presentations.questionnaire.domain.models.FieldTypes


data class QuestionnaireState(
    val isLoading: Boolean = false,
    val canDelete: Boolean = false,
    val fields: List<FieldTypes>? = null,
    val invalidFieldIndex: Int? = null,
    val isNetworkError: Boolean = false,
    val serverMessage: String? = null,
) : BaseViewState {

    companion object {
        fun initialState(): QuestionnaireState {
            return QuestionnaireState()
        }
    }
}