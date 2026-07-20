package com.example.facemash.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.facemash.R
import com.example.facemash.databinding.ItemLanguageBinding
import com.example.magicface.model.LanguageModel

class LanguageAdapter(private val onItemClick: (Int?, Int) -> Unit) :
    ListAdapter<LanguageModel, LanguageAdapter.LanguageViewHolder>(LanguageDiffCallback()) {
    var curPosition: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val binding =
            ItemLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LanguageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        val language = getItem(position)
        holder.itemView.setOnClickListener {
            onItemClick(curPosition, position)
            curPosition = position
        }
        with(holder.binding) {
            imgFlag.setImageResource(language.image)
            tvLanguage.text = language.name
            if (language.isSelected) {
                imgBackground.setImageResource(language.imgSelected)
                tvLanguage.setTextColor(tvLanguage.resources.getColor(R.color.white))
            } else {
                imgBackground.setImageResource(R.drawable.bg_item_language)
                tvLanguage.setTextColor(tvLanguage.resources.getColor(R.color.black))
            }
        }
    }

    inner class LanguageViewHolder(val binding: ItemLanguageBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }

    class LanguageDiffCallback : DiffUtil.ItemCallback<LanguageModel>() {
        override fun areItemsTheSame(oldItem: LanguageModel, newItem: LanguageModel): Boolean {
            return oldItem.languageCode == newItem.languageCode
        }

        override fun areContentsTheSame(oldItem: LanguageModel, newItem: LanguageModel): Boolean {
            return oldItem == newItem
        }
    }
}
