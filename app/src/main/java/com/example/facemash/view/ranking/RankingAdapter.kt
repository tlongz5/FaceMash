package com.example.facemash.view.ranking

import com.example.facemash.base.BaseDiffCallBack
import com.example.facemash.base.BaseListAdapter
import com.example.facemash.databinding.ItemRankingBinding
import com.example.facemash.utils.Constants
import com.example.facemash.utils.setAssetImageToDrawable

class RankingAdapter(
    private val onItemClick: (String, Int) -> Unit
) : BaseListAdapter<String, ItemRankingBinding>(
    diffCallBack = object : BaseDiffCallBack<String>() {},
    bindingInflater = ItemRankingBinding::inflate,
    onItemClick = onItemClick
) {

    override fun onBind(binding: ItemRankingBinding, item: String, position: Int) {
        binding.tvGameName.text = Constants.rankingTypeMultiList[position]
        binding.imgGameAvatar.setImageDrawable(setAssetImageToDrawable(binding.root.context,item))
    }
}