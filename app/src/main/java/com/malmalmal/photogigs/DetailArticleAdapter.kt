package com.malmalmal.photogigs

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.article_detail_content_row.view.*

//class DetailArticleAdapter() {

//}
//class DetailArticleAdapter(private val listType : List<Int>) : RecyclerView.Adapter<DetailArticleAdapter.ViewHolder>() {
//
//
//    companion object {
//        val ITEM_A = 1
//        val ITEM_B = 2
//    }
//
//    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
//
//        val inflater = LayoutInflater.from(p0.context)
////        return
//        if (p1 == 0 ) {
//            val asdssfff = inflater.inflate(R.layout.article_detail_image_row, false)
//            ITEM_A -> ViewHolderArticleImage(
//                else -> ViewHolderArticleContent(inflater.inflate(R.layout.article_detail_content_row, false))
//        }
//
//    }
//
//
//    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
//
//        val viewType = listType[p1]
//        when (viewType) {
//            ITEM_A -> {
//                val viewHolderA = p0 as ViewHolderArticleImage
//                viewHolderA.view.textView_article_detail.text = "Coba - coba lah bang ! sat $p1"
//            }   else -> {
//
//                }
//        }
//    }
//
//    override fun getItemCount(): Int {
//        return listType.size
//    }
//
//    override fun getItemViewType(position: Int): Int {
//
//
//        return listType[position]
//    }
//
//
//    open inner class ViewHolder(val view : View) : RecyclerView.ViewHolder(view)
//
//    inner class ViewHolderArticleImage(val viewImage : View) : ViewHolder(viewImage)
//
//    inner class ViewHolderArticleContent(val viewContent : View) : ViewHolder(viewContent)
//
//}