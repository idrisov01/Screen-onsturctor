package com.example.presentations.questionnaire.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.common.extension.setOnSingleClickListener
import com.example.databinding.ItemSubfieldBinding
import com.example.presentations.questionnaire.domain.entity.SubFieldEntity


class SubFieldAdapter(
    private val toDetailSubField: (Int) -> Unit,
) : ListAdapter<List<SubFieldEntity>, SubFieldAdapter.SubFieldHolder>(DiffSubField()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubFieldHolder {
        return SubFieldHolder(
            itemBinding = ItemSubfieldBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ),
            toDetailSubField = toDetailSubField
        )
    }

    override fun onBindViewHolder(holder: SubFieldHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SubFieldHolder(
        private val itemBinding: ItemSubfieldBinding,
        private val toDetailSubField: (Int) -> Unit,
    ) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(entity: List<SubFieldEntity>) {

            with(itemBinding) {

                titleTextView.text = entity.firstOrNull()?.values?.joinToString()
                descriptionTextView.text = entity.getOrNull(1)?.values?.joinToString()

                root.setOnSingleClickListener { toDetailSubField(bindingAdapterPosition) }

            }
        }
    }

    class DiffSubField : DiffUtil.ItemCallback<List<SubFieldEntity>>() {

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
}