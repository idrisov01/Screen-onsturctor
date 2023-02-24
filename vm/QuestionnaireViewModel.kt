package com.example.presentations.questionnaire.vm

import android.os.Bundle
import com.github.terrakok.cicerone.ResultListener
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import com.example.base.BaseViewModel
import com.example.common.extension.indexOfFirstOrNull
import com.example.common.extension.postDelayed
import com.example.common.extension.replaceByIndex
import com.example.core.navigation.router.CustomRouter
import com.example.core.navigation.screen.Screens
import com.example.core.network.error.parsing.ErrorEntity
import com.example.presentations.questionnaire.QuestionnaireFeature
import com.example.presentations.questionnaire.domain.entity.FormEntity
import com.example.presentations.questionnaire.domain.fieldhandler.FieldHandler
import com.example.presentations.questionnaire.domain.fieldvalidator.FieldValidator
import com.example.presentations.questionnaire.domain.models.FieldActions
import com.example.presentations.questionnaire.domain.models.FieldTypes
import com.example.presentations.questionnaire.domain.models.FieldTypes.Companion.fieldId
import com.example.presentations.questionnaire.domain.models.FieldTypes.Companion.fieldIsEmpty
import com.example.presentations.questionnaire.domain.models.FieldTypes.Companion.isRequired
import com.example.presentations.questionnaire.domain.models.FieldTypes.Companion.isValidData
import com.example.presentations.questionnaire.domain.models.FieldTypes.Companion.myValue
import com.example.presentations.questionnaire.domain.models.QuestionnaireType
import com.example.presentations.questionnaire.domain.models.TextInputTypes
import com.example.presentations.questionnaire.domain.repository.QuestionnaireRepository
import ru.tinkoff.decoro.MaskImpl
import ru.tinkoff.decoro.slots.PredefinedSlots
import java.util.*
import java.util.Collections.emptyList


