package dev.angryl1on.cameraapp.presentation.adapters

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.videoFrameMillis
import dev.angryl1on.cameraapp.R
import dev.angryl1on.cameraapp.databinding.ItemMediaBinding
import dev.angryl1on.cameraapp.presentation.fragments.GalleryFragment

class GalleryAdapter(
    private val fragment: GalleryFragment,
    private var mediaUris: List<Uri>
) : RecyclerView.Adapter<GalleryAdapter.MediaViewHolder>() {

    class MediaViewHolder(val binding: ItemMediaBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding = ItemMediaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MediaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val uri = mediaUris[position]
        val mimeType = fragment.requireContext().contentResolver.getType(uri) ?: ""

        holder.binding.imageViewMedia.load(uri) {
            crossfade(true)

            if (mimeType.startsWith("video")) {
                // Загружаем кадр видео на 1 секунде (1000 миллисекунд)
                videoFrameMillis(1000)
            }
        }

        // Отображение иконки воспроизведения для видео
        holder.binding.imageViewPlayIcon.visibility =
            if (mimeType.startsWith("video")) View.VISIBLE else View.GONE

        holder.binding.root.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("mediaIndex", position)
            }

            fragment.findNavController().navigate(R.id.action_gallery_to_mediaView, bundle)
        }
    }

    override fun getItemCount(): Int = mediaUris.size

    // Метод для обновления списка медиафайлов
    fun updateMediaUris(newMediaUris: List<Uri>) {
        mediaUris = newMediaUris
        notifyDataSetChanged()
    }
}
