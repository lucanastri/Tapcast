package com.kizune.tapcast.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.kizune.tapcast.GlideApp
import com.kizune.tapcast.R
import com.kizune.tapcast.databinding.RecyclerviewChildItemBinding
import com.kizune.tapcast.model.Podcast


class DashboardChildAdapter(
    val onItemClickListener: (Podcast) -> Unit,
) : ListAdapter<Podcast, DashboardChildAdapter.PodcastViewHolder>(DiffCallback) {

    inner class PodcastViewHolder(
        private var binding: RecyclerviewChildItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(podcast: Podcast) {
            binding.title.text = podcast.title
            binding.authors.text = podcast.authors.joinToString(", ")
            val thumbnailRef = Firebase.storage.reference
                .child(podcast.podcastID)
                .child(podcast.thumbnailURL)

            GlideApp.with(binding.root)
                .load(thumbnailRef)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.icon_placeholder)
                .into(binding.thumbnail)

            binding.root.setOnClickListener {
                onItemClickListener(podcast)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PodcastViewHolder {
        return PodcastViewHolder(
            RecyclerviewChildItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PodcastViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Podcast>() {
            override fun areItemsTheSame(oldItem: Podcast, newItem: Podcast): Boolean {
                return oldItem.podcastID == newItem.podcastID
            }

            override fun areContentsTheSame(oldItem: Podcast, newItem: Podcast): Boolean {
                return oldItem == newItem
            }
        }
    }
}