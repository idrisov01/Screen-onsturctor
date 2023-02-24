package com.example.presentations.questionnaire.domain.models

import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.parcelize.Parcelize
import com.example.databinding.ItemQuestionnaireCheckboxBinding
import com.example.databinding.ItemQuestionnaireDividerBinding
import com.example.databinding.ItemQuestionnaireFileBinding
import com.example.databinding.ItemQuestionnaireListBinding
import com.example.databinding.ItemQuestionnaireMultipleFieldsBinding
import com.example.databinding.ItemQuestionnaireProgressBinding
import com.example.databinding.ItemQuestionnaireSendBinding
import com.example.databinding.ItemQuestionnaireSpaceBinding
import com.example.databinding.ItemQuestionnaireTextBinding
import com.example.databinding.ItemQuestionnaireTitleBinding
import com.example.databinding.ItemQuestionnaireWorkExperienceBinding
import com.example.presentations.questionnaire.ui.viewholders.QuestionnaireCheckboxHolder
import com.example.presentations.questionnaire.ui.viewholders.QuestionnaireDividerHolder
import com.example.presentations.questionnaire.ui.viewholders.QuestionnaireFileHolder
import com.example.presentations.questionnaire.ui.viewholders.QuestionnaireListHolder
import com.example.presentations.questionnaire.ui.viewholders.QuestionnaireMultipleFieldsHolder
import com.example.presentations.questionnaire.ui.viewholders.QuestionnaireMultipleHolder
import com.example.presentations.questionnaire.ui.viewholders.QuestionnaireProgressHolder
import com.example.presentations.questionnaire.ui.viewholders.QuestionnaireSendHolder
import com.example.presentations.questionnaire.ui.viewholders.QuestionnaireSingleHolder
import com.example.presentations.questionnaire.ui.viewholders.QuestionnaireSpaceHolder
import com.example.presentations.questionnaire.ui.viewholders.QuestionnaireTextHolder
import com.example.presentations.questionnaire.ui.viewholders.QuestionnaireTitleHolder
import com.example.presentations.questionnaire.ui.viewholders.QuestionnaireWorkExperienceHolder


sealed class FieldViewHolders : Parcelable {