class QuestionnaireViewModel @AssistedInject constructor(
    @Assisted("router") private val router: CustomRouter,
    @Assisted("vacancyId") private val vacancyId: Int,
    @Assisted("type") private val questionnaireType: QuestionnaireType,
    private val repository: QuestionnaireRepository,
    private val fieldValidator: FieldValidator,
    private val fieldHandler: FieldHandler,
) : BaseViewModel<QuestionnaireState, QuestionnaireEvent>(
    initialState = QuestionnaireState.initialState()
) {

    private companion object {

        private const val KEY_UPDATE_FILE = "UPDATE_FILE"
        private const val KEY_UPDATE_FILE_ID = "id"
        private const val KEY_UPDATE_FILE_COUNT = "count"
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("router") router: CustomRouter,
            @Assisted("vacancyId") vacancyId: Int,
            @Assisted("type") questionnaireType: QuestionnaireType,
        ): QuestionnaireViewModel
    }

    private val listAllFields
        get() = viewState().value.fields.orEmpty().toMutableList()

    private val allFilledFieldsIsCorrect
        get() = filledFieldsIsCorrect()

    private val allFieldsIsValid
        get() = allFieldsIsValid()

    private val fieldMap
        get() = convertFieldsToMap()

    private var invalidIndexInFields: Int? = null

    private var jobLoadQuestionnaire: Job? = null
    private var jobUploadQuestionnaire: Job? = null
    private var jobUploadDraft: Job? = null
    private var jobRemoveDraft: Job? = null

    init {
        when (questionnaireType) {
            QuestionnaireType.QUALIFICATION_FORM -> loadQualificationForm()
            QuestionnaireType.EXTENDED_FORM -> loadExtendedForm()
        }
    }

    override fun observe(event: QuestionnaireEvent) {
        when (event) {
            is QuestionnaireEvent.RemoveDraft -> removeDraft(vacancyId)
            is QuestionnaireEvent.FieldAction -> handleFieldAction(event.action)
            is QuestionnaireEvent.SendQuestionnaire -> sendQuestionnaire()
            is QuestionnaireEvent.RepeatRequest -> repeatRequest()
            is QuestionnaireEvent.OnBackPressed -> saveDraft()
        }
    }

    private fun handleFieldAction(action: FieldActions) {

        if (action is FieldActions.File) {
            navigateToFileScreen(action)
        } else {
            fieldHandler.handleFieldAction(listAllFields, action)
            updateState { copy(fields = fieldHandler.updatedFields) }
        }

        fieldHandler.checkButtonState(listAllFields)
        updateState { copy(fields = fieldHandler.updatedFields) }
    }

    private fun setupLoading() {

        val listFields = mutableListOf<FieldTypes>()
        listFields.add(FieldTypes.Progress)
        updateState { copy(fields = listFields) }
    }

    private fun loadQualificationForm() {

        setupLoading()

        updateState { copy(isNetworkError = false) }

        jobLoadQuestionnaire = launchCoroutine(
            { error ->
                when (error) {
                    ErrorEntity.Network -> updateState {
                        copy(isNetworkError = true, fields = emptyList())
                    }
                    else -> updateState {
                        copy(fields = emptyList())
                    }
                }

                true
            }
        ) {

            val entity = repository.loadQualificationForm(vacancyId)

            val maskPhone = MaskImpl(PredefinedSlots.RUS_PHONE_NUMBER, true)
            maskPhone.insertFront(repository.getUserPhone())

            setupFields(entity.qualificationForm, maskPhone.toString())

            updateState { copy(canDelete = entity.canDelete) }

            jobLoadQuestionnaire = null
        }
    }

    private fun loadExtendedForm() {

        setupLoading()

        updateState { copy(isNetworkError = false) }

        jobLoadQuestionnaire = launchCoroutine(
            { error ->
                when (error) {
                    ErrorEntity.Network -> updateState {
                        copy(isNetworkError = true, fields = emptyList())
                    }
                    else -> updateState {
                        copy(fields = emptyList())
                    }
                }

                true
            }
        ) {

            val entity = repository.loadExtendedForm(vacancyId)

            setupFields(entity.extendedForm)

            jobLoadQuestionnaire = null
        }
    }

    private fun setupFields(questionnaireForm: List<FormEntity>?, userPhone: String? = null) {

        if (questionnaireForm == null) return

        val questionnaireFields = mutableListOf<FieldTypes>()

        questionnaireForm.forEachIndexed { index, qualification ->

            if (index != 0) questionnaireFields.add(FieldTypes.Divider)
            questionnaireFields.add(FieldTypes.Title(qualification.title))
            questionnaireFields.addAll(qualification.formFields)
            questionnaireFields.add(FieldTypes.Space)
        }

        if (userPhone != null) {
            questionnaireFields.indexOfFirstOrNull { field ->
                field is FieldTypes.Text && field.inputType == TextInputTypes.PHONE
            }?.let { suitIndex ->

                val newItem = (questionnaireFields[suitIndex] as FieldTypes.Text)
                    .copy(myValues = listOf(userPhone), fieldIsEmpty = userPhone.isBlank())

                questionnaireFields.replaceByIndex(suitIndex, newItem)
            }
        }

        val listWithDividers = setupDividers(questionnaireFields).toMutableList()
        listWithDividers.add(FieldTypes.Send())

        fieldHandler.checkButtonState(listWithDividers)

        updateState { copy(fields = fieldHandler.updatedFields) }
    }

    private fun setupDividers(questionnaireFields: List<FieldTypes>): List<FieldTypes> {

        val listWithUpperDividers = questionnaireFields.mapIndexed { index, field ->
            when (field) {
                is FieldTypes.WorkExperience, is FieldTypes.MultipleFields -> {
                    when (questionnaireFields.getOrNull(index - 1)) {
                        is FieldTypes.Divider, is FieldTypes.Title -> field
                        null -> field
                        else -> {
                            (field as? FieldTypes.WorkExperience)?.copy(hasUpperDivider = true)
                                ?: (field as? FieldTypes.MultipleFields)?.copy(hasUpperDivider = true)
                                ?: field
                        }
                    }
                }
                else -> field
            }
        }

        val listWithAllDividers = listWithUpperDividers.mapIndexed { index, field ->
            when (field) {
                is FieldTypes.WorkExperience, is FieldTypes.MultipleFields -> {
                    when (questionnaireFields.getOrNull(index + 1)) {
                        is FieldTypes.Divider -> field
                        is FieldTypes.WorkExperience -> field
                        is FieldTypes.MultipleFields -> field
                        null -> field
                        else -> {
                            (field as? FieldTypes.WorkExperience)?.copy(hasLowerDivider = true)
                                ?: (field as? FieldTypes.MultipleFields)?.copy(hasLowerDivider = true)
                                ?: field
                        }
                    }
                }
                else -> field
            }
        }

        return listWithAllDividers
    }

    private fun saveDraft() {

        changeButtonState(isEnable = false)

        updateState { copy(invalidFieldIndex = null, isLoading = true) }

        if (allFilledFieldsIsCorrect) {
            launchCoroutine {
                uploadDraft()
                jobUploadDraft?.join()
                changeButtonState(isEnable = true)
                router.back()
            }
        } else {
            scrollToInvalidPosition()
            changeButtonState(isEnable = true)
        }
    }

    private fun sendQuestionnaire() {

        changeButtonState(isEnable = false)

        updateState { copy(invalidFieldIndex = null, isLoading = true) }

        if (allFieldsIsValid) {
            launchCoroutine {
                uploadDraft()
                jobUploadDraft?.join()
                uploadQuestionnaire()
                jobUploadQuestionnaire?.join()
                if (jobUploadQuestionnaire == null) router.back()
                changeButtonState(isEnable = true)
            }
        } else {
            scrollToInvalidPosition()
        }
    }

    private fun scrollToInvalidPosition() {
        postDelayed {
            updateState { copy(invalidFieldIndex = invalidIndexInFields, isLoading = false) }
        }
    }

    private fun uploadDraft() {

        updateState { copy(isNetworkError = false, serverMessage = null) }

        jobUploadDraft = launchCoroutine(
            { error ->

                changeButtonState(isEnable = true)
                updateState { copy(isLoading = false) }

                when (error) {
                    ErrorEntity.Network, ErrorEntity.Timeout -> updateState {
                        copy(isNetworkError = true)
                    }
                    else -> updateState {
                        copy(serverMessage = (error as? ErrorEntity.CustomError)?.message)
                    }
                }
                true
            }
        ) {

            repository.uploadDraft(vacancyId, fieldMap)
            repository.emitSaveDraft()

            jobUploadDraft = null
        }
    }

    private fun changeButtonState(isEnable: Boolean) {

        val newListFields = listAllFields

        val newItem = (listAllFields.lastOrNull() as? FieldTypes.Send)?.copy(isEnable = isEnable)

        listAllFields.indexOfFirstOrNull { fieldTypes -> fieldTypes is FieldTypes.Send }
            ?.let { index -> newListFields.replaceByIndex(index, newItem) }

        updateState { copy(fields = newListFields) }
    }

    private fun removeDraft(vacancyId: Int) {

        changeButtonState(isEnable = false)

        jobRemoveDraft = launchCoroutine(
            { error ->

                updateState {
                    copy(
                        isNetworkError = false,
                        serverMessage = null,
                        isLoading = false
                    )
                }

                when (error) {
                    ErrorEntity.Network, ErrorEntity.Timeout -> {
                        updateState { copy(isNetworkError = true) }
                    }
                    else -> updateState {
                        copy(serverMessage = (error as? ErrorEntity.CustomError)?.message)
                    }
                }
                true
            }
        ) {

            repository.removeDraft(vacancyId)
            repository.emitResponse(vacancyId, true)

            jobRemoveDraft = null

            changeButtonState(isEnable = true)

            router.back()
        }
    }

    private fun uploadQuestionnaire() {

        updateState { copy(isNetworkError = false, serverMessage = null) }
        jobUploadQuestionnaire = launchCoroutine(
            { error ->

                updateState { copy(isLoading = false) }

                when (error) {
                    ErrorEntity.Network -> {
                        updateState { copy(isNetworkError = true) }
                    }
                    else -> updateState {
                        copy(serverMessage = (error as? ErrorEntity.CustomError)?.message)
                    }
                }
                true
            }
        ) {

            repository.uploadQuestionnaire(vacancyId)
            repository.emitResponse(vacancyId)

            jobUploadQuestionnaire = null
        }
    }

    private fun repeatRequest() {

        when {
            jobRemoveDraft != null -> removeDraft(vacancyId)
            jobLoadQuestionnaire != null -> when (questionnaireType) {
                QuestionnaireType.QUALIFICATION_FORM -> loadQualificationForm()
                QuestionnaireType.EXTENDED_FORM -> loadExtendedForm()
            }
            jobUploadQuestionnaire != null -> {
                launchCoroutine {
                    uploadDraft()
                    jobUploadDraft?.join()
                    uploadQuestionnaire()
                    jobUploadQuestionnaire?.join()
                    router.back()
                }
            }
            jobUploadDraft != null -> uploadDraft()
        }
    }

    private fun handleUpdateListFiles(): ResultListener {
        return ResultListener { data ->

            if (data is Bundle) {

                val id = data.getInt(KEY_UPDATE_FILE_ID)
                val count = data.getInt(KEY_UPDATE_FILE_COUNT)

                val newList = listAllFields.toMutableList()

                newList.indexOfFirstOrNull { field ->
                    field is FieldTypes.File && field.myFiles?.fieldId == id
                }?.let { suitIndex ->

                    val newItem = newList[suitIndex] as? FieldTypes.File ?: return@let
                    val newValues = newItem.myFiles?.copy(filesCount = count)

                    newList.replaceByIndex(suitIndex, newItem.copy(myFiles = newValues))

                    fieldHandler.checkButtonState(newList)

                    updateState { copy(fields = fieldHandler.updatedFields) }
                }
            }
        }
    }

    private fun allFieldsIsValid(): Boolean {

        val newListFields = listAllFields.map { field ->
            val incorrectField = fieldValidator.checkRequiredFieldIsFill(listAllFields, field)
            incorrectField ?: field
        }.toMutableList()

        listAllFields.forEach { verificationField ->

            fieldValidator.checkFieldIsCorrect(verificationField)?.let { incorrectField ->
                newListFields.indexOfFirstOrNull { f -> f.fieldId == incorrectField.fieldId }
                    ?.let { index ->

                        val newItem = (newListFields[index] as? FieldTypes.Text)
                            ?.copy(isValidData = incorrectField.isValidData)

                        newItem?.let { newListFields.replaceByIndex(index, newItem) }
                    }
            }
        }

        val allFieldIsValid = newListFields.filter { field -> field.isRequired }
            .all { field -> !field.fieldIsEmpty }

        val allFieldsIsCorrect = newListFields.filterIsInstance<FieldTypes.Text>()
            .filter { field -> field.myValues?.isNotEmpty() == true }
            .all { field -> field.isValidData == true }

        invalidIndexInFields = newListFields
            .indexOfFirstOrNull { field ->
                field.isRequired && (field.fieldIsEmpty || !field.isValidData)
            }

        updateState { copy(fields = newListFields) }

        return allFieldIsValid && allFieldsIsCorrect
    }

    private fun filledFieldsIsCorrect(): Boolean {

        val newListFields = listAllFields.toMutableList()

        listAllFields.forEach { verificationField ->

            fieldValidator.checkFieldIsCorrect(verificationField)?.let { incorrectField ->
                listAllFields.indexOfFirstOrNull { f -> f.fieldId == incorrectField.fieldId }
                    ?.let { index ->
                        newListFields.replaceByIndex(index, incorrectField)
                    }
            }
        }

        invalidIndexInFields = newListFields.indexOfFirstOrNull { field ->
            field is FieldTypes.Text && field.isValidData == false
        }

        updateState { copy(fields = newListFields) }

        return newListFields.filterIsInstance<FieldTypes.Text>()
            .filter { field -> field.myValues?.joinToString()?.isNotEmpty() == true }
            .all { field -> field.isValidData == true }
    }

    private fun convertFieldsToMap(): HashMap<String, String> {

        val hashMap = hashMapOf<String, String>()

        listAllFields
            .filter { field ->
                field !is FieldTypes.WorkExperience
                        && field !is FieldTypes.File
                        && field !is FieldTypes.MultipleFields
                        && field !is FieldTypes.MultipleSelect
            }
            .forEach { field ->
                field.fieldId?.let { fieldId ->
                    field.myValue?.let { myValue -> hashMap[fieldId] = myValue }
                }
            }

        listAllFields.filterIsInstance<FieldTypes.MultipleSelect>()
            .forEach { multipleSelect ->
                if (multipleSelect.myValues.isNullOrEmpty()) {
                    hashMap[multipleSelect.fieldId] = ""
                } else {
                    multipleSelect.myValues.forEachIndexed { i, v ->
                        hashMap["${multipleSelect.fieldId}[$i]"] = v
                    }
                }
            }

        listAllFields.filterIsInstance<FieldTypes.MultipleFields>()
            .forEach { field ->
                field.mySubfieldsValues.forEachIndexed { index, subFields ->
                    subFields.forEach { subField ->
                        val indexOfField = "${field.fieldId}[${index}][${subField.fieldName}]"
                        hashMap[indexOfField] = subField.values?.joinToString().orEmpty()
                    }
                }
            }

        listAllFields.filterIsInstance<FieldTypes.WorkExperience>()
            .forEach { field ->
                field.mySubfieldsValues.forEachIndexed { index, subFields ->
                    subFields.forEach { subField ->
                        val indexOfField = "${field.fieldId}[${index}][${subField.fieldName}]"
                        hashMap[indexOfField] = subField.values?.joinToString().orEmpty()
                    }
                }
            }

        return hashMap
    }

    private fun navigateToFileScreen(action: FieldActions.File) {

        router.addResultListener(
            key = KEY_UPDATE_FILE,
            listener = handleUpdateListFiles()
        )
        router.forward(Screens.filesFragment(vacancyId, action.field))

    }

    override fun onCleared() {
        QuestionnaireFeature.destroyModuleGraph()
    }
}