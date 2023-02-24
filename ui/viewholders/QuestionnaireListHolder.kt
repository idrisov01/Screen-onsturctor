package com.example.presentations.questionnaire.ui.viewholders

import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.R
import com.example.common.extension.isVisibleTextAsterisk
import com.example.common.extension.makeGone
import com.example.common.extension.makeVisible
import com.example.common.extension.orFalse
import com.example.common.extension.setOnSingleClickListener
import com.example.databinding.ItemQuestionnaireListBinding
import com.example.presentations.questionnaire.domain.models.FieldTypes


class QuestionnaireListHolder(
    private val itemBinding: ItemQuestionnaireListBinding,
    private val showList: (FieldTypes.Spinner) -> Unit,
    private val showError: (String?) -> Unit
) : RecyclerView.ViewHolder(itemBinding.root) {

    fun bind(entity: FieldTypes.Spinner) {

        with(itemBinding) {

            if (entity.myValues.isNullOrEmpty()) setupHint(entity) else setupText(entity)

            setupErrorField(entity)

            root.setOnSingleClickListener { showList(entity) }

        }

    }

    private fun ItemQuestionnaireListBinding.setupHint(entity: FieldTypes.Spinner) {

        hint.makeGone()

        title.text = entity.hint
        title.isVisibleTextAsterisk(entity.isRequired)
        title.setTextColor(ContextCompat.getColor(root.context, R.color.gray))
    }

    private fun ItemQuestionnaireListBinding.setupText(entity: FieldTypes.Spinner) {

        hint.makeVisible()
        hint.text = entity.hint
        hint.isVisibleTextAsterisk(entity.isRequired)

        title.text = entity.myValues?.firstOrNull()
        title.setTextColor(ContextCompat.getColor(root.context, R.color.dark))
    }

    private fun ItemQuestionnaireListBinding.setupErrorField(entity: FieldTypes.Spinner) {

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