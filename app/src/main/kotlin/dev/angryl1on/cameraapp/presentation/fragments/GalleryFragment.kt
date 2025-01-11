package dev.angryl1on.cameraapp.presentation.fragments

import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import dev.angryl1on.cameraapp.R
import dev.angryl1on.cameraapp.databinding.FragmentGalleryBinding
import dev.angryl1on.cameraapp.presentation.viewmodels.SharedViewModel
import dev.angryl1on.cameraapp.presentation.adapters.GalleryAdapter

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private lateinit var galleryAdapter: GalleryAdapter

    companion object {
        private const val TAG = "GalleryFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        galleryAdapter = GalleryAdapter(this, emptyList())

        binding.recyclerViewGallery.apply {
            adapter = galleryAdapter
            layoutManager = GridLayoutManager(requireContext(), 3)
        }

        loadMedia()

        binding.buttonOpenPhoto.setOnClickListener {
            findNavController().navigate(R.id.action_gallery_to_photo)
        }
        binding.buttonOpenVideo.setOnClickListener {
            findNavController().navigate(R.id.action_gallery_to_video)
        }
    }

    private fun loadMedia() {
        val mediaUris = mutableListOf<Uri>()
        Log.d(TAG, "mediaUris cleared")

        val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val videoCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.MIME_TYPE
        )

        // Загрузка изображений
        val imageQuery = requireContext().contentResolver.query(
            imageCollection,
            projection,
            null,
            null,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )

        imageQuery?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val uri = ContentUris.withAppendedId(imageCollection, id)
                mediaUris.add(uri)
            }
        }

        // Загрузка видео
        val videoQuery = requireContext().contentResolver.query(
            videoCollection,
            projection,
            null,
            null,
            "${MediaStore.Video.Media.DATE_ADDED} DESC"
        )

        videoQuery?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val uri = ContentUris.withAppendedId(videoCollection, id)
                mediaUris.add(uri)
            }
        }

        // Логирование количества загруженных медиафайлов
        Log.d(TAG, "Загружено медиафайлов: ${mediaUris.size}")

        // Обновление SharedViewModel
        sharedViewModel.setMediaUris(mediaUris)

        // Обновление адаптера
        galleryAdapter.updateMediaUris(mediaUris)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
