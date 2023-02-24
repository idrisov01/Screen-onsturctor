package com.example.presentations.questionnaire.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.example.R
import com.example.common.extension.doWithoutAnimation
import com.example.common.extension.hideIfArrayIsEmpty
import com.example.common.extension.indexOfFirstOrNull
import com.example.common.extension.isNotNullOrNotEmpty
import com.example.common.extension.orFalse
import com.example.common.extension.parcelable
import com.example.common.extension.replaceAll
import com.example.common.extension.replaceByIndex
import com.example.common.extension.uiLazy
import com.example.common.extension.withArgs
import com.example.common.uikit.DividerItemDecorator
import com.example.databinding.FragmentDialogQuestionnaireSingleSelectBinding
import com.example.presentations.questionnaire.domain.entity.ItemSelectorEntity
import com.example.presentations.questionnaire.domain.models.FieldTypes
import com.example.presentations.questionnaire.ui.adapters.MultipleSelectorAdapter
import com.example.presentations.questionnaire.ui.adapters.SingleSelectorAdapter


class SelectorBottomSheetDialog(
    private val selectSingleItem: ((FieldTypes.SingleSelect) -> Unit)? = null,
    private val selectMultipleItem: ((FieldTypes.MultipleSelect) -> Unit)? = null,
) : BottomSheetDialogFragment() {

    companion object {

        fun show(
            fm: FragmentManager,
            field: FieldTypes.SingleSelect,
            selectItem: (FieldTypes.SingleSelect) -> Unit,
        ) {
            SelectorBottomSheetDialog(selectSingleItem = selectItem).withArgs {
                putParcelable(ARG_SINGLE_FIELD, field)
                putBoolean(ARG_IS_SINGLE_SELECT, true)
            }.show(fm, SelectorBottomSheetDialog::class.simpleName)
        }


        fun show(
            fm: FragmentManager,
            field: FieldTypes.MultipleSelect,
            selectItem: (FieldTypes.MultipleSelect) -> Unit,
        ) {
            SelectorBottomSheetDialog(selectMultipleItem = selectItem).withArgs {
                putParcelable(ARG_MULTIPLE_FIELD, field)
                putBoolean(ARG_IS_SINGLE_SELECT, false)
            }.show(fm, SelectorBottomSheetDialog::class.simpleName)
        }

        private const val ARG_SINGLE_FIELD = "SINGLE_FIELD"
        private const val ARG_MULTIPLE_FIELD = "MULTIPLE_FIELD"
        private const val ARG_IS_SINGLE_SELECT = "SINGLE_SELECT"
    }

    private var _binding: FragmentDialogQuestionnaireSingleSelectBinding? = null

    private val viewBinding
        get() = _binding!!

    private val singleField by uiLazy {
        arguments?.parcelable(ARG_SINGLE_FIELD) as FieldTypes.SingleSelect?
    }

    private val multipleField by uiLazy {
        arguments?.parcelable(ARG_MULTIPLE_FIELD) as FieldTypes.MultipleSelect?
    }

    private val isSingleSelect by uiLazy {
        arguments?.getBoolean(ARG_IS_SINGLE_SELECT).orFalse()
    }

    private val singleSelectorAdapter by uiLazy {
        SingleSelectorAdapter { selectedItem -> updateItem(selectedItem) }
    }

    private val multiSelectorAdapter by uiLazy {
        MultipleSelectorAdapter { selectedItem -> updateItem(selectedItem) }
    }

    private val isRequiredFields by uiLazy {
        singleField?.isRequired ?: multipleField?.isRequired ?: false
    }

    private val isEnableButton
        get() = if (isRequiredFields) listValues.any { value -> value.isSelected } else true

    private val listValues = mutableListOf<ItemSelectorEntity>()
    private var searchDebounceJob: Job? = null
    private var selectItemJob: Job? = null
    private var searchQuery = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDialogQuestionnaireSingleSelectBinding.inflate(inflater)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        singleField?.values?.let(listValues::addAll)
        multipleField?.values?.let(listValues::addAll)
        setupDialog()
        setupUi()
        setupListeners()
        setupVisibilities()
    }

    private fun setupRecycler() {

        with(viewBinding.recyclerView) {

            adapter = if (isSingleSelect) singleSelectorAdapter else multiSelectorAdapter

            addItemDecoration(
                DividerItemDecorator(
                    ContextCompat.getDrawable(requireContext(), R.drawable.background_divider)
                )
            )

            if (isSingleSelect) {
                singleSelectorAdapter.submitList(singleField?.values)
            } else {
                multiSelectorAdapter.submitList(multipleField?.values)
            }

        }
    }

    private fun setupUi() {

        with(viewBinding) {

            titleTextView.text = singleField?.hint ?: multipleField?.hint
            searchView.imeOption = EditorInfo.IME_ACTION_DONE

            setupRecycler()

            applyButton.isEnabled = isEnableButton

            emptyContainer.titleTextView.text = getString(R.string.search_empty_screen_title)
            emptyContainer.descriptionTextView.text = getString(R.string.search_empty_screen_desc)
        }

    }

    private fun setupDialog() {

        val bottomSheet = (dialog as? BottomSheetDialog)
            ?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

        if (bottomSheet is FrameLayout) {
            bottomSheet.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT

            BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun setupListeners() {

        with(viewBinding) {

            searchView.onSearchQueryListener { query -> handleSearchEvent(query) }
            searchView.performSearch { query -> handleSearchEvent(query) }

            closeImageView.setOnClickListener { this@SelectorBottomSheetDialog.dismiss() }

            resetTextView.setOnClickListener {

                listValues.map { item -> item.copy(isSelected = false) }
                    .let { unselectedList -> listValues.replaceAll(unselectedList) }

                updateList(filteredList(searchQuery))
            }

            applyButton.setOnClickListener {
                this@SelectorBottomSheetDialog.dismiss()

                val selectedValues = listValues
                    .filter { item -> item.isSelected }
                    .map { item -> item.value }

                if (isSingleSelect) {
                    val newField = singleField ?: return@setOnClickListener
                    selectSingleItem?.invoke((newField.copy(myValues = selectedValues)))
                } else {
                    val newField = multipleField ?: return@setOnClickListener
                    selectMultipleItem?.invoke((newField.copy(myValues = selectedValues)))
                }
            }
        }
    }

    private fun setupVisibilities() {

        with(viewBinding) {

            recyclerView.isVisible = singleField?.values.isNotNullOrNotEmpty()
                    || multipleField?.values.isNotNullOrNotEmpty()

            applyButton.isVisible = singleField?.values.isNotNullOrNotEmpty()
                    || multipleField?.values.isNotNullOrNotEmpty()

            resetTextView.isVisible = !isSingleSelect
        }
    }

    private fun handleSearchEvent(query: String) {

        searchDebounceJob?.cancel()

        searchDebounceJob = CoroutineScope(Dispatchers.Main + SupervisorJob())
            .launch {

                val filteredList = filteredList(query)

                val currentAdapter = if (isSingleSelect) {
                    singleSelectorAdapter
                } else {
                    multiSelectorAdapter
                }

                currentAdapter.submitList(filteredList) {
                    with(viewBinding) {
                        recyclerView.hideIfArrayIsEmpty(filteredList)
                        recyclerView.scrollToPosition(0)
                        applyButton.hideIfArrayIsEmpty(filteredList)
                        emptyContainer.root.isVisible = filteredList.isEmpty()
                    }
                }
            }
    }

    private fun filteredList(query: String): List<ItemSelectorEntity> {

        searchQuery = query

        val newList = mutableListOf<ItemSelectorEntity>()

        if (query.isBlank()) {
            newList.addAll(listValues)
        } else {
            newList.addAll(
                listValues.filter { item -> item.value.contains(query, true) }
            )
        }

        return newList
    }

    private fun updateList(values: List<ItemSelectorEntity>) {

        with(viewBinding) {

            applyButton.isEnabled = isEnableButton

            recyclerView.doWithoutAnimation {
                if (isSingleSelect) {
                    singleSelectorAdapter.submitList(values)
                } else {
                    multiSelectorAdapter.submitList(values)
                }
            }
        }
    }

    private fun updateItem(selectedItem: ItemSelectorEntity) {

        selectItemJob?.cancel()

        selectItemJob = CoroutineScope(Dispatchers.Main + SupervisorJob())
            .launch {

                if (isSingleSelect) {
                    listValues.map { item -> item.copy(isSelected = false) }
                        .let { unselectedList -> listValues.replaceAll(unselectedList) }
                }

                listValues.indexOfFirstOrNull { item -> item.value == selectedItem.value }
                    ?.let { index -> listValues.replaceByIndex(index, selectedItem) }

                updateList(filteredList(searchQuery))
            }
    }
}