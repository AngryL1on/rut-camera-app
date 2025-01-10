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
    private val mediaUris: List<Uri>
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
                // Загружаем кадр видео для превью
                videoFrameMillis(1000) // Загружаем кадр на 1 секунде видео
            }
        }

        // Отображение иконки воспроизведения для видео
        if (mimeType.startsWith("video")) {
            holder.binding.imageViewPlayIcon.visibility = View.VISIBLE
        } else {
            holder.binding.imageViewPlayIcon.visibility = View.GONE
        }

        holder.binding.root.setOnClickListener {
            val bundle = Bundle().apply {
                putString("mediaUri", uri.toString())
            }

            fragment.findNavController().navigate(R.id.action_gallery_to_mediaView, bundle)
        }
    }

    override fun getItemCount(): Int = mediaUris.size
}
