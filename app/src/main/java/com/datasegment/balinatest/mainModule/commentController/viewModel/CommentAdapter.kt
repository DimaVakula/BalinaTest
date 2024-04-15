package com.datasegment.balinatest.mainModule.commentController.viewModel

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.datasegment.balinatest.R
import com.datasegment.balinatest.mainModule.commentController.model.Comment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommentAdapter(private val context: Context) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {
    private var commentList = mutableListOf<Comment>()
    private var isLoading = false
    private var loadMoreListener: (() -> Unit)? = null
    private var commentItemLongClickListener: OnCommentItemLongClickListener? = null

    fun setOnCommentItemLongClickListener(listener: OnCommentItemLongClickListener) {
        commentItemLongClickListener = listener
    }

    interface OnCommentItemLongClickListener {
        fun onCommentItemLongClick(position: Int)
    }

    fun removeCommentAtPosition(position: Int) {
        commentList.removeAt(position)
        notifyItemRemoved(position)
    }

    fun setLoadMoreListener(listener: () -> Unit) {
        loadMoreListener = listener
    }

    fun addComments(comments: List<Comment>) {
        commentList.addAll(comments)
        notifyDataSetChanged()
    }

    fun setLoading(isLoading: Boolean) {
        this.isLoading = isLoading
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        if (position >= commentList.size) {
            holder.showLoading()
            loadMoreListener?.invoke()
        } else {
            holder.bind(commentList[position])

            holder.itemView.setOnLongClickListener {
                val adapterPosition = holder.adapterPosition
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    commentItemLongClickListener?.onCommentItemLongClick(adapterPosition)
                }
                true
            }
        }
    }

    fun getCommentIdAtPosition(position: Int): Int {
        return if (position in 0 until commentList.size) {
            commentList[position].id
        } else {
            -1
        }
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textComment: TextView = itemView.findViewById(R.id.text_comment)
        private val textDate: TextView = itemView.findViewById(R.id.text_date)

        fun bind(comment: Comment) {
            textComment.text = comment.text
            textDate.text = formatDate(comment.date)
        }

        fun showLoading() {
        }
    }

    private fun formatDate(date: Int): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy hh:mm a", Locale.getDefault())
        return dateFormat.format(Date(date.toLong() * 1000))
    }
}

