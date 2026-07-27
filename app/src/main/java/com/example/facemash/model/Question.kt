package com.example.baseproject.model

import kotlin.random.Random

data class Question(
    val question: String = "",
    val trueAnswer: String = "",
    val falseAnswer: String = "",
    var trueSide: Int = Random.nextInt(2)
)