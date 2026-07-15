package com.example.facemash.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseListAdapter<T:Any, VB: ViewBinding>(
    diffCallBack: BaseDiffCallBack<T> = BaseDiffCallBack(),
    private val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB,
    private val onItemClick: (T) -> Unit = {},
    private val onItemLongClick: (T) -> Unit = {}
): ListAdapter<T, BaseListAdapter.BaseViewHolder<VB>>(diffCallBack) {
    class BaseViewHolder<VB: ViewBinding>(val binding: VB): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<VB> {
        val binding = bindingInflater.invoke(LayoutInflater.from(parent.context), parent, false)
        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<VB>, position: Int){
        holder.binding.root.setOnClickListener{
            onItemClick.invoke(getItem(position))
        }
        holder.binding.root.setOnLongClickListener{
            onItemLongClick.invoke(getItem(position))
            true
        }
        onBind(holder.binding, getItem(position))
    }

    protected fun onBind(binding: VB, item: T){

    }


}