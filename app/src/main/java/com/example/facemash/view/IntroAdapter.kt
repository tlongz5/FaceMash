package com.example.facemash.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.facemash.databinding.ItemIntroBinding
import com.example.facemash.utils.Helper.introList

class IntroAdapter : RecyclerView.Adapter<IntroAdapter.IntroViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): IntroViewHolder {
        val binding = ItemIntroBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IntroViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: IntroViewHolder,
        position: Int
    ) {
        with(holder.binding) {
            viewPager.setImageResource(introList[position].image)
            tvName.text = introList[position].name
            tvDes.text = introList[position].des
        }
    }

    override fun getItemCount(): Int {
        return 3
    }

    inner class IntroViewHolder(val binding: ItemIntroBinding) :
        RecyclerView.ViewHolder(binding.root)
}