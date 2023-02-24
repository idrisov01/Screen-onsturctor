package com.example.presentations.questionnaire.ui.viewholders

import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.R
import com.example.common.extension.orFalse
import com.example.common.extension.setOnSingleClickListener
import com.example.common.extension.toDp
import com.example.common.extension.uiLazy
import com.example.common.uikit.OffsetDecorator
import com.example.common.uikit.OffsetDecorator.Offsets
import com.example.databinding.ItemQuestionnaireWorkExperienceBinding
import com.example.presentations.questionnaire.domain.models.FieldTypes
import com.example.presentations.questionnaire.ui.adapters.WorkPlaceAdapter


class QuestionnaireWorkExperienceHolder(
    private val itemBinding: ItemQuestionnaireWorkExperienceBinding,
    private val addWorkPlace: (FieldTypes.WorkExperience) -> Unit,
    private val toDetailWorkExperience: (Int, Int) -> Unit,
    private val showError: (String?) -> Unit,
) : RecyclerView.ViewHolder(itemBinding.root) {

    private val subFieldAdapter by uiLazy {
        WorkPlaceAdapter { indexOfWorkPlace ->
            toDetailWorkExperience(bindingAdapterPosition, indexOfWorkPlace)
        }
    }

    init {
        with(itemBinding.workRecycler) {
            adapter = subFieldAdapter
            addItemDecoration(OffsetDecorator(Offsets(bottom = Offsets.space8.toDp)))
        }
    }

    fun bind(entity: FieldTypes.WorkExperience) {

        with(itemBinding) {

            setupVisibilities(entity)
            setupErrorField(entity)
            subFieldAdapter.submitList(entity.mySubfieldsValues)
            addWorkPlaceText.setOnSingleClickListener { addWorkPlace(entity) }
            addMorePlaceText.setOnSingleClickListener { addWorkPlace(entity) }
        }

    }

    private fun ItemQuestionnaireWorkExperienceBinding.setupVisibilities(entity: FieldTypes.WorkExperience) {

        addWorkPlaceText.isVisible = entity.mySubfieldsValues.isEmpty()
        upperDivider.isVisible = entity.hasUpperDivider
        lowerDivider.isVisible = entity.hasLowerDivider

        workRecycler.isVisible = entity.mySubfieldsValues.isNotEmpty()
        addMorePlaceText.isVisible = entity.mySubfieldsValues.isNotEmpty()
    }

    private fun ItemQuestionnaireWorkExperienceBinding.setupErrorField(entity: FieldTypes.WorkExperience) {

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