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

    private fun showImageDialog(imageUrl: String) {
        Log.d("ImageURL", "URL: $imageUrl")
        val builder = AlertDialog.Builder(context)
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
        //replace the logo image with another photo (error or something else)
        imageView.setImageResource(R.drawable.logo)
    }

    class GalleryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageViewGallery)
    }
}
