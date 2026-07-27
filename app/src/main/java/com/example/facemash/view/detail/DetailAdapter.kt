package com.example.facemash.view.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.facemash.databinding.ItemDetailBinding
import com.example.facemash.model.TrendingData
import com.example.facemash.utils.PlayerInstance
import com.example.facemash.utils.PlayerInstance.play

class DetailAdapter(
    private val videoList: List<TrendingData>,
    private val callbackOnClickPlay: (TrendingData) -> Unit
    ): RecyclerView.Adapter<DetailAdapter.DetailViewHolder>() {
    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): DetailViewHolder {
        val binding = ItemDetailBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return DetailViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: DetailViewHolder,
        position: Int
    ) {
        holder.binding.tvTag.text = videoList[position].hasTag
        holder.binding.btnCreate.setOnClickListener {
            callbackOnClickPlay(videoList[position])
        }
    }

    override fun onViewRecycled(holder: DetailViewHolder) {
        super.onViewRecycled(holder)
        holder.binding.playerView.player =null
    }

    override fun getItemCount(): Int {
        return videoList.size
    }


    class DetailViewHolder(val binding: ItemDetailBinding): RecyclerView.ViewHolder(binding.root)

}