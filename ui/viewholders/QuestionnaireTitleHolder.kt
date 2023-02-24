package com.example.presentations.questionnaire.ui.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.example.databinding.ItemQuestionnaireTitleBinding
import com.example.presentations.questionnaire.domain.models.FieldTypes


class QuestionnaireTitleHolder(
    private val itemBinding: ItemQuestionnaireTitleBinding
) : RecyclerView.ViewHolder(itemBinding.root) {

    fun bind(entity: FieldTypes.Title) {
            itemBinding.titleTextView.text = entity.title
    }
}