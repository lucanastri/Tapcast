package com.kizune.tapcast.model

import android.net.Uri

data class PodcastWithEpisodesAndUri(
    var podcast: Podcast = Podcast(),
    var episodes: List<Episode> = emptyList(),
    var uris: MutableList<Uri> = mutableListOf()
)
