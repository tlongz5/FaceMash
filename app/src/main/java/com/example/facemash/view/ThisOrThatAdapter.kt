package com.example.facemash.view

import com.example.facemash.R
import com.example.facemash.base.BaseDiffCallBack
import com.example.facemash.base.BaseListAdapter
import com.example.facemash.databinding.ItemRankingBinding
import com.example.facemash.model.Game

class ThisOrThatAdapter(
    private val onItemClick: (Game, Int) -> Unit
) : BaseListAdapter<Game, ItemRankingBinding>(
    diffCallBack = object : BaseDiffCallBack<Game>() {},
    bindingInflater = ItemRankingBinding::inflate,
    onItemClick = onItemClick
) {

    override fun onBind(binding: ItemRankingBinding, item: Game, position: Int) {
        binding.tvGameName.text = if (item.categoryItem.isNotEmpty()) item.categoryItem else item.name
        binding.imgGameAvatar.setImageResource(R.drawable.img_this_or_that)
    }
}
