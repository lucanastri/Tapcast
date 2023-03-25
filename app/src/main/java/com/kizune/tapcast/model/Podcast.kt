package com.kizune.tapcast.model

import android.os.Parcelable
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.parcelize.Parcelize

@Parcelize
data class Podcast(
    val podcastID: String = "ID",
    val title: String = "",
    val authors: List<String> = emptyList(),
    val description: String = "",
    val genre: String = "",
    val duration: Long = 0L,
    val date: Long = 0L,
    val thumbnailURL: String = "THUMBNAIL"
) : Parcelable {
    companion object {
        fun DocumentSnapshot.toPodcast(): Podcast? {
            return try {
                val title = getString("title")!!
                val authors: List<String> = get("authors") as List<String>
                val description = getString("description")!!
                val genre = getString("genre")!!
                val duration = getLong("duration")!!
                val date = getTimestamp("date")!!.toDate().time
                val thumbnailURL = getString("thumbnailURL") ?: "thumbnail.png"
                Podcast(id, title, authors, description, genre, duration, date, thumbnailURL)
            } catch (e: Exception) {
                Log.e("MyTag", "Error converting podcast object:", e)
                null
            }
        }
    }
}
