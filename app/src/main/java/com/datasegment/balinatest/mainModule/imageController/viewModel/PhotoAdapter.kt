package com.datasegment.balinatest.mainModule.imageController.viewModel

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.datasegment.balinatest.R
import com.datasegment.balinatest.mainModule.database.model.Photo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date

class PhotoAdapter(private val context: Context) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {
    private var photoList = mutableListOf<Photo>()
    private var isLoading = false
    private var loadMoreListener: (() -> Unit)? = null
    private var photoItemLongClickListener: OnPhotoItemLongClickListener? = null
    private var photoItemClickListener: OnPhotoItemClickListener? = null

    fun setOnPhotoItemClickListener(listener: OnPhotoItemClickListener) {
        photoItemClickListener = listener
    }


    fun setOnPhotoItemLongClickListener(listener: OnPhotoItemLongClickListener) {
        photoItemLongClickListener = listener
    }

    interface OnPhotoItemClickListener {
        fun onPhotoItemClick(photoId: Int, photoUrl : String?, photoLng : Double, photoLat : Double, photoDate : Int)
    }


    interface OnPhotoItemLongClickListener {
        fun onPhotoItemLongClick(position: Int)
    }

    fun removePhotoAtPosition(position: Int) {
        photoList.removeAt(position)
        notifyItemRemoved(position)
    }


    fun setLoadMoreListener(listener: () -> Unit) {
        loadMoreListener = listener
    }

    fun addPhotos(photos: List<Photo>) {
        photoList.addAll(photos)
        notifyDataSetChanged()
    }

    fun setLoading(isLoading: Boolean) {
        this.isLoading = isLoading
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        if (position >= photoList.size) {
            holder.showLoading()
            loadMoreListener?.invoke()
        } else {
            holder.bind(photoList[position])

            // Обработчик долгого нажатия на элемент
            holder.itemView.setOnLongClickListener {
                val adapterPosition = holder.adapterPosition
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    photoItemLongClickListener?.onPhotoItemLongClick(adapterPosition)
                }
                true
            }

            holder.itemView.setOnClickListener {
                val photoId = photoList[position].id
                val photoUrl = photoList[position].url
                val photoLng = photoList[position].lng
                val photoLat = photoList[position].lat
                val photoDate = photoList[position].date
                photoItemClickListener?.onPhotoItemClick(photoId, photoUrl, photoLng, photoLat, photoDate)
            }

        }
    }

    fun getPhotoIdAtPosition(position: Int): Int {
        return if (position in 0 until photoList.size) {
            photoList[position].id
        } else {
            -1 // Если позиция некорректна, вернуть -1 или другое значение по умолчанию
        }
    }

    override fun getItemCount(): Int {
        return photoList.size
    }

    inner class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val photoImageView: ImageView = itemView.findViewById(R.id.photoImageView)
        private val dateTextView: TextView = itemView.findViewById(R.id.photoTextView)

        fun bind(photo: Photo) {
            GlobalScope.launch(Dispatchers.Main) {
                try {
                    val bitmap = withContext(Dispatchers.IO) {
                        Glide.with(context)
                            .asBitmap()
                            .load(photo.url)
                            .submit()
                            .get()
                    }
                    photoImageView.setImageBitmap(bitmap)
                    dateTextView.text = formatDate(photo.date)
                    photoImageView.visibility = View.VISIBLE
                    dateTextView.visibility = View.VISIBLE
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        fun showLoading() {
            // Показать индикатор загрузки
            val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
            progressBar.visibility = View.VISIBLE
        }
    }

    private fun formatDate(date: Int): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy")
        return dateFormat.format(Date(date.toLong() * 1000))
    }
}
