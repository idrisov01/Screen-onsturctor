package com.example.presentations.questionnaire.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.databinding.ItemQuestionnaireSpinnerBinding


class SpinnerAdapter(
    private val selectValue: (String?, String) -> Unit,
) : ListAdapter<String, SpinnerAdapter.SpinnerHolder>(DiffSpinner()) {

    var fieldId: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpinnerHolder {
        return SpinnerHolder(
            itemBinding = ItemQuestionnaireSpinnerBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: SpinnerHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SpinnerHolder(
        private val itemBinding: ItemQuestionnaireSpinnerBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(value: String) {
            with(itemBinding.root) {
                text = value
                setOnClickListener { selectValue(fieldId, value) }
            }
        }
    }

    class DiffSpinner : DiffUtil.ItemCallback<String>() {

        override fun areItemsTheSame(
            oldItem: String,
            newItem: String,
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: String,
            newItem: String,
        ): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }

}