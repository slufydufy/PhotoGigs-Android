package com.malmalmal.photogigs

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

class CustomItemDecoration : RecyclerView.ItemDecoration {

    private var top: Int = 0
    private var bottom: Int = 0
    private var left: Int = 0
    private var right: Int = 0

    /**
     * @param top_bottom for top and bottom margin
     * @param left_right for left and right margin
     */
    constructor(top: Int, bottom: Int, left: Int = 0, right: Int) {
        this.top = top
        this.bottom = bottom
        this.left = left
        this.right = left
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.top = top
        outRect.bottom = bottom
        outRect.right = left
        outRect.left = right
    }
}