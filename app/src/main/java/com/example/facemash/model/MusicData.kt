package com.example.baseproject.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MusicData(
    val id: String = "",
    val name: String = "",
    val isDefault: Boolean = false,
    val type: String = "",
    val url: String = ""
): Parcelable
