package com.example.facemash.utils

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

object MusicPlayerInstance {
    var player: ExoPlayer? = null
    fun initPlayer(context: Context) {
        if (player == null) {
            player = ExoPlayer.Builder(context).build().apply {
                playWhenReady = true
                repeatMode = ExoPlayer.REPEAT_MODE_ONE
            }
        }
    }

    fun play(url: String) {
        if (url.isEmpty()) return
        val mediaItem = if (url.startsWith("http") || url.startsWith("content") || url.startsWith("asset")|| url.startsWith("https")) {
            MediaItem.fromUri(url)
        } else {
            MediaItem.fromUri("asset:///$url")
        }
        player?.stop()
        player?.clearMediaItems()
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.play()
    }

    fun pause() {
        try {
            player?.pause()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        try {
            player?.stop()
            player?.clearMediaItems()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
