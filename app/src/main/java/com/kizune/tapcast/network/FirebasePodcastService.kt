package com.kizune.tapcast.network

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kizune.tapcast.model.Episode
import com.kizune.tapcast.model.Episode.Companion.toEpisode
import com.kizune.tapcast.model.Podcast
import com.kizune.tapcast.model.Podcast.Companion.toPodcast
import com.kizune.tapcast.model.PodcastCategory
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

object FirebasePodcastService {
    fun getPodcasts(): Flow<List<PodcastCategory>> {
        val firestore = Firebase.firestore
        return callbackFlow {
            val listenerRegistration = firestore.collection("Podcast")
                .addSnapshotListener { querySnapshot: QuerySnapshot?, exception: FirebaseFirestoreException? ->
                    if (exception != null) {
                        cancel(message = "Error fetching podcasts", cause = exception)
                        return@addSnapshotListener
                    }
                    val map = querySnapshot!!.documents.mapNotNull { it.toPodcast() }.groupBy { it.genre }
                    val list = map.map { PodcastCategory(it.key, it.value) }
                    trySend(list)
                }
            awaitClose {
                Log.d("MyTag", "Cancelling multiple podcast listener")
                listenerRegistration.remove()
            }
        }
    }

    fun getPodcast(podcastID: String): Flow<Podcast> {
        val firestore = Firebase.firestore
        return callbackFlow {
            val listenerRegistration = firestore.collection("Podcast").document(podcastID)
                .addSnapshotListener { documentSnapshot: DocumentSnapshot?, exception: FirebaseFirestoreException? ->
                    if (exception != null) {
                        cancel(message = "Error fetching podcast", cause = exception)
                        return@addSnapshotListener
                    }
                    val podcast = documentSnapshot!!.toPodcast()
                    if (podcast != null) {
                        trySend(podcast)
                    }
                }
            awaitClose {
                Log.d("MyTag", "Cancelling podcast listener")
                listenerRegistration.remove()
            }
        }
    }

    fun getEpisodes(podcastID: String): Flow<List<Episode>> {
        val firestore = Firebase.firestore
        return callbackFlow {
            val listenerRegistration = firestore.collection("Episode")
                .whereEqualTo("parent", podcastID)
                .addSnapshotListener { querySnapshot: QuerySnapshot?, exception: FirebaseFirestoreException? ->
                    if (exception != null) {
                        cancel(message = "Error fetching episodes", cause = exception)
                        return@addSnapshotListener
                    }
                    val list = querySnapshot!!.documents.mapNotNull { it.toEpisode() }.sortedByDescending { it.date }
                    trySend(list)
                }
            awaitClose {
                Log.d("MyTag", "Cancelling episodes listener")
                listenerRegistration.remove()
            }
        }
    }
}