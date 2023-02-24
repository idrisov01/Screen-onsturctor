package com.example.presentations.questionnaire.ui

import android.content.DialogInterface
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.R
import com.example.common.extension.asBoolean
import com.example.common.extension.asString
import com.example.common.extension.doWithoutAnimation
import com.example.common.extension.indexOfFirstOrNull
import com.example.common.extension.orFalse
import com.example.common.extension.orZero
import com.example.common.extension.parcelable
import com.example.common.extension.replaceByIndex
import com.example.common.extension.showErrorDialog
import com.example.common.extension.toDp
import com.example.common.extension.uiLazy
import com.example.common.extension.withArgs
import com.example.common.uikit.DividerItemDecorator
import com.example.common.uikit.OffsetDecorator
import com.example.common.uikit.OffsetDecorator.Offsets
import com.example.databinding.FragmentDialogQuestionnaireSpinnerBinding
import com.example.databinding.FragmentDialogQuestionnaireSubfieldBinding
import com.example.presentations.questionnaire.QuestionnaireFeature
import com.example.presentations.questionnaire.domain.entity.SubFieldEntity
import com.example.presentations.questionnaire.domain.fieldvalidator.FieldValidator
import com.example.presentations.questionnaire.domain.models.FieldActions
import com.example.presentations.questionnaire.domain.models.FieldTypes
import com.example.presentations.questionnaire.domain.models.FieldTypes.Companion.code
import com.example.presentations.questionnaire.domain.models.FieldTypes.Companion.fieldId
import com.example.presentations.questionnaire.domain.models.FieldTypes.Companion.fieldIsEmpty
import com.example.presentations.questionnaire.domain.models.FieldTypes.Companion.isRequired
import com.example.presentations.questionnaire.domain.models.FieldTypes.Companion.isValidData
import com.example.presentations.questionnaire.domain.models.FieldTypes.Companion.myValue
import com.example.presentations.questionnaire.ui.adapters.QuestionnaireAdapter
import com.example.presentations.questionnaire.ui.adapters.SpinnerAdapter
import javax.inject.Inject


