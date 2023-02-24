package com.example.presentations.questionnaire.ui.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.example.databinding.ItemQuestionnaireCheckboxBinding
import com.example.presentations.questionnaire.domain.models.FieldTypes


class QuestionnaireCheckboxHolder(
    private val itemBinding: ItemQuestionnaireCheckboxBinding,
    private val isChecked: (FieldTypes.Checkbox) -> Unit
) : RecyclerView.ViewHolder(itemBinding.root) {

    fun bind(entity: FieldTypes.Checkbox) {
        with(itemBinding.root) {
            text = entity.hint
            setOnCheckedChangeListener { _, isChecked ->
                isChecked(entity.copy(isChecked = isChecked))
            }
            isChecked = entity.isChecked
        }
    }
}