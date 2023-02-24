package com.example.presentations.questionnaire.ui

import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.R
import com.example.base.BaseMviFragment
import com.example.common.extension.hideKeyboard
import com.example.common.extension.orFalse
import com.example.common.extension.orZero
import com.example.common.extension.parcelable
import com.example.common.extension.showDialog
import com.example.common.extension.showErrorDialog
import com.example.common.extension.showNetworkErrorDialog
import com.example.common.extension.toDp
import com.example.common.extension.uiLazy
import com.example.common.extension.viewModels
import com.example.common.extension.withArgs
import com.example.common.uikit.DividerItemDecorator
import com.example.common.uikit.OffsetDecorator
import com.example.common.uikit.OffsetDecorator.Offsets
import com.example.databinding.FragmentDialogQuestionnaireSpinnerBinding
import com.example.databinding.FragmentQuestionnaireBinding
import com.example.presentations.questionnaire.QuestionnaireFeature
import com.example.presentations.questionnaire.domain.models.FieldActions
import com.example.presentations.questionnaire.domain.models.FieldTypes
import com.example.presentations.questionnaire.domain.models.FieldTypes.MultipleFields.Companion.mapToWorkExperience
import com.example.presentations.questionnaire.domain.models.FieldTypes.WorkExperience.Companion.mapToMultipleFields
import com.example.presentations.questionnaire.domain.models.QuestionnaireType
import com.example.presentations.questionnaire.ui.adapters.QuestionnaireAdapter
import com.example.presentations.questionnaire.ui.adapters.SpinnerAdapter
import com.example.presentations.questionnaire.vm.QuestionnaireEvent
import com.example.presentations.questionnaire.vm.QuestionnaireState
import com.example.presentations.questionnaire.vm.QuestionnaireViewModel
import javax.inject.Inject


