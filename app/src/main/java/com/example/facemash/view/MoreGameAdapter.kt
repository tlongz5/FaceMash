package com.example.facemash.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.facemash.databinding.ItemMoreGameBinding
import com.example.magicface.model.Game

class MoreGameAdapter(private val onClick: (Pair<Int, String>) -> Unit) :
    ListAdapter<Pair<Int, String>, MoreGameAdapter.MoreGameViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoreGameViewHolder {
        val binding = ItemMoreGameBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MoreGameViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MoreGameViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MoreGameViewHolder(private val binding: ItemMoreGameBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(game: Pair<Int, String>) {
            binding.tvGame.text = game.second
            
            Glide.with(binding.root.context)
                .load(game.first)
                .into(binding.imgGame)

            binding.root.setOnClickListener {
                onClick(game)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Pair<Int, String>>() {
        override fun areItemsTheSame(oldItem: Pair<Int, String>, newItem: Pair<Int, String>): Boolean {
            return oldItem.second == newItem.second
        }
        override fun areContentsTheSame(oldItem: Pair<Int, String>, newItem: Pair<Int, String>): Boolean {
            return oldItem == newItem
        }
    }
}
