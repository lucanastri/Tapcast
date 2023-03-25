package com.kizune.tapcast.viewmodel

import androidx.lifecycle.*
import com.kizune.tapcast.model.Episode
import com.kizune.tapcast.model.Podcast
import com.kizune.tapcast.model.PodcastCategory
import com.kizune.tapcast.model.PodcastWithEpisodesAndUri
import com.kizune.tapcast.network.FirebasePodcastService
import com.kizune.tapcast.network.FirebaseStorageService
import kotlinx.coroutines.flow.*

class PodcastViewModel(
    private val state: SavedStateHandle
) : ViewModel() {
    val podcastID = state.getStateFlow("podcastID", "")

    val podcasts: Flow<List<PodcastCategory>> = flow {
        val data = FirebasePodcastService.getPodcasts()
        emitAll(data)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    val podcast: Flow<Podcast> = flow {
        val data = FirebasePodcastService.getPodcast(podcastID.value)
        emitAll(data)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = Podcast()
    )

    val episodes: Flow<List<Episode>> = flow {
        val data = FirebasePodcastService.getEpisodes(podcastID.value)
        emitAll(data)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    val podcastWithEpisodesAndUri: Flow<PodcastWithEpisodesAndUri?> = flow {
        val data = FirebaseStorageService.getPodcastWithEpisodesAndUri(podcastID.value)
        emitAll(data)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = null
    )

    fun setSelectedPodcast(podcast: Podcast) {
        state["podcastID"] = podcast.podcastID
    }

    fun saveState() {
        state["podcastID"] = podcastID.value
    }
}
