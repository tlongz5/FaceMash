package com.example.facemash.view.main

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baseproject.model.MusicData
import com.example.facemash.R
import com.example.facemash.view.LocalRepo
import com.example.facemash.model.Game
import com.example.facemash.model.TrendingData
import com.example.facemash.utils.getImagesFromAsset
import com.example.facemash.utils.getJsonFromRaw
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val localRepo: LocalRepo,
    @ApplicationContext private val context: Context
): ViewModel() {
    val game = listOf(
        R.drawable.img_ranking to "Ranking",
        R.drawable.img_this_or_that to "This Or That",
        R.drawable.img_lip_poses to "Lip Pose",
        R.drawable.img_princess_or_witch to "Princess Or Witch"
    )

    val ranking = getImagesFromAsset(context)
    val firstElementsList: List<String> = ranking.mapNotNull { subList ->
        subList.firstOrNull()
    }

    val multipliedList = List(10) { firstElementsList }.flatten()



    private val _gamesData = MutableLiveData<List<Game>>()
    val gamesData: LiveData<List<Game>> get() = _gamesData

    private val _dataMusicFromAssets = MutableLiveData<List<MusicData>>()
    val dataMusicFromAssets: LiveData<List<MusicData>> get() = _dataMusicFromAssets

    private val _selectedMusic = MutableLiveData<MusicData?>()
    val selectedMusic: LiveData<MusicData?> get() = _selectedMusic

    private val _trendingData = MutableLiveData<List<TrendingData>>()
    val trendingData: LiveData<List<TrendingData>> get() = _trendingData

    fun getTrendingData() = viewModelScope.launch {
        runCatching {
            _trendingData.value = localRepo.getTrendingData()
        }.onFailure { e->
            e.printStackTrace()
        }
    }

    fun getGamesData(context: Context) {
        getJsonFromRaw(context, R.raw.games_data)?.let { list ->
            val listGames = Gson().fromJson(list, Array<Game>::class.java).toList()
            _gamesData.value = listGames.filter { it.type == "Text" }
        }
    }

    fun getMusicData(context: Context) {
        getJsonFromRaw(context, R.raw.music_data)?.let {
            _dataMusicFromAssets.value =
                Gson().fromJson(it, Array<MusicData>::class.java).toList()
        }
    }

    fun setSelectedMusic(musicData: MusicData?){
        _selectedMusic.value = musicData
    }
}