package com.example.presentations.questionnaire.di

import dagger.Component
import com.example.core.scopes.FragmentScope
import com.example.presentations.questionnaire.QuestionnaireDependencies
import com.example.presentations.questionnaire.ui.QuestionnaireFragment
import com.example.presentations.questionnaire.ui.SubFieldBottomSheet

@FragmentScope
@Component(
    modules = [QuestionnaireModule::class],
    dependencies = [QuestionnaireDependencies::class]
)
interface QuestionnaireComponent {

    @Component.Factory
    interface Factory {
        fun create(
            coreDependencies: QuestionnaireDependencies
        ): QuestionnaireComponent
    }

    fun inject(frag: QuestionnaireFragment)
    fun inject(frag: SubFieldBottomSheet)

}