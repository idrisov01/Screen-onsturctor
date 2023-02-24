package com.example.presentations.questionnaire.ui.viewholders

import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.R
import com.example.common.extension.isVisibleTextAsterisk
import com.example.common.extension.makeGone
import com.example.common.extension.makeVisible
import com.example.common.extension.orFalse
import com.example.databinding.ItemQuestionnaireListBinding
import com.example.presentations.questionnaire.domain.models.FieldTypes


class QuestionnaireMultipleHolder(
    private val itemBinding: ItemQuestionnaireListBinding,
    private val showList: (FieldTypes.MultipleSelect) -> Unit,
    private val showError: (String?) -> Unit,
) : RecyclerView.ViewHolder(itemBinding.root) {

    fun bind(entity: FieldTypes.MultipleSelect) {

        with(itemBinding) {
            if (entity.myValues.isNullOrEmpty()) setupHint(entity) else setupText(entity)

            setupErrorField(entity)

            root.setOnClickListener { showList(entity) }
        }

    }

    private fun ItemQuestionnaireListBinding.setupHint(entity: FieldTypes.MultipleSelect) {

        hint.makeGone()

        title.text = entity.hint
        title.isVisibleTextAsterisk(entity.isRequired)
        title.setTextColor(ContextCompat.getColor(root.context, R.color.gray))
    }

    private fun ItemQuestionnaireListBinding.setupText(entity: FieldTypes.MultipleSelect) {

        hint.makeVisible()
        hint.text = entity.hint
        hint.isVisibleTextAsterisk(entity.isRequired)

        title.text = entity.myValues?.joinToString()
        title.setTextColor(ContextCompat.getColor(root.context, R.color.dark))
    }

    private fun ItemQuestionnaireListBinding.setupErrorField(entity: FieldTypes.MultipleSelect) {

        errorContainer.isVisible = entity.fieldIsEmpty.orFalse()
        errorContainer.isClickable = entity.description != null
        errorContainer.setOnClickListener { showError(entity.description) }

        errorTextView.text =
            errorTextView.context.getString(R.string.questionnaire_screen_error_required_field_empty)

        errorImageView.isVisible = entity.description != null

        cardVew.strokeColor = ContextCompat.getColor(
            cardVew.context,
            if (entity.fieldIsEmpty.orFalse()) R.color.red else R.color.secondary
        )
    }
}