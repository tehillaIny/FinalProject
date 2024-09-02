package com.example.finalproject.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.databinding.ItemCommentBinding
import com.example.finalproject.models.Comment
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

class CommentsAdapter(private val comments: List<Comment>) : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.bind(comment)
    }

    override fun getItemCount(): Int {
        return comments.size
    }

    inner class CommentViewHolder(private val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comment) {
            binding.textViewUserName.text = comment.userName
            binding.textViewComment.text = comment.text
            val sdf = SimpleDateFormat("dd/MM/yy, HH:mm", Locale.getDefault())
            val formattedDate = sdf.format(Date(comment.timestamp))
            binding.textViewDate.text = formattedDate
        }
    }
}
