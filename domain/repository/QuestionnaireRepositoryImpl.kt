package com.example.presentations.questionnaire.domain.repository

import com.example.base.BaseMapper
import com.example.common.clientmanager.ClientManager
import com.example.common.flow.response.ResponseEntity
import com.example.common.flow.response.ResponseFlow
import com.example.common.flow.savedraft.SaveDraftFlow
import com.example.core.network.RestApi
import com.example.presentations.questionnaire.data.dto.QuestionnaireDto
import com.example.presentations.questionnaire.domain.entity.QuestionnaireEntity
import javax.inject.Inject


class QuestionnaireRepositoryImpl @Inject constructor(
    private val api: RestApi,
    private val questionnaireMapper: BaseMapper<QuestionnaireDto, QuestionnaireEntity>,
    private val clientManager: ClientManager,
    private val responseFlow: ResponseFlow,
    private val saveDraftFlow: SaveDraftFlow,
) : QuestionnaireRepository {

    override suspend fun loadQualificationForm(vacancyId: Int): QuestionnaireEntity {
        return questionnaireMapper.map(api.getQualificationForm(vacancyId))
    }

    override suspend fun loadExtendedForm(vacancyId: Int): QuestionnaireEntity {
        return questionnaireMapper.map(api.getExtendedForm(vacancyId))
    }

    override suspend fun uploadQuestionnaire(vacancyId: Int) {
        api.uploadQuestionnaire(vacancyId)
    }

    override suspend fun uploadDraft(vacancyId: Int, requestBody: Map<String, String>) {
        api.uploadDraft(vacancyId, requestBody)
    }

    override suspend fun removeDraft(vacancyId: Int) {
        api.removeDraft(vacancyId)
    }

    override suspend fun getUserPhone(): String? {
        return clientManager.getPhone()
    }

    override suspend fun emitResponse(id: Int, isDelete: Boolean) {
        responseFlow.emitEvent(ResponseEntity(id, isDelete))
    }

    override suspend fun emitSaveDraft() {
        saveDraftFlow.emitEvent()
    }
}