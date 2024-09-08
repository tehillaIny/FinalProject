package com.example.finalproject.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.finalproject.R
import android.view.View
import android.widget.ImageView
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat

class GalleryAdapter(private val imageUrls: List<String>, private val context: Context) :
    RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gallery_image, parent, false)
        return GalleryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        val imageUrl = imageUrls[position]
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .into(holder.imageView)

        holder.imageView.setOnClickListener {
            showImageDialog(imageUrl)
        }
    }

    override fun getItemCount(): Int {
        return imageUrls.size
    }

    //present one image in a dialog
    private fun showImageDialog(imageUrl: String) {
        Log.d("ImageURL", "URL: $imageUrl")
        val builder = AlertDialog.Builder(context, R.style.DialogTheme)
        val imageView = ImageView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            adjustViewBounds = true
        }
        Glide.with(context)
            .load(imageUrl)
            .into(imageView)
        builder.setView(imageView)
            .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
            .show()
        imageView.setImageResource(R.drawable.error)
    }

    //diaplay one item in the adpater
    class GalleryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageViewGallery)
    }
}
