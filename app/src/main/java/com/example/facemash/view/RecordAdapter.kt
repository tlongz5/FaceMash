package com.example.facemash.view

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.facemash.R
import com.example.facemash.databinding.ItemRecordBinding
import java.io.File
import androidx.core.graphics.toColorInt

class RecordAdapter(
    private val onItemClick: (File) -> Unit,
    private val onSelectionChanged: () -> Unit
) : ListAdapter<File, RecordAdapter.ViewHolder>(DiffCallback()) {

    var isSelectionMode = false
        set(value) {
            field = value
            if (!value) selectedFiles.clear()
            notifyDataSetChanged()
        }

    val selectedFiles = mutableSetOf<File>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = getItem(position)
        holder.bind(file)
    }

    inner class ViewHolder(val binding: ItemRecordBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(file: File) {
            Glide.with(binding.imgPhoto.context)
                .asBitmap()
                .load(file)
                .placeholder(R.drawable.bg_photo_round)
                .error(R.drawable.bg_photo_round)
                .into(binding.imgPhoto)

            if (isSelectionMode) {
                binding.viewSelected.visibility = View.VISIBLE
                binding.imgCheck.visibility = View.VISIBLE
                if (selectedFiles.contains(file)) {
                    binding.imgCheck.setImageResource(R.drawable.ic_selected_record)
                    binding.viewSelected.setBackgroundColor("#60000000".toColorInt())
                } else {
                    binding.imgCheck.setImageResource(R.drawable.ic_unselected_record)
                    binding.viewSelected.setBackgroundColor("#20FFFFFF".toColorInt())
                }
            } else {
                binding.viewSelected.visibility = View.GONE
                binding.imgCheck.visibility = View.GONE
            }

            binding.root.setOnClickListener {
                if (isSelectionMode) {
                    if (selectedFiles.contains(file)) {
                        selectedFiles.remove(file)
                    } else {
                        selectedFiles.add(file)
                    }
                    notifyItemChanged(bindingAdapterPosition)
                    onSelectionChanged()
                } else {
                    onItemClick(file)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<File>() {
        override fun areItemsTheSame(oldItem: File, newItem: File): Boolean = oldItem.absolutePath == newItem.absolutePath
        override fun areContentsTheSame(oldItem: File, newItem: File): Boolean = oldItem == newItem
    }
}