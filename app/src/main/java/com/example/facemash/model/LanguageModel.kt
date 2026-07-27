package com.example.facemash.model

data class LanguageModel (
    val image: Int,
    val name: String,
    val languageCode: String,
    var isSelected: Boolean,
    var imgSelected:Int
)