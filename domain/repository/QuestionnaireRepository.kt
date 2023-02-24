package com.example.presentations.questionnaire.domain.repository

import com.example.presentations.questionnaire.domain.entity.QuestionnaireEntity


interface QuestionnaireRepository {

    suspend fun loadQualificationForm(vacancyId: Int): QuestionnaireEntity

    suspend fun loadExtendedForm(vacancyId: Int): QuestionnaireEntity

    suspend fun uploadQuestionnaire(vacancyId: Int)

    suspend fun uploadDraft(vacancyId: Int, requestBody: Map<String, String>)

    suspend fun removeDraft(vacancyId: Int)

    suspend fun getUserPhone(): String?

    suspend fun emitResponse(id: Int, isDelete: Boolean = false)

    suspend fun emitSaveDraft()
}