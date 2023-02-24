package com.example.presentations.questionnaire.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.databinding.ItemQuestionnaireSingleSelectBinding
import com.example.presentations.questionnaire.domain.entity.ItemSelectorEntity


class SingleSelectorAdapter(
    private val selectedItem: (ItemSelectorEntity) -> Unit,
) : ListAdapter<ItemSelectorEntity, SingleSelectorAdapter.SingleSelectorHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleSelectorHolder {
        return SingleSelectorHolder(
            itemBinding = ItemQuestionnaireSingleSelectBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: SingleSelectorHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SingleSelectorHolder(
        private val itemBinding: ItemQuestionnaireSingleSelectBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(entity: ItemSelectorEntity) {

            with(itemBinding.root) {
                text = entity.value
                isChecked = entity.isSelected

                setOnClickListener {
                    selectedItem(currentList[bindingAdapterPosition].copy(isSelected = isChecked))
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ItemSelectorEntity>() {

        override fun areItemsTheSame(
            oldItem: ItemSelectorEntity,
            newItem: ItemSelectorEntity,
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: ItemSelectorEntity,
            newItem: ItemSelectorEntity,
        ): Boolean {
            return oldItem.value == newItem.value || oldItem.isSelected == newItem.isSelected
        }

    }
}