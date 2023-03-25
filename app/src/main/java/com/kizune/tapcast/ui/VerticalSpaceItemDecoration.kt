package com.kizune.tapcast.ui

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.kizune.tapcast.utils.toPx

class VerticalSpaceItemDecoration(
    spacing: Int = 32
): RecyclerView.ItemDecoration() {
    private var spacingInPx: Int = spacing.toPx()

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.top = spacingInPx
    }
}