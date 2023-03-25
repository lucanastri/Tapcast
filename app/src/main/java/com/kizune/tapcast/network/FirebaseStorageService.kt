package com.kizune.tapcast.network

import android.net.Uri
import android.util.Log
import androidx.media3.common.MediaItem
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.kizune.tapcast.model.Episode.Companion.toEpisode
import com.kizune.tapcast.model.Podcast
import com.kizune.tapcast.model.Podcast.Companion.toPodcast
import com.kizune.tapcast.model.PodcastWithEpisodesAndUri
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

object FirebaseStorageService {
    fun getPodcastWithEpisodesAndUri(podcastID: String): Flow<PodcastWithEpisodesAndUri?> {
        val firestore = Firebase.firestore
        val storage = Firebase.storage.reference
        return callbackFlow {
            val result = PodcastWithEpisodesAndUri()
            firestore.collection("Podcast")
                .document(podcastID)
                .addSnapshotListener podcastSnapshot@{ documentSnapshot, exception ->
                    if (exception != null) {
                        cancel(message = "Error fetching podcast document", cause = exception)
                        return@podcastSnapshot
                    }
                    val podcast = documentSnapshot!!.toPodcast()
                    result.podcast = podcast ?: Podcast()
                    firestore.collection("Episode")
                        .whereEqualTo("parent", podcast?.podcastID)
                        .addSnapshotListener episodeSnapshot@{ querySnapshot, error ->
                            if (error != null) {
                                cancel(message = "Error fetching episodes document", cause = error)
                                return@episodeSnapshot
                            }
                            val list = querySnapshot!!.documents.mapNotNull { it.toEpisode() }
                                .sortedByDescending { it.date }
                            //Per evitare che non si mandi il risultato quando non si hanno episodi
                            if(list.isEmpty()) trySend(result)
                            result.episodes = list
                            result.uris = MutableList(result.episodes.size) { Uri.parse("") }
                            list.forEachIndexed { index, episode ->
                                storage.child(episode.podcastID)
                                    .child(episode.episodeID + ".mp3")
                                    .downloadUrl.addOnSuccessListener { uri ->
                                        result.uris[index] = uri
                                        trySend(result)
                                    }.addOnFailureListener { exception ->
                                        Log.e("MyTag", "Download URL", exception)
                                        return@addOnFailureListener
                                    }
                            }
                        }
                }
            awaitClose {
                Log.d("MyTag", "Cancelling PodcastWithEpisodesAndUri listener")
            }
        }
    }
}