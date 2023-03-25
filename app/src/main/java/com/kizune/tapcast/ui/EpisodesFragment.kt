package com.kizune.tapcast.ui

import android.content.ComponentName
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
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.fragment.findNavController
import com.google.common.util.concurrent.MoreExecutors
import com.kizune.tapcast.adapter.EpisodesAdapter
import com.kizune.tapcast.databinding.FragmentEpisodesBinding
import com.kizune.tapcast.model.PodcastWithEpisodesAndUri
import com.kizune.tapcast.player.TapcastService
import com.kizune.tapcast.viewmodel.PodcastViewModel
import kotlinx.coroutines.launch

class EpisodesFragment : Fragment() {
    private var _binding: FragmentEpisodesBinding? = null
    private val binding get() = _binding!!

    private lateinit var onNavigateUpCallback: OnBackPressedCallback

    private lateinit var adapter: EpisodesAdapter
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
        _binding = FragmentEpisodesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val onPlayPauseClickListener: (Int) -> Unit = { newIndex ->
            val currentIndex = mediaController.currentMediaItemIndex
            when(currentIndex != newIndex) {
                true -> {
                    mediaController.seekTo(newIndex, 0L)
                    playOrPause()
                    adapter.notifyItemChanged(currentIndex)
                }
                false -> {
                    playOrPause()
                }
            }
        }

        binding.backButton.setOnClickListener {
            onNavigateUpCallback.handleOnBackPressed()
        }

        adapter = EpisodesAdapter(onPlayPauseClickListener)
        binding.recyclerView.itemAnimator = null
        binding.recyclerView.addItemDecoration(VerticalSpaceItemDecoration(24))
        binding.recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            podcastViewModel.podcastWithEpisodesAndUri.collect { item ->
                if (item != null) {
                    binding.title.text = item.podcast.title
                    buildMediaController(item)
                }
            }
        }
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
                mediaController.addListener(mediaControllerListener)
                adapter.attachMediaController(mediaController)
                adapter.submitList(item.episodes)
            },
            MoreExecutors.directExecutor()
        )
    }

    private fun playOrPause() {
        if(mediaController.isPlaying) {
            mediaController.pause()
        } else {
            mediaController.play()
        }
    }

    private val mediaControllerListener = object: Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)
            adapter.notifyItemChanged(mediaController.currentMediaItemIndex)
            adapter.notifyItemChanged(mediaController.nextMediaItemIndex)
            adapter.notifyItemChanged(mediaController.previousMediaItemIndex)
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            adapter.notifyItemChanged(mediaController.currentMediaItemIndex)
        }
    }

}