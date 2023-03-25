package com.kizune.tapcast.player

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer

/**
 * Singleton class for accessing player
 */
class TapcastPlayer {
    companion object {
        @Volatile
        private var INSTANCE: ExoPlayer? = null

        fun getInstance(context: Context): ExoPlayer =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildPlayer(context).also { INSTANCE = it }
            }

        private fun buildPlayer(context: Context) =
            ExoPlayer.Builder(context).build()
    }
}