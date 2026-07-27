package com.example.facemash.view.hometrending

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.facemash.R
import com.example.facemash.databinding.ItemHomeTrendingTagBinding

class HomeTrendingTagAdapter(
    private val tags: List<String>,
    private val onTagClick: (Int) -> Unit
) : RecyclerView.Adapter<HomeTrendingTagAdapter.ViewHolder>() {
    private var positionSelected = 0
    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemHomeTrendingTagBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        viewHolder: ViewHolder,
        position: Int
    ) {
        val tag = tags[position]
        if (position == positionSelected) {
            viewHolder.binding.btnTag.setBackgroundColor(viewHolder.binding.root.context.getColor(R.color.white))
            viewHolder.binding.btnTag.setTextColor(viewHolder.binding.root.context.getColor(R.color.tv_tag_selected))
        }else{
            viewHolder.binding.btnTag.setBackgroundColor(viewHolder.binding.root.context.getColor(R.color.bg_tag))
            viewHolder.binding.btnTag.setTextColor(viewHolder.binding.root.context.getColor(R.color.white))
        }
        viewHolder.binding.btnTag.text = tag
        viewHolder.binding.btnTag.setOnClickListener {
            onTagClick(position)
            val curPosition = viewHolder.bindingAdapterPosition
            positionSelected = curPosition
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return tags.size
    }

    class ViewHolder(val binding: ItemHomeTrendingTagBinding) : RecyclerView.ViewHolder(binding.root)

}