class QuestionnaireFragment @Inject constructor() :
    BaseMviFragment<QuestionnaireState, QuestionnaireEvent, QuestionnaireViewModel>(
        layoutResId = R.layout.fragment_questionnaire
    ) {

    companion object {

        fun newInstance(
            vacancyId: Int,
            isModifyQuestionnaire: Boolean,
            questionnaireType: QuestionnaireType,
        ): QuestionnaireFragment {
            return QuestionnaireFragment().withArgs {
                putInt(ARG_VACANCY_ID, vacancyId)
                putBoolean(ARG_IS_MODIFY_QUESTIONNAIRE, isModifyQuestionnaire)
                putParcelable(ARG_QUESTIONNAIRE_TYPE, questionnaireType)
            }
        }

        private const val ARG_VACANCY_ID = "VACANCY_ID"
        private const val ARG_IS_MODIFY_QUESTIONNAIRE = "IS_MODIFY_QUESTIONNAIRE"
        private const val ARG_QUESTIONNAIRE_TYPE = "QUESTIONNAIRE_TYPE"
    }

    @Inject lateinit var viewModelFactory: QuestionnaireViewModel.Factory

    private val viewBinding by viewBinding(FragmentQuestionnaireBinding::class.java)
    override val viewModel by viewModels {
        viewModelFactory.create(tabRouter, vacancyId, questionnaireType)
    }

    private val vacancyId by uiLazy {
        arguments?.getInt(ARG_VACANCY_ID).orZero()
    }

    private val isModifyQuestionnaire by uiLazy {
        arguments?.getBoolean(ARG_IS_MODIFY_QUESTIONNAIRE).orFalse()
    }

    private val questionnaireType by uiLazy {
        arguments?.parcelable(ARG_QUESTIONNAIRE_TYPE) as QuestionnaireType?
            ?: QuestionnaireType.EXTENDED_FORM
    }

    private val questionnaireAdapter by uiLazy {
        QuestionnaireAdapter(
            fieldAction = { action -> handleFieldActions(action) },
            showError = { message -> renderError(message) }
        )
    }

    private val spinnerAdapter by uiLazy {
        SpinnerAdapter { fieldId, value ->

            spinnerDialog?.dismiss()

            val spinnerField = questionnaireAdapter.currentList
                .filterIsInstance<FieldTypes.Spinner>()
                .firstOrNull { field -> field.fieldId == fieldId }
                ?.copy(myValues = listOf(value))
                ?: return@SpinnerAdapter

            viewModel.perform(QuestionnaireEvent.FieldAction(FieldActions.Spinner(spinnerField)))
        }
    }

    private var spinnerDialog: AlertDialog? = null
    private var isNeedScrollToInvalidField = true

    override fun setupInjection() {
        QuestionnaireFeature.getComponent().inject(this)
    }

    override fun setupUi() {

        attachKeyboardObserver()

        with(viewBinding) {

            titleTextView.text = getString(
                when (questionnaireType) {
                    QuestionnaireType.QUALIFICATION_FORM -> R.string.questionnaire_screen_qualification_toolbar_title
                    QuestionnaireType.EXTENDED_FORM -> R.string.questionnaire_screen_extended_toolbar_title
                }
            )

            deleteQuestionnaireImage.isVisible = !isModifyQuestionnaire &&
                    questionnaireType == QuestionnaireType.QUALIFICATION_FORM

            arrowBackImage.setOnClickListener { onBackPressed() }

            deleteQuestionnaireImage.setOnClickListener { showRemoveDraftDialog() }

            questionnaireRecycler.apply {
                itemAnimator = null
                addItemDecoration(OffsetDecorator(Offsets(bottom = Offsets.space8.toDp), true))
                adapter = questionnaireAdapter
            }
        }
    }

    override fun onShowKeyboard() {
        showNavigationMenu(false)
    }

    override fun onHideKeyboard() {
        showNavigationMenu(true)
        activity?.currentFocus?.clearFocus()
    }

    override fun render(state: QuestionnaireState) {
        renderUiState(state.isLoading, state.canDelete)
        renderFields(state.fields)
        renderNetworkError(state.isNetworkError)
        renderError(state.serverMessage)
        scrollToInvalidPosition(state.invalidFieldIndex)
    }

    private fun renderFields(fields: List<FieldTypes>?) {
        questionnaireAdapter.submitList(fields)
    }

    private fun renderUiState(isLoading: Boolean, canDelete: Boolean) {
        with(viewBinding) {
            deleteQuestionnaireImage.isVisible = canDelete
            progressBar.root.isVisible = isLoading
        }
    }

    private fun renderNetworkError(isNetworkError: Boolean) {
        if (isNetworkError) {
            showNetworkErrorDialog { viewModel.perform(QuestionnaireEvent.RepeatRequest) }
        }
    }

    private fun renderError(message: String?) {
        if (message != null) showErrorDialog(message)
    }

    private fun showRemoveDraftDialog() {
        showDialog(
            title = getString(R.string.questionnaire_screen_dialog_remove_draft_title),
            description = getString(R.string.questionnaire_screen_dialog_remove_draft_description),
            positiveBtnText = getString(R.string.questionnaire_screen_dialog_remove_draft_positive_button),
            negativeBtnText = getString(R.string.questionnaire_screen_dialog_remove_draft_negative_button),
            negativeBtnAction = { viewModel.perform(QuestionnaireEvent.RemoveDraft) }
        )
    }

    private fun handleFieldActions(action: FieldActions) {

        if (action !is FieldActions.Text) hideKeyboard()

        when (action) {
            is FieldActions.MultipleSelect -> showMultipleSelectorDialog(action.field)
            is FieldActions.SingleSelect -> showSingleSelectorDialog(action.field)
            is FieldActions.Spinner -> showSpinnerDialog(action.field)
            is FieldActions.Text -> handleTextFieldAction(action)
            is FieldActions.Checkbox -> viewModel.perform(QuestionnaireEvent.FieldAction(action))
            is FieldActions.File -> viewModel.perform(QuestionnaireEvent.FieldAction(action))
            is FieldActions.Send -> {
                isNeedScrollToInvalidField = true
                viewModel.perform(QuestionnaireEvent.SendQuestionnaire)
            }
            is FieldActions.SubField -> {
                questionnaireAdapter.currentList.filterIsInstance<FieldTypes.MultipleFields>()
                    .firstOrNull { field -> field.fieldId == action.field.fieldId }
                    ?.let { field -> showSubFieldDialog(field) }
            }
            is FieldActions.SubFieldDetail -> {
                questionnaireAdapter.currentList
                    .filterIsInstance<FieldTypes.MultipleFields>()
                    .firstOrNull { field -> field.fieldId == action.field.fieldId }
                    ?.let { field -> showSubFieldDialog(field = field, index = action.index) }
            }
            is FieldActions.WorkExperience -> {
                questionnaireAdapter.currentList
                    .filterIsInstance<FieldTypes.WorkExperience>()
                    .firstOrNull { field -> field.fieldId == action.field.fieldId }
                    ?.let { field -> showWorkExperienceDialog(field) }
            }
            is FieldActions.WorkExperienceDetail -> {
                questionnaireAdapter.currentList
                    .filterIsInstance<FieldTypes.WorkExperience>()
                    .firstOrNull { field -> field.fieldId == action.field.fieldId }
                    ?.let { field -> showWorkExperienceDialog(field, action.index) }
            }
        }
    }

    private fun handleTextFieldAction(text: FieldActions.Text) {
        if (!text.isFocused) {
            viewModel.perform(QuestionnaireEvent.FieldAction(text))
        }
    }

    private fun showSpinnerDialog(spinner: FieldTypes.Spinner) {

        if (spinner.values == null) return

        val dialogItemBinding = FragmentDialogQuestionnaireSpinnerBinding.inflate(layoutInflater)

        spinnerDialog = MaterialAlertDialogBuilder(
            requireContext(), R.style.MaterialAlertDialog_Rounded
        ).apply {
            spinnerAdapter.fieldId = spinner.fieldId
            spinnerAdapter.submitList(spinner.values)
            setView(dialogItemBinding.root)
        }.show()

        with(dialogItemBinding) {

            titleTextView.text = spinner.title
            recyclerView.adapter = spinnerAdapter
            recyclerView.addItemDecoration(
                DividerItemDecorator(
                    ContextCompat.getDrawable(requireContext(), R.drawable.background_divider)
                )
            )
        }
    }

    private fun scrollToInvalidPosition(invalidPosition: Int?) {

        if (invalidPosition == null) return
        if (!isNeedScrollToInvalidField) return

        with(viewBinding) {
            isNeedScrollToInvalidField = false
            questionnaireRecycler.smoothScrollToPosition(invalidPosition)
        }
    }

    private fun showSingleSelectorDialog(field: FieldTypes.SingleSelect) {
        SelectorBottomSheetDialog.show(childFragmentManager, field) { updatedField ->
            viewModel.perform(
                QuestionnaireEvent.FieldAction(FieldActions.SingleSelect(updatedField))
            )
        }
    }

    private fun showMultipleSelectorDialog(field: FieldTypes.MultipleSelect) {

        SelectorBottomSheetDialog.show(childFragmentManager, field) { updatedField ->
            viewModel.perform(
                QuestionnaireEvent.FieldAction(FieldActions.MultipleSelect(updatedField))
            )
        }
    }

    private fun showSubFieldDialog(field: FieldTypes.MultipleFields, index: Int? = null) {
        SubFieldBottomSheet.show(childFragmentManager, field, index) { newSubField ->
            viewModel.perform(
                QuestionnaireEvent.FieldAction(FieldActions.SubField(newSubField))
            )
        }
    }

    private fun showWorkExperienceDialog(field: FieldTypes.WorkExperience, index: Int? = null) {
        SubFieldBottomSheet.show(
            childFragmentManager,
            field.mapToMultipleFields(),
            index
        ) { newMultipleFields ->
            viewModel.perform(
                QuestionnaireEvent.FieldAction(
                    FieldActions.WorkExperience(newMultipleFields.mapToWorkExperience())
                )
            )
        }
    }

    override fun onDestroyView() {
        detachKeyBoardObserver()
        super.onDestroyView()
    }

    override fun onBackPressed(): Boolean {
        activity?.currentFocus?.clearFocus()
        isNeedScrollToInvalidField = true
        viewModel.perform(QuestionnaireEvent.OnBackPressed)
        return true
    }
}