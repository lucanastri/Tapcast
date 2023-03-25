package com.kizune.tapcast.player

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media3.common.*
import androidx.media3.common.Player.STATE_ENDED
import androidx.media3.common.Player.STATE_READY
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.kizune.tapcast.GlideApp
import com.kizune.tapcast.R

class TapcastService : MediaSessionService(), MediaSession.Callback {
    private var mediaSession: MediaSession? = null
    private lateinit var notificationManager: NotificationManager
    private var bitmap: Bitmap? = null

    private lateinit var playAction: NotificationCompat.Action
    private lateinit var pauseAction: NotificationCompat.Action
    private lateinit var nextAction: NotificationCompat.Action
    private lateinit var prevAction: NotificationCompat.Action

    companion object {
        const val CHANNEL_ID = "Tapcast Playback"
        const val NOTIFICATION_ID = 104
        const val CHANNEL_NAME = "Media Channel"
        const val PLAY_ACTION = "com.kizune.tapcast.PLAY"
        const val PAUSE_ACTION = "com.kizune.tapcast.PAUSE"
        const val NEXT_ACTION = "com.kizune.tapcast.NEXT"
        const val PREV_ACTION = "com.kizune.tapcast.PREV"
    }

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(AudioAttributes.DEFAULT, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()
        mediaSession = MediaSession.Builder(this, player).setCallback(this).build()

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.setShowBadge(true)
            notificationChannel.enableVibration(false)
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationManager.createNotificationChannel(notificationChannel)
        }

        playAction = TapcastPlayerAction.getPlayAction(this)
        pauseAction = TapcastPlayerAction.getPauseAction(this)
        nextAction = TapcastPlayerAction.getNextAction(this)
        prevAction = TapcastPlayerAction.getPrevAction(this)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    /**
     * Needed to start the playback, it doesn't work passing uri
     */
    override fun onAddMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: MutableList<MediaItem>
    ): ListenableFuture<MutableList<MediaItem>> {
        val updatedMediaItems =
            mediaItems.map {
                loadArtworkBitmap(it.mediaMetadata)
                it.buildUpon().setUri(Uri.parse(it.mediaId)).build()
            }.toMutableList()
        return Futures.immediateFuture(updatedMediaItems)
    }

    private fun loadArtworkBitmap(metadata: MediaMetadata) {
        val thumbnailRef = Firebase.storage.reference
            .child(metadata.albumTitle.toString())
            .child(metadata.artworkUri.toString())

        GlideApp.with(this)
            .asBitmap()
            .load(thumbnailRef)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    bitmap = resource
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            PLAY_ACTION -> mediaSession?.player?.play()
            PAUSE_ACTION -> mediaSession?.player?.pause()
            NEXT_ACTION -> mediaSession?.player?.seekToNext()
            PREV_ACTION -> mediaSession?.player?.seekToPrevious()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    @androidx.annotation.OptIn(UnstableApi::class)
    override fun onUpdateNotification(session: MediaSession) {
        val metadata = session.player.mediaMetadata

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setLargeIcon(bitmap)
            .setContentTitle(metadata.title)
            .setContentText(metadata.subtitle)
            .setOnlyAlertOnce(true)
            .setStyle(MediaStyleNotificationHelper.MediaStyle(session))

        if (session.player.hasPreviousMediaItem()) notificationBuilder.addAction(prevAction)
        if (session.player.isPlaying) {
            notificationBuilder.setOngoing(true)
            notificationBuilder.addAction(pauseAction)
        } else {
            notificationBuilder.setOngoing(true)
            notificationBuilder.addAction(playAction)
        }

        if (session.player.hasNextMediaItem()) notificationBuilder.addAction(nextAction)

        if(session.player.playbackState == STATE_READY || session.player.playbackState == STATE_ENDED)
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}