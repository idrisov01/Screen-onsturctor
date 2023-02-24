package com.example.presentations.questionnaire.ui.viewholders

import android.text.InputType
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.R
import com.example.common.extension.addTextListener
import com.example.common.extension.hideKeyboard
import com.example.common.extension.isVisibleHintAsterisk
import com.example.common.extension.removeTextListeners
import com.example.databinding.ItemQuestionnaireTextBinding
import com.example.presentations.questionnaire.domain.models.FieldTypes
import com.example.presentations.questionnaire.domain.models.TextInputTypes
import ru.tinkoff.decoro.MaskImpl
import ru.tinkoff.decoro.watchers.MaskFormatWatcher


class QuestionnaireTextHolder(
    private val itemBinding: ItemQuestionnaireTextBinding,
    private val enteredData: (FieldTypes.Text, Boolean) -> Unit,
    private val showError: (String?) -> Unit,
) : RecyclerView.ViewHolder(itemBinding.root) {

    fun bind(entity: FieldTypes.Text) {

        with(itemBinding) {

            textInputLayout.hint = entity.hint
            textInputLayout.isVisibleHintAsterisk(entity.isRequired)

            setupListeners(entity)
            setupInputField(entity)
            setupErrorField(entity)
        }
    }

    private fun ItemQuestionnaireTextBinding.setupListeners(entity: FieldTypes.Text) {

        textInputEditText.setOnEditorActionListener { view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                view.clearFocus()
                view.context.hideKeyboard(view)
            }
            true
        }

        textInputEditText.setOnFocusChangeListener { view, isFocused ->
            enteredData(
                entity.copy(enteredText = (view as EditText).text.toString()),
                isFocused
            )
        }
    }

    private fun ItemQuestionnaireTextBinding.setupInputField(entity: FieldTypes.Text) {

        textInputEditText.inputType = when (entity.inputType) {
            TextInputTypes.NUMBER -> InputType.TYPE_CLASS_PHONE
            TextInputTypes.NAME -> InputType.TYPE_TEXT_FLAG_CAP_WORDS
            else -> InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        }

        textInputEditText.isSingleLine = false
        textInputEditText.imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION

        val isUserPhone = entity.inputType == TextInputTypes.PHONE

        textInputLayout.isEnabled = !isUserPhone

        textInputEditText.setTextColor(
            textInputEditText.context.getColor(if (isUserPhone) R.color.gray else R.color.dark)
        )

        textInputEditText.removeTextListeners()

        if (entity.mask != null) {
            val mask = MaskImpl.createTerminated(entity.mask.toTypedArray())
                .apply {
                    isHideHardcodedHead = true
                }
            textInputEditText.addTextListener(MaskFormatWatcher(mask))
        }

        textInputEditText.setText(entity.myValues?.firstOrNull())

        if (entity.inputType == TextInputTypes.TEXT) {
            textInputEditText.isSingleLine = false
        }

    }

    private fun ItemQuestionnaireTextBinding.setupErrorField(entity: FieldTypes.Text) {

        val isRequiredFieldIsEmpty = entity.isRequired && entity.fieldIsEmpty == true
        val isIncorrectData = entity.isValidData == false

        val isError = isRequiredFieldIsEmpty || isIncorrectData

        errorContainer.isVisible = isError
        errorContainer.isClickable = entity.description != null
        errorContainer.setOnClickListener { showError(entity.description) }

        errorTextView.text = with(errorTextView.context) {
            when {
                entity.fieldIsEmpty == true -> {
                    getString(R.string.questionnaire_screen_error_required_field_empty)
                }
                entity.isValidData == false -> {
                    getString(R.string.questionnaire_screen_error_invalid_field)
                }
                else -> null
            }
        }

        errorImageView.isVisible = entity.description != null

        cardVew.strokeColor = ContextCompat.getColor(
            cardVew.context,
            if (isError) {
                R.color.red
            } else {
                R.color.secondary
            }
        )
    }
}