package com.example.facemash.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseListAdapter<T:Any, VB: ViewBinding>(
    diffCallBack: BaseDiffCallBack<T> = BaseDiffCallBack(),
    private val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB,
    private val onItemClick: (T, Int) -> Unit = {_,_ ->},
    private val onItemLongClick: (T, Int) -> Unit = {_,_ ->}
): ListAdapter<T, BaseListAdapter.BaseViewHolder<VB>>(diffCallBack) {
    class BaseViewHolder<VB: ViewBinding>(val binding: VB): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<VB> {
        val binding = bindingInflater.invoke(LayoutInflater.from(parent.context), parent, false)
        val holder = BaseViewHolder(binding)
        holder.binding.root.setOnClickListener{
            val position = holder.bindingAdapterPosition
            onItemClick.invoke(getItem(position),position)
        }
        holder.binding.root.setOnLongClickListener{
            val position = holder.bindingAdapterPosition
            onItemLongClick.invoke(getItem(position),position)
            true
        }
        return holder
    }

    override fun onBindViewHolder(holder: BaseViewHolder<VB>, position: Int){
        onBind(holder.binding, getItem(position), position)
    }

    protected abstract fun onBind(binding: VB, item: T, position: Int)



}