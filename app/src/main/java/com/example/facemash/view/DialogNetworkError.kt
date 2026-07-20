package com.example.facemash.view

import android.R
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.example.facemash.databinding.DialogNetworkErrorBinding

class DialogNetworkError(context: Context): AlertDialog(context) {
    private lateinit var binding: DialogNetworkErrorBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = DialogNetworkErrorBinding.inflate(layoutInflater)
        setView(binding.root)
        super.onCreate(savedInstanceState)
        setCancelable(false)
        window?.setBackgroundDrawableResource(R.color.transparent)
    }
}