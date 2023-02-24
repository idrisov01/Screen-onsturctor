package com.example.presentations.questionnaire.ui.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.example.common.extension.checkButtonState
import com.example.databinding.ItemQuestionnaireSendBinding
import com.example.presentations.questionnaire.domain.models.FieldTypes


class QuestionnaireSendHolder(
    private val itemBinding: ItemQuestionnaireSendBinding,
    private val sendQuestionnaire: () -> Unit,
) : RecyclerView.ViewHolder(itemBinding.root) {

    init {
        itemBinding.sendForVerificationButton.setOnClickListener { sendQuestionnaire() }
    }

    fun bind(entity: FieldTypes.Send) {
        itemBinding.sendForVerificationButton.checkButtonState(entity.isEnable)
    }
}