package com.example.facemash.view

import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.DiffUtil
import com.example.facemash.base.BaseDiffCallBack
import com.example.facemash.base.BaseListAdapter
import com.example.facemash.databinding.ItemRankingBinding
import com.example.facemash.utils.Constants
import com.example.facemash.utils.setAssetImageToDrawable
import java.io.IOException

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