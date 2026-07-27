package com.example.facemash.base

import androidx.recyclerview.widget.DiffUtil

open class BaseDiffCallBack<T:Any> (
    private val areItemsTheSame: (T, T) -> Boolean = { old, new -> old == new },
    private val areContentsTheSame: (T, T) -> Boolean = { old, new -> old == new }
): DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return areItemsTheSame(oldItem, newItem)
    }

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return areContentsTheSame(oldItem, newItem)
    }
}