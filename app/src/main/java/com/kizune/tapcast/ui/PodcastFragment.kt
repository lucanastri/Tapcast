package com.kizune.tapcast.ui

import android.content.ComponentName
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.google.common.util.concurrent.MoreExecutors
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.kizune.tapcast.GlideApp
import com.kizune.tapcast.R
import com.kizune.tapcast.databinding.FragmentPodcastBinding
import com.kizune.tapcast.model.Podcast
import com.kizune.tapcast.model.PodcastWithEpisodesAndUri
import com.kizune.tapcast.player.TapcastService
import com.kizune.tapcast.utils.toFormattedDate
import com.kizune.tapcast.utils.toFormattedDuration
import com.kizune.tapcast.viewmodel.PodcastViewModel
import kotlinx.coroutines.launch


class PodcastFragment : Fragment(), MediaController.Listener {
    private var _binding: FragmentPodcastBinding? = null
    private val binding get() = _binding!!

    private lateinit var onNavigateUpCallback: OnBackPressedCallback
    private lateinit var mediaController: MediaController

    private val podcastViewModel: PodcastViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onNavigateUpCallback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().navigateUp()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPodcastBinding.inflate(inflater, container, false)
        //Prevents layout to not take enough space for text
        binding.root.visibility = View.INVISIBLE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.thumbnail.transitionName
        binding.backButton.setOnClickListener {
            onNavigateUpCallback.handleOnBackPressed()
        }

        binding.episodesLayout.setOnClickListener {
            val action = PodcastFragmentDirections.actionPodcastFragmentToEpisodesFragment()
            findNavController().navigate(action)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            podcastViewModel.podcastWithEpisodesAndUri.collect { item ->
                //Second condition prevents last podcast selected to appear again
                if (item != null && podcastViewModel.podcastID.value == item.podcast.podcastID) {
                    buildPodcastFragment(item.podcast)
                    if (item.episodes.isNotEmpty()) {
                        buildMediaController(item)
                    } else {
                        binding.episodesLayout.visibility = View.GONE
                    }
                }
            }
        }

        binding.playButton.setOnClickListener {
            playOrPause()
        }
    }

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            if (isAdded) {
                when (isPlaying) {
                    true -> {
                        binding.playButton.tag = getString(R.string.pause_text)
                        binding.playButton.text = getString(R.string.pause_text)
                    }
                    false -> {
                        binding.playButton.tag = getString(R.string.play_text)
                        binding.playButton.text = getString(R.string.play_text)
                    }
                }
            }
        }
    }

    private fun playOrPause() {
        if (::mediaController.isInitialized) {
            if (binding.playButton.tag == getString(R.string.play_text)) {
                mediaController.play()
            } else {
                mediaController.pause()
            }
        }
    }

    private fun buildPodcastFragment(item: Podcast) {
        binding.fragmentTitle.text = item.title
        binding.descriptionText.text = item.description
        binding.durationText.text = item.duration.toFormattedDuration()
        binding.dateText.text = item.date.toFormattedDate()
        binding.authorsText.text = item.authors.joinToString(", ")

        val thumbnailRef = Firebase.storage.reference
            .child(item.podcastID)
            .child(item.thumbnailURL)

        GlideApp.with(binding.root)
            .asBitmap()
            .load(thumbnailRef)
            .transition(BitmapTransitionOptions.withCrossFade())
            .into(binding.thumbnail)

        crossfade()
    }

    private fun buildMediaController(item: PodcastWithEpisodesAndUri) {
        val sessionToken = SessionToken(
            requireContext(),
            ComponentName(requireContext(), TapcastService::class.java)
        )

        val controllerFuture =
            MediaController.Builder(requireContext(), sessionToken).buildAsync()
        controllerFuture.addListener(
            {
                mediaController = controllerFuture.get()
                buildMediaItems(item)
                mediaController.prepare()

                mediaController.addListener(listener)
                //To set button correct state on resuming fragment
                listener.onIsPlayingChanged(mediaController.isPlaying)
            },
            MoreExecutors.directExecutor()
        )
    }

    private fun buildMediaItems(item: PodcastWithEpisodesAndUri) {
        if(mediaController.mediaItemCount > 0) {
            val mediaItem = mediaController.getMediaItemAt(0)
            val podcastID = mediaItem.mediaMetadata.albumTitle
            if(podcastID != item.podcast.podcastID) {
                buildMediaItemsList(item)
                mediaController.pause()
            }
        } else {
            buildMediaItemsList(item)
        }
    }

    private fun buildMediaItemsList(item: PodcastWithEpisodesAndUri) {
        val mediaItems = mutableListOf<MediaItem>()
        item.episodes.forEachIndexed { index, episode ->
            val metadata = MediaMetadata.Builder()
                .setAlbumTitle(item.podcast.podcastID)
                .setTitle(item.podcast.title)
                .setSubtitle(episode.title)
                .setArtworkUri(Uri.parse(item.podcast.thumbnailURL))
                .setTrackNumber(index)
                .build()

            val mediaItem = MediaItem.Builder()
                .setMediaId(item.uris[index].toString())
                .setMediaMetadata(metadata)
                .build()
            mediaItems.add(mediaItem)
        }
        mediaController.setMediaItems(mediaItems)
    }


    private fun crossfade() {
        binding.root.apply {
            visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}