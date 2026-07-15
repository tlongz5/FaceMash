//package com.example.magicface.utils
//
//import android.content.Context
//import android.net.Uri
//import androidx.media3.common.MediaItem
//import androidx.media3.exoplayer.ExoPlayer
//import java.io.File
//
//object PlayerInstance {
//    var player: ExoPlayer? = null
//    fun initPlayer(context: Context) {
//        player = ExoPlayer.Builder(context).build().apply {
//            playWhenReady = true
//            repeatMode = ExoPlayer.REPEAT_MODE_ONE
//        }
//    }
//
//    fun play(url:String){
//        val mediaItem = if (url.startsWith("http") || url.startsWith("content")) {
//            MediaItem.fromUri(url)
//        } else {
//            MediaItem.fromUri(Uri.fromFile(File(url)))
//        }
//        player?.setMediaItem(mediaItem)
//        player?.prepare()
//        player?.play()
//    }
//
//    fun pause(){
//        try {
//            player?.pause()
//        }catch (e:Exception){
//            e.printStackTrace()
//        }
//    }
//
//    fun stop(){
//        try {
//            player?.stop()
//            player?.clearMediaItems()
//        }catch (e:Exception){
//            e.printStackTrace()
//        }
//    }
//}