    companion object {

        private const val PROGRESS = 100
        private const val SEND = 101
        private const val TITLE = 102
        private const val TEXT = 103
        private const val SPINNER = 104
        private const val SINGLE = 105
        private const val MULTIPLE = 106
        private const val WORK_EXPERIENCE = 107
        private const val MULTI_FIELDS = 108
        private const val CHECKBOX = 109
        private const val FILE = 110
        private const val DIVIDER = 111
        private const val SPACE = 112

        fun getItemType(allFields: List<FieldTypes>, position: Int): Int {
            return when (allFields[position]) {
                is FieldTypes.Checkbox -> CHECKBOX
                is FieldTypes.File -> FILE
                is FieldTypes.MultipleFields -> MULTI_FIELDS
                is FieldTypes.MultipleSelect -> MULTIPLE
                is FieldTypes.Progress -> PROGRESS
                is FieldTypes.Send -> SEND
                is FieldTypes.SingleSelect -> SINGLE
                is FieldTypes.Divider -> DIVIDER
                is FieldTypes.Space -> SPACE
                is FieldTypes.Spinner -> SPINNER
                is FieldTypes.Text -> TEXT
                is FieldTypes.Title -> TITLE
                is FieldTypes.WorkExperience -> WORK_EXPERIENCE
            }
        }

        fun getViewType(
            type: Int,
            parent: ViewGroup,
            allFields: List<FieldTypes>,
            fieldAction: (FieldActions) -> Unit,
            showError: (String?) -> Unit,
        ): RecyclerView.ViewHolder {
            return when (type) {
                PROGRESS -> Progress.inflate(parent)
                SEND -> Send.inflate(parent, fieldAction)
                TITLE -> Title.inflate(parent)
                TEXT -> Text.inflate(parent, fieldAction, showError)
                SPINNER -> Spinner.inflate(parent, fieldAction, showError)
                SINGLE -> SingleSelect.inflate(parent, fieldAction, showError)
                MULTIPLE -> MultiSelect.inflate(parent, fieldAction, showError)
                CHECKBOX -> CheckBox.inflate(parent, fieldAction)
                WORK_EXPERIENCE -> WorkExperience.inflate(parent, allFields, fieldAction, showError)
                FILE -> File.inflate(parent, fieldAction, showError)
                DIVIDER -> Divider.inflate(parent)
                SPACE -> Space.inflate(parent)
                MULTI_FIELDS -> MultipleField.inflate(parent, allFields, fieldAction, showError)
                else -> error("Can`t get viewType for position $type")
            }
        }

        fun getItemViewType(
            allFields: List<FieldTypes>,
            holder: RecyclerView.ViewHolder,
            position: Int,
        ) {

            when (getItemType(allFields, position)) {
                PROGRESS -> (holder as QuestionnaireProgressHolder)
                DIVIDER -> (holder as QuestionnaireDividerHolder)
                SPACE -> (holder as QuestionnaireSpaceHolder)
                SEND -> (holder as QuestionnaireSendHolder).bind(allFields[position] as FieldTypes.Send)
                TITLE -> (holder as QuestionnaireTitleHolder).bind(allFields[position] as FieldTypes.Title)
                TEXT -> (holder as QuestionnaireTextHolder).bind(allFields[position] as FieldTypes.Text)
                SPINNER -> (holder as QuestionnaireListHolder).bind(allFields[position] as FieldTypes.Spinner)
                SINGLE -> (holder as QuestionnaireSingleHolder).bind(allFields[position] as FieldTypes.SingleSelect)
                MULTIPLE -> (holder as QuestionnaireMultipleHolder).bind(allFields[position] as FieldTypes.MultipleSelect)
                CHECKBOX -> (holder as QuestionnaireCheckboxHolder).bind(allFields[position] as FieldTypes.Checkbox)
                WORK_EXPERIENCE -> (holder as QuestionnaireWorkExperienceHolder).bind(allFields[position] as FieldTypes.WorkExperience)
                FILE -> (holder as QuestionnaireFileHolder).bind(allFields[position] as FieldTypes.File)
                MULTI_FIELDS -> (holder as QuestionnaireMultipleFieldsHolder).bind(allFields[position] as FieldTypes.MultipleFields)
            }
        }
    }

    @Parcelize
    object Progress : FieldViewHolders() {

        fun inflate(parent: ViewGroup): QuestionnaireProgressHolder =
            QuestionnaireProgressHolder(
                ItemQuestionnaireProgressBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
    }

    @Parcelize
    object Divider : FieldViewHolders() {

