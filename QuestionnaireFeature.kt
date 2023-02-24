package com.example.presentations.questionnaire

import com.example.App
import com.example.base.BaseComponent
import com.example.common.clientmanager.ClientManager
import com.example.common.flow.response.ResponseFlow
import com.example.common.flow.savedraft.SaveDraftFlow
import com.example.core.network.NetworkWrapper
import com.example.core.network.RestApi
import com.example.presentations.questionnaire.di.DaggerQuestionnaireComponent
import com.example.presentations.questionnaire.di.QuestionnaireComponent
import javax.inject.Inject


object QuestionnaireFeature {

    private var component: QuestionnaireComponent? = null

    fun getComponent(): QuestionnaireComponent {

        return component ?: run {
            component = DaggerQuestionnaireComponent.factory()
                .create(
                    coreDependencies = QuestionnaireDependenciesDelegate(
                        baseComponent = App.app.baseComponent
                    )
                )
            requireNotNull(component)
        }
    }

    fun destroyModuleGraph() {
        component = null
    }

}

interface QuestionnaireDependencies {
    fun provideApi(): RestApi
    fun provideClientManager(): ClientManager
    fun provideResponseFlow(): ResponseFlow
    fun provideDraftFlow(): SaveDraftFlow
}

class QuestionnaireDependenciesDelegate @Inject constructor(
    private val baseComponent: BaseComponent
) : QuestionnaireDependencies {

    override fun provideApi(): RestApi {
        return NetworkWrapper.getApi().provideApiClass()
    }

    override fun provideClientManager(): ClientManager {
        return baseComponent.provideClientManager()
    }

    override fun provideResponseFlow(): ResponseFlow {
        return baseComponent.provideResponseFlow()
    }

    override fun provideDraftFlow(): SaveDraftFlow {
        return baseComponent.provideDraftFlow()
    }
}