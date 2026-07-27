package com.example.facemash.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.baseproject.model.MusicData
import com.example.facemash.R
import com.example.facemash.databinding.ItemSongPickerBinding
import com.example.facemash.utils.hide

class SongPickerAdapter(
    private val onClickItem: (MusicData) -> Unit
): ListAdapter<MusicData, SongPickerAdapter.ViewHolder>(DiffCallback) {
    class ViewHolder(val binding: ItemSongPickerBinding): RecyclerView.ViewHolder(binding.root)
    var selectedPosition = 0
    private var context: Context? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemSongPickerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val item = getItem(position)

        holder.binding.apply {
            songName.isSelected = true
            if (selectedPosition == position) {
                mainView.setBackgroundResource(R.drawable.bg_song_item_selected)
                imgPlay.setImageResource(R.drawable.ic_pause)
                imgSong.setImageResource(R.drawable.ic_song_active)
                context?.let {
                    songName.setTextColor(ContextCompat.getColor(it, R.color.white))
                }
            } else {
                mainView.setBackgroundResource(R.drawable.bg_song_item)
                imgPlay.setImageResource(R.drawable.ic_play_song)
                imgSong.setImageResource(R.drawable.ic_song_inactive)
                context?.let {
                    songName.setTextColor(ContextCompat.getColor(it, R.color.black))
                }
            }

            if (item.name.isEmpty()) {
                songName.text = "No Sound"
                imgPlay.hide()
                imgSong.setImageResource(R.drawable.img_no_song)
            } else {
                songName.text = item.name
            }
            mainView.setOnClickListener {
                val newPosition = holder.bindingAdapterPosition

                val oldPosition = selectedPosition
                selectedPosition = newPosition

                notifyItemChanged(oldPosition)
                notifyItemChanged(newPosition)

                onClickItem.invoke(item)
            }
        }
    }
    companion object{
        private val DiffCallback = object : DiffUtil.ItemCallback<MusicData>(){
            override fun areItemsTheSame(oldItem: MusicData, newItem: MusicData): Boolean {
                return oldItem.id == newItem.id
            }
            override fun areContentsTheSame(oldItem: MusicData, newItem: MusicData): Boolean {
                return oldItem == newItem
            }
        }
    }
}