        fun inflate(parent: ViewGroup) = QuestionnaireDividerHolder(
            ItemQuestionnaireDividerBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    @Parcelize
    object Space : FieldViewHolders() {

        fun inflate(parent: ViewGroup) = QuestionnaireSpaceHolder(
            ItemQuestionnaireSpaceBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    @Parcelize
    object Send : FieldViewHolders() {

        fun inflate(
            parent: ViewGroup,
            fieldAction: (FieldActions) -> Unit,
        ) = QuestionnaireSendHolder(
            ItemQuestionnaireSendBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        ) { fieldAction(FieldActions.Send) }
    }

    @Parcelize
    object Title : FieldViewHolders() {

        fun inflate(parent: ViewGroup) = QuestionnaireTitleHolder(
            ItemQuestionnaireTitleBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    @Parcelize
    object Text : FieldViewHolders() {

        fun inflate(
            parent: ViewGroup,
            fieldAction: (FieldActions) -> Unit,
            showError: (String?) -> Unit,
        ) = QuestionnaireTextHolder(
            itemBinding = ItemQuestionnaireTextBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ),
            enteredData = { text, isFocused -> fieldAction(FieldActions.Text(text, isFocused)) },
            showError = { message -> showError(message) }
        )
    }

    @Parcelize
    object Spinner : FieldViewHolders() {

        fun inflate(
            parent: ViewGroup,
            fieldAction: (FieldActions) -> Unit,
            showError: (String?) -> Unit,
        ) = QuestionnaireListHolder(
            itemBinding = ItemQuestionnaireListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ),
            showList = { spinner -> fieldAction(FieldActions.Spinner(spinner)) },
            showError = { message -> showError(message) }
        )
    }

    @Parcelize
    object SingleSelect : FieldViewHolders() {

        fun inflate(
            parent: ViewGroup,
            fieldAction: (FieldActions) -> Unit,
            showError: (String?) -> Unit,
        ) = QuestionnaireSingleHolder(
            itemBinding = ItemQuestionnaireListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ),
            showList = { single -> fieldAction(FieldActions.SingleSelect(single)) },
            showError = { message -> showError(message) }
        )

    }

    @Parcelize
    object MultiSelect : FieldViewHolders() {

        fun inflate(
            parent: ViewGroup,
            fieldAction: (FieldActions) -> Unit,
            showError: (String?) -> Unit,
        ) = QuestionnaireMultipleHolder(
            itemBinding = ItemQuestionnaireListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ),
            showList = { multiSelect -> fieldAction(FieldActions.MultipleSelect(multiSelect)) },
            showError = { message -> showError(message) }
        )
    }

    @Parcelize
    object CheckBox : FieldViewHolders() {

        fun inflate(parent: ViewGroup, fieldAction: (FieldActions) -> Unit) =
            QuestionnaireCheckboxHolder(
                itemBinding = ItemQuestionnaireCheckboxBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ),
                isChecked = { checkbox -> fieldAction(FieldActions.Checkbox(checkbox)) }
            )
    }

    @Parcelize
    object WorkExperience : FieldViewHolders() {

        fun inflate(
            parent: ViewGroup,
            allFields: List<FieldTypes>,
            fieldAction: (FieldActions) -> Unit,
            showError: (String?) -> Unit,
        ) = QuestionnaireWorkExperienceHolder(
            itemBinding = ItemQuestionnaireWorkExperienceBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ),
            addWorkPlace = { workExperience ->
                fieldAction(FieldActions.WorkExperience(workExperience))
            },
            toDetailWorkExperience = { fieldIndex, workPlaceIndex ->
                fieldAction(
                    FieldActions.WorkExperienceDetail(
                        field = allFields[fieldIndex] as FieldTypes.WorkExperience,
                        index = workPlaceIndex
                    )
                )
            },
            showError = { message -> showError(message) }
        )
    }

    @Parcelize
    object MultipleField : FieldViewHolders() {

        fun inflate(
            parent: ViewGroup,
            allFields: List<FieldTypes>,
            fieldAction: (FieldActions) -> Unit,
            showError: (String?) -> Unit,
        ) = QuestionnaireMultipleFieldsHolder(
            itemBinding = ItemQuestionnaireMultipleFieldsBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ),
            addSubField = { subField -> fieldAction(FieldActions.SubField(subField)) },
            toDetailSubFields = { fieldIndex, subFieldIndex ->
                fieldAction(
                    FieldActions.SubFieldDetail(
                        field = allFields[fieldIndex] as FieldTypes.MultipleFields,
                        index = subFieldIndex
                    )
                )
            },
            showError = { message -> showError(message) }
        )
    }

    @Parcelize
    object File : FieldViewHolders() {

        fun inflate(
            parent: ViewGroup,
            fieldAction: (FieldActions) -> Unit,
            showError: (String?) -> Unit,
        ) = QuestionnaireFileHolder(
            itemBinding = ItemQuestionnaireFileBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ),
            addDocument = { file -> fieldAction(FieldActions.File(file)) },
            showError = { message -> showError(message) }
        )
    }
}
