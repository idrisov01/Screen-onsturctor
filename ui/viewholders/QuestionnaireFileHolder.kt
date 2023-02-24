package com.example.presentations.questionnaire.ui.viewholders

import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.R
import com.example.common.extension.isVisibleTextAsterisk
import com.example.common.extension.orZero
import com.example.databinding.ItemQuestionnaireFileBinding
import com.example.presentations.questionnaire.domain.models.FieldTypes


class QuestionnaireFileHolder(
    private val itemBinding: ItemQuestionnaireFileBinding,
    private val addDocument: (FieldTypes.File) -> Unit,
    private val showError: (String?) -> Unit,
) : RecyclerView.ViewHolder(itemBinding.root) {


    fun bind(entity: FieldTypes.File) {

        with(itemBinding) {

            title.text = entity.title
            title.isVisibleTextAsterisk(entity.isRequired)
            counterTextView.text = entity.myFiles?.filesCount.toString()

            plusImage.isVisible = entity.myFiles?.filesCount == 0
            arrowForwardImage.isVisible = entity.myFiles?.filesCount.orZero() > 0

            counterCardView.isVisible = entity.myFiles?.filesCount.orZero() > 0

            root.setOnClickListener { addDocument(entity) }

            setupErrorField(entity)
        }
    }

    private fun ItemQuestionnaireFileBinding.setupErrorField(entity: FieldTypes.File) {

        val isError = entity.isRequired && entity.fieldIsEmpty == true
                && entity.myFiles?.filesCount == 0

        errorContainer.isVisible = isError
        errorContainer.isClickable = entity.description != null
        errorContainer.setOnClickListener { showError(entity.description) }

        errorTextView.text =
            errorTextView.context.getString(R.string.questionnaire_screen_error_required_field_empty)
        errorImageView.isVisible = entity.description != null

        cardVew.strokeColor = ContextCompat.getColor(
            cardVew.context,
            if (isError) R.color.red else R.color.secondary
        )
    }

}