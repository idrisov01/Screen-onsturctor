package com.example.presentations.questionnaire.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.presentations.questionnaire.domain.models.FieldActions
import com.example.presentations.questionnaire.domain.models.FieldTypes
import com.example.presentations.questionnaire.domain.models.FieldViewHolders


class QuestionnaireAdapter(
    private val fieldAction: (FieldActions) -> Unit,
    private val showError: (String?) -> Unit,
) : ListAdapter<FieldTypes, RecyclerView.ViewHolder>(DiffFields()) {

    override fun getItemViewType(position: Int): Int {
        return FieldViewHolders.getItemType(currentList, position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return FieldViewHolders.getViewType(viewType, parent, currentList, fieldAction, showError)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        return FieldViewHolders.getItemViewType(currentList, holder, position)
    }

    class DiffFields : DiffUtil.ItemCallback<FieldTypes>() {

        override fun areItemsTheSame(oldItem: FieldTypes, newItem: FieldTypes): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: FieldTypes, newItem: FieldTypes): Boolean {

            val isSameText = oldItem is FieldTypes.Text && newItem is FieldTypes.Text
                    && oldItem.fieldId == newItem.fieldId
                    && oldItem.myValues == newItem.myValues
                    && oldItem.enteredText == newItem.enteredText
                    && oldItem.fieldIsEmpty == newItem.fieldIsEmpty
                    && oldItem.isValidData == newItem.isValidData
                    && oldItem.isRequired == newItem.isRequired

            val isSameSpinner = oldItem is FieldTypes.Spinner && newItem is FieldTypes.Spinner
                    && oldItem.fieldId == newItem.fieldId
                    && oldItem.values == newItem.values
                    && oldItem.myValues == newItem.myValues
                    && oldItem.hint == newItem.hint
                    && oldItem.fieldIsEmpty == newItem.fieldIsEmpty

            val isSameSingle = oldItem is FieldTypes.SingleSelect && newItem is FieldTypes.SingleSelect
                    && oldItem.fieldId == newItem.fieldId
                    && oldItem.values == newItem.values
                    && oldItem.myValues == newItem.myValues
                    && oldItem.hint == newItem.hint
                    && oldItem.fieldIsEmpty == newItem.fieldIsEmpty

            val isSameMultipleSelect = oldItem is FieldTypes.MultipleSelect && newItem is FieldTypes.MultipleSelect
                    && oldItem.fieldId == newItem.fieldId
                    && oldItem.values == newItem.values
                    && oldItem.myValues == newItem.myValues
                    && oldItem.hint == newItem.hint
                    && oldItem.fieldIsEmpty == newItem.fieldIsEmpty

            val isSameWorkExperience = oldItem is FieldTypes.WorkExperience && newItem is FieldTypes.WorkExperience
                    && oldItem.fieldId == newItem.fieldId
                    && oldItem.mySubfieldsValues == newItem.mySubfieldsValues
                    && oldItem.subFields == newItem.subFields
                    && oldItem.fieldIsEmpty == newItem.fieldIsEmpty

            val isSameCheckbox = oldItem is FieldTypes.Checkbox && newItem is FieldTypes.Checkbox
                    && oldItem.fieldId == newItem.fieldId
                    && oldItem.hint == newItem.hint
                    && oldItem.isChecked == newItem.isChecked
                    && oldItem.hiddenKey == newItem.hiddenKey

            val isSameFile = oldItem is FieldTypes.File && newItem is FieldTypes.File
                    && oldItem.fieldId == newItem.fieldId
                    && oldItem.myFiles == newItem.myFiles
                    && oldItem.fieldIsEmpty == newItem.fieldIsEmpty
                    && oldItem.isRequired == newItem.isRequired

            val isSameSend = oldItem is FieldTypes.Send && newItem is FieldTypes.Send
                    && oldItem.isEnable == newItem.isEnable

            return isSameText || isSameSpinner || isSameSingle || isSameMultipleSelect
                    || isSameWorkExperience || isSameCheckbox || isSameFile || isSameSend
        }
    }
}