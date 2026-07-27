package com.example.facemash.view.hometrending

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.facemash.databinding.ItemHomeTrendingFilterBinding
import com.example.facemash.model.TrendingData

class HomeTrendingFilterAdapter(
    private val clickItem: (TrendingData, Int) -> Unit
) : ListAdapter<TrendingData, HomeTrendingFilterAdapter.ViewHolder>(diffCallback){
    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemHomeTrendingFilterBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        viewHolder: ViewHolder,
        position: Int
    ) {
        val trendingData = getItem(position)
        with(viewHolder.binding) {
            tvViewCount.text = trendingData.heartCount
            Glide.with(root.context)
                .load(trendingData.image)
                .into(imgThumb)

            root.setOnClickListener {
                clickItem(trendingData, position)
            }
        }
    }

    class ViewHolder(val binding: ItemHomeTrendingFilterBinding): RecyclerView.ViewHolder(binding.root)
    companion object{
         val diffCallback = object : DiffUtil.ItemCallback<TrendingData>(){
            override fun areItemsTheSame(oldItem: TrendingData, newItem: TrendingData): Boolean {
                return oldItem.id == newItem.id
            }
            override fun areContentsTheSame(oldItem: TrendingData, newItem: TrendingData): Boolean {
                return oldItem == newItem
            }
        }
    }


}