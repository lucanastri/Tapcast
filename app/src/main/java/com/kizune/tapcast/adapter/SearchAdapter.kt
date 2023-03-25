package com.kizune.tapcast.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.kizune.tapcast.GlideApp
import com.kizune.tapcast.databinding.RecyclerviewSearchItemBinding
import com.kizune.tapcast.model.Podcast

class SearchAdapter(
    val onItemClickListener: (Podcast) -> Unit,
) : ListAdapter<Podcast, SearchAdapter.DataViewHolder>(DiffCallback), Filterable {
    private var baseList = emptyList<Podcast>()
    private val filter = PodcastFilter()

    fun setBaseList(list: List<Podcast>) {
        baseList = list
    }

    inner class DataViewHolder(
        private var binding: RecyclerviewSearchItemBinding
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
                .into(binding.thumbnail)

            binding.root.setOnClickListener {
                onItemClickListener(podcast)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        return DataViewHolder(
            RecyclerviewSearchItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Podcast>() {
            override fun areItemsTheSame(
                oldItem: Podcast,
                newItem: Podcast
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: Podcast,
                newItem: Podcast
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun getFilter(): Filter = filter

    inner class PodcastFilter : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val query = constraint.toString().lowercase()
            val results = FilterResults()
            val list = if (query.isNotBlank())
                baseList.filter { it.title.lowercase().contains(query) }
            else
                emptyList()
            results.values = list
            results.count = list.size
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            val filteredData = results?.values as List<Podcast>
            submitList(filteredData)
        }
    }
}