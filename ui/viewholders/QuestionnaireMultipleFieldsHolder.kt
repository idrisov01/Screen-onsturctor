package com.example.presentations.questionnaire.ui.viewholders

import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.R
import com.example.common.extension.isVisibleTextAsterisk
import com.example.common.extension.orFalse
import com.example.common.extension.toDp
import com.example.common.extension.uiLazy
import com.example.common.uikit.OffsetDecorator
import com.example.databinding.ItemQuestionnaireMultipleFieldsBinding
import com.example.presentations.questionnaire.domain.models.FieldTypes
import com.example.presentations.questionnaire.ui.adapters.SubFieldAdapter


class QuestionnaireMultipleFieldsHolder(
    private val itemBinding: ItemQuestionnaireMultipleFieldsBinding,
    private val addSubField: (FieldTypes.MultipleFields) -> Unit,
    private val toDetailSubFields: (Int, Int) -> Unit,
    private val showError: (String?) -> Unit,
) : RecyclerView.ViewHolder(itemBinding.root) {

    private val subFieldAdapter by uiLazy {
        SubFieldAdapter { indexOfSunField ->
            toDetailSubFields(bindingAdapterPosition, indexOfSunField)
        }
    }

    init {
        with(itemBinding) {
            subFieldsRecycler.adapter = subFieldAdapter
            subFieldsRecycler.addItemDecoration(
                OffsetDecorator(
                    OffsetDecorator.Offsets(bottom = OffsetDecorator.Offsets.space8.toDp)
                )
            )
        }
    }

    fun bind(entity: FieldTypes.MultipleFields) {

        with(itemBinding) {

            titleTextView.text = entity.title
            titleTextView.isVisibleTextAsterisk(entity.isRequired)

            addSubFieldTextView.text = entity.hint

            subFieldAdapter.submitList(entity.mySubfieldsValues)

            addSubFieldTextView.setOnClickListener { addSubField(entity) }
            addMoreText.setOnClickListener { addSubField(entity) }

            setupVisibilities(entity)
            setupErrorField(entity)
        }
    }

    private fun ItemQuestionnaireMultipleFieldsBinding.setupVisibilities(entity: FieldTypes.MultipleFields) {

        addSubFieldTextView.isVisible = entity.mySubfieldsValues.isEmpty()
        upperDivider.isVisible = entity.hasUpperDivider
        lowerDivider.isVisible = entity.hasLowerDivider

        subFieldsRecycler.isVisible = entity.mySubfieldsValues.isNotEmpty()
        addMoreText.isVisible = entity.mySubfieldsValues.isNotEmpty()
    }

    private fun ItemQuestionnaireMultipleFieldsBinding.setupErrorField(entity: FieldTypes.MultipleFields) {

        errorContainer.isVisible = entity.fieldIsEmpty.orFalse()
        errorContainer.isClickable = entity.description != null
        errorContainer.setOnClickListener { showError(entity.description) }

        if (entity.fieldIsEmpty.orFalse()) {
            errorTextView.text = errorTextView.context
                .getString(R.string.questionnaire_screen_error_required_field_empty)
        }

        errorImageView.isVisible = entity.description != null

        cardVew.strokeColor = ContextCompat.getColor(
            cardVew.context,
            if (entity.fieldIsEmpty.orFalse()) R.color.red else R.color.secondary
        )
    }
}