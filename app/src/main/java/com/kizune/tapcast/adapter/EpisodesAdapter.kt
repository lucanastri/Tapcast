package com.kizune.tapcast.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.media3.session.MediaController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kizune.tapcast.R
import com.kizune.tapcast.databinding.RecyclerviewEpisodeItemBinding
import com.kizune.tapcast.model.Episode
import com.kizune.tapcast.utils.toFormattedDate
import com.kizune.tapcast.utils.toFormattedDuration

class EpisodesAdapter(
    val onPlayPauseClickListener: (Int) -> Unit,
) : ListAdapter<Episode, EpisodesAdapter.EpisodeViewHolder>(DiffCallback) {
    private lateinit var mediaController: MediaController

    fun attachMediaController(value: MediaController) {
        mediaController = value
    }

    inner class EpisodeViewHolder(
        private var binding: RecyclerviewEpisodeItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(episode: Episode) {
            binding.title.text = episode.title
            binding.description.text = episode.description
            binding.date.text = episode.date.toFormattedDate()
            binding.duration.text = episode.duration.toFormattedDuration()


            binding.setPlayButton(absoluteAdapterPosition)
            binding.playButton.setOnClickListener {
                onPlayPauseClickListener(absoluteAdapterPosition)
            }
        }
    }

    private fun RecyclerviewEpisodeItemBinding.setPlayButton(position: Int) {
        when (mediaController.isPlaying && mediaController.currentMediaItemIndex == position) {
            true -> playButton.setIconResource(R.drawable.icon_pause)
            else -> playButton.setIconResource(R.drawable.icon_play)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        return EpisodeViewHolder(
            RecyclerviewEpisodeItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Episode>() {
            override fun areItemsTheSame(oldItem: Episode, newItem: Episode): Boolean {
                return oldItem.episodeID == newItem.episodeID
            }

            override fun areContentsTheSame(oldItem: Episode, newItem: Episode): Boolean {
                return oldItem == newItem
            }
        }
    }
}