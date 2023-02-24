package com.example.presentations.questionnaire.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.R
import com.example.common.extension.asBoolean
import com.example.common.extension.setOnSingleClickListener
import com.example.common.utils.DateUtils
import com.example.databinding.ItemWorkPlaceBinding
import com.example.presentations.questionnaire.domain.entity.SubFieldEntity


class WorkPlaceAdapter(
    private val toDetailWorkPlace: (Int) -> Unit,
) : ListAdapter<List<SubFieldEntity>, WorkPlaceAdapter.WorkPlaceHolder>(DiffWorkPlace()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkPlaceHolder {
        return WorkPlaceHolder(
            itemBinding = ItemWorkPlaceBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ),
            toDetailWorkPlace = toDetailWorkPlace
        )
    }

    override fun onBindViewHolder(holder: WorkPlaceHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class WorkPlaceHolder(
        private val itemBinding: ItemWorkPlaceBinding,
        private val toDetailWorkPlace: (Int) -> Unit,
    ) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(entity: List<SubFieldEntity>) {

            with(itemBinding) {

                val companyName = entity.firstOrNull { workPlaceEntity ->
                    workPlaceEntity.code == COMPANY_TITLE
                }?.values?.joinToString()

                val speciality = entity.firstOrNull { workPlaceEntity ->
                    workPlaceEntity.code == SPECIALITY
                }?.values?.joinToString()

                val dateStartWork = entity.firstOrNull { workPlaceEntity ->
                    workPlaceEntity.code == START_WORK
                }?.values?.joinToString()

                val dateEndWork = entity.firstOrNull { workPlaceEntity ->
                    workPlaceEntity.code == END_WORK
                }?.values?.joinToString()

                val isWorkingNow = entity.firstOrNull { workPlaceEntity ->
                    workPlaceEntity.code == WORKING_NOW
                }?.values?.firstOrNull().asBoolean()

                val workPeriod = DateUtils.calculateWorkPeriod(
                    from = dateStartWork,
                    to = if (isWorkingNow) null else dateEndWork,
                    context = root.context
                )

                val workDate = root.context.getString(
                    R.string.questionnaire_screen_work_experience_work_period,
                    dateStartWork,
                    if (isWorkingNow) {
                        root.context.getString(R.string.questionnaire_screen_work_experience_working_now)
                    } else {
                        dateEndWork
                    }
                )

                companyNameText.text = companyName
                specialityText.text = speciality
                periodWorkText.text = if (workPeriod.isNullOrEmpty()) workDate else "$workDate ($workPeriod)"

                companyNameText.isVisible = !companyName.isNullOrEmpty()
                specialityText.isVisible = !speciality.isNullOrEmpty()
                periodWorkText.isVisible = workDate.isNotEmpty()

                root.setOnSingleClickListener { toDetailWorkPlace(bindingAdapterPosition) }
            }
        }
    }

    class DiffWorkPlace : DiffUtil.ItemCallback<List<SubFieldEntity>>() {

        override fun areItemsTheSame(
            oldItem: List<SubFieldEntity>,
            newItem: List<SubFieldEntity>,
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: List<SubFieldEntity>,
            newItem: List<SubFieldEntity>,
        ): Boolean {
            return oldItem == newItem
        }

    }

    private companion object {
        const val COMPANY_TITLE = "COMPANY_TITLE"
        const val SPECIALITY = "SPECIALITY"
        const val START_WORK = "START_WORK"
        const val END_WORK = "END_WORK"
        const val WORKING_NOW = "WORKING_NOW"
    }
}