class SubFieldBottomSheet(
    private val newSubField: (FieldTypes.MultipleFields) -> Unit,
) : BottomSheetDialogFragment() {

    companion object {

        fun show(
            fm: FragmentManager,
            subField: FieldTypes.MultipleFields?,
            indexSubField: Int? = null,
            newSubField: (FieldTypes.MultipleFields) -> Unit,
        ) {
            SubFieldBottomSheet(newSubField).withArgs {
                putParcelable(ARG_FIELD, subField)
                if (indexSubField != null) putInt(ARG_SUB_FIELD_INDEX, indexSubField)
            }.show(fm, SubFieldBottomSheet::class.simpleName)
        }

        private const val ARG_FIELD = "FIELD"
        private const val ARG_SUB_FIELD_INDEX = "SUB_FIELD_INDEX"
    }

    @Inject lateinit var fieldValidator: FieldValidator

    private var _binding: FragmentDialogQuestionnaireSubfieldBinding? = null
    private val viewBinding
        get() = _binding!!

    private var keyboardListenersAttached = false
    private var spinnerDialog: AlertDialog? = null

    private val keyboardLayoutObserver by uiLazy {
        ViewTreeObserver.OnGlobalLayoutListener {

            val r = Rect()
            viewBinding.rootLayout.getWindowVisibleDisplayFrame(r)

            val screenHeight = viewBinding.rootLayout.rootView?.height.orZero()
            val keypadHeight = screenHeight - r.bottom

            val isShowKeyboard = keypadHeight > screenHeight * 0.15

            if (keyboardListenersAttached && !isShowKeyboard) {
                viewBinding.recyclerView.clearFocus()
            }
        }
    }

    private val spinnerAdapter by uiLazy {
        SpinnerAdapter { fieldId, value ->

            spinnerDialog?.dismiss()

            val spinnerField = subFieldAdapter.currentList
                .filterIsInstance<FieldTypes.Spinner>()
                .firstOrNull { field -> field.fieldId == fieldId }
                ?.copy(myValues = listOf(value))
                ?: return@SpinnerAdapter

            handleSpinnerField(spinnerField)
        }
    }

    private val subField by uiLazy {
        arguments?.parcelable(ARG_FIELD) as FieldTypes.MultipleFields?
    }

    private val checkBoxField by uiLazy {
        subField?.subFields?.filterIsInstance<FieldTypes.Checkbox>()?.firstOrNull()
    }

    private val subFieldAdapter by uiLazy {
        QuestionnaireAdapter(
            fieldAction = { fieldActions -> handleFieldActions(fieldActions) },
            showError = { message -> if (message != null) showErrorDialog(message) }
        )
    }

    private val indexOfSubField by uiLazy {
        requireArguments().getInt(ARG_SUB_FIELD_INDEX, -1)
    }

    private val mySubFieldsValues by uiLazy {
        subField?.mySubfieldsValues?.getOrNull(indexOfSubField).orEmpty().toMutableList()
    }

    private val subFields by uiLazy {
        subField?.subFields?.toMutableList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        QuestionnaireFeature.getComponent().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDialogQuestionnaireSubfieldBinding.inflate(inflater)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setDialog()
        if (indexOfSubField != -1) setupSubField()
        setupUi()
    }

    override fun onDismiss(dialog: DialogInterface) {
        detachKeyBoardObserver()
        super.onDismiss(dialog)
    }

    private fun setDialog() {

        val bottomSheet = (dialog as? BottomSheetDialog)
            ?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

        if (bottomSheet is FrameLayout) {
            bottomSheet.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT

            BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
        }

        attachKeyboardObserver()
    }

    private fun setupUi() {

        with(viewBinding) {

            titleTextView.text = subField?.title

            infoTextView.isVisible = subField?.description?.isNotEmpty().orFalse()
            infoTextView.text = subField?.description

            recyclerView.adapter = subFieldAdapter
            recyclerView.addItemDecoration(OffsetDecorator(Offsets(bottom = Offsets.space8.toDp)))

            subFieldAdapter.submitList(subFields) { updateButtonState() }

            closeImageView.setOnClickListener { this@SubFieldBottomSheet.dismiss() }
            applyButton.setOnClickListener { allFieldIsValid() }
        }
    }

    private fun setupSubField() {

        subField?.subFields?.forEachIndexed { index, subField ->
            mySubFieldsValues.forEach { mySubField ->

                if (mySubField.fieldName == subField.fieldId) {
                    subFields?.removeAt(index)
                    subFields?.add(
                        index = index,
                        element = when (subField) {
                            is FieldTypes.Text -> subField.copy(
                                myValues = mySubField.values,
                                fieldIsEmpty = mySubField.values.isNullOrEmpty()
                            )
                            is FieldTypes.Checkbox -> subField.copy(
                                isChecked = mySubField.values?.first().asBoolean())
                            is FieldTypes.MultipleSelect -> subField.copy(
                                myValues = mySubField.values
                            )
                            is FieldTypes.SingleSelect -> subField.copy(
                                myValues = mySubField.values
                            )
                            is FieldTypes.Spinner -> subField.copy(
                                myValues = mySubField.values
                            )
                            else -> subField
                        }
                    )
                }
            }
        }
    }

    private fun handleFieldActions(action: FieldActions) {

        when (action) {
            is FieldActions.Checkbox -> handleCheckBoxAction(action)
            is FieldActions.Text -> if (!action.isFocused) handleTextAction(action)
            is FieldActions.MultipleSelect -> showMultipleSelectorDialog(action.field)
            is FieldActions.SingleSelect -> showSingleSelectorDialog(action.field)
            is FieldActions.Spinner -> showSpinnerDialog(action.field)
            else -> Unit
        }
    }

    private fun handleCheckBoxAction(action: FieldActions.Checkbox) {

        if (action.field.isChecked) {
            hideFieldByKey(checkBoxField?.hiddenKey)
        } else {
            val allFields = subFields?.map { sf ->
                subFieldAdapter.currentList.firstOrNull { f -> f.fieldId == sf.fieldId } ?: sf
            }
            subFieldAdapter.submitList(allFields) { updateButtonState() }
        }

        val newList = mySubFieldsValues.toMutableList()

        newList.indexOfFirstOrNull { field -> field.fieldName == action.field.fieldId }
            ?.let { index ->
                val newItem = mySubFieldsValues.getOrNull(index) ?: return
                mySubFieldsValues.removeAt(index)
                mySubFieldsValues.add(
                    index,
                    newItem.copy(values = listOf(action.field.isChecked.asString()))
                )
            } ?: run {
            mySubFieldsValues.add(
                SubFieldEntity(
                    code = action.field.code,
                    fieldName = action.field.fieldId,
                    values = listOf(action.field.isChecked.asString())
                )
            )
        }

    }

    private fun handleTextAction(action: FieldActions.Text) {

        val newFields = subFieldAdapter.currentList.toMutableList()
        val newSubfieldsValues = mySubFieldsValues.toMutableList()

        subFieldAdapter.currentList
            .indexOfFirstOrNull { field -> field.fieldId == action.field.fieldId }
            ?.let { index ->
                val newItem = subFieldAdapter.currentList[index] as? FieldTypes.Text ?: return@let
                newFields.removeAt(index)
                newFields.add(
                    index,
                    newItem.copy(
                        myValues = listOf(action.field.enteredText.orEmpty())
                    )
                )
                viewBinding.recyclerView.doWithoutAnimation {
                    subFieldAdapter.submitList(newFields) { updateButtonState() }
                }
            }

        newSubfieldsValues
            .indexOfFirstOrNull { field -> field.fieldName == action.field.fieldId }
            ?.let { index ->
                val newItem = mySubFieldsValues.getOrNull(index) ?: return
                mySubFieldsValues.removeAt(index)
                mySubFieldsValues.add(
                    index,
                    newItem.copy(values = listOf(action.field.enteredText.orEmpty()))
                )
            } ?: run {
            mySubFieldsValues.add(
                SubFieldEntity(
                    code = action.field.code,
                    fieldName = action.field.fieldId,
                    values = listOf(action.field.enteredText.orEmpty()),
                )
            )
        }
    }

    private fun showSingleSelectorDialog(field: FieldTypes.SingleSelect) {
        SelectorBottomSheetDialog.show(childFragmentManager, field) { updatedField ->
            handleSingleSelectionDialog(updatedField)
        }
    }

    private fun handleSingleSelectionDialog(field: FieldTypes.SingleSelect) {

        val newListFields = subFieldAdapter.currentList.toMutableList()
        val newSubfieldsValues = mySubFieldsValues.toMutableList()
        val newSubField = SubFieldEntity(field.code.orEmpty(), field.fieldId, field.myValues)

        mySubFieldsValues.indexOfFirstOrNull { f -> f.fieldName == field.fieldId }
            ?.let { index ->

                if (field.myValues != null) {
                    mySubFieldsValues.replaceByIndex(index, newSubField)
                }
            } ?: mySubFieldsValues.add(newSubField)

        newListFields.indexOfFirstOrNull { f -> f.fieldId == field.fieldId }?.let { index ->

            newListFields.replaceByIndex(
                index = index,
                element = (newListFields[index] as FieldTypes.SingleSelect)
                    .copy(
                        myValues = field.myValues,
                        values = field.values?.map { item ->
                            item.copy(isSelected = item.value == field.myValues?.firstOrNull())
                        }
                    )
            )
        }

        subFieldAdapter.submitList(newListFields) { updateButtonState() }

        newSubfieldsValues
            .indexOfFirstOrNull { f -> f.fieldName == field.fieldId }
            ?.let { index ->
                val newItem = mySubFieldsValues.getOrNull(index) ?: return
                mySubFieldsValues.removeAt(index)
                mySubFieldsValues.add(
                    index,
                    newItem.copy(values = listOf(field.myValue.orEmpty()))
                )
            } ?: run {
            mySubFieldsValues.add(
                SubFieldEntity(
                    code = field.code,
                    fieldName = field.fieldId,
                    values = listOf(field.myValue.orEmpty()),
                )
            )
        }
    }

    private fun showMultipleSelectorDialog(field: FieldTypes.MultipleSelect) {

        SelectorBottomSheetDialog.show(childFragmentManager, field) { updatedField ->
            handleMultipleSelectDialog(updatedField)
        }
    }

    private fun handleMultipleSelectDialog(field: FieldTypes.MultipleSelect) {

        val newListFields = subFieldAdapter.currentList.toMutableList()
        val newSubField = SubFieldEntity(field.code.orEmpty(), field.fieldId, field.myValues)

        mySubFieldsValues.indexOfFirstOrNull { f -> f.fieldName == field.fieldId }
            ?.let { index ->

                if (field.myValues != null) {
                    mySubFieldsValues.replaceByIndex(index, newSubField)
                }
            } ?: mySubFieldsValues.add(newSubField)

        newListFields.indexOfFirstOrNull { f -> f.fieldId == field.fieldId }?.let { index ->

            newListFields.replaceByIndex(
                index = index,
                element = (newListFields[index] as FieldTypes.MultipleSelect)
                    .copy(
                        myValues = field.myValues,
                        values = field.values?.map { item ->
                            item.copy(
                                isSelected = field.myValues
                                    ?.any { myValue -> myValue == item.value }
                                    .orFalse()
                            )
                        }
                    )
            )
        }

        subFieldAdapter.submitList(newListFields) { updateButtonState() }
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

    private fun handleSpinnerField(field: FieldTypes.Spinner) {

        val newListFields = subFieldAdapter.currentList.toMutableList()
        val newItem = SubFieldEntity(field.code.orEmpty(), field.fieldId, field.myValues)

        mySubFieldsValues.indexOfFirstOrNull { f -> f.fieldName == field.fieldId }
            ?.let { index ->

                if (field.myValues != null) {
                    mySubFieldsValues.replaceByIndex(index, newItem)
                }
            } ?: mySubFieldsValues.add(newItem)

        subFieldAdapter.currentList.indexOfFirstOrNull { f -> f.fieldId == field.fieldId }
            ?.let { suitIndex -> newListFields.replaceByIndex(suitIndex, field) }

        subFieldAdapter.submitList(newListFields) { updateButtonState() }
    }

    private fun allFieldIsValid() {

        with(subFieldAdapter) {

            val newListFields = currentList.map { field ->
                val incorrectField = fieldValidator.checkRequiredFieldIsFill(currentList, field)
                incorrectField ?: field
            }.toMutableList()

            currentList.forEach { verificationField ->

                fieldValidator.checkFieldIsCorrect(verificationField)?.let { incorrectField ->
                    newListFields.indexOfFirstOrNull { f -> f.fieldId == incorrectField.fieldId }
                        ?.let { index ->

                            val newItem = (newListFields[index] as? FieldTypes.Text)
                                ?.copy(isValidData = incorrectField.isValidData)

                            newItem?.let { newListFields.replaceByIndex(index, newItem) }
                        }
                }
            }

            submitList(newListFields) { updateButtonState() }

            val requiredFieldIsFill = newListFields
                .filter { field -> field !is FieldTypes.Checkbox && field.isRequired }
                .all { field -> !field.fieldIsEmpty && field.isValidData }

            if (requiredFieldIsFill) applyData()
        }
    }

    private fun applyData() {

        if (checkBoxField != null) {

            val isCheckedCheckbox = mySubFieldsValues
                .firstOrNull { subField -> subField.code == checkBoxField?.code }
                ?.values
                ?.first()
                .asBoolean()

            if (isCheckedCheckbox) {
                mySubFieldsValues
                    .indexOfFirstOrNull { subField -> subField.code == checkBoxField?.hiddenKey }
                    ?.let { mySubFieldsValues.removeAt(it) }
            }

            val newList = mySubFieldsValues.toMutableList()

            newList.indexOfFirstOrNull { subField -> subField.code == checkBoxField?.code }
                ?.let { index ->
                    val newItem = newList[index].copy(
                        values = listOf(isCheckedCheckbox.asString())
                    )
                    mySubFieldsValues.removeAt(index)
                    mySubFieldsValues.add(index, newItem)

                } ?: run {
                val checkBoxField = subFields?.firstOrNull { field ->
                    field.code == checkBoxField?.code
                }
                mySubFieldsValues.add(
                    SubFieldEntity(
                        code = checkBoxField?.code.orEmpty(),
                        fieldName = checkBoxField?.fieldId.orEmpty(),
                        values = listOf(isCheckedCheckbox.asString())
                    )
                )
            }
        }

        val newSubFields = subField?.mySubfieldsValues.orEmpty().toMutableList()
        if (indexOfSubField != -1) {
            newSubFields.removeAt(indexOfSubField)
            newSubFields.add(indexOfSubField, mySubFieldsValues)
        } else {
            newSubFields.add(mySubFieldsValues)
        }

        subField?.let { subField ->
            newSubField(subField.copy(mySubfieldsValues = newSubFields))
        }

        this@SubFieldBottomSheet.dismiss()
    }

    private fun hideFieldByKey(key: String?) {

        val newList = subFieldAdapter.currentList
            .map { f -> if (f is FieldTypes.Checkbox) f.copy(isChecked = true) else f }
            .toMutableList()

        newList.indexOfFirstOrNull { field -> field.code == key }?.let { suitIndex ->
            newList.removeAt(suitIndex)
            subFieldAdapter.submitList(newList.toList()) { updateButtonState() }
        }
    }

    private fun updateButtonState() {

        val allRequiredFieldIsFill = subFieldAdapter.currentList
            .filter { field -> field !is FieldTypes.Checkbox && field.isRequired }
            .all { experience -> !experience.myValue.isNullOrBlank() }

        viewBinding.applyButton.background.alpha = if (allRequiredFieldIsFill) 255 else 120
    }

    private fun attachKeyboardObserver() {

        if (keyboardListenersAttached) return

        viewBinding.rootLayout.viewTreeObserver
            ?.addOnGlobalLayoutListener(keyboardLayoutObserver)

        keyboardListenersAttached = true
    }

    private fun detachKeyBoardObserver() {

        if (keyboardListenersAttached) {

            keyboardListenersAttached = false

            viewBinding.rootLayout.viewTreeObserver
                ?.removeOnGlobalLayoutListener(keyboardLayoutObserver)
        }
    }

}