package com.kizune.tapcast.model

import android.os.Parcelable
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.parcelize.Parcelize

@Parcelize
data class Episode(
    val podcastID: String = "PODCASTID",
    val episodeID: String = "EPISODEID",
    val title: String = "",
    val description: String = "",
    val date: Long = 0L,
    val duration: Long = 0L,
) : Parcelable {
    companion object {
        fun DocumentSnapshot.toEpisode(): Episode? {
            return try {
                val podcastID = getString("parent")!!
                val episodeID = getString("episodeID")!!
                val title = getString("title")!!
                val description = getString("description")!!
                val date = getTimestamp("date")!!.toDate().time
                val duration = getLong("duration")!!
                Episode(podcastID, episodeID, title, description, date, duration)
            } catch (e: Exception) {
                Log.e("MyTag", "Error converting episode object:", e)
                null
            }
        }
    }
}
