package com.example.facemash.model

data class TrendingData(
    val id: String? = null,
    val name: String? = null,
    val hasTag: String? = null,
    val filterType: String? = null,
    val createUser: String? = null,
    val heartCount: String? = null,
    val isRemote: Boolean? = null,
    val image: String? = null,
    val previewImage: String? = null,
    val isDownload: Boolean = false,
    val video: String? = null,
    val type: Int = 0
)
