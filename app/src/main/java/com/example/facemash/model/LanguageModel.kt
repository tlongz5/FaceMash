package com.example.magicface.model

data class LanguageModel (
    val image: Int,
    val name: String,
    val languageCode: String,
    var isSelected: Boolean,
    var imgSelected:Int
)