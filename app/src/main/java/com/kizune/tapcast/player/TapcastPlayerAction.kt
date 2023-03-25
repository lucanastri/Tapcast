package com.kizune.tapcast.player

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.kizune.tapcast.R

object TapcastPlayerAction {
    fun getPlayAction(context: Context): NotificationCompat.Action {
        val playIntent = Intent(context, TapcastService::class.java)
        playIntent.action = TapcastService.PLAY_ACTION
        return NotificationCompat.Action.Builder(
            R.drawable.icon_play,
            "Play",
            PendingIntent.getService(context, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        ).build()
    }
    
    fun getPauseAction(context: Context): NotificationCompat.Action {
        val pauseIntent = Intent(context, TapcastService::class.java)
        pauseIntent.action = TapcastService.PAUSE_ACTION
        return NotificationCompat.Action.Builder(
            R.drawable.icon_pause,
            "Pause",
            PendingIntent.getService(context, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        ).build()
    }
    
    fun getNextAction(context: Context): NotificationCompat.Action {
        val nextIntent = Intent(context, TapcastService::class.java)
        nextIntent.action = TapcastService.NEXT_ACTION
        return NotificationCompat.Action.Builder(
            R.drawable.icon_next,
            "Next",
            PendingIntent.getService(context, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        ).build()
    }

    fun getPrevAction(context: Context): NotificationCompat.Action {
        val prevIntent = Intent(context, TapcastService::class.java)
        prevIntent.action = TapcastService.PREV_ACTION
        return NotificationCompat.Action.Builder(
            R.drawable.icon_prev,
            "Prev",
            PendingIntent.getService(context, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        ).build()
    }
}