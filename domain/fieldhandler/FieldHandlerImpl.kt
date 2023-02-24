package com.example.presentations.questionnaire.domain.fieldhandler

import com.example.common.extension.indexOfFirstOrNull
import com.example.common.extension.orFalse
import com.example.common.extension.replaceByIndex
import com.example.presentations.questionnaire.domain.models.FieldActions
import com.example.presentations.questionnaire.domain.models.FieldTypes
import com.example.presentations.questionnaire.domain.models.FieldTypes.Companion.fieldId
import com.example.presentations.questionnaire.domain.models.FieldTypes.Companion.isRequired
import com.example.presentations.questionnaire.domain.models.FieldTypes.Companion.myValue
import javax.inject.Inject


class FieldHandlerImpl @Inject constructor() : FieldHandler {

    private val listFields = mutableListOf<FieldTypes>()

    override val updatedFields: List<FieldTypes>
        get() = listFields.toList()

    override fun checkButtonState(allFields: List<FieldTypes>) {

        val newListFields = allFields.toMutableList()

        val allFieldIsValid = newListFields.filter { field -> field.isRequired }
            .all { field -> !field.myValue.isNullOrEmpty() }

        val newItem = (allFields.lastOrNull() as? FieldTypes.Send)?.copy(isEnable = allFieldIsValid)

        allFields.indexOfFirstOrNull { fieldTypes -> fieldTypes is FieldTypes.Send }
            ?.let { index -> newListFields.replaceByIndex(index, newItem) }

        listFields.clear()
        listFields.addAll(newListFields)
    }

    override fun handleFieldAction(allFields: List<FieldTypes>, action: FieldActions) {
        when (action) {
            is FieldActions.MultipleSelect -> handleMultipleSelectionField(allFields, action)
            is FieldActions.SingleSelect -> handleSingleSelectionField(allFields, action)
            is FieldActions.Spinner -> handleSpinnerField(allFields, action)
            is FieldActions.SubField -> handleSubFieldsField(allFields, action)
            is FieldActions.Text -> handleTextAction(allFields, action)
            is FieldActions.WorkExperience -> handleWorkExperience(allFields, action)
            else -> Unit
        }

        listFields
            .filter { field -> field.isRequired && field !is FieldTypes.Title }
            .all { item -> !item.myValue.isNullOrEmpty() }
            .run {
                listFields.filterIsInstance<FieldTypes.Send>()
                    .firstOrNull()
                    ?.copy(isEnable = this)
            }
    }

    private fun handleTextAction(allFields: List<FieldTypes>, action: FieldActions.Text) {

        val newListFields = allFields.toMutableList()

        allFields.indexOfFirstOrNull { field -> field.fieldId == action.field.fieldId }
            ?.let { suitIndex ->

                val newField = (allFields[suitIndex] as FieldTypes.Text)

                with(action.field) {
                    newListFields.replaceByIndex(
                        index = suitIndex,
                        element = newField.copy(
                            myValues = if (enteredText != null) listOf(enteredText.trim()) else null
                        )
                    )
                }
            }

        listFields.clear()
        listFields.addAll(newListFields)
    }

    private fun handleSpinnerField(allFields: List<FieldTypes>, action: FieldActions.Spinner) {

        val newListFields = allFields.toMutableList()

        allFields.indexOfFirstOrNull { field -> field.fieldId == action.field.fieldId }
            ?.let { suitIndex -> newListFields.replaceByIndex(suitIndex, action.field) }

        listFields.clear()
        listFields.addAll(newListFields)
    }

    private fun handleSingleSelectionField(
        allFields: List<FieldTypes>,
        action: FieldActions.SingleSelect,
    ) {

        val newListFields = allFields.toMutableList()

        allFields.indexOfFirstOrNull { field -> field.fieldId == action.field.fieldId }
            ?.let { index ->

                newListFields.replaceByIndex(
                    index = index,
                    element = (newListFields[index] as FieldTypes.SingleSelect)
                        .copy(
                            myValues = action.field.myValues,
                            values = action.field.values?.map { item ->
                                item.copy(
                                    isSelected = item.value == action.field.myValues?.firstOrNull()
                                )
                            }
                        )
                )
            }

        listFields.clear()
        listFields.addAll(newListFields)
    }

    private fun handleMultipleSelectionField(
        allFields: List<FieldTypes>,
        action: FieldActions.MultipleSelect,
    ) {

        val newListFields = allFields.toMutableList()

        allFields.indexOfFirstOrNull { field -> field.fieldId == action.field.fieldId }
            ?.let { index ->

                newListFields.replaceByIndex(
                    index = index,
                    element = (allFields[index] as FieldTypes.MultipleSelect)
                        .copy(
                            myValues = action.field.myValues,
                            values = action.field.values?.map { item ->
                                item.copy(
                                    isSelected = action.field.myValues
                                        ?.any { myValue -> myValue == item.value }
                                        .orFalse()
                                )
                            }
                        )
                )

            }

        listFields.clear()
        listFields.addAll(newListFields)
    }

    private fun handleSubFieldsField(allFields: List<FieldTypes>, action: FieldActions.SubField) {

        val newListFields = allFields.toMutableList()

        allFields.indexOfFirstOrNull { field -> field.fieldId == action.field.fieldId }
            ?.let { index ->
                newListFields.replaceByIndex(
                    index,
                    action.field.copy(fieldIsEmpty = action.field.mySubfieldsValues.isEmpty())
                )
            }

        listFields.clear()
        listFields.addAll(newListFields)
    }

    private fun handleWorkExperience(
        allFields: List<FieldTypes>,
        action: FieldActions.WorkExperience,
    ) {

        val newListFields = allFields.toMutableList()

        allFields.indexOfFirstOrNull { field -> field.fieldId == action.field.fieldId }
            ?.let { index ->
                newListFields.replaceByIndex(
                    index,
                    action.field.copy(fieldIsEmpty = action.field.mySubfieldsValues.isEmpty())
                )
            }

        listFields.clear()
        listFields.addAll(newListFields)
    }

}