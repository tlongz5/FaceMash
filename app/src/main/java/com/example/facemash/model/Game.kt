package com.example.facemash.model

import com.example.baseproject.model.Question

data class Game(
    var id: String = "",
    val categoryItem: String = "",
    val name: String = "",
    var type: String = "",
    val category: String = "",
    val questions: List<Question> = listOf(),
    val thumb: String = "",
    var level: String = "",
    var homeThumb: String = "",
    var isFamous: Boolean = false
)