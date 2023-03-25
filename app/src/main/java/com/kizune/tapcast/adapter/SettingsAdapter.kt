package com.kizune.tapcast.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kizune.tapcast.databinding.RecyclerviewSettingsCategoryBinding
import com.kizune.tapcast.databinding.RecyclerviewSettingsItemBinding
import com.kizune.tapcast.model.*

class SettingsAdapter(
    val data: List<Setting>,
    val onSettingItemClicked: (SettingID) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val SETTING_CATEGORY_VIEW = 1
        const val SETTING_ITEM_VIEW = 2
    }

    private inner class CategoryViewHolder(
        private var binding: RecyclerviewSettingsCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: SettingCategory) {
            binding.category.text = binding.root.context.getString(category.title)
        }
    }

    private inner class SettingViewHolder(
        private var binding: RecyclerviewSettingsItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(setting: SettingItem) {
            binding.title.text = setting.title
            binding.summary.text = setting.summary
            binding.chevron.visibility = if (setting.showChevron) View.VISIBLE else View.GONE
            binding.root.setOnClickListener { onSettingItemClicked(setting.id) }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == SETTING_CATEGORY_VIEW) {
            return CategoryViewHolder(
                RecyclerviewSettingsCategoryBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            return SettingViewHolder(
                RecyclerviewSettingsItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(hold: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == SETTING_CATEGORY_VIEW) {
            val holder = hold as CategoryViewHolder
            holder.bind(data[position] as SettingCategory)
        } else {
            val holder = hold as SettingViewHolder
            holder.bind(data[position] as SettingItem)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (data[position] is SettingCategory) {
            SETTING_CATEGORY_VIEW
        } else {
            SETTING_ITEM_VIEW
        }
    }
}