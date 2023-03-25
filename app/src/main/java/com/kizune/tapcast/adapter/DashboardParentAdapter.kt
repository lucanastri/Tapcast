package com.kizune.tapcast.adapter

import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kizune.tapcast.R
import com.kizune.tapcast.databinding.RecyclerviewParentItemBinding
import com.kizune.tapcast.model.Podcast
import com.kizune.tapcast.model.PodcastCategory
import com.kizune.tapcast.ui.HorizontalSpaceItemDecoration
import com.kizune.tapcast.utils.removeItemDecorations

/**Temporary fix for preserving child recyclerView scroll position*/
val scrollStates = mutableMapOf<Int, Parcelable?>()

class DashboardParentAdapter(
    val onItemClickListener: (Podcast) -> Unit,
) : ListAdapter<PodcastCategory, DashboardParentAdapter.DataViewHolder>(DiffCallback), Filterable {
    private var baseList = emptyList<PodcastCategory>()
    private val filter = PodcastFilter()

    fun setBaseList(list: List<PodcastCategory>) {
        baseList = list
    }

    inner class DataViewHolder(
        private var binding: RecyclerviewParentItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: PodcastCategory) {
            val childAdapter = DashboardChildAdapter { podcast ->
                val key = layoutPosition
                scrollStates[key] = binding.childRecyclerView.layoutManager?.onSaveInstanceState()
                onItemClickListener(podcast)
            }
            val itemDecoration = HorizontalSpaceItemDecoration()
            binding.title.text = category.title
            binding.childRecyclerView.adapter = childAdapter
            binding.childRecyclerView.removeItemDecorations()
            binding.childRecyclerView.addItemDecoration(itemDecoration)
            childAdapter.submitList(category.list)

            val key = layoutPosition
            val state = scrollStates[key]

            if (state != null) binding.childRecyclerView.layoutManager?.onRestoreInstanceState(state)
        }
    }

    /**Important for preserving scroll state of child RecyclerView*/
    override fun onViewRecycled(holder: DataViewHolder) {
        super.onViewRecycled(holder)
        val key = holder.layoutPosition
        val recyclerView = holder.itemView.findViewById<RecyclerView>(R.id.childRecyclerView)
        scrollStates[key] = recyclerView.layoutManager?.onSaveInstanceState()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        return DataViewHolder(
            RecyclerviewParentItemBinding.inflate(
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
        private val DiffCallback = object : DiffUtil.ItemCallback<PodcastCategory>() {
            override fun areItemsTheSame(
                oldItem: PodcastCategory,
                newItem: PodcastCategory
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: PodcastCategory,
                newItem: PodcastCategory
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
            val list = mutableListOf<PodcastCategory>()
            baseList.forEach { category ->
                val title = category.title
                val podcasts = category.list.filter { it.title.lowercase().contains(query) }
                if(podcasts.isNotEmpty()) list.add(PodcastCategory(title, podcasts))
            }
            results.values = list
            results.count = list.size
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            val filteredData = results?.values as List<PodcastCategory>
            submitList(filteredData)
        }
    }
}