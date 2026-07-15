package com.example.baseproject.model

data class Question(
    val question: String = "",
    val trueAnswer: String = "",
    val falseAnswer: String = "",
    var trueSide: Int = 0
)