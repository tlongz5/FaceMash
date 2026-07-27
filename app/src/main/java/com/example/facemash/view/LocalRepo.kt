package com.example.facemash.view

import android.content.Context
import com.example.facemash.R
import com.example.facemash.model.TrendingData
import com.example.facemash.utils.getJsonFromRaw
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LocalRepo @Inject constructor(
    @ApplicationContext private val context: Context
){
    suspend fun getTrendingData(): List<TrendingData> {
        val trendingDataJson = getJsonFromRaw(context, R.raw.home_trending_data)
        val trendingData = Gson().fromJson(trendingDataJson, Array<TrendingData>::class.java).toList()
        return trendingData
    }
}