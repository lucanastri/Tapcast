package com.kizune.tapcast.ui

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.kizune.tapcast.utils.toPx

/**
 * Horizontal Divider for Dashboard RecyclerView
 * spacing parameter is in DP
 */
class HorizontalSpaceItemDecoration(
    spacing: Int = 16
): RecyclerView.ItemDecoration() {
    private var spacingInPx: Int = spacing.toPx()

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.left = if(parent.getChildAdapterPosition(view) == 0) spacingInPx else 0
        outRect.right = spacingInPx
    }

}